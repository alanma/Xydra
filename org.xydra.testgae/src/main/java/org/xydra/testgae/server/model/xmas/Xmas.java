package org.xydra.testgae.server.model.xmas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xydra.base.XId;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.XX;
import org.xydra.core.change.DiffWritableModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.NanoClock;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.testgae.server.rest.xmas.XmasResource;
import org.xydra.xgae.gaeutils.GaeTestfixer;

import com.google.apphosting.api.DeadlineExceededException;

/**
 * The Xmas main page. Exposed to REST via {@link XmasResource}.
 * 
 * @author xamde
 * 
 */
public class Xmas {

	static {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
	}

	private static final Logger log = LoggerFactory.getLogger(Xmas.class);

	private static final XId ACTOR_ID = XX.toId("_Xmas-benchmark-user");

	/**
	 * A cached instance used to load and persist data
	 */
	private static WritableRepositoryOnPersistence repo;

	private static GaePersistence persistence;

	public static XId timebasedUniqueId() {
		return XX.toId("benchmark-" + System.currentTimeMillis());
	}

	/**
	 * @param repoIdStr
	 *            may be null. If null, a unique one-time repository with the ID
	 *            'benchmark-{curentTimestamp}' is used.
	 * @return an {@link XWritableRepository}, never null.
	 */
	public static XWritableRepository getRepository(String repoIdStr) {
		/* if there is no repo or the wrong one: init */
		if (repo == null || (repoIdStr != null && !repo.getId().toString().equals(repoIdStr))) {
			XId repoId = repoIdStr == null ? timebasedUniqueId() : XX.toId(repoIdStr);
			NanoClock c = new NanoClock().start();

			/* Create persistence */
			persistence = new GaePersistence(repoId);
			/* Wrap as XWritableRepository and cache instance */
			repo = new WritableRepositoryOnPersistence(persistence, ACTOR_ID);

			c.stop("init-repo");
			log.info("Initialised repository '" + repoId + "'. Stats: " + c.getStats());
		}
		return repo;
	}

	/**
	 * Add lots of test data to repository.
	 * 
	 * @param repoStr
	 *            to which repository (id may be null, see
	 *            {@link #getRepository(String)})
	 * @param listCount
	 *            how many lists to create
	 * @param wishesCount
	 *            how many wishes to add to each list
	 * @param writer
	 *            where to print performance data
	 * @throws IOException
	 *             if the writer has {@link IOException}
	 */
	public static void addData(String repoStr, int listCount, int wishesCount, Writer writer)
			throws IOException {
		XWritableRepository repo = getRepository(repoStr);
		writer.write("Adding to repository '" + repo.getId() + "'<br />\n");
		NanoClock s2 = new NanoClock();
		s2.start();
		for (int l = 0; l < listCount; l++) {
			writer.write("Creating/loading model <br />\n");
			XWritableModel model = createModel(repoStr, XX.createUniqueId());
			// txn
			DiffWritableModel txnModel = new DiffWritableModel(model);
			WishList wishList = new WishList(txnModel);
			wishList.addDemoData(wishesCount, writer);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + model.getId(), "See list '"
					+ model.getId() + "'")
					+ "<br />\n");
			// commit txn
			XTransaction txn = txnModel.toTransaction();
			executeTransaction(txn);

		}
		s2.stop("create " + listCount + " lists with " + wishesCount + " wishes");
		writer.write(s2.getStats() + "<br />\n");
		log.info("Created " + listCount + " lists in " + repoStr + " with " + wishesCount
				+ " wishes initially each");
	}

	public static void clearRepository(String repoStr) {
		XWritableRepository repo = getRepository(repoStr);
		for (XId modelId : repo) {
			repo.removeModel(modelId);
		}
	}

	public static synchronized XWritableModel createModel(String repoIdStr, XId modelId) {
		XWritableRepository repo = getRepository(repoIdStr);
		XWritableModel model = repo.createModel(modelId);
		return model;
	}

	/**
	 * @param txn
	 *            may be null
	 */
	public static void executeTransaction(XTransaction txn) {
		if (txn != null) {
			persistence.executeCommand(ACTOR_ID, txn);
		}
	}

	/**
	 * Renders the complete repository to HTML, if that is possible within the
	 * given time-frame. Otherwise this method is likely to cause a
	 * {@link DeadlineExceededException}.
	 * 
	 * @param repoStr
	 *            to which repository (id may be null, see
	 *            {@link #getRepository(String)})
	 * @param view
	 *            can be 'expanded' or 'collaped'. Expanded renders all models
	 *            in detail, collapsed merely lists them. Collapsed should be
	 *            MUCH faster.
	 * @param writer
	 *            where to print the HTML
	 * @throws IOException
	 *             if the writer has {@link IOException}
	 */
	public static void get(String repoStr, String view, Writer writer) throws IOException {
		NanoClock s1 = new NanoClock();
		s1.start();
		writer.write("<h2>All wishlists in repository '" + Xmas.getRepository(repoStr).getId()
				+ "'</h2>\n");
		if (view.equals("expanded")) {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=collapsed", "collapse all")
					+ "<br />\n");
		} else {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=expanded", "expand all")
					+ "<br />\n");
		}
		int count = 0;
		XWritableRepository repo = getRepository(repoStr);
		for (XId modelId : repo) {
			count++;
			XWritableModel model = repo.getModel(modelId);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + modelId + "", "Wish list '"
					+ modelId + "'<br />\n"));
			if (view.equals("expanded")) {
				WishList list = new WishList(model);
				writer.write(list.toHtml());
			}
		}
		s1.stop("read list with " + count + " wishes");
		writer.write(s1.getStats() + "<br />\n");
	}

	/**
	 * Run locally: Create 10 lists with 10 wishes each and render to console.
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 *             can happen
	 */
	public static void main(String[] args) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		Xmas.addData("test", 10, 10, writer);
		Xmas.get("test", "collapsed", writer);
		System.out.println("Done. You must terminate the VM now.");
	}

}

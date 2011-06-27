package org.xydra.testgae.server.model.xmas;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.Clock;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.testgae.server.rest.xmas.WishlistResource;
import org.xydra.testgae.server.rest.xmas.XmasResource;

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
	
	/**
	 * A cached instance used to load and persist data
	 */
	private static WritableRepositoryOnPersistence repo;
	
	/**
	 * @param repoIdStr may be null. If null, a unique one-time repository with
	 *            the ID 'benchmark-{curentTimestamp}' is used.
	 * @return an {@link XWritableRepository}, never null.
	 */
	public static XWritableRepository getRepository(String repoIdStr) {
		/* if there is no repo or the wrong one: init */
		if(repo == null || (repoIdStr != null && !repo.getID().toString().equals(repoIdStr))) {
			XID repoId = repoIdStr == null ? XX.toId("benchmark-" + System.currentTimeMillis())
			        : XX.toId(repoIdStr);
			Clock c = new Clock().start();
			
			/* Create persistence */
			XydraPersistence persistence = new GaePersistence(repoId);
			/* Wrap as XWritableRepository and cache instance */
			repo = new WritableRepositoryOnPersistence(persistence, XX.toId("benchmark-user"));
			
			c.stop("init-repo");
			log.info("Initialised repository '" + repoId + "'. Stats: " + c.getStats());
		}
		return repo;
	}
	
	/**
	 * Add lots of test data to repository.
	 * 
	 * @param repoStr to which repository (id may be null, see
	 *            {@link #getRepository(String)})
	 * @param listCount how many lists to create
	 * @param wishesCount how many wishes to add to each list
	 * @param writer where to print performance data
	 * @throws IOException if the writer has {@link IOException}
	 */
	public static void addData(String repoStr, int listCount, int wishesCount, Writer writer)
	        throws IOException {
		writer.write("Adding to repository '" + getRepository(repoStr).getID() + "'<br />\n");
		Clock s2 = new Clock();
		s2.start();
		for(int l = 1; l <= listCount; l++) {
			writer.write("Creating/loading model <br />\n");
			XWritableModel model = getRepository(repoStr).createModel(XX.toId("list" + l));
			WishList wishList = WishlistResource.load(getRepository(repoStr), model.getID());
			wishList.addDemoData(wishesCount, writer);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + model.getID(), "See list '"
			        + model.getID() + "'")
			        + "<br />\n");
		}
		s2.stop("create " + listCount + " lists with " + wishesCount + " wishes");
		writer.write(s2.getStats() + "<br />\n");
	}
	
	/**
	 * Renders the complete repository to HTML, if that is possible within the
	 * given time-frame. Otherwise this method is likely to cause a
	 * {@link DeadlineExceededException}.
	 * 
	 * @param repoStr to which repository (id may be null, see
	 *            {@link #getRepository(String)})
	 * @param view can be 'expanded' or 'collaped'. Expanded renders all models
	 *            in detail, collapsed merely lists them. Collapsed should be
	 *            MUCH faster.
	 * @param writer where to print the HTML
	 * @throws IOException if the writer has {@link IOException}
	 */
	public static void get(String repoStr, String view, Writer writer) throws IOException {
		Clock s1 = new Clock();
		s1.start();
		writer.write("<h2>All wishlists in repository '" + Xmas.getRepository(repoStr).getID()
		        + "'</h2>\n");
		if(view.equals("expanded")) {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=collapsed", "collapse all")
			        + "<br />\n");
		} else {
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "?view=expanded", "expand all")
			        + "<br />\n");
		}
		int count = 0;
		for(XID modelId : getRepository(repoStr)) {
			count++;
			XWritableModel model = Xmas.getRepository(repoStr).getModel(modelId);
			writer.write(HtmlUtils.link("/xmas/" + repoStr + "/" + modelId + "", "Wish list '"
			        + modelId + "'<br />\n"));
			if(view.equals("expanded")) {
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
	 * @param args ignored
	 * @throws IOException can happen
	 */
	public static void main(String[] args) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		Xmas.addData("test", 10, 10, writer);
		Xmas.get("test", "collapsed", writer);
		System.out.println("Done. You must terminate the VM now.");
	}
	
}

package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.common.NanoClock;
import org.xydra.core.change.DiffWritableModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.testgae.server.model.xmas.Wish;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;
import org.xydra.xgae.gaeutils.GaeTestfixer;

/**
 * Expose a single {@link Wish} to the web.
 *
 * @author xamde
 */
public class WishResource {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishResource.class);

	public static void restless(final Restless r, final String path) {
		r.addMethod(path + "/{repo}/{list}/{wish}/delete", "GET", WishResource.class, "delete",
				false, // .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wish", null)// .
		);

		r.addMethod(path + "/{repo}/{list}/{wish}/editName", "GET", WishResource.class, "editName",
				false, // .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wish", null), // .
				new RestlessParameter("name", null) // .
		);

		r.addMethod(path + "/{repo}/{list}/{wish}/editPrice", "GET", WishResource.class,
				"editPrice", false, // .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wish", null), // .
				new RestlessParameter("price", "0") // .
		);

		r.addMethod(path + "/{repo}/{list}/{wish}/editUrl", "GET", WishResource.class, "editUrl",
				false, // .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wish", null), // .
				new RestlessParameter("url", null) // .
		);

		r.addGet(path + "/{repo}/{list}/{wish}", WishResource.class, "get",// .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wish", null)// .
		);
	}

	public static synchronized void delete(final String repoStr, final String listStr, final String wishStr,
			final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Writer w = HtmlUtils.startHtmlPage(res, "Delete");
		w.write("Deleting<br />");

		final NanoClock s1 = new NanoClock().start();

		// create txn
		final DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr,
				Base.toId(listStr)));
		final WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.removeWish(Base.toId(wishStr));
		// execute txn
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);

		s1.stop("delete a wish");
		w.write(s1.getStats() + "<br/>\n");

		w.write(SharedHtmlUtils.link("..", "See all wishes in this list"));
		w.flush();
		w.close();
	}

	public static synchronized void editName(final String repoStr, final String listStr, final String wishStr,
			final String name, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Writer w = HtmlUtils.startHtmlPage(res, "Edit Name");
		w.write("Renaming to " + name + " <br />");

		final NanoClock s1 = new NanoClock().start();

		// create txn
		final DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr,
				Base.toId(listStr)));
		final WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.editWishName(Base.toId(wishStr), name);
		// execute txn
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);

		s1.stop("edit a wish");
		w.write(s1.getStats() + "<br/>\n");

		w.write(SharedHtmlUtils.link("..", "See all wishes in this list"));
		w.flush();
		w.close();
	}

	public static synchronized void editPrice(final String repoStr, final String listStr, final String wishStr,
			final String priceStr, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Writer w = HtmlUtils.startHtmlPage(res, "Edit Price");
		w.write("Changig price to " + priceStr + " <br />");

		final NanoClock s1 = new NanoClock().start();

		// create txn
		final DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr,
				Base.toId(listStr)));
		final WishList wishList = new WishList(txnModel);
		// manipulate txn
		final int price = Integer.parseInt(priceStr);
		wishList.editWishPrice(Base.toId(wishStr), price);
		// execute txn
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);

		s1.stop("edit a wish");
		w.write(s1.getStats() + "<br/>\n");

		w.write(SharedHtmlUtils.link("..", "See all wishes in this list"));
		w.flush();
		w.close();
	}

	public static synchronized void editUrl(final String repoStr, final String listStr, final String wishStr,
			final String urlStr, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Writer w = HtmlUtils.startHtmlPage(res, "Edit URL");
		w.write("Changing URL to " + urlStr + " <br />");

		final NanoClock s1 = new NanoClock().start();

		// create txn
		final DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr,
				Base.toId(listStr)));
		final WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.editWishUrl(Base.toId(wishStr), urlStr);
		// execute txn
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);

		s1.stop("edit a wish");
		w.write(s1.getStats() + "<br/>\n");

		w.write(SharedHtmlUtils.link("..", "See all wishes in this list"));
		w.flush();
		w.close();
	}

	public static synchronized void get(final String repoStr, final String listStr, final String wishStr,
			final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Wish wish = load(repoStr, listStr, wishStr);
		final Writer w = HtmlUtils.startHtmlPage(res, "List");
		w.write(wish.toHtml());
		w.write(SharedHtmlUtils.link("/xmas/" + repoStr + "/" + listStr, "See all wishes in this lists"));
		w.flush();
		w.close();
	}

	private static Wish load(final String repoStr, final String listStr, final String wishStr) {
		return load(Xmas.getRepository(repoStr), Base.toId(listStr), Base.toId(wishStr));
	}

	public static Wish load(final XWritableRepository repo, final XId listId, final XId wishId) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final XWritableModel model = repo.createModel(listId);
		final XWritableObject xo = model.createObject(wishId);
		return new Wish(xo);
	}

	public static String toRootRelativeUrl(final String repoStr, final String list, final XId wishId) {
		return "/xmas/" + repoStr + "/" + list + "/" + wishId;
	}

}

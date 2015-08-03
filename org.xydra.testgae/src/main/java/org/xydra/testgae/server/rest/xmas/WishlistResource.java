package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.DiffWritableModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;
import org.xydra.xgae.gaeutils.GaeTestfixer;

/**
 * Expose a {@link WishList} (list of wishes) to the web
 *
 * @author xamde
 */
public class WishlistResource {

	private static final Logger log = LoggerFactory.getLogger(WishlistResource.class);

	public static void restless(final Restless r, final String path) {
		r.addGet(path + "/{repo}/{list}/add", WishlistResource.class, "addData",// .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("wishes", "1")// .
		);
		r.addGet(path + "/{repo}/{list}", WishlistResource.class, "get",// .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null),// .
				new RestlessParameter("format", "html")// .
		);
		r.addGet(path + "/{repo}/{list}/clear", WishlistResource.class, "deleteAllWishes",// .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("list", null)// .
		);
		// expose also individual wishes
		WishResource.restless(r, path);
	}

	public static synchronized void addData(final String repoStr, final String list, final String wishesStr,
			final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");

		final Writer w = HtmlUtils.startHtmlPage(res, "Add Data");
		w.write("Adding test data ...?wishes=" + wishesStr + " wishes. Start at "
				+ System.currentTimeMillis() + "\n");
		final int wishesCount = Integer.parseInt(wishesStr);
		final XWritableModel model = Xmas.createModel(repoStr, Base.toId(list));
		final DiffWritableModel txnModel = new DiffWritableModel(model);
		final WishList wishList = new WishList(txnModel);
		wishList.addDemoData(wishesCount, w);
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);
		w.write(SharedHtmlUtils.link(".", "See all wishes"));
		w.flush();
		w.close();
	}

	/**
	 * Delete all wishes and the list itself
	 *
	 * @param repoStr
	 *            ..
	 * @param list
	 *            ..
	 * @param req
	 *            ..
	 * @param res
	 *            ..
	 * @throws IOException
	 *             ...
	 */
	public static synchronized void deleteAllWishes(final String repoStr, final String list,
			final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final Writer w = HtmlUtils.startHtmlPage(res, "Delete Wishes");
		w.write("Deleting all wishes.");
		// create txn
		final DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr, Base.toId(list)));
		final WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.removeAllWishes(w);
		// execute txn
		final XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);

		final XWritableRepository repo = Xmas.getRepository(repoStr);
		repo.removeModel(Base.toId(list));
		w.write(SharedHtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		w.flush();
		w.close();
	}

	/**
	 * @param repoStr
	 *            ..
	 * @param list
	 *            ..
	 * @param format
	 *            can be "html" (default) for "urls" for machine access
	 * @param res
	 *            ..
	 * @throws IOException
	 *             ...
	 */
	public static synchronized void get(final String repoStr, final String list, final String format,
			final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// no txn, read only
		final WishList wishList = new WishList(Xmas.createModel(repoStr, Base.toId(list)));
		if (format.equals("urls")) {
			ServletUtils.headers(res, "text/plain");
			final Writer w = res.getWriter();
			for (final XId wishId : wishList) {
				w.write(WishResource.toRootRelativeUrl(repoStr, list, wishId) + "\n");
			}
			// always write at least a single blank line
			w.write("\n");
			w.flush();
			w.close();
		} else {
			ServletUtils.headers(res, "text/html");
			final Writer w = HtmlUtils.startHtmlPage(res, "Wishes");
			w.write(wishList.toHtml());

			w.write(SharedHtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/" + list + "/add")
					.withInputText("wishes", "1").withInputSubmit("Add wishes").toString());
			w.write(SharedHtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
			HtmlUtils.endHtmlPage(w);
		}
		log.info("Done " + repoStr + "/" + list + "&format=" + format + " at "
				+ System.currentTimeMillis());
	}

	/**
	 * @param repoId
	 * @param modelId
	 *            ~ wish list id
	 * @return a URL relative to the server root
	 */
	public static String toRelativeUrl(final String repoId, final XId modelId) {
		assert !modelId.toString().endsWith("/");
		return "/xmas/" + repoId + "/" + modelId;
	}

}

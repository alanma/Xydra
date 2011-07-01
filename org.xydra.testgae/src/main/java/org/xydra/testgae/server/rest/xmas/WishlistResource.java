package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.DiffWritableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.HtmlUtils.METHOD;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;


/**
 * Expose a {@link WishList} to the web
 * 
 * @author xamde
 */
public class WishlistResource {
	
	private static final Logger log = LoggerFactory.getLogger(WishlistResource.class);
	
	public static void restless(Restless r, String path) {
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
	
	public static synchronized void addData(String repoStr, String list, String wishesStr,
	        HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		
		Writer w = HtmlUtils.startHtmlPage(res, "Add Data");
		w.write("Adding test data ...?wishes=" + wishesStr + " wishes. Start at "
		        + System.currentTimeMillis() + "\n");
		int wishesCount = Integer.parseInt(wishesStr);
		XWritableModel model = Xmas.getOrCreateModel(repoStr, XX.toId(list));
		DiffWritableModel txnModel = new DiffWritableModel(model);
		WishList wishList = new WishList(txnModel);
		wishList.addDemoData(wishesCount, w);
		XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);
		w.write(HtmlUtils.link(".", "See all wishes"));
		w.flush();
		w.close();
	}
	
	/**
	 * Delete all wishes and the list itself
	 * 
	 * @param repoStr ..
	 * @param list ..
	 * @param req ..
	 * @param res ..
	 * @throws IOException ...
	 */
	public static synchronized void deleteAllWishes(String repoStr, String list,
	        HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "Delete Wishes");
		w.write("Deleting all wishes.");
		// create txn
		DiffWritableModel txnModel = new DiffWritableModel(Xmas.getOrCreateModel(repoStr,
		        XX.toId(list)));
		WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.removeAllWishes(w);
		// execute txn
		XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);
		
		XWritableRepository repo = Xmas.getRepository(repoStr);
		repo.removeModel(XX.toId(list));
		w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		w.flush();
		w.close();
	}
	
	/**
	 * @param repoStr ..
	 * @param list ..
	 * @param format can be "html" (default) for "urls" for machine access
	 * @param res ..
	 * @throws IOException ...
	 */
	public static synchronized void get(String repoStr, String list, String format,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// no txn, read only
		WishList wishList = new WishList(Xmas.getOrCreateModel(repoStr, XX.toId(list)));
		if(format.equals("urls")) {
			ServletUtils.headers(res, "text/plain");
			Writer w = res.getWriter();
			for(XID wishId : wishList) {
				w.write(WishResource.toRootRelativeUrl(repoStr, list, wishId) + "\n");
			}
			// always write at least a single blank line
			w.write("\n");
			w.flush();
			w.close();
		} else {
			ServletUtils.headers(res, "text/html");
			Writer w = HtmlUtils.startHtmlPage(res, "Wishes");
			w.write(wishList.toHtml());
			
			w.write(HtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/" + list + "/add")
			        .withInputText("wishes", "1").withInputSubmit("Add wishes").toString());
			w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
			HtmlUtils.endHtmlPage(w);
		}
		log.info("Done " + repoStr + "/" + list + "&format=" + format + " at "
		        + System.currentTimeMillis());
	}
	
	/**
	 * @param modelId ~ wish list id
	 * @return a URL relative to the server root
	 */
	public static String toRelativeUrl(String repoId, XID modelId) {
		assert !modelId.toString().endsWith("/");
		return "/xmas/" + repoId + "/" + modelId;
	}
	
}

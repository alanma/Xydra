package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.HtmlUtils.METHOD;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;


/**
 * Expose a {@link WishList} to the web
 * 
 * @author xamde
 */
public class WishlistResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishlistResource.class);
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/{repo}/{list}/add", WishlistResource.class, "addData",// .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("list", null),// .
		        new RestlessParameter("wishes", "1")// .
		);
		r.addGet(path + "/{repo}/{list}", WishlistResource.class, "get",// .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("list", null)// .
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
		ServletUtils.headers(res, "text/html");
		
		Writer w = HtmlUtils.startHtmlPage(res, "Add Data");
		w.write("Adding test data ...?wishes=" + wishesStr + " wishes. Start at "
		        + System.currentTimeMillis() + "\n");
		int wishesCount = Integer.parseInt(wishesStr);
		WishList wishList = load(repoStr, list);
		wishList.addDemoData(wishesCount, w);
		w.write(HtmlUtils.link(".", "See all wishes"));
		w.flush();
	}
	
	public static synchronized void deleteAllWishes(String repoStr, String list,
	        HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "Delete Wishes");
		w.write("Deleting all wishes.");
		WishList wishList = load(repoStr, list);
		wishList.removeAllWishes(w);
		w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		w.flush();
	}
	
	public static synchronized void get(String repoStr, String list, HttpServletResponse res)
	        throws IOException {
		ServletUtils.headers(res, "text/html");
		WishList wishList = load(repoStr, list);
		Writer w = HtmlUtils.startHtmlPage(res, "Wishes");
		w.write(wishList.toHtml());
		
		w.write(HtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/" + list + "/add")
		        .withInputText("wishes", "1").withInputSubmit("Add wishes").toString());
		w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		w.flush();
	}
	
	private static WishList load(String repoStr, String wishlistId) {
		return load(Xmas.getRepository(repoStr), XX.toId(wishlistId));
	}
	
	public static synchronized WishList load(XWritableRepository repo, XID listId) {
		XWritableModel model = repo.createModel(listId);
		return new WishList(model);
	}
	
}

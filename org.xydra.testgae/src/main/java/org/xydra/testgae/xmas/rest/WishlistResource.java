package org.xydra.testgae.xmas.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.xydra.testgae.xmas.data.WishList;
import org.xydra.testgae.xmas.data.Xmas;


public class WishlistResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishlistResource.class);
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/{repo}/{list}/add", WishlistResource.class, "addData",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null),

		new RestlessParameter("wishes", "1")

		);
		
		r.addGet(path + "/{repo}/{list}", WishlistResource.class, "get",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null)

		);
		
		r.addGet(path + "/{repo}/{list}/clear", WishlistResource.class, "deleteAllWishes",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null)

		);
		
		WishResource.restless(r, path);
		
	}
	
	private WishList wishList;
	
	public void addData(String repoStr, String list, String wishesStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Adding test data ...?wishes=" + wishesStr + " wishes. Start at "
		        + System.currentTimeMillis() + "\n");
		int wishesCount = Integer.parseInt(wishesStr);
		init(repoStr, list);
		this.wishList.addDemoData(wishesCount, new OutputStreamWriter(res.getOutputStream(),
		        "utf-8"));
		w.write(HtmlUtils.link(".", "See all wishes"));
		w.flush();
	}
	
	public void deleteAllWishes(String repoStr, String list, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Deleting all wishes.");
		init(repoStr, list);
		this.wishList.removeAllWishes(new OutputStreamWriter(res.getOutputStream(), "utf-8"));
		w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		new OutputStreamWriter(res.getOutputStream(), "utf-8").flush();
	}
	
	public void get(String repoStr, String list, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		init(repoStr, list);
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write(this.wishList.toHtml(null));
		
		w.write(HtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/" + list + "/add")
		        .withInputText("wishes", "1").withInputSubmit("Add wishes").toString());
		w.write(HtmlUtils.link("/xmas/" + repoStr, "See all wish lists"));
		new OutputStreamWriter(res.getOutputStream(), "utf-8").flush();
	}
	
	private void init(String repoStr, String wishlistId) {
		this.wishList = load(Xmas.getRepository(repoStr), XX.toId(wishlistId));
	}
	
	public static WishList load(XWritableRepository repo, XID listId) {
		XWritableModel model = repo.createModel(listId);
		return new WishList(model);
	}
	
}

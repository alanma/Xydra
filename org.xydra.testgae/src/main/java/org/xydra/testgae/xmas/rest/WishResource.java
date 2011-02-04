package org.xydra.testgae.xmas.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.testgae.ServletUtils;
import org.xydra.testgae.Stopwatch;
import org.xydra.testgae.xmas.HtmlUtils;
import org.xydra.testgae.xmas.data.Wish;
import org.xydra.testgae.xmas.data.Xmas;


public class WishResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishResource.class);
	
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/{repo}/{list}/{wish}/delete", "GET", WishResource.class, "delete",
		        false,

		        new RestlessParameter("repo", null),

		        new RestlessParameter("list", null),

		        new RestlessParameter("wish", null)

		);
		
		r.addGet(path + "/{repo}/{list}/{wish}", WishResource.class, "get",

		new RestlessParameter("repo", null),

		new RestlessParameter("list", null),

		new RestlessParameter("wish", null)

		);
	}
	
	private Wish wish;
	
	public void delete(String repoStr, String listStr, String wishStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		res.getWriter().println("Deleting<br />");
		
		Stopwatch s1 = new Stopwatch().start();
		WishlistResource.load(Xmas.getRepository(repoStr), XX.toId(listStr)).removeWish(
		        XX.toId(wishStr));
		s1.stop();
		res.getWriter().write(s1.getFormattedResult("delete wish", 1) + "<br/>\n");
		
		res.getWriter().write(HtmlUtils.link("..", "See all wishes in this list"));
		res.getWriter().flush();
	}
	
	public void get(String repoStr, String listStr, String wishStr, HttpServletResponse res)
	        throws IOException {
		ServletUtils.headers(res, "text/html");
		init(repoStr, listStr, wishStr);
		res.getWriter().write(this.wish.toHtml());
		res.getWriter().write(
		        HtmlUtils.link("/xmas/" + repoStr + "/" + listStr, "See all wishes in this lists"));
		res.getWriter().flush();
	}
	
	private void init(String repoStr, String listStr, String wishStr) {
		this.wish = load(Xmas.getRepository(repoStr), XX.toId(listStr), XX.toId(wishStr));
	}
	
	public static Wish load(XWritableRepository repo, XID listId, XID wishId) {
		XWritableModel model = repo.createModel(listId);
		XWritableObject xo = model.createObject(wishId);
		return new Wish(xo);
	}
	
}

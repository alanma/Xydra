package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.DiffWritableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.Clock;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.testgae.server.model.xmas.Wish;
import org.xydra.testgae.server.model.xmas.WishList;
import org.xydra.testgae.server.model.xmas.Xmas;


/**
 * Expose a {@link Wish} to the web.
 * 
 * @author xamde
 */
public class WishResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishResource.class);
	
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/{repo}/{list}/{wish}/delete", "GET", WishResource.class, "delete",
		        false, // .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("list", null),// .
		        new RestlessParameter("wish", null)// .
		);
		
		r.addGet(path + "/{repo}/{list}/{wish}", WishResource.class, "get",// .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("list", null),// .
		        new RestlessParameter("wish", null)// .
		);
	}
	
	public static synchronized void delete(String repoStr, String listStr, String wishStr,
	        HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "Delete");
		w.write("Deleting<br />");
		
		Clock s1 = new Clock().start();
		
		// create txn
		DiffWritableModel txnModel = new DiffWritableModel(Xmas.createModel(repoStr,
		        XX.toId(listStr)), true);
		WishList wishList = new WishList(txnModel);
		// manipulate txn
		wishList.removeWish(XX.toId(wishStr));
		// execute txn
		XTransaction txn = txnModel.toTransaction();
		Xmas.executeTransaction(txn);
		
		s1.stop("delete a wish");
		w.write(s1.getStats() + "<br/>\n");
		
		w.write(HtmlUtils.link("..", "See all wishes in this list"));
		w.flush();
		w.close();
	}
	
	public static synchronized void get(String repoStr, String listStr, String wishStr,
	        HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		Wish wish = load(repoStr, listStr, wishStr);
		Writer w = HtmlUtils.startHtmlPage(res, "List");
		w.write(wish.toHtml());
		w.write(HtmlUtils.link("/xmas/" + repoStr + "/" + listStr, "See all wishes in this lists"));
		w.flush();
		w.close();
	}
	
	private static Wish load(String repoStr, String listStr, String wishStr) {
		return load(Xmas.getRepository(repoStr), XX.toId(listStr), XX.toId(wishStr));
	}
	
	public static Wish load(XWritableRepository repo, XID listId, XID wishId) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		XWritableModel model = repo.createModel(listId);
		XWritableObject xo = model.createObject(wishId);
		return new Wish(xo);
	}
	
	public static String toRootRelativeUrl(String repoStr, String list, XID wishId) {
		return "/xmas/" + repoStr + "/" + list + "/" + wishId;
	}
	
}

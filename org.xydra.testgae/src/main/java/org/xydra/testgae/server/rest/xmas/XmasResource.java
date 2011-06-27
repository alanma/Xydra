package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.HtmlUtils.METHOD;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.testgae.server.model.xmas.Xmas;


/**
 * Main resource for the Xmas sample application
 * 
 * @author xamde
 * 
 */
public class XmasResource {
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/xmas/{repo}/add", XmasResource.class, "addData", // .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("lists", "1"),// .
		        new RestlessParameter("wishes", "1")// .
		);
		r.addGet(path + "/xmas/{repo}", XmasResource.class, "get",// .
		        new RestlessParameter("repo", null),// .
		        new RestlessParameter("view", "collapsed")// .
		);
		// expose wish lists
		WishlistResource.restless(r, path + "/xmas");
	}
	
	public void addData(String repoStr, String listsStr, String wishesStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		int listCount = Integer.parseInt(listsStr);
		int wishesCount = Integer.parseInt(wishesStr);
		Writer w = HtmlUtils.startHtmlPage(res, "Xmas | Adding test data");
		w.write("Adding test data ...?lists=" + listsStr + "&wishes=" + wishesStr
		        + " wishes.<br />");
		Xmas.addData(repoStr, listCount, wishesCount, w);
		w.write("<a href='.'>See all wish lists in repository '" + repoStr + "'</a>");
		HtmlUtils.endHtmlPage(w);
	}
	
	public void get(String repoStr, String view, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "Xmas | Adding test data");
		
		Xmas.get(repoStr, view, w);
		w.write(HtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/add").withInputText("lists", "1")
		        .withInputText("wishes", "1").withInputSubmit("Add lists with wishes").toString());
		HtmlUtils.endHtmlPage(w);
	}
	
}

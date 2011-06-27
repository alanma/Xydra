package org.xydra.testgae.server.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;


/**
 * A dashboard for the administrator of this app.
 * 
 * @author xamde
 */
public class AdminDashboardResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AdminDashboardResource.class);
	
	public static void restless(Restless r) {
		r.addGet("/", AdminDashboardResource.class, "index");
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Test GAE");
		w.write("<p>This webapp tests the <a href=\"http://xydra.org\">Xydra GAE datamodel for Google AppEngine</a>.</p>");
		
		w.write("<h2>Generic tools</h2>");
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/gaeinfo", "Shows generic info about runtime environment"),

		HtmlUtils.link("/echo", "Echo current time to verify basic functionality"),

		HtmlUtils.link("/admin/restless", "Introspect all Restless methods")

		));
		
		w.write("<h2>Xmas Wish List example app</h2>");
		
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/xmas/repo1",
		        "Xmas example, repo1. You can use any repo with /xmas/{repoId}.")

		));
		
		w.write("Start any request URL with '/logged/' to record AppStats.");
		
		w.flush();
		w.close();
	}
}

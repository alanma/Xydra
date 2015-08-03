package org.xydra.testgae.server.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.gae.admin.GaeConfigurationResource;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils;

/**
 * A dashboard for the administrator of this app.
 *
 * @author xamde
 */
public class AdminDashboardResource {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AdminDashboardResource.class);

	public static void restless(final Restless r, final String path) {
		r.addGet(path + "/", AdminDashboardResource.class, "index");

		GaeInfoResource.restless(r, path);
		GaeConfigurationResource.restless(r, "/admin");
	}

	public static void index(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		final Writer w = HtmlUtils.startHtmlPage(res, "Xydra Test GAE");
		w.write("<p>This webapp tests the <a href=\"http://xydra.org\">Xydra GAE datamodel for Google AppEngine</a>.</p>");

		w.write("<h2>Generic tools</h2>");
		w.write(SharedHtmlUtils.toOrderedList(

		SharedHtmlUtils.link("/gaeinfo", "Shows generic info about runtime environment"),

		SharedHtmlUtils.link("/echo", "Echo current time to verify basic functionality"),

		SharedHtmlUtils.link("/admin/restless", "Introspect all Restless methods - Admin only"),

		SharedHtmlUtils.link("/admin/gaeconf", "Gae Config - Admin only")

		));

		w.write("<h2>Xmas Wish List example app</h2>");

		w.write(SharedHtmlUtils.toOrderedList(

		SharedHtmlUtils.link("/xmas/repo1",
				"Xmas example, repo1. You can use any repo with /xmas/{repoId}.")

		));

		w.write("Start any request URL with '/logged/' to record AppStats.");

		w.flush();
		w.close();
	}
}

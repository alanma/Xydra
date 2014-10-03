package org.xydra.testgae.server.rest;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.xgae.gaeutils.GaeTestfixer;

/**
 * Some simple basic information about Google AppEngine.
 * 
 * Provides some info + an echo resource that delivers the current time.
 * 
 * @author xamde
 * 
 */
public class GaeInfoResource {

	public static void restless(Restless r, String path) {
		r.addGet(path + "/gaeinfo", GaeInfoResource.class, "index");
		r.addGet(path + "/echo", GaeInfoResource.class, "echo");
	}

	public void index(HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "GAE Information");

		w.write("GAE is in production? " + GaeTestfixer.inProduction() + "<br />");

		/*
		 * check status of java 'assert' keyword which is disabled on AppEngine
		 * in production
		 */
		try {
			assert false : "vm assertions are on";
			w.write("If you can read this, java 'assert' keyword is off");
		} catch (AssertionError e) {
			w.write("If you can read this, java 'assert' keyword is on");
		}
	}

	public String echo() {
		return "It is " + System.currentTimeMillis();
	}

}

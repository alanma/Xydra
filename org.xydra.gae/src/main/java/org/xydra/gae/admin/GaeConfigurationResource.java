package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeUtils;


/**
 * Allows to configure web cache at '.../gaeconf'. Only allowed if called via a
 * path starting with '/admin'.
 */
public class GaeConfigurationResource {
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/gaeconf/set", "GET", GaeUtils.class, "setCacheConf", true,
		        new RestlessParameter("memcache", null));
		r.addMethod(path + "/gaeconf/get", "GET", GaeUtils.class, "getConf", true);
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
		
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
		        + GaePersistence.INSTANCE_ID);
		w.write("GAE cache conf. ");
		w.write(HtmlUtils.link(req.getRequestURI() + "/get", "get stats") + "<br/>");
		w.write(HtmlUtils.link(req.getRequestURI() + "/set?memcache=true",
		        "set memcache=true(default)") + "<br/>");
		w.write(HtmlUtils.link(req.getRequestURI() + "/set?memcache=false", "set memcache=false")
		        + "<br/>");
		w.write(GaeUtils.getConf());
		w.flush();
		w.close();
	}
	
}

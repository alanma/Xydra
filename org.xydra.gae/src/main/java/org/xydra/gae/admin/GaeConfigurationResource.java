package org.xydra.gae.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
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
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "howto", true);
		
	}
	
	public static void howto(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setCharacterEncoding("utf-8");
		res.setContentType("Text/html");
		res.setStatus(200);
		res.getWriter().write("GAE cache conf. ");
		res.getWriter().write(
		        org.xydra.restless.utils.HtmlUtils.link(req.getRequestURI() + "/get", "get stats")
		                + "<br/>");
		res.getWriter().write(
		        org.xydra.restless.utils.HtmlUtils.link(req.getRequestURI() + "/set?memcache=true",
		                "set memcache=true(default)") + "<br/>");
		res.getWriter().write(
		        org.xydra.restless.utils.HtmlUtils.link(
		                req.getRequestURI() + "/set?memcache=false", "set memcache=false")
		                + "<br/>");
		res.getWriter().write(GaeUtils.getConf());
	}
	
}

package org.xydra.testgae.xmas.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.testgae.ServletUtils;
import org.xydra.testgae.xmas.HtmlUtils;


/**
 * TODO these methods should be admin-only in a real app
 * 
 * @author xamde
 * 
 */
public class GaeConfigurator {
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/gaeconf/set", GaeUtils.class, "setCacheConf", new RestlessParameter(
		        "memcache", null), new RestlessParameter("vmcache", null));
		r.addGet(path + "/gaeconf/get", GaeUtils.class, "getConf");
		r.addGet(path + "/gaeconf", GaeConfigurator.class, "howto");
		
	}
	
	public static void howto(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		res.getWriter().write("GAE cache conf. ");
		res.getWriter().write(HtmlUtils.link(req.getRequestURI() + "/get", "get stats") + "<br/>");
		res.getWriter().write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=true&vmcache=true",
		                "set memcache=true, vmcache=true (default)") + "<br/>");
		res.getWriter().write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=true&vmcache=false",
		                "set memcache=true, vmcache=false (default)") + "<br/>");
		res.getWriter().write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=false&vmcache=true",
		                "set memcache=false, vmcache=true (default)") + "<br/>");
		res.getWriter().write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=false&vmcache=false",
		                "set memcache=false, vmcache=false (default)") + "<br/>");
		res.getWriter().write(GaeUtils.getConf());
	}
	
}

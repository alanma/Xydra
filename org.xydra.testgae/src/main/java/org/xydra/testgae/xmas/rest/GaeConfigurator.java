package org.xydra.testgae.xmas.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.GaeUtils;


/**
 * TODO these methods should be admin-only in a real app
 * 
 * @author xamde
 * 
 */
public class GaeConfigurator {
	
	public static void restless(Restless r, String path) {
		r.addGet(path + "/gaeconf/set", GaeUtils.class, "setCacheConf", new RestlessParameter(
		        "memcache", null));
		r.addGet(path + "/gaeconf/get", GaeUtils.class, "getConf");
		r.addGet(path + "/gaeconf", GaeConfigurator.class, "howto");
		
	}
	
	public static void howto(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write("GAE cache conf. ");
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write(HtmlUtils.link(req.getRequestURI() + "/get", "get stats") + "<br/>");
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=true",
		                "set memcache=true(default)") + "<br/>");
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write(
		        HtmlUtils.link(req.getRequestURI() + "/set?memcache=false", "set memcache=false")
		                + "<br/>");
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write(GaeUtils.getConf());
	}
	
}

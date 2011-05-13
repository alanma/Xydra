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
import org.xydra.store.impl.gae.GaeTestfixer;
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
		r.addMethod(path + "/gaeconf/set", "GET", GaeConfigurationResource.class, "setCacheConf",
		        true, new RestlessParameter("instance", null), new RestlessParameter("memcache",
		                null));
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
		
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
		        + GaePersistence.INSTANCE_ID);
		w.write("<h2>GAE cache conf on instance " + GaePersistence.INSTANCE_ID + " </h2>");
		w.write(HtmlUtils.link("/admin/gaeconf/get", "get stats") + "<br/>");
		w.write(HtmlUtils.link("/admin/gaeconf/set?instance=" + GaePersistence.INSTANCE_ID
		        + "&memcache=true", "set memcache=true(default) on this instance")
		        + "<br/>");
		w.write(HtmlUtils.link(req.getRequestURI() + "/set?instance=" + GaePersistence.INSTANCE_ID
		        + "&memcache=false", "set memcache=false on this instance")
		        + "<br/>");
		w.write(GaeUtils.getConf());
		w.flush();
	}
	
	/**
	 * @param memcache 'true' or 'false'
	 * @throws IOException ...
	 */
	public static void setCacheConf(String instanceId, String memcache, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(GaePersistence.INSTANCE_ID.equals(instanceId)) {
			boolean memcache_ = Boolean.parseBoolean(memcache);
			GaeUtils.setUseMemCache(memcache_);
			res.sendRedirect("/admin/gaeconf");
		} else {
			ServletUtils.headers(res, "text/html");
			Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
			        + GaePersistence.INSTANCE_ID);
			w.write("InstandID: " + GaePersistence.INSTANCE_ID + "<br />");
			w.write("Requested instance: " + instanceId + "<br />");
			w.write("No action. Please retry and hope your request will hit the right instance.");
		}
		
	}
	
}

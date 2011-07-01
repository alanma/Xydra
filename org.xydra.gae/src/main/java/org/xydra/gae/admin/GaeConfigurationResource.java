package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeUtils;


/**
 * Allows to configure web cache at '.../gaeconf'. Only allowed if called via a
 * path starting with '/admin'.
 */
public class GaeConfigurationResource {
	
	/**
	 * Clear the memcache.
	 */
	public static void clearCache(HttpServletResponse res) throws IOException {
		Writer w = startPage(GaePersistence.INSTANCE_ID, res);
		XydraRuntime.getMemcache().clear();
		w.write("Memcache cleared.");
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
		        + GaePersistence.INSTANCE_ID);
		w.write("<h2>GAE cache conf on instance " + GaePersistence.INSTANCE_ID + " </h2>");
		w.write("<p>Memcache Conf: " + GaeUtils.getConf() + "</p>");
		
		w.write(HtmlUtils.link("/admin/gaeconf/memcachse/set?instance="
		        + GaePersistence.INSTANCE_ID + "&memcache=true",
		        "set memcache=true(default) on this instance")
		        + "<br/>");
		w.write(HtmlUtils.link("/admin/gaeconf/memcache/set?instance=" + GaePersistence.INSTANCE_ID
		        + "&memcache=false", "set memcache=false on this instance")
		        + "<br/>");
		w.write(HtmlUtils.link("/admin/gaeconf/memcache/clear", "Clear MemCache for all instances"));
		
		w.write("<p>GaeAssert enabled? " + GaeAssert.isEnabled() + "</p>");
		w.write(HtmlUtils.link("/admin/gaeconf/assert/set?instance=" + GaePersistence.INSTANCE_ID
		        + "&assert=false", "set assert=´false(default) on this instance")
		        + "<br/>");
		w.write(HtmlUtils.link("/admin/gaeconf/assert/set?instance=" + GaePersistence.INSTANCE_ID
		        + "&assert=true", "set assert=true on this instance")
		        + "<br/>");
		
		w.write(GaeUtils.getConf());
		
		w.write("<h3>Memcache Stats</h3> " + XydraRuntime.getMemcache().stats() + " Size: "
		        + XydraRuntime.getMemcache().size() + "</p>");
		
		w.flush();
	}
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/gaeconf/memcache/set", "GET", GaeConfigurationResource.class,
		        "setCacheConf", true, new RestlessParameter("instance", null),
		        new RestlessParameter("memcache", null));
		r.addMethod(path + "/gaeconf/memcache/clear", "GET", GaeConfigurationResource.class,
		        "clearCache", true, new RestlessParameter("instance", null));
		
		r.addMethod(path + "/gaeconf/assert/set", "GET", GaeConfigurationResource.class,
		        "setAssert", true, new RestlessParameter("instance", null), new RestlessParameter(
		                "assert", null));
		
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
	}
	
	/**
	 * @param instanceId
	 * @return true if the given instanceId matches the ID of this AppEngine
	 *         server instance.
	 */
	private static boolean onRightInstance(String instanceId) {
		return GaePersistence.INSTANCE_ID.equals(instanceId);
	}
	
	/**
	 * @param memcache 'true' or 'false'
	 * @throws IOException ...
	 */
	public static void setCacheConf(String instanceId, String memcache, HttpServletResponse res)
	        throws IOException {
		Writer w = startPage(GaePersistence.INSTANCE_ID, res);
		if(onRightInstance(instanceId)) {
			boolean memcache_ = Boolean.parseBoolean(memcache);
			GaeUtils.setUseMemCache(memcache_);
			w.write("Memcache set to " + memcache + "<br />");
		} else {
			w.write("No action. Please retry and hope your request will hit the right instance.");
		}
	}
	
	public static void setAssert(String instanceId, String gaeAssert, HttpServletResponse res)
	        throws IOException {
		Writer w = startPage(GaePersistence.INSTANCE_ID, res);
		if(onRightInstance(instanceId)) {
			boolean gaeAssert_ = Boolean.parseBoolean(gaeAssert);
			GaeAssert.setEnabled(gaeAssert_);
			w.write("GaeAssert set to " + gaeAssert + "<br />");
		} else {
			w.write("No action. Please retry and hope your request will hit the right instance.");
		}
	}
	
	private static Writer startPage(String instanceId, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
		        + GaePersistence.INSTANCE_ID);
		w.write("InstanceID: " + GaePersistence.INSTANCE_ID + "<br />");
		w.write("Requested instance: " + instanceId + "<br />");
		return w;
	}
}

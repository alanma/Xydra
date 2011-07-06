package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.perf.StatsGatheringPersistenceWrapper;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.Clock;
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
	
	private static final Logger log = LoggerFactory.getLogger(GaeConfigurationResource.class);
	
	public static final String PROP_ASSERT = "assert";
	
	private static final String PROP_USEMEMCACHE = "usememcache";
	
	/** only used for incoming web request to clear memcache immediately */
	private static final String PROP_CLEARMEMCACHE_NOW = "clearmemcache";
	
	private static GaeConfiguration currentConf = null;
	
	/** first 60 seconds after boot this config is valid */
	public static final GaeConfiguration DEFAULT = GaeConfiguration.createWithLifetime(60 * 1000);
	
	static {
		DEFAULT.map().put(PROP_ASSERT, "false");
		DEFAULT.map().put(PROP_USEMEMCACHE, "true");
		// no PROP_CLEARMEMCACHE_NOW
		DEFAULT.map().put(XydraRuntime.PROP_MEMCACHESTATS, "false");
		DEFAULT.map().put(XydraRuntime.PROP_PERSISTENCESTATS, "false");
		currentConf = DEFAULT;
	}
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/gaeconf/set", "GET", GaeConfigurationResource.class,
		        "setConfiguration", true);
		
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
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
		
		// FIXME ...
		// --------------------------------------- stats
		if(StatsGatheringPersistenceWrapper.isEnabled()) {
			w.write(HtmlUtils.link("/admin/statsPersistence/stop", "Stop")
			        + " stats-gathering persistence");
		} else {
			w.write(HtmlUtils.link("/admin/statsPersistence/start", "Stop")
			        + " stats-gathering persistence");
		}
		
		w.flush();
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
	 * Set some parameters just for a specific instance
	 * 
	 * @throws IOException ...
	 */
	public static void setInstanceConfiguration(String instanceId, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Writer w = HtmlUtils.startHtmlPage(res, "Setting configuration on instance "
		        + GaePersistence.INSTANCE_ID);
		w.write("InstanceID: " + GaePersistence.INSTANCE_ID + "<br />");
		w.write("Requested instance: " + instanceId + "<br />");
		if(onRightInstance(instanceId)) {
			w.write("Yeah, right instance");
		} else {
			w.write("No action. Please retry and hope your request will hit the right instance.");
		}
	}
	
	/**
	 * Take all parameters found {@link HttpServletRequest} and put them in the
	 * current configuration.
	 * 
	 * @throws IOException ...
	 */
	public static void setConfiguration(HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Clock c = new Clock().start();
		Writer w = HtmlUtils.startHtmlPage(res, "Setting GAE Configuration");
		Map<String,String> params = ServletUtils.getRequestparametersAsMap(req);
		
		if(params.containsKey(PROP_CLEARMEMCACHE_NOW)) {
			w.write("Clearing memcache (effective for all instances)<br />");
			w.flush();
			XydraRuntime.getMemcache().clear();
			w.write("Cleared memcache.<br />");
			w.flush();
		} else {
			w.write("No memcache clear. Request one with &" + PROP_CLEARMEMCACHE_NOW + "<br />");
			
		}
		
		w.write("Loading current conf ...<br />");
		w.flush();
		GaeConfiguration conf = getCurrentConfiguration();
		w.write("Stll valid for " + conf.getTimeToLive() + " ms (until " + conf.getValidUntilUTC()
		        + ") -- and there is nothing (yet) you can do to speed this up<br />");
		w.write("Updating conf ...<br />");
		w.flush();
		String validUntilUtcStr = params.get(GaeConfiguration.PROP_VALID_UTC);
		if(validUntilUtcStr == null) {
			throw new IllegalArgumentException("paramter '" + GaeConfiguration.PROP_VALID_UTC
			        + "' must be set");
		}
		conf.setValidUntilUTC(Long.parseLong(validUntilUtcStr));
		w.write("New config valid until " + conf.getValidUntilUTC() + " = expires in "
		        + conf.getTimeToLive() + "+ms<br />");
		w.flush();
		for(String key : params.keySet()) {
			if(key.equals(GaeConfiguration.PROP_VALID_UTC)) {
				continue;
			}
			String value = params.get(key);
			if(value == null) {
				String previous = conf.map().remove(key);
				w.write("Removing key '" + key + "' from config (was set to '" + previous
				        + "' before)<br />");
			} else {
				String previous = conf.map().put(key, value);
				w.write("Setting key '" + key + "' to '" + value + "' in config (was set to '"
				        + previous + "' before)<br />");
			}
			w.flush();
		}
		w.write("Persisting conf... <br />");
		w.flush();
		persistsConfiguration(conf);
		w.write("Processing conf (now all the stuff you said becomes effective - ON THIS INSTANCE only)<br />");
		w.flush();
		processConfiguration(conf);
		w.write("Done with config update. Stats: " + c.stop("request").getStats());
		HtmlUtils.endHtmlPage(w);
	}
	
	private static void processConfiguration(GaeConfiguration conf) {
		// assertions
		boolean gaeAssert = conf.getAsBoolean(PROP_ASSERT);
		GaeAssert.setEnabled(gaeAssert);
		// memcache
		boolean usememcache = Boolean.parseBoolean(PROP_USEMEMCACHE);
		GaeUtils.setUseMemCache(usememcache);
		// memcache stats
		boolean memcachestats = Boolean.parseBoolean(XydraRuntime.PROP_MEMCACHESTATS);
		setMemcacheStats(memcachestats);
		// memcache stats
		boolean persistencestats = Boolean.parseBoolean(XydraRuntime.PROP_PERSISTENCESTATS);
		setPersistenceStats(persistencestats);
	}
	
	private static void setMemcacheStats(boolean memcachestats) {
		XydraRuntime.setParameter(XydraRuntime.PROP_MEMCACHESTATS, "" + memcachestats);
		XydraRuntime.forceReInitialisation();
	}
	
	private static void setPersistenceStats(boolean persistencestats) {
		XydraRuntime.setParameter(XydraRuntime.PROP_PERSISTENCESTATS, "" + persistencestats);
		XydraRuntime.forceReInitialisation();
	}
	
	private static void persistsConfiguration(GaeConfiguration conf) {
		GaeConfiguration.store(conf);
	}
	
	public static synchronized GaeConfiguration getCurrentConfiguration() {
		if(currentConf == null || !currentConf.isStillValid()) {
			log.info("Current config is either null or too old. Getting a fresh one.");
			currentConf = GaeConfiguration.getInstance();
		}
		return currentConf;
	}
	
}

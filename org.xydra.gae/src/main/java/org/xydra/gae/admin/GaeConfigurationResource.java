package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.Clock;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.HtmlUtils.METHOD;
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
	
	private static GaeConfiguration currentConf = GaeConfiguration.DEFAULT;
	
	/** only used for incoming web request to clear memcache immediately */
	private static final String PROP_CLEARMEMCACHE_NOW = "clearmemcache";
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/gaeconf/set", "GET", GaeConfigurationResource.class,
		        "setConfiguration", true);
		
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
	}
	
	private static void addCommonStyle(Writer w) throws IOException {
		w.write("<style>\n"

		+ "form { display:inline; } \n"

		+ "</style>");
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeConfigurationResource.getCurrentConfiguration().applyIfNecessary();
		Writer w = HtmlUtils.startHtmlPage(res, "GAE cache conf on instance "
		        + GaePersistence.INSTANCE_ID);
		writeIndex(w);
		HtmlUtils.endHtmlPage(w);
	}
	
	private static void writeIndex(Writer w) throws IOException {
		addCommonStyle(w);
		w.write("<h2>Instance " + GaePersistence.INSTANCE_ID + " </h2>");
		
		w.write("Last config processing on XydraRuntime in this instance was "
		        + (System.currentTimeMillis() - XydraRuntime.getLastTimeInitialisedAt())
		        + " ms ago. Every instance checks every " + GaeConfiguration.CONFIG_APPLY_INTERVAL
		        + "ms to update.");
		w.flush();
		
		w.write("<h3>GaeUtils Conf</h3>"

		+ "<p>" + GaeUtils.getConf() + "</p>");
		
		w.write("<h3>XydraRuntime</h3>");
		writeToggle(GaeConfiguration.PROP_USEMEMCACHE, w);
		w.write(HtmlUtils.link("/admin/gaeconf/set?" + PROP_CLEARMEMCACHE_NOW,
		        "Clear MemCache for all instances") + "<br />");
		writeToggle(XydraRuntime.PROP_MEMCACHESTATS, w);
		writeToggle(XydraRuntime.PROP_PERSISTENCESTATS, w);
		
		w.write("<h3>GaeAssert</h3>");
		w.write("<p>Enabled? " + GaeAssert.isEnabled() + "</p>");
		writeToggle(GaeConfiguration.PROP_ASSERT, w);
		
		w.write("<h3>Memcache Stats</h3> " + XydraRuntime.getMemcache().stats() + " Size: "
		        + XydraRuntime.getMemcache().size() + "</p>");
		
		w.write("<h3>XydraRuntime (" + XydraRuntime.getConfigMap().size() + " entries)</h3>");
		w.write(HtmlUtils.toDefinitionList(XydraRuntime.getConfigMap()));
		w.flush();
	}
	
	private static void writeToggle(String property, Writer w) throws IOException {
		String current = XydraRuntime.getConfigMap().get(property);
		
		w.write("Property '"
		        + property
		        + "'"
		        + " = '"
		        + (current == null ? "false" : current)
		        + "'. Set to "
		        
		        + HtmlUtils
		                .form(METHOD.GET, "/admin/gaeconf/set")
		                .withHiddenInputText(property, "true")
		                .withHiddenInputText(GaeConfiguration.PROP_VALID_UTC,
		                        "" + (System.currentTimeMillis() + 60 * 1000))
		                .withInputSubmit("true")
		        
		        + HtmlUtils
		                .form(METHOD.GET, "/admin/gaeconf/set")
		                .withHiddenInputText(property, "false")
		                .withHiddenInputText(GaeConfiguration.PROP_VALID_UTC,
		                        "" + (System.currentTimeMillis() + 60 * 1000))
		                .withInputSubmit("false")

		        + "<br/>");
	}
	
	/**
	 * @param requestedInstanceId
	 * @return true if the given instanceId matches the ID of this AppEngine
	 *         server instance.
	 */
	private static boolean onRightInstance(String requestedInstanceId) {
		return GaePersistence.INSTANCE_ID.equals(requestedInstanceId);
	}
	
	/**
	 * Set some parameters just for a specific instance.
	 * 
	 * Currently, there are no instance specific parameters.
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
	 * @param req ..
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void setConfiguration(HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeConfigurationResource.getCurrentConfiguration().applyIfNecessary();
		
		Clock c = new Clock().start();
		Writer w = HtmlUtils.startHtmlPage(res, "Setting GAE Configuration");
		Map<String,String> params = ServletUtils.getRequestparametersAsMap(req);
		
		// ------------------ clearmemcache
		if(params.containsKey(PROP_CLEARMEMCACHE_NOW)) {
			w.write("Clearing memcache (effective for all instances)<br />");
			w.flush();
			XydraRuntime.getMemcache().clear();
			w.write("<b>Cleared memcache</b>.<br />");
			w.flush();
		} else {
			w.write("No memcache clear.");
			
		}
		
		// ------------- load conf
		w.write("Loading current conf ...<br />");
		w.flush();
		GaeConfiguration conf = getCurrentConfiguration();
		w.write("Still valid for " + conf.getTimeToLive() + " ms (until " + conf.getValidUntilUTC()
		        + ") -- and there is nothing (yet ;-) you can do to speed this up<br />");
		w.write("Updating conf ...<br />");
		w.flush();
		String validUntilUtcStr = params.get(GaeConfiguration.PROP_VALID_UTC);
		if(validUntilUtcStr == null) {
			log.warn("No paramter '" + GaeConfiguration.PROP_VALID_UTC
			        + "' set. Using default 60 seconds.");
			conf.setValidUntilUTC(System.currentTimeMillis() + 60 * 1000);
		} else {
			conf.setValidUntilUTC(Long.parseLong(validUntilUtcStr));
		}
		w.write("New config valid until " + conf.getValidUntilUTC()
		        + " (set from web request) = expires in " + conf.getTimeToLive() + " ms<br />");
		w.flush();
		w.write("<b>Updates</b><br />");
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
				w.write("Setting property '" + key + "' = '" + value + "' (Old: '" + previous
				        + "')<br />");
			}
			w.flush();
		}
		w.write("Persisting conf... <br />");
		w.flush();
		persistsConfiguration(conf);
		w.write("<h3>Current configuration in XydraRuntime:</h3>");
		w.write(HtmlUtils.toDefinitionList(XydraRuntime.getConfigMap()));
		w.flush();
		w.write("Done with config update. Stats: " + c.stop("request").getStats());
		writeIndex(w);
		HtmlUtils.endHtmlPage(w);
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

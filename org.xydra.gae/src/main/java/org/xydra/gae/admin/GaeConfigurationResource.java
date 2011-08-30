package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.Clock;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.XydraConfigUtils;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.GaeUtils;


/**
 * Allows to configure web cache at '.../gaeconf'. Only allowed if called via a
 * path starting with '/admin'.
 */
public class GaeConfigurationResource {
	
	private static final Logger log = LoggerFactory.getLogger(GaeConfigurationResource.class);
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod(path + "/gaeconf", "GET", GaeConfigurationResource.class, "index", true);
	}
	
	private static void addCommonStyle(Writer w) throws IOException {
		w.write("<style type='text/css'>\n"

		+ "form { display:inline; } \n"

		+ ".cpodebug h1, .cpodebug h2, .cpodebug h3, .cpodebug h4{\r\n" + "   color: #1E93F6;\r\n"
		        + "   font-size: 120%;\r\n" + "}   \r\n" + ".cpodebug div, \r\n"
		        + ".cpodebug span, \r\n" + ".cpodebug h1, \r\n" + ".cpodebug h2, \r\n"
		        + ".cpodebug h3, \r\n" + ".cpodebug h4, \r\n" + ".cpodebug h5, \r\n"
		        + ".cpodebug h6, \r\n" + ".cpodebug p, \r\n" + ".cpodebug blockquote, \r\n"
		        + ".cpodebug pre,\r\n" + ".cpodebug a, \r\n" + ".cpodebug code, \r\n"
		        + ".cpodebug img, \r\n" + ".cpodebug dl, \r\n" + ".cpodebug dt, \r\n"
		        + ".cpodebug dd, \r\n" + ".cpodebug ol, \r\n" + ".cpodebug ul, \r\n"
		        + ".cpodebug li,\r\n" + ".cpodebug table, \r\n" + ".cpodebug tbody, \r\n"
		        + ".cpodebug tfoot, \r\n" + ".cpodebug thead, \r\n" + ".cpodebug tr, \r\n"
		        + ".cpodebug th, \r\n" + ".cpodebug td {\r\n" + "    margin: 4px;\r\n"
		        + "    padding: 2px;\r\n" + "}   \r\n"
		        + ".cpodebug dt { color: white; background-color: #1E93F6; display: inline; }\r\n"
		        + ".cpodebug dd { margin-left: 15px; display: inline; }\r\n"
		        + ".cpodebug dd { display: block; }\r\n" + "\r\n" + ".cpodebug .key {\r\n"
		        + "   color: #1E93F6;\r\n" + "   font-weight: bold;\r\n" + "} \r\n"
		        + ".cpodebug .comment {\r\n" + "		  width: 25%;\r\n"
		        + "		  white-space: normal;\r\n" + "}\r\n" + "\r\n"
		        + ".cpodebug th, .cpodebug td {\r\n" + "  border: 1px solid #ccc;\r\n" + "}"

		        + "</style>");
	}
	
	/**
	 * Take all parameters found {@link HttpServletRequest} and put them in the
	 * current configuration.
	 * 
	 * Then display current config.
	 * 
	 * @param req ..
	 * @param res ..
	 * @throws IOException ...
	 */
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		GaeConfigurationManager.assertValidGaeConfiguration();
		Clock c = new Clock().start();
		
		ServletUtils.headers(res, ServletUtils.CONTENTTYPE_TEXT_HTML);
		Writer w = res.getWriter();
		HtmlUtils.writeHtmlHeaderOpenBody(w,
		        "GAE cache conf on instance " + XydraRuntime.getInstanceId());
		w.flush();
		addCommonStyle(w);
		
		/**
		 * Process request
		 */
		w.write("Processing current request...<br />");
		// process backend conf
		w.write("Processing GAE back-end configuration from request...<br />");
		Map<String,String> params = ServletUtils.getRequestparametersAsMap(req);
		w.write("<dl class='cpodebug'>");
		w.write("<dt>Clear memcache</dt><dd>");
		handleClearMemcache(params, w);
		w.write("</dd>");
		w.write("<dt>Process config now</dt><dd>");
		handleProcessConfigNow(params, w);
		w.write("</dd>");
		w.write("</dl>");
		w.flush();
		
		// load current instance
		w.write("Loading current GAE conf ...<br />");
		w.flush();
		GaeConfiguration gaeConf = GaeConfigurationManager.getCurrentConfiguration();
		assert gaeConf.isStillValid();
		w.write("Valid for " + gaeConf.getTimeToLive() + " ms (until " + gaeConf.getValidUntilUTC()
		        + ") <br />");
		w.flush();
		
		// process instance conf
		w.write("Processing updates to current conf ...<br />");
		w.flush();
		// first extract validUtc
		String validUntilUtcStr = params.get(GaeConfiguration.PROP_VALID_UTC);
		params.remove(GaeConfiguration.PROP_VALID_UTC);
		
		Map<String,String> changeMap = XydraConfigUtils.getChanges(gaeConf.map(), params);
		boolean changes = false;
		for(String key : changeMap.keySet()) {
			String value = changeMap.get(key);
			if(value.equals(XydraConfigUtils.EMPTY_VALUE)) {
				// remove request, persist as key=empty so that instances can
				// know about this change
				gaeConf.map().put(key, value);
				w.write("Config command: '" + key + "': ' => EMPTY<br />");
			} else {
				gaeConf.map().put(key, value);
				w.write("Config command: '" + key + "': ' => '" + value + "'<br />");
			}
			changes = true;
			w.flush();
		}
		
		if(changes) {
			w.write("Config changed. Persisting... ");
			setNewTimeToLive(gaeConf, validUntilUtcStr, w);
			w.flush();
			gaeConf.store();
			w.write(" Done.<br/>");
			w.flush();
			w.write("Applying locally: Send changes to all local listeners ...");
			GaeConfigurationManager.fireOnChange(gaeConf);
			w.write(" Done.<br/>");
			w.flush();
		} else {
			w.write("No changes.<br/>");
		}
		w.flush();
		
		/**
		 * Display current config
		 */
		w.write("<h2>Config summary for instance " + XydraRuntime.getInstanceId() + " </h2>");
		long lastUpdateMsAgo = System.currentTimeMillis() - XydraRuntime.getLastTimeInitialisedAt();
		w.write("Last XydraRuntime init on this instance was " + lastUpdateMsAgo + " ms ago.<br />");
		
		TreeSet<String> keys = new TreeSet<String>();
		keys.addAll(params.keySet());
		keys.addAll(XydraRuntime.getConfigMap().keySet());
		keys.addAll(gaeConf.map().keySet());
		// hide this key
		keys.remove(GaeConfiguration.PROP_VALID_UTC);
		// add keys to force input fields for them
		keys.add(GaeConfigSettings.PROP_USEMEMCACHE);
		keys.add(XydraRuntime.PROP_MEMCACHESTATS);
		keys.add(XydraRuntime.PROP_PERSISTENCESTATS);
		keys.add(GaeConfigSettings.CLEAR_LOCAL_VM_CACHE);
		
		w.write("<form method='get' action='/admin/gaeconf' onSubmit='"

		+ "document.forms[0].__protoValue.name=document.forms[0].__protoKey.value;"

		+ "document.forms[0].removeChild(document.forms[0].__protoKey);"

		+ "'>");
		w.write("<table class='cpodebug'>");
		w.write("<tr>" +

		"<th scope='col'>Key</th>" +

		"<th scope='col'>Request</th>" +

		"<th scope='col'>GaeConf</th>" +

		"<th scope='col'>This instance</th>" +

		"</tr>");
		for(String key : keys) {
			String gaeConfValue = normalize(gaeConf.map().get(key));
			
			w.write("<tr><td>" + key + "</td>" +

			"<td>" + normalize(params.get(key)) + "</td>" +

			"<td>'" + gaeConfValue + "' => " + formField(key, gaeConfValue) + "</td>" +

			"<td>" + normalize(XydraRuntime.getConfigMap().get(key)) + "</td>" +

			"</tr>");
		}
		w.write("</table>");
		
		w.write("Add key = <input type='text' name='__protoKey' value='' />");
		w.write(", value = <input type='text' name='__protoValue' value='' /><br />");
		
		long now = System.currentTimeMillis();
		long in1Minute = now + (60 * 60000);
		long in1Hour = now + (60 * 60000);
		long in1Day = now + (24 * 60 * 60000);
		w.write("Valid for "

		+ "<input type='radio' name='" + GaeConfiguration.PROP_VALID_UTC + "' value='" + in1Minute
		        + "'  checked='checked' />1 minute"

		        + "<input type='radio' name='" + GaeConfiguration.PROP_VALID_UTC + "' value='"
		        + in1Hour + "'/>1 hour"

		        + "<input type='radio' name='" + GaeConfiguration.PROP_VALID_UTC + "' value='"
		        + in1Day + "'/>1 day (careful)");
		
		w.write("<input type='submit' value='Save' />");
		w.write("<a href='/admin/gaeconf'>Browse only</a>");
		w.write("</form>");
		
		// more data
		w.write("<hr />");
		w.write("<h3>Direct data on this instance</h3>");
		w.write("GaeUtils raw conf: <tt>" + GaeUtils.getConf() + "</tt><br />");
		w.write("GaeAssert: <tt>" + GaeAssert.isEnabled() + "</tt><br />");
		w.write("Memcache Stats: <tt>" + XydraRuntime.getMemcache().stats() + "</tt><br />");
		w.write("Memcache size: <tt>" + XydraRuntime.getMemcache().size() + "</tt><br />");
		w.flush();
		
		// stats
		w.write("Processing stats for this request: " + c.stop("request").getStats() + " <br/>");
		
		HtmlUtils.endHtmlPage(w);
	}
	
	private static void handleProcessConfigNow(Map<String,String> params, Writer w)
	        throws IOException {
		String value = normalize(params.get(GaeConfigSettings.PROCESS_CONFIG_NOW));
		if(!value.equals("")) {
			w.write("Force processing local gae conf (as it was before this request)<br/>");
			w.write("Force reload from datastore...<br/>");
			w.flush();
			GaeConfigurationManager.loadConfigOrUseDefaults();
			w.write("Force processing...<br/>");
			w.flush();
			GaeConfigurationManager.fireOnChange(GaeConfigurationManager.getCurrentConfiguration());
			w.write("<b>Done</b>.<br />");
			w.flush();
		} else {
			w.write("None. <a href='?" + GaeConfigSettings.PROCESS_CONFIG_NOW
			        + "=true'>Request conf processing NOW</a><br />");
		}
		// remove key as we processed it already
		params.remove(GaeConfigSettings.PROCESS_CONFIG_NOW);
	}
	
	/**
	 * @param value can be null
	 * @return never null. Returns value or empty String to denote null.
	 */
	public static String normalize(String value) {
		if(value == null || value.trim().equals("") || value.trim().equals("null")
		        || value.trim().equals("false")) {
			return "";
		} else {
			return value;
		}
	}
	
	private static String formField(String key, String initialValue) {
		return "<input type='text' name='" + key + "' value='" + initialValue + "' />";
	}
	
	/**
	 * @param currentConf never null
	 * @param params never null
	 * @param w never null
	 * @throws IOException
	 */
	private static void setNewTimeToLive(GaeConfiguration currentConf, String validUntilUtcStr,
	        Writer w) throws IOException {
		if(validUntilUtcStr == null) {
			log.warn("No paramter '" + GaeConfiguration.PROP_VALID_UTC
			        + "' set. Using default 60 seconds.");
			currentConf.setValidUntilUTC(System.currentTimeMillis() + 60 * 1000);
		} else {
			currentConf.setValidUntilUTC(Long.parseLong(validUntilUtcStr));
		}
		w.write("New config valid until " + currentConf.getValidUntilUTC()
		        + " (set from web request) = expires in " + currentConf.getTimeToLive()
		        + " ms<br />");
	}
	
	private static void handleClearMemcache(Map<String,String> params, Writer w) throws IOException {
		String value = normalize(params.get(GaeConfigSettings.PROP_CLEARMEMCACHE_NOW));
		if(!value.equals("")) {
			w.write("Clearing memcache <b>now</b> (effective for all instances)<br />");
			w.flush();
			XydraRuntime.getMemcache().clear();
			w.write("<b>Cleared memcache</b>.<br />");
			w.flush();
		} else {
			w.write("No memcache clear requested. <a href='?"
			        + GaeConfigSettings.PROP_CLEARMEMCACHE_NOW + "=true'>Request one</a><br />");
		}
		// remove key as we processed it already
		params.remove(GaeConfigSettings.PROP_CLEARMEMCACHE_NOW);
	}
	
	/**
	 * @param requestedInstanceId
	 * @return true if the given instanceId matches the ID of this AppEngine
	 *         server instance.
	 */
	private static boolean onRightInstance(String requestedInstanceId) {
		return XydraRuntime.getInstanceId().equals(requestedInstanceId);
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
		
		Writer w = HtmlUtils.startHtmlPage(res,
		        "Setting configuration on instance " + XydraRuntime.getInstanceId());
		w.write("InstanceID: " + XydraRuntime.getInstanceId() + "<br />");
		w.write("Requested instance: " + instanceId + "<br />");
		if(onRightInstance(instanceId)) {
			w.write("Yeah, right instance");
		} else {
			w.write("No action. Please retry and hope your request will hit the right instance.");
		}
	}
	
}

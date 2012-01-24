package org.xydra.googleanalytics.logsink;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.xydra.googleanalytics.FocusPoint;
import org.xydra.googleanalytics.GaEvent;
import org.xydra.googleanalytics.Tracker;
import org.xydra.googleanalytics.UserInfo;
import org.xydra.googleanalytics.Utils;
import org.xydra.log.ILogListener;
import org.xydra.log.Logger;


/**
 * Routes Xydra log events (of level 'warn' and 'error') to Google Analytics
 * events.
 * 
 * To avoid endless loops, this class uses no Logger itself.
 * 
 * <h3>Syntax</h3> Syntax for data logging via GA?key=val&...
 * 
 * Recognised key names are: 'category', 'action', 'label', and 'value'.
 * Category and action are mandatory.
 * 
 * Level debug is NEVER logging anything.
 * 
 * @author voelkel
 */
public class GALogListener implements ILogListener, UserInfo {
	
	private long currentSessionStartTime = Utils.getCurrentTimeInSeconds();
	
	private String domainName;
	
	private long thirtyOneBitId = Utils.random31bitInteger();
	private Tracker tracker;
	
	/**
	 * @param trackerCode like 'UA-271022-28'
	 * @param domainName without leading www. Other 3rd level domain names may
	 *            remain.
	 */
	public GALogListener(String trackerCode, String domainName) {
		this.tracker = new Tracker(trackerCode);
		this.domainName = domainName;
	}
	
	@Override
	public void debug(Logger log, String msg) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	@Override
	public void error(Logger log, String msg) {
		track(log, "error", msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		track(log, "error", msg, t);
	}
	
	@Override
	public long get31BitId() {
		return this.thirtyOneBitId;
	}
	
	@Override
	public long getCurrentSessionStartTime() {
		return this.currentSessionStartTime;
	}
	
	@Override
	public String getDomainName() {
		return this.domainName;
	}
	
	@Override
	public long getFirstVisitStartTime() {
		return this.currentSessionStartTime;
	}
	
	@Override
	public String getHostName() {
		InetAddress addr;
		String hostname = "(not set)";
		try {
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch(UnknownHostException e) {
			// ("Could not determine local hostname, never mind", e);
		}
		return hostname;
	}
	
	@Override
	public long getLastVisitStartTime() {
		return this.currentSessionStartTime;
	}
	
	@Override
	public long getSessionCount() {
		return 1;
	}
	
	public Tracker getTracker() {
		return this.tracker;
	}
	
	// TODO document what would be a legal value and re-enable in UrchinCookie
	// code
	@Override
	public String getVar() {
		return null;
	}
	
	@Override
	public void info(Logger log, String msg) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	@Override
	public void trace(Logger log, String msg) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		if(hasGaData(msg)) {
			trackData(log, "info", msg);
		}
	}
	
	/**
	 * @param log
	 * @param gaEvent
	 * @deprecated use explicit logging syntax GAE?... (see class comment)
	 */
	@Deprecated
	private void track(Logger log, GaEvent gaEvent) {
		this.tracker.track(new FocusPoint(log.toString()), "-", this, gaEvent);
	}
	
	/**
	 * @param log
	 * @param logLevel
	 * @param msg
	 * @deprecated use explicit logging syntax GAE?... (see class comment)
	 */
	@Deprecated
	private void track(Logger log, String logLevel, String msg) {
		track(log, new GaEvent(logLevel, msg));
	}
	
	/**
	 * @param log ..
	 * @param logLevel ..
	 * @param msg must have format 'bla bla bla GA?key=val?key=val' (end)
	 */
	private void trackData(Logger log, String logLevel, String msg) {
		assert hasGaData(msg);
		String dataPart = msg.substring(msg.indexOf(GA_URI) + GA_URI.length());
		Map<String,String> dataMap = parseQueryString(dataPart);
		GaEvent gaEvent;
		String category = dataMap.get("category");
		assert category != null;
		String action = dataMap.get("action");
		assert action != null;
		String optionalLabel = dataMap.get("label");
		String optionalValue = dataMap.get("value");
		int value = -1;
		if(optionalValue != null) {
			value = Integer.parseInt(optionalValue);
		}
		if(optionalLabel == null) {
			gaEvent = new GaEvent(category, action);
		} else {
			if(value == -1) {
				gaEvent = new GaEvent(category, action, optionalLabel);
			} else {
				gaEvent = new GaEvent(category, action, optionalLabel, value);
			}
		}
		
		track(log, gaEvent);
	}
	
	private void track(Logger log, String logLevel, String msg, Throwable t) {
		track(log, new GaEvent(logLevel, t.getClass().getCanonicalName(), t.getMessage()));
	}
	
	@Override
	public void warn(Logger log, String msg) {
		track(log, "warn", msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		trace(log, "info", t);
	}
	
	public static final String GA_URI = "GA?";
	
	public static boolean hasGaData(String s) {
		return s.contains(GA_URI);
	}
	
	public static Map<String,String> parseQueryString(String q) {
		Map<String,String> map = new HashMap<String,String>();
		String[] pairs = q.split("\\&");
		
		for(String s : pairs) {
			String[] parts = s.split("=");
			String key = parts[0];
			String value = parts.length > 1 ? parts[1] : null;
			map.put(key, value);
		}
		return map;
	}
	
}

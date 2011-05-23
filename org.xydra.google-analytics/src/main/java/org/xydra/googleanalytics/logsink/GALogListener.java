package org.xydra.googleanalytics.logsink;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
	}
	
	@Override
	public void error(Logger log, String msg) {
		track(log, "error", msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		track(log, "error", msg, t);
	}
	
	public long get31BitId() {
		return this.thirtyOneBitId;
	}
	
	@Override
	public long getCurrentSessionStartTime() {
		return this.currentSessionStartTime;
	}
	
	public String getDomainName() {
		return this.domainName;
	}
	
	@Override
	public long getFirstVisitStartTime() {
		return this.currentSessionStartTime;
	}
	
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
	
	Tracker getTracker() {
		return this.tracker;
	}
	
	// TODO document what would be a legal value and re-enable in UrchinCookie
	// code
	public String getVar() {
		return null;
	}
	
	@Override
	public void info(Logger log, String msg) {
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
	}
	
	@Override
	public void trace(Logger log, String msg) {
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
	}
	
	private void track(Logger log, GaEvent gaEvent) {
		this.tracker.track(new FocusPoint(log.toString()), "-", this, gaEvent);
	}
	
	private void track(Logger log, String logLevel, String msg) {
		track(log, new GaEvent(logLevel, msg));
	}
	
	private void track(Logger log, String logLevel, String msg, Throwable t) {
		track(log, new GaEvent(logLevel, msg, t.getMessage()));
	}
	
	@Override
	public void warn(Logger log, String msg) {
		track(log, "warn", msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		trace(log, "info", t);
	}
	
}

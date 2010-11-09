package org.xydra.googleanalytics;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class UserInfoImpl implements UserInfo {
	
	private String domainName;
	
	public UserInfoImpl(String domainName) {
		this.domainName = domainName;
	}
	
	private static Logger log = LoggerFactory.getLogger(UserInfoImpl.class);
	
	public long getCurrentSessionStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	public long getFirstVisitStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	public long getLastVisitStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	public long getSessionCount() {
		return 1;
	}
	
	public String getDomainName() {
		return this.domainName;
	}
	
	// TODO document what would be a legal value and re-enable in UrchinCookie
	// code
	public String getVar() {
		return null;
	}
	
	public String getHostName() {
		InetAddress addr;
		String hostname = "(not set)";
		try {
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch(UnknownHostException e) {
			log.debug("Could not determine local hostname, never mind", e);
		}
		return hostname;
	}
	
	public long get31BitId() {
		return Utils.random31bitInteger();
	}
	
}

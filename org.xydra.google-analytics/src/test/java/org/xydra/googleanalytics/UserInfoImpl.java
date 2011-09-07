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
	
	@Override
    public long getCurrentSessionStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	@Override
    public long getFirstVisitStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	@Override
    public long getLastVisitStartTime() {
		return Utils.getCurrentTimeInSeconds();
	}
	
	@Override
    public long getSessionCount() {
		return 1;
	}
	
	@Override
    public String getDomainName() {
		return this.domainName;
	}
	
	// TODO document what would be a legal value and re-enable in UrchinCookie
	// code
	@Override
    public String getVar() {
		return null;
	}
	
	@Override
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
	
	@Override
    public long get31BitId() {
		return Utils.random31bitInteger();
	}
	
}

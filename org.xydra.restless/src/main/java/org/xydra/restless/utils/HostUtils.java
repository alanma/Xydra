package org.xydra.restless.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class HostUtils {
	
	public static final Logger log = LoggerFactory.getLogger(HostUtils.class);
	
	private static String hostname = null;
	
	public static String getLocalHostname() {
		if(hostname == null) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			} catch(UnknownHostException e) {
				log.warn("Sorry, could not create a better localhost name than 'localhost'");
				hostname = "localhost";
			}
		}
		return hostname;
	}
	
	public static int getRequestPort(HttpServletRequest req) {
		return req.getServerPort();
	}
	
	public static String getServernameWithPort(HttpServletRequest req) {
		int port = getRequestPort(req);
		String hostname = isLocalRequest(req) ? getLocalHostname() : req.getServerName();
		return hostname + (port == 80 ? "" : ":" + port);
	}
	
	/**
	 * @param req ..
	 * @return true if host indicated in 'req' is a local host
	 */
	public static boolean isLocalRequest(HttpServletRequest req) {
		String serverName = req.getServerName();
		log.debug("localhost = " + serverName);
		if(serverName.equals("127.0.0.1") || serverName.equals("localhost")
		        || serverName.equals(getLocalHostname())) {
			return true;
		}
		return false;
	}
	
}

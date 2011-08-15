package org.xydra.restless.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class HostUtils {
	
	public static final Logger log = LoggerFactory.getLogger(HostUtils.class);
	
	public static String getLocalHostname() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			return hostname;
		} catch(UnknownHostException e) {
			log.warn("Sorry, could not create a better localhost name than 'localhost'");
			return "localhost";
		}
	}
	
}

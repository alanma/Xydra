package org.xydra.restless.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	
}

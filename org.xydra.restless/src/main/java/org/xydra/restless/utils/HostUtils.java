package org.xydra.restless.utils;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

@ThreadSafe
public class HostUtils {

	public static final Logger log = LoggerFactory.getLogger(HostUtils.class);

	/**
	 * these variable are read and written by static methods. Synchronized
	 * access is necessary, even directly in this class. It's safest to only
	 * read them by calling the getLocalHostname()/getLocalIpAddress() methods.
	 */
	private static String hostname = null, ipaddress = null;

	/**
	 * @return the username of the user currently logged in on the local
	 *         machine. Makes only sense for local testing.
	 */
	public static String getLocalUserName() {
		return System.getProperty("user.name");
	}

	public static synchronized String getLocalHostname() {
		if (hostname == null) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				hostname = addr.getHostName();
			} catch (UnknownHostException e) {
				log.warn("Sorry, could not create a better localhost name than 'localhost'");
				hostname = "localhost";
			}
		}
		return hostname;
	}

	public static synchronized String getLocalIpAddress() {
		if (ipaddress == null) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				ipaddress = addr.getHostAddress();
			} catch (UnknownHostException e) {
				log.warn("Sorry, could not create a better IP address than '127.0.0.1'");
				ipaddress = "127.0.0.1";
			}
		}
		return ipaddress;
	}

	/**
	 * 
	 * @param req
	 * @NeverNull
	 * @return the port number of the request
	 */
	public static int getRequestPort(@NeverNull HttpServletRequest req) {
		return req.getServerPort();
	}

	/**
	 * 
	 * @param req
	 * @NeverNull
	 * @return the server name (part before the port)
	 */
	public static String getServernameWithPort(@NeverNull HttpServletRequest req) {
		int port = getRequestPort(req);
		String hostname = isLocalRequest(req) ? getLocalIpAddress() : req.getServerName();
		return hostname + (port == 80 ? "" : ":" + port);
	}

	/**
	 * @param req
	 *            .. @NeverNull
	 * @return true if host indicated in 'req' is a local host
	 */
	public static boolean isLocalRequest(@NeverNull HttpServletRequest req) {
		String serverName = req.getServerName();
		log.debug("serverName = " + serverName);
		if (serverName.equals("127.0.0.1") || serverName.equals("localhost")
				|| serverName.equals(getLocalHostname())) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		System.out.println(getLocalIpAddress() + " = " + getLocalHostname());
	}

}

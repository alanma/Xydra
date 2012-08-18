package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.restless.utils.HostUtils;


/**
 * A tweaked request that returns the server name with support for
 * Restless.X_HOST_Override. For security reasons, this is only effective on
 * localhost, where it simplifies testing..
 * 
 * @author xamde
 */

@ThreadSafe
class TweakedRequest extends HttpServletRequestWrapper {
	
	/*
	 * TODO it's not really clear, if this class needs explicit synchronization.
	 * Maybe instances of this class are only used by single threads, like
	 * normal HttpServletRequests?
	 */
	
	private boolean initalised = false;
	
	private String hostOverride = null;
	private String pathInfo = null;
	
	/**
	 * 
	 * @param baseReq @NeverNull
	 */
	public TweakedRequest(@NeverNull HttpServletRequest baseReq) {
		super(baseReq);
	}
	
	@Override
	public synchronized String getServerName() {
		if(!this.initalised) {
			initialise();
		}
		return this.hostOverride;
	}
	
	@Override
	public synchronized String getPathInfo() {
		if(!this.initalised) {
			initialise();
		}
		return this.pathInfo;
	}
	
	private void initialise() {
		this.initalised = true;
		String serverName = super.getServerName();
		assert isLocalhost(serverName);
		
		// look for override param
		this.hostOverride = super.getParameter(Restless.X_HOST_Override);
		
		// process path
		String superPathInfo = super.getPathInfo();
		if(superPathInfo != null) {
			StringBuffer adaptedPathInfo = new StringBuffer();
			if(superPathInfo.startsWith("/")) {
				adaptedPathInfo.append("/");
			}
			String[] parts = superPathInfo.split("/");
			boolean hadSignificantPart = false;
			for(int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if(part.startsWith(Restless.X_HOST_Override + "=")) {
					this.hostOverride = part.substring(Restless.X_HOST_Override.length() + 1);
					// do not append anything
				} else if(part.length() > 0) {
					if(i > 0 && hadSignificantPart) {
						adaptedPathInfo.append("/");
					}
					adaptedPathInfo.append(part);
					hadSignificantPart = true;
				}
			}
			if(superPathInfo.endsWith("/")) {
				adaptedPathInfo.append("/");
			}
			this.pathInfo = adaptedPathInfo.toString();
		}
		
		if(this.hostOverride == null) {
			this.hostOverride = serverName;
		}
		
	}
	
	/**
	 * @param serverName
	 * @return true if the servername denotes localhost be means of 'localhost',
	 *         '127.0.0.1' or COMPUTERNAME
	 */
	static boolean isLocalhost(@CanBeNull String serverName) {
		if(serverName == null) {
			return false;
		} else {
			return serverName.equals("localhost") || serverName.equals("127.0.0.1")
			        || serverName.equals(HostUtils.getLocalHostname());
		}
	}
	
}

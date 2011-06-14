package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


class TweakedRequest extends HttpServletRequestWrapper {
	
	private boolean initalised = false;
	
	private String hostOverride = null;
	private String pathInfo = null;
	
	public TweakedRequest(HttpServletRequest baseReq) {
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
	
	static boolean isLocalhost(String serverName) {
		return serverName.equals("localhost") || serverName.equals("127.0.0.1");
	}
	
}

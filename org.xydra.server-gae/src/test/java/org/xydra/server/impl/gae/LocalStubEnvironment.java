package org.xydra.server.impl.gae;

import java.util.HashMap;
import java.util.Map;

import com.google.apphosting.api.ApiProxy;


public class LocalStubEnvironment implements ApiProxy.Environment {
	
	public String getAppId() {
		return "cxmserver-test";
	}
	
	public String getVersionId() {
		return "1.0";
	}
	
	public String getEmail() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isLoggedIn() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isAdmin() {
		throw new UnsupportedOperationException();
	}
	
	public String getAuthDomain() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("deprecation")
	@Deprecated
	public String getRequestNamespace() {
		return "";
	}
	
	public Map<String,Object> getAttributes() {
		return new HashMap<String,Object>();
	}
}

package org.xydra.gae;

import java.util.HashMap;
import java.util.Map;

import com.google.apphosting.api.ApiProxy;


public class LocalStubEnvironment implements ApiProxy.Environment {
	
	@Override
    public String getAppId() {
		return "cxmserver-test";
	}
	
	@Override
    public String getVersionId() {
		return "1.0";
	}
	
	@Override
    public String getEmail() {
		throw new UnsupportedOperationException();
	}
	
	@Override
    public boolean isLoggedIn() {
		throw new UnsupportedOperationException();
	}
	
	@Override
    public boolean isAdmin() {
		throw new UnsupportedOperationException();
	}
	
	@Override
    public String getAuthDomain() {
		throw new UnsupportedOperationException();
	}
	
	/*
	 * The method LocalStubEnvironment.getRequestNamespace() overrides a
	 * deprecated method from ApiProxy.Environment - however we need to have it
	 * to implement the Java interface
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.google.apphosting.api.ApiProxy.Environment#getRequestNamespace()
	 */
	@Override
	@Deprecated
	public String getRequestNamespace() {
		return "";
	}
	
	@Override
    public Map<String,Object> getAttributes() {
		return new HashMap<String,Object>();
	}
}

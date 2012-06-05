package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RestlessContextImpl implements IRestlessContext {
	
	private HttpServletRequest req;
	
	private String requestIdentifier;
	
	private HttpServletResponse res;
	
	private Restless restless;
	
	public RestlessContextImpl(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String requestIdentifier) {
		this.restless = restless;
		this.req = req;
		this.res = res;
		this.requestIdentifier = requestIdentifier;
	}
	
	public HttpServletRequest getRequest() {
		return this.req;
	}
	
	@Override
	public String getRequestIdentifier() {
		return this.requestIdentifier;
	}
	
	public HttpServletResponse getResponse() {
		return this.res;
	}
	
	@Override
	public Restless getRestless() {
		return this.restless;
	}
	
}

package org.xydra.client.gwt.service;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


/**
 * Dummy RequestCallback that ignores everything.
 * 
 * @author dscharrer
 * 
 */
class NoCallback implements RequestCallback {
	
	static private final Logger log = LoggerFactory.getLogger(NoCallback.class);
	
	private static NoCallback instance;
	
	private NoCallback() {
		// do nothing
	}
	
	public void onResponseReceived(Request req, Response resp) {
		// ignore
		log.info("no callback: request complete");
	}
	
	public void onError(Request req, Throwable t) {
		// ignore
		log.info("no callback: error: " + t);
	}
	
	public static NoCallback getInstance() {
		if(instance == null) {
			instance = new NoCallback();
		}
		return instance;
	}
	
}

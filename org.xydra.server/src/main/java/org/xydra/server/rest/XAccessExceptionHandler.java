package org.xydra.server.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.store.AccessException;


/**
 * Handles {@link AccessException} by sending appropriate HTTP status code
 **/
public class XAccessExceptionHandler implements RestlessExceptionHandler {
	
	public boolean handleException(Throwable t, HttpServletRequest req, HttpServletResponse res) {
		
		if(t instanceof AccessException) {
			try {
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch(IOException e) {
				throw new RuntimeException("Error while sending response for XAccessException", e);
			}
		}
		
		/* let other handlers see this exception */
		return false;
	}
	
}

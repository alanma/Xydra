package org.xydra.server;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.model.session.XAccessException;
import org.xydra.restless.RestlessExceptionHandler;


public class XAccessExceptionHandler implements RestlessExceptionHandler {
	
	public boolean handleException(Throwable t, HttpServletRequest req, HttpServletResponse res) {
		
		if(t instanceof XAccessException) {
			try {
				res.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch(IOException e) {
				throw new RuntimeException("error while sending response for XAccessException", e);
			}
		}
		
		return false;
	}
	
}

package org.xydra.server.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.xydra.core.AccessException;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.RestlessExceptionHandler;


/**
 * Handles {@link AccessException} by sending appropriate HTTP status code
 **/
public class XAccessExceptionHandler implements RestlessExceptionHandler {
	
	@Override
	public boolean handleException(Throwable t, IRestlessContext context) {
		
		if(t instanceof AccessException) {
			try {
				context.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch(IOException e) {
				throw new RuntimeException("Error while sending response for XAccessException", e);
			}
		}
		
		/* let other handlers see this exception */
		return false;
	}
	
}

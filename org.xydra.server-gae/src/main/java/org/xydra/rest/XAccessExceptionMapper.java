package org.xydra.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.xydra.core.model.session.XAccessException;



/**
 * A class that tells jersey to map {@link XAccessException}s to the
 * UNAUTHORIZED HTTP status code.
 * 
 * @author dscharrer
 * 
 */
@Provider
public class XAccessExceptionMapper implements ExceptionMapper<XAccessException> {
	
	public Response toResponse(XAccessException exception) {
		return Response.status(Status.UNAUTHORIZED).entity(exception.getMessage()).build();
	}
	
}

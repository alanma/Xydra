package org.xydra.restless;

/**
 * Exception that indicates that an exception handler threw an exception.
 * 
 * @author dscharrer
 * 
 */
public class ExceptionHandlerException extends Exception {
	
	private static final long serialVersionUID = -6194544060690045708L;
	
	public ExceptionHandlerException(Throwable cause) {
		super("error while invoking exception handler", cause);
	}
	
}

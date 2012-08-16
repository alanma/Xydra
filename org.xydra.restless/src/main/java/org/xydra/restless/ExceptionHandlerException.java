package org.xydra.restless;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NotThreadSafe;


/**
 * Exception that indicates that an exception handler threw an exception.
 * 
 * @author dscharrer
 * 
 */
@NotThreadSafe
public class ExceptionHandlerException extends Exception {
	
	private static final long serialVersionUID = -6194544060690045708L;
	
	/**
	 * 
	 * @param cause @CanBeNull
	 */
	public ExceptionHandlerException(@CanBeNull Throwable cause) {
		super("error while invoking exception handler", cause);
	}
	
}

package org.xydra.client;

/**
 * A common superclass for exceptions that can occur when accessing an X*Service
 * API
 * 
 * @author dscharrer
 * 
 */
public class ServiceException extends Throwable {
	
	private static final long serialVersionUID = 1143195110206047976L;
	
	public ServiceException(String message) {
		super(message);
	}
	
}

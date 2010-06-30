package org.xydra.client;

/**
 * An exception that indicates that there was a problem with the request to the
 * Xydra Server.
 * 
 * @author dscharrer
 * 
 */
public class RequestException extends ServiceException {
	
	private static final long serialVersionUID = -1316683211161143105L;
	
	public RequestException(String message) {
		super(message);
	}
	
}

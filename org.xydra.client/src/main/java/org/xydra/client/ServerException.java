package org.xydra.client;

/**
 * An exception that indicates that there was an internal problem in the Xydra
 * Server.
 * 
 * HTTP based implementations might map HTTP/500+ response codes to this
 * exception.
 * 
 * @author dscharrer
 * 
 */
public class ServerException extends ServiceException {
	
	private static final long serialVersionUID = -1316683211161143105L;
	
	public ServerException(String message) {
		super(message);
	}
	
}

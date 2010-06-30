package org.xydra.client;

/**
 * An exception that indicates that there was an internal problem in the Xydra
 * Server.
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

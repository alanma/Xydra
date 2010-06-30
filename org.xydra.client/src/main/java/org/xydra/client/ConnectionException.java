package org.xydra.client;

/**
 * An exception that indicates that there was a problem reaching the Xydra
 * Server.
 * 
 * @author dscharrer
 * 
 */
public class ConnectionException extends ServiceException {
	
	private static final long serialVersionUID = 4396299471886468245L;
	
	public ConnectionException(String message) {
		super("connection error: " + message);
	}
	
}

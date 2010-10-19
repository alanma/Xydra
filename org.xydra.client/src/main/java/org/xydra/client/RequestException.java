package org.xydra.client;

/**
 * An exception that indicates that the Xydra Server did not understand the
 * request it received. Possible causes might be an API incompatibility or wrong
 * server URL.
 * 
 * HTTP-based implementations might map unexplained HTTP/400 Bad Request
 * responses to this exception.
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

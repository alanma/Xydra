package org.xydra.store;

/**
 * An exception that indicates that the Xydra Server did not understand the
 * request it received. Possible causes might be an API incompatibility or wrong
 * server URL.
 * 
 * HTTP-based implementations might map unexplained HTTP/400 Bad Request
 * responses to this exception.
 * 
 * This is similar to an {@link IllegalArgumentException} which could only be
 * detected by a remote implementation. Local implementations should
 * synchronously throw an {@link IllegalArgumentException} instead.
 * 
 * @author dscharrer
 */
public class RequestException extends StoreException {
	
	private static final long serialVersionUID = -1316683211161143105L;
	
	public RequestException(String message) {
		super(message);
	}
	
}

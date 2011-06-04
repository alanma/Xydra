package org.xydra.store;

/**
 * An exception that indicates that there was an internal problem in the Xydra
 * Server.
 * 
 * HTTP based implementations might map HTTP/500+ response codes to this
 * exception.
 * 
 * @author dscharrer
 */
public class InternalStoreException extends StoreException {
	
	private static final long serialVersionUID = -1316683211161143105L;
	
	public InternalStoreException(String message) {
		super(message);
	}
	
	public InternalStoreException(String message, Throwable cause) {
		super(message, cause);
	}
	
}

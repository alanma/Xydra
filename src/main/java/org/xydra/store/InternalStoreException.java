package org.xydra.store;

import org.xydra.core.StoreException;

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
	private int statusCode = 500;
	
	public InternalStoreException(String message) {
		super(message);
	}
	
	public InternalStoreException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message ..
	 * @param cause ..
	 * @param statusCode a suggested HTTP status code
	 */
	public InternalStoreException(String message, Throwable cause, int statusCode) {
		super(message, cause);
		this.statusCode = statusCode;
	}
	
	/**
	 * @return a suggested HTTP status code
	 */
	public int getStatusCode() {
		return this.statusCode;
	}
	
}

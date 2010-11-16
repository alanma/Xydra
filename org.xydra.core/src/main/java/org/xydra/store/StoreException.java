package org.xydra.store;

/**
 * Common super-class for {@link XydraStore} exceptions.
 * 
 * @author voelkel
 */
public class StoreException extends RuntimeException {
	
	private static final long serialVersionUID = -9078969178565352270L;
	
	public StoreException(String message) {
		super(message);
	}
	
	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}
}

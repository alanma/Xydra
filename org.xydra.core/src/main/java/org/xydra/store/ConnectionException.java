package org.xydra.store;

/**
 * An exception that indicates that there was a problem reaching the Xydra
 * Server.
 * 
 * Implementations that connect to remote servers should map DNS, dropped
 * connections, etc. to this exception. Connection timeouts should be mapped to
 * a {@link TimeoutException}.
 * 
 * @author dscharrer
 */
public class ConnectionException extends StoreException {
	
	private static final long serialVersionUID = 4396299471886468245L;
	
	public ConnectionException(String message) {
		super("connection error: " + message);
	}
	
	public ConnectionException(String message, boolean asIs) {
		super(message);
	}
	
	public ConnectionException(String message, Throwable cause) {
		super("connection error: " + message, cause);
	}
	
}

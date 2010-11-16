package org.xydra.store;

/**
 * This exception signals that the supplied authorisation credentials (i.e.
 * actorId and passwordHash) do not match.
 * 
 * @author dscharrer
 */
public class AutorisationException extends StoreException {
	
	private static final long serialVersionUID = -5525416974599450496L;
	
	public AutorisationException(String message) {
		super(message);
	}
	
}

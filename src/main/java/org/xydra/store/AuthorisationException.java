package org.xydra.store;

import org.xydra.core.StoreException;

/**
 * This exception signals that the supplied authorisation credentials (i.e.
 * actorId and passwordHash) do not match.
 * 
 * @author dscharrer
 */
public class AuthorisationException extends StoreException {
	
	private static final long serialVersionUID = -5525416974599450496L;
	
	public AuthorisationException(String message) {
		super(message);
	}
	
}

package org.xydra.store;

import org.xydra.core.StoreException;

/**
 * Too many requests have been issued in a too short period of time. Exact
 * conditions can vary between implementations and may depend on payment or just
 * prevent brute-force password attacks.
 * 
 * @author xamde
 */
public class QuotaException extends StoreException {
	
	private static final long serialVersionUID = -1212667531963701286L;
	
	public QuotaException(String message) {
		super(message);
	}
	
}

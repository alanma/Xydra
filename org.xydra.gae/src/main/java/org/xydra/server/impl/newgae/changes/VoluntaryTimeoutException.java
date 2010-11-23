package org.xydra.server.impl.newgae.changes;

import org.xydra.store.InternalStoreException;


/**
 * An exception thrown by {@link GaeChangesService} while executing commands to
 * abort the change if we get to close to the timeout limit.
 * 
 * @author dscharrer
 * 
 */
public class VoluntaryTimeoutException extends InternalStoreException {
	
	private static final long serialVersionUID = 3509805951788168084L;
	
	public VoluntaryTimeoutException(String message) {
		super(message);
	}
}

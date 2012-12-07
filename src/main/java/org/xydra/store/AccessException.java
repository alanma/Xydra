package org.xydra.store;

import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An exception type which will be thrown if someone tries to access (e.g. read,
 * write or administer) a protected {@link XRepository}, {@link XModel},
 * {@link XObject} or {@link XField} without the allowance to do so.
 * 
 * @author dscharrer
 */
public class AccessException extends StoreException {
	
	private static final long serialVersionUID = -5273702667318966040L;
	
	public AccessException(String message) {
		super(message);
	}
	
}

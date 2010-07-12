package org.xydra.core.model.session;

import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An exception type which will be thrown if someone tries to access a protected
 * {@link XRepository}, {@link XModel}, {@link XObject} or {@link XField}
 * without the allowance to do so.
 * 
 * @author dscharrer
 * 
 */
public class XAccessException extends RuntimeException {
	
	private static final long serialVersionUID = -5273702667318966040L;
	
	public XAccessException() {
		super();
	}
	
	public XAccessException(String message) {
		super(message);
	}
	
}

package org.xydra.core.model.session;

/**
 * An exception thrown on disallowed access to a protected repository, model,
 * object or field.
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

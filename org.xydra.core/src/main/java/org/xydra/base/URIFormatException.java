package org.xydra.base;

/**
 * An exception that is thrown if an URI is used that doesn't conform the URI
 * standards of XModel.
 * 
 * @author Kaidel
 * 
 */

public class URIFormatException extends RuntimeException {
	
	private static final long serialVersionUID = 157634748016802214L;
	
	public URIFormatException(String message) {
		super(message);
	}
}

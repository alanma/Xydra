package org.xydra.client.gwt;

public class ConnectionException extends ServiceException {
	
	private static final long serialVersionUID = 4396299471886468245L;
	
	public ConnectionException(String message) {
		super("connection error: " + message);
	}
	
}

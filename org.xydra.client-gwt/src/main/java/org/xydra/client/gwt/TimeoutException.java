package org.xydra.client.gwt;

public class TimeoutException extends ConnectionException {
	
	private static final long serialVersionUID = -3928886997375812959L;
	
	private final int timeout;
	
	public TimeoutException(int timeout) {
		super("timeout after " + timeout + "ms");
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return this.timeout;
	}
	
}

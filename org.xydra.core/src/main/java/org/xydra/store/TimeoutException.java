package org.xydra.store;

/**
 * An exception that indicates that the connection to the (potentially remote)
 * {@link XydraStore} implementation timed out.
 * 
 * @author dscharrer
 */
public class TimeoutException extends ConnectionException {
	
	private static final long serialVersionUID = -3928886997375812959L;
	
	private final int timeout;
	
	public TimeoutException(int timeout) {
		super("timeout after " + timeout + "ms");
		this.timeout = timeout;
	}
	
	/**
	 * @return the timeout in milliseconds
	 */
	public int getTimeout() {
		return this.timeout;
	}
	
}

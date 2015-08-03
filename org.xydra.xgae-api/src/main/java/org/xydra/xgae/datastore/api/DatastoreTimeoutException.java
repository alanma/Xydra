package org.xydra.xgae.datastore.api;

public class DatastoreTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 5181603692209183255L;

	public DatastoreTimeoutException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public DatastoreTimeoutException(final String msg) {
		super(msg);
	}

}

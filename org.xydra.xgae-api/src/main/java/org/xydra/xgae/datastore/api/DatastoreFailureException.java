package org.xydra.xgae.datastore.api;

public class DatastoreFailureException extends RuntimeException {

	private static final long serialVersionUID = 5181603692209183255L;

	public DatastoreFailureException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public DatastoreFailureException(final String msg) {
		super(msg);
	}

}

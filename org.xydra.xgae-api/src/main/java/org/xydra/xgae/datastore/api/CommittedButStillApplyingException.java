package org.xydra.xgae.datastore.api;

public class CommittedButStillApplyingException extends RuntimeException {

	private static final long serialVersionUID = 5181603692209183255L;

	public CommittedButStillApplyingException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public CommittedButStillApplyingException(final String msg) {
		super(msg);
	}

}

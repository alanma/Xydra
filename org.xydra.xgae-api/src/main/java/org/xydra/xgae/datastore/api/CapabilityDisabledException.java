package org.xydra.xgae.datastore.api;

public class CapabilityDisabledException extends RuntimeException {

	private static final long serialVersionUID = 5181603692209183255L;

	public CapabilityDisabledException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

	public CapabilityDisabledException(final String msg) {
		super(msg);
	}

}

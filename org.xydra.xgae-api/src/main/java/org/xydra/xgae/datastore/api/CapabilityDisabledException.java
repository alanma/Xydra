package org.xydra.xgae.datastore.api;

public class CapabilityDisabledException extends RuntimeException {

	private static final long serialVersionUID = 5181603692209183255L;

	public CapabilityDisabledException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public CapabilityDisabledException(String msg) {
		super(msg);
	}

}

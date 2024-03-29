package org.xydra.core.serialize.json;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 *
 * @author JSON.org
 * @version 2008-09-18
 */
public class JSONException extends Exception {

	private static final long serialVersionUID = 5894276831604379907L;
	private Throwable cause;

	/**
	 * Constructs a JSONException with an explanatory message.
	 *
	 * @param message Detail about the reason for the exception.
	 */
	public JSONException(final String message) {
		super(message);
	}

	public JSONException(final Throwable t) {
		super(t.getMessage());
		this.cause = t;
	}

	@Override
	public synchronized Throwable getCause() {
		return this.cause;
	}
}

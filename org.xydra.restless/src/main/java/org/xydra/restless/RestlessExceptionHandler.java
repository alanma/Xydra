package org.xydra.restless;

public interface RestlessExceptionHandler {

	/**
	 * Handle the given exception.
	 *
	 * @param t
	 * @param context
	 *
	 * @return true, if the exception has been handled, false if it should be
	 *         processed further.
	 */
	boolean handleException(Throwable t, IRestlessContext context);

}

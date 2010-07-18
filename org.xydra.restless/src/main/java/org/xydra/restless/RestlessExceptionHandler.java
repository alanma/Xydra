package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface RestlessExceptionHandler {
	
	/**
	 * Handle the given exception.
	 * 
	 * @return true, if the exception has been handled, false if it should be
	 *         processed further.
	 */
	boolean handleException(Throwable t, HttpServletRequest req, HttpServletResponse res);
	
}

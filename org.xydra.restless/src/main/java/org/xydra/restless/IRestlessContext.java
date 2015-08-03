package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides convenient access to all meta-data available in Restless.
 *
 * To reduce the number of injected parameters, this context also allows access
 * to {@link HttpServletRequest} and {@link HttpServletResponse}, although those
 * can be injected individually, too.
 *
 * In addition, this class provides a unique request identifier
 * {@link #getRequestIdentifier()}, a reference to Restless itself
 * {@link #getRestless()}.
 *
 * @author xamde
 *
 */
public interface IRestlessContext {

	/**
	 * parameter name used to re-use request IDs from query parameters or
	 * cookies.
	 *
	 * @see #getRequestIdentifier()
	 */
	final String PARAM_REQUEST_ID = "restless__rid";

	/**
	 * @return Restless itself
	 */
	Restless getRestless();

	/**
	 * @return the {@link HttpServletRequest}. Instead of using the
	 *         {@link IRestlessContext} in your Java method you can also use
	 *         {@link HttpServletRequest} directly.
	 */
	HttpServletRequest getRequest();

	/**
	 * @return the {@link HttpServletResponse}. Instead of using the
	 *         {@link IRestlessContext} in your Java method you can also use
	 *         {@link HttpServletResponse} directly.
	 */
	HttpServletResponse getResponse();

	/**
	 * A unique tracking string to relate rendered web responses (which can also
	 * be errors) and server logs.
	 *
	 * @return a UUID unique for the given request. Exception: GET-requests
	 *         following a redirect can carry the same identifier as the request
	 *         issuing the redirect. This allows to trace a logical
	 *         request-response even over redirects.
	 */
	String getRequestIdentifier();

}

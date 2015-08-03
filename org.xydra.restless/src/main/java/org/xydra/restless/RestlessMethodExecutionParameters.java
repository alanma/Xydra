package org.xydra.restless;

import java.lang.reflect.Method;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.NotThreadSafe;
import org.xydra.common.NanoClock;

/**
 * A simple wrapper class which holds all parameters calculated by
 * {@link RestlessMethod#prepareMethodExecution(Restless, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, NanoClock)}
 * and which are needed for
 * {@link RestlessMethod#execute(RestlessMethodExecutionParameters, Restless, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * .
 *
 * @author kaidel
 * @author xamde
 */
@NotThreadSafe
class RestlessMethodExecutionParameters {

	private final Method method;
	private final IRestlessContext restlessContext;
	private final List<Object> javaMethodArgs;
	private final boolean hasHttpServletResponseParameter;
	private final String uniqueRequestId;
	private final NanoClock clock;
	private final boolean isMultipartFormDataHandler;
	private final String progressToken;

	/**
	 *
	 * @param method @NeverNull
	 * @param isMultipartFormDataHandler
	 * @param progressToken @CanBeNull to retrieve progress information while
	 *            uploading
	 * @param restlessContext @NeverNull
	 * @param javaMethodArgs @NeverNull
	 * @param hasHttpServletResponseParameter @NeverNull
	 * @param uniqueRequestId @NeverNull
	 * @param clock @NeverNull
	 */
	public RestlessMethodExecutionParameters(@NeverNull final Method method,
			final boolean isMultipartFormDataHandler, @CanBeNull final String progressToken,
			@NeverNull final IRestlessContext restlessContext, @NeverNull final List<Object> javaMethodArgs,
			final boolean hasHttpServletResponseParameter, @NeverNull final String uniqueRequestId,
			@NeverNull final NanoClock clock) {
		this.method = method;
		this.isMultipartFormDataHandler = isMultipartFormDataHandler;
		this.progressToken = progressToken;
		this.restlessContext = restlessContext;
		this.javaMethodArgs = javaMethodArgs;
		this.hasHttpServletResponseParameter = hasHttpServletResponseParameter;
		this.uniqueRequestId = uniqueRequestId;
		this.clock = clock;
	}

	public Method getMethod() {
		return this.method;
	}

	public IRestlessContext getRestlessContext() {
		return this.restlessContext;
	}

	public List<Object> getJavaMethodArgs() {
		return this.javaMethodArgs;
	}

	public boolean hasHttpServletResponseParameter() {
		return this.hasHttpServletResponseParameter;
	}

	public String getUniqueRequestId() {
		return this.uniqueRequestId;
	}

	public NanoClock getClock() {
		return this.clock;
	}

	/**
	 * @return true if the method exposed to Restless is a special handler for a
	 *         multi-part form upload which should be processed in a streaming
	 *         fashion. Non-streaming uploads can simply be handled by accessing
	 *         the named parameters in normal Restless fashion.
	 */
	public boolean isMultipartFormDataHandler() {
		return this.isMultipartFormDataHandler;
	}

	/**
	 * @return a token which is supplied during a multi-part form upload (makes
	 *         only sense if {@link #isMultipartFormDataHandler()} is true).
	 *         This token allows to retrieve status updates
	 *         <em>while the upload runs</em> via another, built-in servlet. See
	 *         {@link ProgressManager}.
	 */
	public String getProgressToken() {
		return this.progressToken;
	}

}

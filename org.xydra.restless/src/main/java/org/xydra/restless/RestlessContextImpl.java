package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;

@ThreadSafe
public class RestlessContextImpl implements IRestlessContext {

	private final HttpServletRequest req;

	private final String requestIdentifier;

	private final HttpServletResponse res;

	private final Restless restless;

	/**
	 *
	 * @param restless
	 * @NeverNull
	 * @param req
	 * @NeverNull
	 * @param res
	 * @NeverNull
	 * @param requestIdentifier
	 * @NeverNull
	 */
	public RestlessContextImpl(@NeverNull final Restless restless, @NeverNull final HttpServletRequest req,
			@NeverNull final HttpServletResponse res, @NeverNull final String requestIdentifier) {
		this.restless = restless;
		this.req = req;
		this.res = res;
		this.requestIdentifier = requestIdentifier;
	}

	@Override
	public HttpServletRequest getRequest() {
		return this.req;
	}

	@Override
	public String getRequestIdentifier() {
		return this.requestIdentifier;
	}

	@Override
	public HttpServletResponse getResponse() {
		return this.res;
	}

	@Override
	public Restless getRestless() {
		return this.restless;
	}

}

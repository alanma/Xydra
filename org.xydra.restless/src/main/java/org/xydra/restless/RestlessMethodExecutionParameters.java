package org.xydra.restless;

import java.lang.reflect.Method;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.NotThreadSafe;
import org.xydra.restless.utils.NanoClock;


/**
 * A simple wrapper class which holds all parameters calculated by
 * {@link RestlessMethod#prepareMethodExecution(Restless, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, NanoClock)}
 * and which are needed for
 * {@link RestlessMethod#execute(RestlessMethodExecutionParameters, Restless, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 * .
 * 
 * @author Kaidel
 * 
 */
@NotThreadSafe
class RestlessMethodExecutionParameters {
	private Method method;
	private IRestlessContext restlessContext;
	private List<Object> javaMethodArgs;
	private boolean hasHttpServletResponseParameter;
	private String uniqueRequestId;
	private NanoClock clock;
	
	/**
	 * 
	 * @param method @NeverNull
	 * @param restlessContext @NeverNull
	 * @param javaMethodArgs @NeverNull
	 * @param hasHttpServletResponseParameter @NeverNull
	 * @param uniqueRequestId @NeverNull
	 * @param clock @NeverNull
	 */
	public RestlessMethodExecutionParameters(@NeverNull Method method,
	        @NeverNull IRestlessContext restlessContext, @NeverNull List<Object> javaMethodArgs,
	        @NeverNull boolean hasHttpServletResponseParameter, @NeverNull String uniqueRequestId,
	        @NeverNull NanoClock clock) {
		this.method = method;
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
	
}

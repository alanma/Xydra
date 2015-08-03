package org.xydra.restless;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.common.NanoClock;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.IMultipartFormDataHandler.IProgressReporter;
import org.xydra.restless.utils.QueryStringUtils;
import org.xydra.restless.utils.ServletUtils;

/**
 * A HTTP method (GET, PUT,.... ) and a {@link PathTemplate} mapped to an object and a method name.
 *
 * Restless can inject {@link HttpServletRequest}, {@link HttpServletResponse}, and {@link Restless} (which gives access
 * to servlet context etc).
 *
 * @author xamde
 */

@ThreadSafe
class RestlessMethod {

	private static final String UPLOAD_PARAM = "_upload_";

	/* See http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common -misuse/ for a discussion of the
	 * different parameters.
	 *
	 * Important: The way we're using the ConcurrentHashMap is only safe if nothings changed here. If other methods
	 * which operate on instanceCache are added, the current implementation might not necessarily be thread-safe
	 * anymore.
	 *
	 * TODO test/check performance with different ConcurrentHashMap. Different parameters might improve/degrade the
	 * performance of Restless, so some fine-tuning might be necessary. */
	private static ConcurrentHashMap<Class<?>, Object> instanceCache = new ConcurrentHashMap<Class<?>, Object>(20,
			0.75f, 5);

	private static Logger log = LoggerFactory.getThreadSafeLogger(RestlessMethod.class);

	private final boolean adminOnly;
	/**
	 * GET, PUT, POST, DELETE
	 */
	/* currently, httpMethod (and its contents) is never written after creation and all methods only read this variable
	 * -> no synchronization necessary at the moment */
	private final String httpMethod;

	/* Only thread-safe classes are allowed here! */
	private final Object instanceOrClass;

	/* currently, methodName is never written after creation and all methods only read this variable -> no
	 * synchronization necessary at the moment */
	private final String methodName;

	/** The parameter required by this restless method */
	/* currently, requiredNamedParameter (and its contents) is never written after creation and all methods only read
	 * this variable -> no synchronization necessary at the moment */
	private final RestlessParameter[] requiredNamedParameters;

	/* currently, pathTemplate (and its contents) is never written after creation and all methods only read this
	 * variable -> no synchronization necessary at the moment */
	private final PathTemplate pathTemplate;

	/**
	 * @param object instance to be called when web method is used - or class to be instantiated @NeverNull
	 * @param httpMethod 'GET', 'PUT', 'POST', or' DELETE' @NeverNull
	 * @param methodName instance method to be called. This method may not have several signatures with the same name.
	 *
	 *        The method signature can use {@link HttpServletRequest} and {@link HttpServletResponse} in any position.
	 *        It is set with the corresponding instances from the servlet environment.
	 *
	 *        If no {@link HttpServletResponse} is used, a default content type of text/plain is used and the method
	 *        return type is expected to be of type String. This facility is designer to return status information at
	 *        development time. @NeverNull
	 * @param pathTemplate see {@link PathTemplate} for syntax @NeverNull
	 * @param adminOnly if true, this method can only be executed it the request URL starts with '/admin'.
	 * @param parameter in order of variables in 'method'. See {@link RestlessParameter}. @NeverNull TODO is this
	 *        correct?
	 */
	protected RestlessMethod(@NeverNull final Object object, @NeverNull final String httpMethod, @NeverNull final String methodName,
			@NeverNull final PathTemplate pathTemplate, final boolean adminOnly, @NeverNull final RestlessParameter[] parameter) {
		this.instanceOrClass = object;
		this.httpMethod = httpMethod;

		/* TODO why is the given methodName not checked for well-formedness? What if an instance is created with a
		 * methodName which is not GET, PUT, POST or DELETE? */
		this.methodName = methodName;
		this.pathTemplate = pathTemplate;
		this.adminOnly = adminOnly;
		this.requiredNamedParameters = parameter;
	}

	/**
	 * Checks whether this method fits to the given parameters and returns parameters which are necessary for executing
	 * the method after this check.
	 *
	 * @param restless
	 * @param req
	 * @param res
	 * @param requestClock
	 * @return the necessary parameters for executing this method or null if this method doesn't fit the given
	 *         parameters.
	 * @throws IOException
	 */
	public RestlessMethodExecutionParameters prepareMethodExecution(@NeverNull final Restless restless,
			@NeverNull final HttpServletRequest req, @NeverNull final HttpServletResponse res,
			@NeverNull final NanoClock requestClock) throws IOException {

		requestClock.stopAndStart("servlet->restless.run");

		// set standard headers
		res.setHeader(Restless.X_FRAME_OPTIONS_HEADERNAME, Restless.X_FRAME_OPTIONS_DEFAULT);

		Method method;

		synchronized (this.instanceOrClass) {
			method = RestlessStatic.methodByName(this.instanceOrClass, this.methodName);
		}

		/* TODO is this okay? instanceOrClass might change after the method was returned, which might result in slightly
		 * inconsistent behavior...
		 *
		 * Would only be a problem if methods can be removed or their behavior could be changed... is this possible? */

		if (method == null) {
			/* TODO synchronization necessary here? Can the name change? */
			res.sendError(500, "Malconfigured server. Method '" + this.methodName + "' not found in '"
					+ RestlessStatic.instanceOrClass_className(this.instanceOrClass) + "'");
			return null;
		} else {
			// try to prepare parameters for execution of the method
			final List<Object> javaMethodArgs = new ArrayList<Object>();

			/** build up parameters */
			// extract values from path
			/* PathTemplate is thread-safe, so no synchronization is necessary here */
			final Map<String, String> urlPathParameterMap = RestlessUtils.getUrlParametersAsMap(req, this.pathTemplate);

			// extract Cookie values
			final Map<String, String> cookieMap = ServletUtils.getCookiesAsMap(req);

			final String uniqueRequestId = reuseOrCreateUniqueRequestIdentifier(urlPathParameterMap, cookieMap);

			// define context
			final IRestlessContext restlessContext = new RestlessContextImpl(restless, req, res, uniqueRequestId);

			final Flag hasHttpServletResponseParameter = new Flag(false);
			/* now we need to decide who parses the request body: default servlet methods or a specialist streaming
			 * multi-part handler? */
			final boolean isMultiPartStreamHandlerMethod = isMultiPartFormEncoded_HandlerMethod(method);

			final boolean mayReadRequestBody = req.getMethod().equals("GET") || !isMultiPartStreamHandlerMethod;

			Map<String, Object> multipartMap = null;
			Map<String, List<String>> queryMap = null;
			if (mayReadRequestBody) {
				// extract multi-part-upload
				multipartMap = getMultiPartPostAsMap(req);
			} else {
				// look in query params only
				queryMap = QueryStringUtils.parse(req.getQueryString());
			}

			assert!mayReadRequestBody || multipartMap != null;
			assert mayReadRequestBody || queryMap != null;

			int boundNamedParameterNumber = 0;

			// try to fill each parameter
			for (final Class<?> requiredParamType : method.getParameterTypes()) {
				// fill the built-in predefined types
				final boolean filled = fillBuiltInInjectedParameter(requiredParamType, javaMethodArgs,
						hasHttpServletResponseParameter, restlessContext, requestClock);
				if (!filled) {
					/* Method might require a non-trivial parameter type for a named parameter (usually: String) */
					if (this.requiredNamedParameters.length == 0) {
						/* Java method tries to fill a non-built-in parameter type, i.e. a parameter type for which we
						 * need to get a value from the request. If there is no named parameter from which we can even
						 * try to fill the value, throw a usage error */
						throw new IllegalArgumentException("It looks like you have parameters in your java method '"
								+ methodReference(this.instanceOrClass, method)
								+ "' for which there have no RestlessParameter been defined.");
					}
					if (this.requiredNamedParameters.length <= boundNamedParameterNumber) {
						throw new IllegalArgumentException(
								"Require " + this.requiredNamedParameters.length + " named parameters in method '"
										+ RestlessStatic.toClass(this.instanceOrClass).getCanonicalName() + ":"
										+ method.getName() + "', processed " + boundNamedParameterNumber
										+ " parameters from request so far. Required parameters: "
										+ Arrays.toString(
												this.requiredNamedParameters)
										+ ". I.e. your Java method wants more parameters than defined in your restless() method.");
					}
					final RestlessParameter requiredParam = this.requiredNamedParameters[boundNamedParameterNumber];

					final Object value = getNamedParameter(requiredParam.isArray(), requiredParam.getName(),
							requiredParam.getDefaultValue(), requiredParam.isRequired(),

							mayReadRequestBody, req, urlPathParameterMap, cookieMap, queryMap, multipartMap);

					javaMethodArgs.add(value);
					boundNamedParameterNumber++;
					if (boundNamedParameterNumber > this.requiredNamedParameters.length) {
						log.debug(
								"More non-trivial parameter required by Java method than mapped via RestlessParameters");
						return null;
					}
				}
			}

			final String progressToken = (String) getNamedParameter(false, IMultipartFormDataHandler.PARAM_PROGRESS_TOKEN,
					null, false, mayReadRequestBody, req, urlPathParameterMap, cookieMap, queryMap, multipartMap);

			return new RestlessMethodExecutionParameters(method, isMultiPartStreamHandlerMethod, progressToken,
					restlessContext, javaMethodArgs, hasHttpServletResponseParameter.getValue(), uniqueRequestId,
					requestClock);
		}

	}

	private static Object getNamedParameter(final boolean parameterIsArray, final String parameterName, final Object defaultValue,
			final boolean isRequired, final boolean mayReadRequestBody, final HttpServletRequest req,
			final Map<String, String> urlPathParameterMap, final Map<String, String> cookieMap, final Map<String, List<String>> queryMap,
			final Map<String, Object> multipartMap) {
		Object value = null;

		/* 1) look in urlParameters (path params, not query params) */
		if (!parameterIsArray) {
			value = urlPathParameterMap.get(parameterName);
		}

		/* 2) look in POST params and query params */
		if (notSet(value)) {
			if (mayReadRequestBody) {
				final String[] values = req.getParameterValues(parameterName);
				if (values != null) {
					// handle POST and query param values
					if (parameterIsArray) {
						value = values;
					} else if (values.length > 1) {
						// remove redundant
						final Set<String> uniqueValues = new HashSet<String>();
						for (final String s : values) {
							uniqueValues.add(s);
						}
						if (uniqueValues.size() > 1) {
							final StringBuffer buf = new StringBuffer();
							for (int j = 0; j < values.length; j++) {
								buf.append(values[j]);
								buf.append(", ");
							}

							if (isRequired) {
								throw new IllegalArgumentException("Parameter '" + parameterName
										+ "' required but not explicitly defined. Found multiple values.");
							} else {
								log.warn("Multiple values for parameter '" + parameterName + "' (values="
										+ buf.toString() + ") from queryString and POST params, using default ("
										+ defaultValue + ")");
								value = defaultValue;
							}

						} else {
							value = uniqueValues.iterator().next();
						}

					} else {
						value = values[0];
					}
				}
			} else {
				assert queryMap != null;
				final List<String> values = queryMap.get(parameterName);
				if (values != null) {
					// handle query param values
					if (parameterIsArray) {
						value = values.toArray();
					} else if (values.size() > 1) {
						// remove redundant
						final Set<String> uniqueValues = new HashSet<String>();
						for (final String s : values) {
							uniqueValues.add(s);
						}
						if (uniqueValues.size() > 1) {
							final StringBuffer buf = new StringBuffer();
							for (int j = 0; j < values.size(); j++) {
								buf.append(values.get(j));
								buf.append(", ");
							}

							if (isRequired) {
								throw new IllegalArgumentException("Parameter '" + parameterName
										+ "' required but not explicitly defined. Found multiple values.");
							} else {
								log.warn("Multiple values for parameter '" + parameterName + "' (values="
										+ buf.toString() + ") from queryString and POST params, using default ("
										+ defaultValue + ")");
								value = defaultValue;
							}

						} else {
							value = uniqueValues.iterator().next();
						}

					} else {
						value = values.get(0);
					}
				}
			}
		}

		/* 3) look in cookies */
		if (notSet(value) && !parameterIsArray) {
			value = cookieMap.get(parameterName);
		}

		/* 4) look in multipart-upload */
		if (mayReadRequestBody && notSet(value) && !parameterIsArray) {
			assert multipartMap != null;
			value = multipartMap.get(parameterName);
		}

		/* 5) use default values, if defined */
		if (notSet(value)) {
			if (isRequired) {
				log.debug("Parameter '" + parameterName + "' required but no explicitly defined. Found no value.");
				return null;
			} else {
				value = defaultValue;
			}
		}

		return value;
	}

	/**
	 * @param requiredParamType
	 * @param javaMethodArgs
	 * @param hasHttpServletResponseParameter
	 * @param restlessContext
	 * @param requestClock
	 * @return
	 */
	private static boolean fillBuiltInInjectedParameter(final Class<?> requiredParamType, final List<Object> javaMethodArgs,
			final Flag hasHttpServletResponseParameter, final IRestlessContext restlessContext, final NanoClock requestClock) {
		if (requiredParamType.equals(HttpServletResponse.class)) {
			javaMethodArgs.add(restlessContext.getResponse());
			hasHttpServletResponseParameter.setTrue();
			return true;
		} else if (requiredParamType.equals(HttpServletRequest.class)) {
			javaMethodArgs.add(restlessContext.getRequest());
			return true;
		} else if (requiredParamType.equals(Restless.class)) {
			javaMethodArgs.add(restlessContext.getRestless());
			return true;
		} else if (requiredParamType.equals(IRestlessContext.class)) {
			javaMethodArgs.add(restlessContext);
			hasHttpServletResponseParameter.setTrue();
			return true;
		} else if (requiredParamType.equals(NanoClock.class)) {
			javaMethodArgs.add(requestClock);
			return true;
		}
		return false;
	}

	/**
	 * Please note: Great care must be taken, that instanceOrClass isn't changed between calling
	 * prepareMethodExecution() and actually executing the method with the returned parameters in such a fundamental
	 * way, that the parameters which were returned by prepareMethodExecution() become outdated. This might result in
	 * all kinds of problems.
	 *
	 * This shouldn't be a problem at the moment, since only thread-safe classes are allowed for instanceOrClass and all
	 * things which are read on instanceOrClass (and on which the following method execution depends) during
	 * prepareMethodExecution() only look for the correct method and it's name. These parameters cannot be changed
	 * during runtime, so there shouldn't be a problem.
	 *
	 * But, if it would be for example possible to remove methods from instanceOrClass, no guarantees could be made and
	 * splitting the preparation and execution wouldn't work with the current implementation. In this example-case, we
	 * would need to make sure that the method which is returned in the parameters by prepareMethodExecution() isn't
	 * removed until it was executed.
	 */

	/**
	 * Checks if the given required parameter types of a Java method look like the method is a pure stream handler or an
	 * ordinary Restless parameter handler
	 *
	 * @param method
	 * @return true if method handles 100% of request body in a streaming fashion
	 */
	private static boolean isMultiPartFormEncoded_HandlerMethod(final Method method) {
		return method.getReturnType().equals(IMultipartFormDataHandler.class);
	}

	/**
	 * Executes the method on the mapped instance.
	 *
	 * Precedence of variable extraction: urlPath (before questionmark) > httpParams (query params + POST params ) >
	 * default value
	 *
	 * IMPROVE distinguish query params from POST params to define a clearer precedence
	 *
	 * @param params @NeverNull
	 * @param restless @NeverNull
	 * @param req @NeverNull
	 * @param res @NeverNull
	 *
	 * @return true if method launched successfully, i.e. parameters matched
	 * @throws IOException if result writing fails
	 */
	public boolean execute(@NeverNull final RestlessMethodExecutionParameters params, @NeverNull final Restless restless,
			@NeverNull final HttpServletRequest req, @NeverNull final HttpServletResponse res) throws IOException {

		final Method method = params.getMethod();
		/* Help debugging */
		Thread.currentThread().setName("Restless-exe-" + method.getName());

		final IRestlessContext restlessContext = params.getRestlessContext();
		final List<Object> javaMethodArgs = params.getJavaMethodArgs();
		final boolean hasHttpServletResponseParameter = params.hasHttpServletResponseParameter();
		final String uniqueRequestId = params.getUniqueRequestId();
		final NanoClock requestClock = params.getClock();

		try {
			requestClock.stopAndStart("restless.execute->invoke");
			// onBefore-run-event
			restless.fireRequestStarted(restlessContext);
			// run
			final Object result = invokeMethod(method, this.instanceOrClass, javaMethodArgs);
			requestClock.stopAndStart("invoke " + methodReference(this.instanceOrClass, method));

			// stream
			if (params.isMultipartFormDataHandler()) {
				final String progressToken = params.getProgressToken();
				final IMultipartFormDataHandler multipartFormDataHandler = (IMultipartFormDataHandler) result;
				handleStreaming(restlessContext, multipartFormDataHandler, progressToken);
				requestClock.stopAndStart("stream " + methodReference(this.instanceOrClass, method));
			} else if (!hasHttpServletResponseParameter) {
				// we need to send back something standard ourselves
				res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset=" + Restless.CONTENT_TYPE_CHARSET_UTF8);
				res.setStatus(200);
				final Writer w = res.getWriter();
				w.write("Executed " + methodReference(this.instanceOrClass, method) + "\n");
				if (result != null && result instanceof String) {
					w.write("Result: " + result);
				}
				w.flush();
			}
			// post-run-event
			restless.fireRequestFinished(restlessContext);

			requestClock.stopAndStart("response");

		} catch (final InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RestlessException) {
				final RestlessException re = (RestlessException) cause;
				res.setStatus(re.getStatusCode());
				res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset=" + Restless.CONTENT_TYPE_CHARSET_UTF8);
				final Writer w = res.getWriter();
				w.write(re.getMessage());
			} else {

				final IRestlessContext context = new RestlessContextImpl(restless, req, res, uniqueRequestId);

				boolean handled = false;

				try {
					handled = callLocalExceptionHandler(cause, this.instanceOrClass, context);
				} catch (final InvocationTargetException ite) {
					cause = new ExceptionHandlerException(e.getCause());
				} catch (final Throwable th) {
					cause = new ExceptionHandlerException(th);
				}

				if (!handled) {
					try {
						handled = callGlobalExceptionHandlers(cause, context);
					} catch (final Throwable th) {
						cause = new ExceptionHandlerException(th);
					}
				}

				if (!handled) {
					// TODO hide internal messages better from user
					final StringWriter sw = new StringWriter();
					final PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					final String stacktrace = sw.toString();
					if (!res.isCommitted()) {
						res.sendError(500, e + " -- " + stacktrace);
					}
					log.error("Exception while executing RESTless method. Stacktrace: " + stacktrace, cause);
				}
			}
		} catch (final IllegalArgumentException e) {
			res.sendError(500, e.toString());
			log.error("RESTless method registered with wrong arguments: ", e);
		} catch (final IllegalAccessException e) {
			res.sendError(500, e.toString());
			log.error("", e);
		}

		return true;
	}

	/**
	 * @param req
	 * @param multipartFormDataHandler
	 * @param progressToken @CanBeNull
	 * @param res
	 * @throws IOException
	 * @throws FileUploadException
	 */
	private static void handleStreaming(final IRestlessContext ctx, final IMultipartFormDataHandler multipartFormDataHandler,
			final String progressToken) throws IOException {
		// Check that we have a file upload request
		final boolean isMultipart = ServletFileUpload.isMultipartContent(ctx.getRequest());
		assert isMultipart;

		@CanBeNull IProgressReporter progressReporter = null;
		if (progressToken != null) {
			progressReporter = new IProgressReporter() {

				@Override
				public void reportProgress(final String progressMessage) {
					ProgressManager.DEFAULT_PROGRESS_BROKER.appendProgress(progressToken, progressMessage);
				}
			};
		}

		// Create a new file upload handler = really the parser
		final ServletFileUpload upload = new ServletFileUpload();

		// Parse the request
		try {
			final FileItemIterator itemStreamIt = upload.getItemIterator(ctx.getRequest());
			while (itemStreamIt.hasNext()) {
				final FileItemStream item = itemStreamIt.next();

				final String fieldName = item.getFieldName();
				final String contentType = item.getContentType();
				final String contentName = item.getName();
				final Map<String, String> requestHeaderMapOfOneItem = new HashMap<String, String>();
				final FileItemHeaders headers = item.getHeaders();
				final Iterator<String> headerNameIt = headers.getHeaderNames();
				while (headerNameIt.hasNext()) {
					final String headerName = headerNameIt.next();
					final String headerValue = headers.getHeader(headerName);
					requestHeaderMapOfOneItem.put(headerName, headerValue);
				}
				final InputStream is = item.openStream();

				if (item.isFormField()) {
					final String value = Streams.asString(is, "UTF-8");
					multipartFormDataHandler.onContentPartString(fieldName, contentName, requestHeaderMapOfOneItem,
							contentType, value, progressReporter);
				} else {
					multipartFormDataHandler.onContentPartStream(fieldName, contentName, requestHeaderMapOfOneItem,
							contentType, is, progressReporter);
				}

				is.close();
			}
			multipartFormDataHandler.onEndOfRequest(ctx, progressReporter);
		} catch (final FileUploadException e) {
			// IMPROVE good idea?
			if (progressReporter != null) {
				progressReporter.reportProgress("ERROR");
			}
			throw new IOException("while uploading", e);
		}

	}

	/**
	 * @param req
	 * @return @NeverNull an empty map if not a multi-part content
	 */
	private static Map<String, Object> getMultiPartPostAsMap(final HttpServletRequest req) {
		final Map<String, Object> map = new HashMap<String, Object>();
		if (!ServletFileUpload.isMultipartContent(req)) {
			return map;
		}
		final ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator it;
		try {
			it = upload.getItemIterator(req);
			while (it.hasNext()) {
				final FileItemStream item = it.next();
				final String fieldName = item.getFieldName();
				final InputStream stream = item.openStream();
				if (item.isFormField()) {
					final String value = Streams.asString(stream);
					map.put(fieldName, value);
				} else {
					// IMPROVE security, performance: add protection against
					// gigantic uploads
					final byte[] bytes = IOUtils.toByteArray(stream);
					if (fieldName.equals(UPLOAD_PARAM)) {
						map.put(UPLOAD_PARAM, bytes);
					} else {
						// do the same, IMPROVE do something smarter
						map.put(fieldName, bytes);
						map.put(UPLOAD_PARAM, bytes);
					}
				}
			}
		} catch (final FileUploadException e) {
			log.warn("", e);
			throw new RestlessException(500, "Could not process file upload", e);
		} catch (final IOException e) {
			log.warn("", e);
			throw new RestlessException(500, "Could not process file upload", e);
		}
		return map;

	}

	public String getHttpMethod() {
		return this.httpMethod;
	}

	/**
	 * Attention: Do not write on the returned object, only read-access is allowed. No guarantees to the behavior of the
	 * application can be made if you write on the returned object (for example, this might result in synchronization
	 * problems).
	 */
	protected PathTemplate getPathTemplate() {
		return this.pathTemplate;
	}

	/**
	 * Attention: Do not write on the returned object, only read-access is allowed. No guarantees to the behavior of the
	 * application can be made if you write on the returned object (for example, this might result in synchronization
	 * problems).
	 */
	protected Object getInstanceOrClass() {
		return this.instanceOrClass;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public boolean isAdminOnly() {
		return this.adminOnly;
	}

	/**
	 * Attention: Do not write on the returned object, only read-access is allowed. No guarantees to the behavior of the
	 * application can be made if you write on the returned object (for example, this might result in synchronization
	 * problems).
	 */
	protected RestlessParameter[] getRequiredNamedParameter() {
		return this.requiredNamedParameters;
	}

	/**
	 *
	 * @param value
	 * @CanBeNull
	 * @return true, if the given value is null or equals the empty string
	 */
	private static boolean notSet(@CanBeNull final Object value) {
		return value == null || value.equals("");
	}

	private static String methodReference(@NeverNull final Object instanceOrClass, @NeverNull final Method method) {
		Class<?> clazz;
		if (instanceOrClass instanceof Class<?>) {
			clazz = (Class<?>) instanceOrClass;
		} else {
			clazz = instanceOrClass.getClass();
		}
		return clazz.getSimpleName() + "." + method.getName() + "(..)";
	}

	private static Object invokeMethod(@NeverNull final Method method, @NeverNull final Object instanceOrClass,
			@NeverNull final List<Object> javaMethodArgs)
					throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		/* Instantiate only for non-static methods */
		final boolean isStatic = Modifier.isStatic(method.getModifiers());
		Object result;
		if (isStatic) {
			result = method.invoke(null, javaMethodArgs.toArray(new Object[0]));
		} else {
			final Object instance = toInstance(instanceOrClass);

			/* synchronization on the instance is necessary here, since the instances might be shared through the
			 * instanceCache */
			synchronized (instance) {
				result = method.invoke(instance, javaMethodArgs.toArray(new Object[0]));
			}
		}
		return result;
	}

	/**
	 * Each class can provide its own local exception handler with a method 'onException'. Injectable parameters are:
	 * Throwable and the usual standard restless built-ins.
	 *
	 * @param cause
	 * @param instanceOrClass
	 * @param context
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static boolean callLocalExceptionHandler(@NeverNull final Throwable cause, @NeverNull final Object instanceOrClass,
			@NeverNull final IRestlessContext context)
					throws InvocationTargetException, IllegalArgumentException, IllegalAccessException {

		final Method method = RestlessStatic.methodByName(instanceOrClass, "onException");
		if (method == null) {
			// no local exception handler available
			return false;
		}

		final List<Object> javaMethodArgs = new ArrayList<Object>();

		for (final Class<?> requiredParamType : method.getParameterTypes()) {

			if (requiredParamType.equals(Throwable.class)) {
				javaMethodArgs.add(cause);
			} else if (requiredParamType.equals(HttpServletResponse.class)) {
				javaMethodArgs.add(context.getResponse());
			} else if (requiredParamType.equals(HttpServletRequest.class)) {
				javaMethodArgs.add(context.getRequest());
			} else if (requiredParamType.equals(Restless.class)) {
				javaMethodArgs.add(context.getRestless());
			} else if (requiredParamType.equals(IRestlessContext.class)) {
				javaMethodArgs.add(context);
			}
		}

		final Object result = invokeMethod(method, instanceOrClass, javaMethodArgs);

		if (result instanceof Boolean) {
			return (Boolean) result;
		}

		return true;
	}

	/**
	 * Handle exceptions via handlers registered before at
	 * {@link Restless#addExceptionHandler(RestlessExceptionHandler)}
	 *
	 * @param cause
	 * @NeverNull
	 * @param context
	 * @NeverNull
	 * @return
	 */
	private static boolean callGlobalExceptionHandlers(@NeverNull final Throwable cause,
			@NeverNull final IRestlessContext context) {
		final List<RestlessExceptionHandler> exceptionHandlers = context.getRestless().exceptionHandlers;
		synchronized (exceptionHandlers) {
			for (final RestlessExceptionHandler handler : exceptionHandlers) {
				if (handler.handleException(cause, context)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * If the given parameter is a Class, return an instance of it, otherwise simply return the given parameter itself.
	 *
	 * @param instanceOrClass
	 * @NeverNull
	 * @return an instance
	 */
	private static Object toInstance(@NeverNull final Object instanceOrClass) {
		if (instanceOrClass instanceof Class<?>) {
			// need to created instance
			final Class<?> clazz = (Class<?>) instanceOrClass;

			Object instance;

			// look in cache
			instance = instanceCache.get(clazz);
			if (instance != null) {
				return instance;
			}

			try {
				final Constructor<?> constructor = clazz.getConstructor();
				try {
					instance = constructor.newInstance();
					// cache and return
					final Object previousInstance = instanceCache.putIfAbsent(clazz, instance);

					/* if another thread added an instance of the class between our instanceCache.get(), we should
					 * return this instance, to keep everything consistent. "putIfAbsent" atomically checks whether an
					 * instance was added to the cache and if this is the case, it returns this instance and keeps the
					 * cache unchanged. If still no such instance exists, our newly created instance is added to the
					 * cache and putIfAbsent returns null.
					 *
					 * This might result in an unnecessary object instantiation, but at least keeps everything
					 * consistent and is not as restricting as blocking the whole cache completely every time we need to
					 * access it.
					 *
					 * This looks like dangerous double checked locking, but this should be safe because of the behavior
					 * of ConcurrentHashMap and the way we're using it here. */
					if (previousInstance != null) {
						return previousInstance;
					} else {
						return instance;
					}
				} catch (final IllegalArgumentException e) {
					throw new RestlessException(500, "Server misconfigured - constructor needs to have no parameters",
							e);
				} catch (final InstantiationException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				} catch (final IllegalAccessException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				} catch (final InvocationTargetException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				}
			} catch (final SecurityException e) {
				throw new RestlessException(500, "Server misconfigured", e);
			} catch (final NoSuchMethodException e) {
				throw new RestlessException(500, "Server misconfigured - constructor needs to have no parameters", e);
			}

		} else {
			return instanceOrClass;
		}
	}

	public static String createUniqueRequestIdentifier() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Creates an identifier unique for the current request to assist correlating web server responses with
	 * corresponding log files. To further assist the common case of POST-redirect-GET, where the second get might end
	 * up on a completely different machine, the request identifier can also be set via a cookie or query parameter.
	 *
	 * Note: POST-parameters are ignored.
	 *
	 * @param urlParameter
	 * @NeverNull
	 * @param cookieMap
	 * @NeverNull
	 * @return an existing request id found in URL parameters or cookies.
	 */
	private static String reuseOrCreateUniqueRequestIdentifier(@NeverNull final Map<String, String> urlParameter,
			@NeverNull final Map<String, String> cookieMap) {
		String requestId = urlParameter.get(IRestlessContext.PARAM_REQUEST_ID);
		if (requestId == null) {
			requestId = cookieMap.get(IRestlessContext.PARAM_REQUEST_ID);
		}
		if (requestId == null) {
			requestId = createUniqueRequestIdentifier();
		}
		return requestId;
	}

	@Override
	public String toString() {
		return this.pathTemplate + " ==> " + this.instanceOrClass.getClass() + "." + this.methodName + "(...)";
	}
}

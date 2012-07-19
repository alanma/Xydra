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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.NanoClock;
import org.xydra.restless.utils.ServletUtils;


/**
 * A HTTP method (GET, PUT,.... ) and a {@link PathTemplate} mapped to an object
 * and a method name.
 * 
 * Restless can inject {@link HttpServletRequest}, {@link HttpServletResponse},
 * and {@link Restless} (which gives access to servlet context etc).
 * 
 * @author voelkel
 */
public class RestlessMethod {
	
	private static final String UPLOAD_PARAM = "_upload_";
	
	private static Map<Class<?>,Object> instanceCache = new HashMap<Class<?>,Object>();
	
	private static Logger log = LoggerFactory.getLogger(RestlessMethod.class);
	
	boolean adminOnly;
	/**
	 * GET, PUT, POST, DELETE
	 */
	
	/*
	 * TODO Why are this parameters not declared as private/public or protected?
	 */
	String httpMethod;
	Object instanceOrClass;
	String methodName;
	/** The parameter required by this restless method */
	RestlessParameter[] requiredNamedParameter;
	PathTemplate pathTemplate;
	
	/**
	 * @param object instance to be called when web method is used - or class to
	 *            be instantiated
	 * @param httpMethod 'GET', 'PUT', 'POST', or' DELETE'
	 * @param methodName instance method to be called. This method may not have
	 *            several signatures with the same name.
	 * 
	 *            The method signature can use {@link HttpServletRequest} and
	 *            {@link HttpServletResponse} in any position. It is set with
	 *            the corresponding instances from the servlet environment.
	 * 
	 *            If no {@link HttpServletResponse} is used, a default content
	 *            type of text/plain is used and the method return type is
	 *            expected to be of type String. This facility is designer to
	 *            return status information at development time.
	 * @param pathTemplate see {@link PathTemplate} for syntax
	 * @param adminOnly if true, this method can only be executed it the request
	 *            URL starts with '/admin'.
	 * @param parameter in order of variables in 'method'. See
	 *            {@link RestlessParameter}.
	 */
	public RestlessMethod(Object object, String httpMethod, String methodName,
	        PathTemplate pathTemplate, boolean adminOnly, RestlessParameter[] parameter) {
		this.instanceOrClass = object;
		this.httpMethod = httpMethod;
		this.methodName = methodName;
		this.pathTemplate = pathTemplate;
		this.adminOnly = adminOnly;
		this.requiredNamedParameter = parameter;
	}
	
	/**
	 * Executes the method on the mapped instance.
	 * 
	 * Precedence of variable extraction: urlPath (before questionmark) >
	 * httpParams (query params + POST params ) > default value
	 * 
	 * IMPROVE distinguish query params from POST params to define a clearer
	 * precedence
	 * 
	 * @param restless @NeverNull
	 * @param req @NeverNull
	 * @param res @NeverNull
	 * @param requestClock @NeverNull already be started
	 * 
	 * @return true if method launched successfully, i.e. parameters matched
	 * @throws IOException if result writing fails
	 */
	public boolean run(final Restless restless, final HttpServletRequest req,
	        final HttpServletResponse res, NanoClock requestClock) throws IOException {
		requestClock.stopAndStart("servlet->restless.run");
		
		// set standard headers
		res.setHeader(Restless.X_FRAME_OPTIONS_HEADERNAME, Restless.X_FRAME_OPTIONS_DEFAULT);
		
		Method method = Restless.methodByName(this.instanceOrClass, this.methodName);
		if(method == null) {
			res.sendError(500, "Malconfigured server. Method '" + this.methodName
			        + "' not found in '" + Restless.instanceOrClass_className(this.instanceOrClass)
			        + "'");
		} else {
			// try to call it
			List<Object> javaMethodArgs = new ArrayList<Object>();
			// build up parameters
			
			// extract values from path
			Map<String,String> urlParameter = getUrlParametersAsMap(req, this.pathTemplate);
			
			// extract Cookie values
			Map<String,String> cookieMap = ServletUtils.getCookiesAsMap(req);
			
			// extract multi-part-upload
			Map<String,Object> multipartMap = getMultiPartPostAsMap(req);
			
			int boundNamedParameterNumber = 0;
			boolean hasHttpServletResponseParameter = false;
			final String uniqueRequestId =
			        reuseOrCeateUniqueRequestIdentifier(urlParameter, cookieMap);
			// define context
			IRestlessContext restlessContext =
			        new RestlessContextImpl(restless, req, res, uniqueRequestId);
			
			for(Class<?> requiredParamType : method.getParameterTypes()) {
				
				// try to fill each parameter
				
				// fill predefined types
				if(requiredParamType.equals(HttpServletResponse.class)) {
					javaMethodArgs.add(res);
					hasHttpServletResponseParameter = true;
				} else if(requiredParamType.equals(HttpServletRequest.class)) {
					javaMethodArgs.add(req);
				} else if(requiredParamType.equals(Restless.class)) {
					javaMethodArgs.add(restless);
				} else if(requiredParamType.equals(IRestlessContext.class)) {
					javaMethodArgs.add(restlessContext);
					hasHttpServletResponseParameter = true;
				} else if(requiredParamType.equals(NanoClock.class)) {
					javaMethodArgs.add(requestClock);
				} else {
					/*
					 * Method might require a non-trivial parameter type for a
					 * named parameter (usually: String)
					 */
					synchronized(this.requiredNamedParameter) {
						if(this.requiredNamedParameter.length == 0) {
							/*
							 * Java method tries to fill a non-built-in
							 * parameter type, i.e. a parameter type for which
							 * we need to get a value from the request. If there
							 * is no named parameter from which we can even try
							 * to fill the value, throw a usage error
							 */
							throw new IllegalArgumentException(
							        "It looks like you have parameters in your java method '"
							                + methodReference(this.instanceOrClass, method)
							                + "' for which there have no RestlessParameter been defined.");
						}
						if(this.requiredNamedParameter.length <= boundNamedParameterNumber) {
							throw new IllegalArgumentException(
							        "Require "
							                + this.requiredNamedParameter.length
							                + " named parameters in method '"
							                + Restless.toClass(this.instanceOrClass)
							                        .getCanonicalName()
							                + ":"
							                + method.getName()
							                + "', processed "
							                + boundNamedParameterNumber
							                + " parameters from request so far. Required parameters: "
							                + this.requiredNamedParameter
							                + ". I.e. your Java method wants more parameters than defined in your restless() method.");
						}
						RestlessParameter param =
						        this.requiredNamedParameter[boundNamedParameterNumber];
						Object value = null;
						
						/* 1) look in urlParameters (not query params) */
						if(!param.isArray) {
							value = urlParameter.get(param.name);
						}
						
						/* 2) look in POST params and query params */
						if(notSet(value)) {
							String[] values = req.getParameterValues(param.name);
							if(values != null) {
								// handle POST and query param values
								if(param.isArray) {
									value = values;
								} else if(values.length > 1) {
									// remove redundant
									Set<String> uniqueValues = new HashSet<String>();
									for(String s : values) {
										uniqueValues.add(s);
									}
									if(uniqueValues.size() > 1) {
										StringBuffer buf = new StringBuffer();
										for(int j = 0; j < values.length; j++) {
											buf.append(values[j]);
											buf.append(", ");
										}
										
										if(param.mustBeDefinedExplicitly()) {
											throw new IllegalArgumentException(
											        "Parameter '"
											                + param.name
											                + "' required but not explicitly defined. Found multiple values.");
										} else {
											log.warn("Multiple values for parameter '"
											        + param.name
											        + "' (values="
											        + buf.toString()
											        + ") from queryString and POST params, using default ("
											        + param.defaultValue + ")");
											value = param.defaultValue;
										}
										
									} else {
										value = uniqueValues.iterator().next();
									}
									
								} else {
									value = values[0];
								}
							}
						}
						
						/* 3) look in cookies */
						if(notSet(value) && !param.isArray) {
							value = cookieMap.get(param.name);
						}
						
						/* 4) look in multipart-upload */
						if(notSet(value) && !param.isArray) {
							value = multipartMap.get(param.name);
						}
						
						/* 5) use default */
						if(notSet(value)) {
							if(param.mustBeDefinedExplicitly()) {
								log.debug("Parameter '" + param.name
								        + "' required but no explicitly defined. Found no value.");
								return false;
							} else {
								value = param.defaultValue;
							}
						}
						javaMethodArgs.add(value);
						boundNamedParameterNumber++;
						if(boundNamedParameterNumber > this.requiredNamedParameter.length) {
							log.debug("More non-trivial parameter required by Java method than mapped via RestlessParameters");
							return false;
						}
					}
				}
			}
			
			synchronized(this.instanceOrClass) {
				try {
					requestClock.stopAndStart("restless.run->invoke");
					// onBefore-run-event
					restless.fireRequestStarted(restlessContext);
					// run
					
					Object result = invokeMethod(method, this.instanceOrClass, javaMethodArgs);
					
					requestClock.stopAndStart("invoke "
					        + methodReference(this.instanceOrClass, method));
					
					if(!hasHttpServletResponseParameter) {
						res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset="
						        + Restless.CONTENT_TYPE_CHARSET_UTF8);
						res.setStatus(200);
						Writer w = res.getWriter();
						// we need to send back something standard ourselves
						w.write("Executed " + methodReference(this.instanceOrClass, method) + "\n");
						if(result != null && result instanceof String) {
							w.write("Result: " + result);
						}
						w.flush();
					}
					// post-run-event
					restless.fireRequestFinished(restlessContext);
					
					requestClock.stopAndStart("response");
					
				} catch(InvocationTargetException e) {
					Throwable cause = e.getCause();
					if(cause instanceof RestlessException) {
						RestlessException re = (RestlessException)cause;
						res.setStatus(re.getStatusCode());
						res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset="
						        + Restless.CONTENT_TYPE_CHARSET_UTF8);
						Writer w = res.getWriter();
						w.write(re.getMessage());
					} else {
						
						IRestlessContext context =
						        new RestlessContextImpl(restless, req, res, uniqueRequestId);
						
						boolean handled = false;
						
						try {
							handled =
							        callLocalExceptionHandler(cause, this.instanceOrClass, context);
						} catch(InvocationTargetException ite) {
							cause = new ExceptionHandlerException(e.getCause());
						} catch(Throwable th) {
							cause = new ExceptionHandlerException(th);
						}
						
						if(!handled) {
							try {
								handled = callGlobalExceptionHandlers(cause, context);
							} catch(Throwable th) {
								cause = new ExceptionHandlerException(th);
							}
						}
						
						if(!handled) {
							// TODO hide internal messages better from user
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							e.printStackTrace(pw);
							String stacktrace = sw.toString();
							if(!res.isCommitted()) {
								res.sendError(500, e + " -- " + stacktrace);
							}
							log.error("Exception while executing RESTless method. Stacktrace: "
							        + stacktrace, cause);
						}
					}
				} catch(IllegalArgumentException e) {
					res.sendError(500, e.toString());
					log.error("RESTless method registered with wrong arguments: ", e);
				} catch(IllegalAccessException e) {
					res.sendError(500, e.toString());
					log.error("", e);
				}
				
			}
		}
		return true;
	}
	
	private static Map<String,Object> getMultiPartPostAsMap(HttpServletRequest req) {
		Map<String,Object> map = new HashMap<String,Object>();
		if(!ServletFileUpload.isMultipartContent(req)) {
			return map;
		}
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator it;
		try {
			it = upload.getItemIterator(req);
			while(it.hasNext()) {
				FileItemStream item = it.next();
				String fieldName = item.getFieldName();
				InputStream stream = item.openStream();
				if(item.isFormField()) {
					String value = Streams.asString(stream);
					map.put(fieldName, value);
				} else {
					// IMPROVE security, performance: add protection against
					// gigantic uploads
					byte[] bytes = IOUtils.toByteArray(stream);
					if(fieldName.equals(UPLOAD_PARAM)) {
						map.put(UPLOAD_PARAM, bytes);
					} else {
						// do the same, maybe do something smarter
						map.put(fieldName, bytes);
						map.put(UPLOAD_PARAM, bytes);
					}
				}
			}
		} catch(FileUploadException e) {
			log.warn("", e);
			throw new RestlessException(500, "Could not process file upload", e);
		} catch(IOException e) {
			log.warn("", e);
			throw new RestlessException(500, "Could not process file upload", e);
		}
		return map;
		
	}
	
	private static boolean notSet(Object value) {
		return value == null || value.equals("");
	}
	
	private static String methodReference(Object instanceOrClass, Method method) {
		Class<?> clazz;
		if(instanceOrClass instanceof Class<?>) {
			clazz = (Class<?>)instanceOrClass;
		} else {
			clazz = instanceOrClass.getClass();
		}
		return clazz.getSimpleName() + "." + method.getName() + "(..)";
	}
	
	private static Object invokeMethod(Method method, Object instanceOrClass,
	        List<Object> javaMethodArgs) throws IllegalArgumentException, IllegalAccessException,
	        InvocationTargetException {
		/* Instantiate only for non-static methods */
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		Object result;
		if(isStatic) {
			result = method.invoke(null, javaMethodArgs.toArray(new Object[0]));
		} else {
			Object instance = toInstance(instanceOrClass);
			result = method.invoke(instance, javaMethodArgs.toArray(new Object[0]));
		}
		return result;
	}
	
	/**
	 * Each class can provide its own local exception handler with a method
	 * 'onException'. Injectable parameters are: Throwable and the usual
	 * standard restless built-ins.
	 * 
	 * @param cause
	 * @param instanceOrClass
	 * @param context
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static boolean callLocalExceptionHandler(Throwable cause, Object instanceOrClass,
	        IRestlessContext context) throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException {
		
		Method method = Restless.methodByName(instanceOrClass, "onException");
		if(method == null) {
			// no local exception handler available
			return false;
		}
		
		List<Object> javaMethodArgs = new ArrayList<Object>();
		
		for(Class<?> requiredParamType : method.getParameterTypes()) {
			
			if(requiredParamType.equals(Throwable.class)) {
				javaMethodArgs.add(cause);
			} else if(requiredParamType.equals(HttpServletResponse.class)) {
				javaMethodArgs.add(context.getResponse());
			} else if(requiredParamType.equals(HttpServletRequest.class)) {
				javaMethodArgs.add(context.getRequest());
			} else if(requiredParamType.equals(Restless.class)) {
				javaMethodArgs.add(context.getRestless());
			} else if(requiredParamType.equals(IRestlessContext.class)) {
				javaMethodArgs.add(context);
			}
		}
		
		Object result = invokeMethod(method, instanceOrClass, javaMethodArgs);
		
		if(result instanceof Boolean) {
			return (Boolean)result;
		}
		
		return true;
	}
	
	/**
	 * Handle exceptions via handlers registered before at
	 * {@link Restless#addExceptionHandler(RestlessExceptionHandler)}
	 * 
	 * @param cause
	 * @param context
	 * @return
	 */
	private static boolean callGlobalExceptionHandlers(Throwable cause, IRestlessContext context) {
		List<RestlessExceptionHandler> exceptionHandlers = context.getRestless().exceptionHandlers;
		synchronized(exceptionHandlers) {
			for(RestlessExceptionHandler handler : exceptionHandlers) {
				if(handler.handleException(cause, context)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * @param req @NeverNull
	 * @param pathTemplate @NeverNull
	 * @return a single map of key-value pairs extracted from the path-part of
	 *         the request-URI
	 */
	public static Map<String,String> getUrlParametersAsMap(HttpServletRequest req,
	        PathTemplate pathTemplate) {
		Map<String,String> urlParameter = new HashMap<String,String>();
		String urlPath = req.getPathInfo();
		if(urlPath != null) {
			List<String> variablesFromUrlPath = pathTemplate.extractVariables(urlPath);
			synchronized(variablesFromUrlPath) {
				for(int i = 0; i < pathTemplate.getVariableNames().size(); i++) {
					urlParameter
					        .put(pathTemplate.getVariableNames().get(i), variablesFromUrlPath.get(i));
				}
			}
		}
		return urlParameter;
	}
	
	/**
	 * If the given parameter is a Class, return an instance of it, otherwise
	 * simply return the given parameter itself.
	 * 
	 * @param instanceOrClass
	 * @return an instance
	 */
	private static Object toInstance(Object instanceOrClass) {
		if(instanceOrClass instanceof Class<?>) {
			// need to created instance
			Class<?> clazz = (Class<?>)instanceOrClass;
			
			Object instance;
			
			// look in cache
			instance = instanceCache.get(clazz);
			if(instance != null) {
				return instance;
			}
			
			try {
				Constructor<?> constructor = clazz.getConstructor();
				try {
					instance = constructor.newInstance();
					// cache and return
					instanceCache.put(clazz, instance);
					return instance;
				} catch(IllegalArgumentException e) {
					throw new RestlessException(500,
					        "Server misconfigured - constructor needs to have no parameters", e);
				} catch(InstantiationException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				} catch(IllegalAccessException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				} catch(InvocationTargetException e) {
					throw new RestlessException(500, "Server misconfigured", e);
				}
			} catch(SecurityException e) {
				throw new RestlessException(500, "Server misconfigured", e);
			} catch(NoSuchMethodException e) {
				throw new RestlessException(500,
				        "Server misconfigured - constructor needs to have no parameters", e);
			}
			
		} else {
			return instanceOrClass;
		}
	}
	
	public static String createUniqueRequestIdentifier() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Creates an identifier unique for the current request to assist
	 * correlating web server responses with corresponding log files. To further
	 * assist the common case of POST-redirect-GET, where the second get might
	 * end up on a completely different machine, the request identifier can also
	 * be set via a cookie or query parameter.
	 * 
	 * Note: POST-parameters are ignored.
	 * 
	 * @param urlParameter
	 * @param cookieMap
	 * @return an existing request id found in URL parameters or cookies.
	 */
	private static String reuseOrCeateUniqueRequestIdentifier(Map<String,String> urlParameter,
	        Map<String,String> cookieMap) {
		String requestId = urlParameter.get(IRestlessContext.PARAM_REQUEST_ID);
		if(requestId == null) {
			requestId = cookieMap.get(IRestlessContext.PARAM_REQUEST_ID);
		}
		if(requestId == null) {
			requestId = createUniqueRequestIdentifier();
		}
		return requestId;
	}
}

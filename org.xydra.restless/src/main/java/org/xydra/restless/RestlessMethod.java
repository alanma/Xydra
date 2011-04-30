package org.xydra.restless;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
	
	private static Map<Class<?>,Object> instanceCache = new HashMap<Class<?>,Object>();
	
	private static Logger log = LoggerFactory.getLogger(RestlessMethod.class);
	
	boolean adminOnly;
	/**
	 * GET, PUT, POST, DELETE
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
	 * TODO distinguish query params from POST params
	 * 
	 * @param req never null
	 * @param res never null
	 * @throws IOException if result writing fails
	 */
	public void run(final Restless restless, final HttpServletRequest req,
	        final HttpServletResponse res) throws IOException {
		Object instance = toInstance(this.instanceOrClass);
		
		Method method = Restless.methodByName(instance, this.methodName);
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
			
			int boundNamedParameterNumber = 0;
			boolean hasHttpServletResponseParameter = false;
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
					IRestlessContext restlessContext = new IRestlessContext() {
						
						public Restless getRestless() {
							return restless;
						}
						
						public HttpServletResponse getResponse() {
							return res;
						}
						
						public HttpServletRequest getRequest() {
							return req;
						}
					};
					javaMethodArgs.add(restlessContext);
				} else {
					/* Method requires a parameter type for a named parameter */

					if(this.requiredNamedParameter.length == 0) {
						/*
						 * Java method tries to fill a non-built-in parameter
						 * type, i.e. a parameter type for which we need to get
						 * a value from the request. If there is no named
						 * parameter from which we can even try to fill the
						 * value, throw a usage error
						 */
						throw new IllegalArgumentException(
						        "It looks like you have parameters in your java method '"
						                + instance.getClass().getCanonicalName()
						                + "."
						                + method.getName()
						                + "(..)' for which there have no RestlessParameter been defined.");
					}
					assert this.requiredNamedParameter.length > boundNamedParameterNumber : "Require "
					        + this.requiredNamedParameter.length
					        + " named parameters, processed "
					        + boundNamedParameterNumber + " parameters from request so far.";
					RestlessParameter param = this.requiredNamedParameter[boundNamedParameterNumber];
					
					Object value = null;
					
					/* 1) look in urlParameters (not query params) */
					if(!param.isArray) {
						value = urlParameter.get(param.name);
					}
					
					/* 2) look in POST params and query params */
					if(value == null) {
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
									log.warn("Multiple values for parameter '" + param.name
									        + "' (values=" + buf.toString()
									        + ") from queryString and POST params, using default ("
									        + param.defaultValue + ")");
									value = param.defaultValue;
								} else {
									value = uniqueValues.iterator().next();
								}
								
							} else {
								value = values[0];
							}
						}
					}
					
					/* 3) look in cookies */
					if(value == null && !param.isArray) {
						value = cookieMap.get(param.name);
					}
					
					/* 4) use default */
					if(value == null) {
						value = param.defaultValue;
					}
					javaMethodArgs.add(value);
					boundNamedParameterNumber++;
					if(boundNamedParameterNumber > this.requiredNamedParameter.length) {
						throw new IllegalArgumentException(
						        "More non-trivial parameter required by Java method than mapped via RestlessParameters");
					}
				}
			}
			
			try {
				Object result = method.invoke(instance, javaMethodArgs.toArray(new Object[0]));
				if(!hasHttpServletResponseParameter) {
					res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset="
					        + Restless.CHARSET_UTF8);
					res.setStatus(200);
					Writer w = res.getWriter();
					// we need to send back something standard ourselves
					w.write("Executed " + instance.getClass().getSimpleName() + "."
					        + this.methodName + "\n");
					if(result != null && result instanceof String) {
						w.write("Result: " + result);
					}
					w.flush();
				}
			} catch(InvocationTargetException e) {
				Throwable cause = e.getCause();
				if(cause instanceof RestlessException) {
					RestlessException re = (RestlessException)cause;
					res.setStatus(re.getStatusCode());
					res.setContentType(Restless.MIME_TEXT_PLAIN + "; charset="
					        + Restless.CHARSET_UTF8);
					Writer w = res.getWriter();
					w.write(re.getMessage());
				} else {
					boolean handled = false;
					for(RestlessExceptionHandler handler : restless.exceptionHandlers) {
						if(handler.handleException(cause, req, res)) {
							handled = true;
							break;
						}
					}
					if(!handled) {
						// TODO hide internal messages better from user
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String stacktrace = sw.toString();
						res.sendError(500, e + " -- " + stacktrace);
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
	
	public static Map<String,String> getUrlParametersAsMap(HttpServletRequest req,
	        PathTemplate pathTemplate) {
		Map<String,String> urlParameter = new HashMap<String,String>();
		String urlPath = req.getPathInfo();
		if(urlPath != null) {
			List<String> variablesFromUrlPath = pathTemplate.extractVariables(urlPath);
			for(int i = 0; i < pathTemplate.variableNames.size(); i++) {
				urlParameter.put(pathTemplate.variableNames.get(i), variablesFromUrlPath.get(i));
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
	private Object toInstance(Object instanceOrClass) {
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
	
}

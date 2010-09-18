package org.xydra.restless;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A minimalistic servlet to help using servlets.
 * 
 * Usage:
 * 
 * 1) in web.xml add
 * 
 * <pre>
 * &lt;servlet&gt;
 *  &lt;servlet-name&gt;restless&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;org.xydra.restless.Restless&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *    &lt;param-name&gt;app&lt;/param-name&gt;
 *    &lt;param-value&gt;com.example.Application&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *  &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;restless&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/mypath/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * 2) create a corresponding class com.example.Application
 * 
 * It should look like this:
 * 
 * <pre>
 * public class Application {
 * 	
 * 	static {
 * 		new Foo().restless();
 * 		new Bar().restless();
 * 		// ... add more
 * 	}
 * 	
 * }
 * </pre>
 * 
 * 3) In your domain classes (Foo, Bar) add the mapping:
 * 
 * <pre>
 * public void restless() {
 * 	Restless.addGet(
 * 
 * 	&quot;/mysubpath/&quot;, //or any path you like
 * 	        
 * 	        this, &quot;getBaz&quot;, // the method name to call
 * 	        
 * 	        new RestlessParameter(&quot;source&quot;, // the query param name
 * 	                
 * 	                null // the default value of the param
 * 	        )
 * 
 * 	// or further RestlessParameter
 * 	        
 * 	        );
 * }
 * </pre>
 * 
 * The method "getBaz" needs to have at least one parameter of type
 * {@link HttpServletResponse}. All other parameters are filled with the values
 * from the request, using the mapping you provide.
 * 
 * 4) The configuration can be accessed at the URL "/admin/restless"
 * 
 * 
 * @author voelkel
 * 
 */
public class Restless extends HttpServlet {
	
	/**
	 * Methods registered with the
	 * {@link #addAdminOnlyMethod(String, String, Object, String, RestlessParameter...)}
	 * are executed only if accessed visa a URL starting with this prefix.
	 */
	public static final String ADMIN_ONLY_URL_PREFIX = "/admin";
	public static final String CHARSET_UTF8 = "utf-8";
	
	static List<RestlessExceptionHandler> exceptionHandlers = new LinkedList<RestlessExceptionHandler>();
	
	private static Map<String,String> initParams = new HashMap<String,String>();
	
	private static Logger log = LoggerFactory.getLogger(Restless.class);
	/**
	 * All publicly exposed methods
	 */
	private static List<RestlessMethod> methods = new LinkedList<RestlessMethod>();
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_XHTML = "application/xhtml+xml";
	private static final long serialVersionUID = -1906300614203565189L;
	private static ServletContext servletContext;
	
	public static final String XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
	
	public static final String XHTML_NS = "xmlns=\"http://www.w3.org/1999/xhtml\"";
	
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static void addAdminOnlyMethod(String pathTemplate, String httpMethod, Object object,
	        String methodName, RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		methods.add(new RestlessMethod(object, httpMethod, methodName, pt, true, parameter));
		assert methodByName(object, methodName) != null : "method '" + methodName + "' not found";
	}
	
	/**
	 * Add a method to be called if an HTTP DELETE matches the given
	 * pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addDelete(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "DELETE", object, methodName, parameter);
	}
	
	/**
	 * Register a handler that will receive exceptions thrown by the executed
	 * REST methods.
	 */
	public static void addExceptionHandler(RestlessExceptionHandler handler) {
		exceptionHandlers.add(handler);
	}
	
	/**
	 * If the method must have a parameter of type {@link HttpServletResponse},
	 * Restless hands over the response object and ignores the method response.
	 * 
	 * @param pathPrefix starts always with /
	 * @param methodName
	 * @param restlessParameter
	 * 
	 * @param pathTemplate see {@link PathTemplate} for syntax
	 * @param httpMethod one of 'GET', 'PUT', 'POST', or 'DELETE'
	 * @param object Java instance to be called
	 * @param methodName to be called on the Java instance. This method may not
	 *            have several signatures.
	 * @param parameter in the order in which they are used in the Java method.
	 *            The Java method may additionally use
	 *            {@link HttpServletRequest} and {@link HttpServletResponse} at
	 *            any position in the Java method. {@link HttpServletResponse}
	 *            should be used to send a response.
	 */
	public static void addGeneric(String pathTemplate, String httpMethod, Object object,
	        String methodName, RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		methods.add(new RestlessMethod(object, httpMethod, methodName, pt, false, parameter));
		assert methodByName(object, methodName) != null : "method '" + methodName + "' not found";
	}
	
	public static void addGenericStatic(String pathTemplate, String httpMethod, Class<?> clazz,
	        String methodName, boolean adminOnly, RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		methods.add(new RestlessMethod(clazz, httpMethod, methodName, pt, adminOnly, parameter));
		assert methodByName(clazz, methodName) != null : "method '" + methodName + "' not found";
	}
	
	/**
	 * Add a method to be called if an HTTP GET matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addGet(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "GET", object, methodName, parameter);
	}
	
	/**
	 * Add a method to be called if an HTTP POST matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addPost(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "POST", object, methodName, parameter);
	}
	
	/**
	 * Add a method to be called if an HTTP PUT matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addPut(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "PUT", object, methodName, parameter);
	}
	
	/**
	 * Turn all cookies that the request contains into a map, cookie name as
	 * key, cookie value as map value.
	 * 
	 * @param req
	 * @return never null
	 */
	public static Map<String,String> getCookiesAsMap(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		Map<String,String> cookieMap = new HashMap<String,String>();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				String name = cookie.getName();
				String value = cookie.getValue();
				// ignoring:
				// cookie.getComment()
				// cookie.getDomain()
				// cookie.getMaxAge()
				// cookie.getPath()
				// cookie.getSecure()
				// cookie.getVersion()
				cookieMap.put(name, value);
			}
		}
		return cookieMap;
	}
	
	/**
	 * Map contains key=value from a query string in a URL (the part after the
	 * '?'). Multiple values for the same key are put in order of appearance in
	 * the list. Duplicate values are omitted.
	 * 
	 * The members of the {@link SortedSet} may be null if the query string was
	 * just 'a=&b=foo'.
	 * 
	 * Encoding UTF-8 is used for URLDecoding the key and value strings.
	 * 
	 * Keys and values get URL-decoded.
	 * 
	 * @param queryString
	 * @return
	 */
	public static Map<String,SortedSet<String>> getQueryStringAsMap(String queryString) {
		Map<String,SortedSet<String>> map = new HashMap<String,SortedSet<String>>();
		if(queryString == null) {
			return map;
		}
		
		String[] pairs = queryString.split("&");
		for(String pair : pairs) {
			String[] keyvalue = pair.split("=");
			if(keyvalue.length > 2) {
				// invalid pair, give up on unreliable parsing
				throw new IllegalArgumentException("Malformed query string " + queryString);
			} else {
				String encKey = keyvalue[0];
				String key;
				try {
					key = URLDecoder.decode(encKey, "utf-8");
					SortedSet<String> values = map.get(key);
					if(values == null) {
						values = new TreeSet<String>();
						map.put(key, values);
					}
					if(keyvalue.length == 2) {
						String rawValue = keyvalue[1];
						String value = URLDecoder.decode(rawValue, "utf-8");
						values.add(value);
					} else {
						values.add(null);
					}
				} catch(UnsupportedEncodingException e) {
					throw new RuntimeException("No utf-8 on this system?", e);
				}
			}
		}
		return map;
	}
	
	public static ServletContext getServletContextFromInit() {
		return servletContext;
	}
	
	public static Map<String,String> getWebXmlInitParameter() {
		return initParams;
	}
	
	/**
	 * @param object
	 * @param methodName
	 * @return true if the Java method with the given name has a parameter of
	 *         type {@link HttpServletResponse}
	 */
	@SuppressWarnings("unused")
	private static boolean hasHttpServletResponseParameter(Object object, String methodName) {
		// probe for consistency
		Method method = methodByName(object, methodName);
		if(method == null) {
			return false;
		}
		
		for(Class<?> paramType : method.getParameterTypes()) {
			if(paramType.equals(HttpServletResponse.class)) {
				// ok
				return true;
			}
		}
		return false;
	}
	
	protected static final String instanceOrClass_className(Object instanceOrClass) {
		if(instanceOrClass instanceof Class<?>) {
			return ((Class<?>)instanceOrClass).getCanonicalName();
		} else {
			return instanceOrClass.getClass().getName();
		}
	}
	
	/**
	 * @param object
	 * @param methodName
	 * @return a java.lang.reflect.{@link Method} from a String
	 */
	private static Method methodByName(Class<?> clazz, String methodName) {
		for(Method method : clazz.getMethods()) {
			if(method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * @param object
	 * @param methodName
	 * @return a java.lang.reflect.{@link Method} from a String
	 */
	static Method methodByName(Object object, String methodName) {
		return methodByName(object.getClass(), methodName);
	}
	
	/**
	 * @param raw
	 * @return the input string with XML escaping
	 */
	private static final String xmlEncode(String raw) {
		String safe = raw;
		safe = safe.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quto;");
		return safe;
	}
	
	private String app;
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		if(req.getRequestURI().contains("admin/restless")) {
			doIntrospection(req, res);
		} else {
			restlessService(req, res);
		}
	}
	
	/**
	 * Print the current mapping from URL patterns to Java methods as a web
	 * page.
	 * 
	 * @param req
	 * @param res
	 */
	private void doIntrospection(HttpServletRequest req, HttpServletResponse res) {
		res.setContentType(MIME_XHTML);
		res.setCharacterEncoding(CHARSET_UTF8);
		try {
			res.getWriter().println(XML_DECLARATION);
			res.getWriter().println(XHTML_DOCTYPE);
			res.getWriter().println(
			        "<html " + XHTML_NS
			                + "><head><title>Restless Configuration</title></head><body>");
			res.getWriter().println("<h3>Restless configuration</h3>");
			res.getWriter().println("<p><ol>");
			for(RestlessMethod rm : Restless.methods) {
				res.getWriter().print("<li>");
				res.getWriter().print(
				        "Mapping " + rm.httpMethod + " <a href='"
				                + xmlEncode(rm.pathTemplate.getRegex()) + "'>"
				                + xmlEncode(rm.pathTemplate.getRegex()) + "</a> --&gt; ");
				res.getWriter().print(
				        instanceOrClass_className(rm.instanceOrClass) + "#" + rm.methodName);
				res.getWriter().println(" access:" + (rm.adminOnly ? "ADMIN ONLY" : "PUBLIC"));
				res.getWriter().println("</li>");
			}
			res.getWriter().println("</ol></p>");
			res.getWriter().println("</body></html>");
			res.getWriter().flush();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		res.setStatus(200);
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/**
	 * @return the configured Application class
	 */
	public String getApp() {
		return this.app;
	}
	
	/*
	 * Called from the servlet environment if
	 * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; is in web.xml set for
	 * the Restless servlet.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init(ServletConfig servletConfig) {
		log.info("Restless init...");
		
		/** provide servletContext object for other parts of the application */
		servletContext = servletConfig.getServletContext();
		
		/** copy init parameters for others to use */
		Enumeration<?> enumeration = servletConfig.getInitParameterNames();
		while(enumeration.hasMoreElements()) {
			String key = (String)enumeration.nextElement();
			String value = servletConfig.getInitParameter(key);
			initParams.put(key, value);
		}
		
		initParams.put("context:contextPath", servletConfig.getServletContext().getContextPath());
		initParams.put("context:realPath of '/'", servletConfig.getServletContext()
		        .getRealPath("/"));
		initParams.put("context:servletContextName", servletConfig.getServletContext()
		        .getServletContextName());
		initParams.put("context:serverInfo", servletConfig.getServletContext().getServerInfo());
		
		/** invoke restless('/') on configured application class */
		this.app = servletConfig.getInitParameter("app");
		try {
			Class<?> clazz = Class.forName(this.app);
			try {
				Method restlessMethod = clazz.getMethod("restless", String.class);
				try {
					restlessMethod.invoke(null, "/");
				} catch(IllegalArgumentException e) {
					throw new RuntimeException("Class '" + this.app + ".restless(String)' failed",
					        e);
				} catch(IllegalAccessException e) {
					throw new RuntimeException("Class '" + this.app + ".restless(String)' failed",
					        e);
				} catch(InvocationTargetException e) {
					throw new RuntimeException("Class '" + this.app + ".restless(String)' failed",
					        e);
				}
			} catch(NoSuchMethodException e) {
				log
				        .warn("Class '"
				                + this.app
				                + "' has no restless( String prefix ) method. Relying on static initializer.");
				// trigger it to make sure static blocks are run
				log.info("Configured with " + clazz.getName());
			}
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Class '" + this.app + "' not found");
		}
		log.info("Done Restless init.");
	}
	
	/**
	 * Generic method to map incoming web requests to mapped
	 * {@link RestlessMethod}. Match path and HTTP method.
	 * 
	 * @param req
	 * @param res
	 */
	protected void restlessService(HttpServletRequest req, HttpServletResponse res) {
		// find class mapped to path
		String path = req.getPathInfo();
		if(path == null) {
			path = "";
		}
		boolean foundPath = false;
		boolean foundMethod = false;
		boolean mayAccess = false;
		
		String httpMethod = req.getHeader("X-HTTP-Method-Override");
		if(httpMethod == null) {
			httpMethod = req.getMethod();
		}
		
		// look through all registered methods
		for(RestlessMethod restlessMethod : methods) {
			// if path matches
			if(restlessMethod.pathTemplate.matches(path)) {
				foundPath = true;
				// and HTTP method matches
				if(httpMethod.equalsIgnoreCase(restlessMethod.httpMethod)) {
					foundMethod = true;
					if(restlessMethod.adminOnly) {
						// check security
						String accessUrl = req.getRequestURI();
						if(accessUrl.startsWith(ADMIN_ONLY_URL_PREFIX)) {
							// calling from potentially secured url, run
							mayAccess = true;
							try {
								restlessMethod.run(req, res);
							} catch(IOException e) {
								throw new RuntimeException(e);
							}
						} else {
							// access denied
							mayAccess = false;
							log.warn("Someone tried to access '" + accessUrl + "'");
						}
					} else {
						mayAccess = true;
						// just run
						try {
							restlessMethod.run(req, res);
						} catch(IOException e) {
							throw new RuntimeException(e);
						}
					}
					break;
				}
			}
		}
		
		try {
			
			if(foundMethod) {
				if(!mayAccess) {
					res.sendError(403, "Forbidden. Admin parts must be accessed via /admin.");
				}
			} else {
				res
				        .sendError(
				                404,
				                "No handler matched your "
				                        + req.getMethod()
				                        + "-request path '"
				                        + path
				                        + "'. "
				                        + (foundPath ? "Found at least a path mapping (wrong HTTP method or missing parameters)."
				                                : "Found not even a path mapping."));
			}
			
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}

package org.xydra.restless;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.ILoggerFactorySPI;
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
 *    &lt;param-value&gt;org.xydra.example.ExampleApp&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *    &lt;param-name&gt;loggerFactory&lt;/param-name&gt;
 *    &lt;!-- Example if you run on AppEngine. Param can be left out to use default. --&gt;
 *    &lt;param-value&gt;org.xydra.log.gae.GaeLoggerFactorySPI&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *  &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;restless&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/mypath/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * 2) create a corresponding class org.xydra.example.ExampleApp. See
 * org.xydra.restless.example.ExampleApp in test sources.
 * 
 * 3) The ExampleApp initializes the resources of your application. See
 * org.xydra.restless.example.ExampleResource in test sources for an example.
 * 
 * 4) The configuration can be accessed at the URL "/admin/restless"
 * 
 * @author voelkel
 * 
 */
public class Restless extends HttpServlet {
	
	/**
	 * Methods registered with the
	 * {@link #addMethod(String, String, Object, String, boolean, RestlessParameter...)}
	 * with the boolean set to TRUE are executed only if accessed visa a URL
	 * starting with this prefix.
	 */
	public static final String ADMIN_ONLY_URL_PREFIX = "/admin";
	public static final String CHARSET_UTF8 = "utf-8";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_XHTML = "application/xhtml+xml";
	public static final String XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
	public static final String XHTML_NS = "xmlns=\"http://www.w3.org/1999/xhtml\"";
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	private static Logger log;
	
	private static final long serialVersionUID = -1906300614203565189L;
	
	/** =========== Utilities ================ */
	
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
				// cookie.getSecure() if true, sent only over HTTPS
				// cookie.getVersion() usually = 1
				if(cookieMap.containsKey(name)) {
					log.warn("Found multiple cookies with the name '" + name
					        + "' with values, e.g., '" + cookieMap.get(name) + "' or '" + value
					        + "'");
				}
				cookieMap.put(name, value);
			}
		}
		return cookieMap;
	}
	
	/**
	 * @param queryString
	 * @return a Map that contains key=value from a query string in a URL (the
	 *         part after the '?'). Multiple values for the same key are put in
	 *         order of appearance in the list. Duplicate values are omitted.
	 * 
	 *         The members of the {@link SortedSet} may be null if the query
	 *         string was just 'a=&b=foo'.
	 * 
	 *         Encoding UTF-8 is used for URLDecoding the key and value strings.
	 * 
	 *         Keys and values get URL-decoded.
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
	public static Method methodByName(Object object, String methodName) {
		if(object instanceof Class<?>) {
			return methodByName((Class<?>)object, methodName);
		} else {
			return methodByName(object.getClass(), methodName);
		}
	}
	
	/**
	 * @param clazz Class from which to get the method reference
	 * @param methodName Name of Java method to get
	 * @return a java.lang.reflect.{@link Method} from a Class with a given
	 *         methodName
	 */
	public static Method methodByName(Class<?> clazz, String methodName) {
		for(Method method : clazz.getMethods()) {
			if(method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * @param raw
	 * @return the input string with XML escaping
	 */
	public static final String xmlEncode(String raw) {
		String safe = raw;
		safe = safe.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quto;");
		return safe;
	}
	
	/** =========== Instance code ================ */
	
	private String apps;
	
	List<RestlessExceptionHandler> exceptionHandlers = new LinkedList<RestlessExceptionHandler>();
	
	private Map<String,String> initParams = new HashMap<String,String>();
	
	/**
	 * All publicly exposed methods
	 */
	private List<RestlessMethod> methods = new LinkedList<RestlessMethod>();
	
	private ServletContext servletContext;
	private String loggerFactory;
	
	/**
	 * Register a handler that will receive exceptions thrown by the executed
	 * REST methods.
	 */
	public void addExceptionHandler(RestlessExceptionHandler handler) {
		this.exceptionHandlers.add(handler);
	}
	
	/**
	 * Shortcut for adding addMethod( method = 'GET', admin-only = 'false' )
	 * 
	 * @param pathTemplate
	 * @param instanceOrClass
	 * @param javaMethodName
	 */
	public void addGet(String pathTemplate, Object instanceOrClass, String javaMethodName,
	        RestlessParameter ... parameter) {
		addMethod(pathTemplate, "GET", instanceOrClass, javaMethodName, false, parameter);
	}
	
	/**
	 * If the method has a parameter of type {@link HttpServletResponse},
	 * Restless hands over the response object and ignores the method response,
	 * if any. Otherwise the method return value is converted toString() and
	 * returned as text/plain.
	 * 
	 * @param pathTemplate see {@link PathTemplate} for syntax
	 * @param httpMethod one of 'GET', 'PUT', 'POST', or 'DELETE'
	 * @param instanceOrClass Java instance to be called or Java class to be
	 *            instantiated
	 * @param javaMethodName to be called on the Java instance. This method may
	 *            not have several signatures.
	 * @param parameter in the order in which they are used in the Java method.
	 *            The Java method may additionally use
	 *            {@link HttpServletRequest} and {@link HttpServletResponse} at
	 *            any position in the Java method. {@link HttpServletResponse}
	 *            should be used to send a response.
	 */
	public void addMethod(String pathTemplate, String httpMethod, Object instanceOrClass,
	        String javaMethodName, boolean adminOnly, RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		this.methods.add(new RestlessMethod(instanceOrClass, httpMethod, javaMethodName, pt,
		        adminOnly, parameter));
		assert methodByName(instanceOrClass, javaMethodName) != null : "method '" + javaMethodName
		        + "' not found";
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
		String uri = req.getRequestURI();
		if(uri.startsWith("/admin/restless")) {
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
		String servletPath = getServletPath(req);
		res.setContentType(MIME_XHTML);
		res.setCharacterEncoding(CHARSET_UTF8);
		try {
			res.getWriter().println(XML_DECLARATION);
			res.getWriter().println(XHTML_DOCTYPE);
			res.getWriter().println(
			        "<html " + XHTML_NS + "><head><title>Restless Configuration</title>" +
			        /* styling */
			        "<style type='text/css'> \n" +

			        "body { font-family: Verdana,sans-serif; }" + "\n"

			        + "</style>\n" +

			        "</head><body>");
			res.getWriter().println("<h3>Restless configuration</h3>");
			res.getWriter().println("<p><ol>");
			for(RestlessMethod rm : this.methods) {
				res.getWriter().print("<li>");
				String url = servletPath + xmlEncode(rm.pathTemplate.getRegex());
				res.getWriter().print(
				        (rm.adminOnly ? "ADMIN ONLY" : "PUBLIC") + " resource <b class='resource'>"
				                + url + "</b>: " + rm.httpMethod + " =&gt; ");
				res.getWriter().print(
				        instanceOrClass_className(rm.instanceOrClass) + "#" + rm.methodName);
				
				/* list parameters */
				res.getWriter().print("<form action='" + url + "' method='" + rm.httpMethod + "'>");
				for(RestlessParameter parameter : rm.parameter) {
					res.getWriter().print(
					        parameter.name + " <input type='text' name='" + parameter.name
					                + "' value='" + parameter.defaultValue + "' />");
				}
				res.getWriter().print("<input type='submit' value='Send' /></form>");
				
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
	
	/**
	 * @param req
	 * @return "/foo/" for a request uri of "/foo/bar" with a pathInfo of "bar"
	 */
	public static String getServletPath(HttpServletRequest req) {
		String uri = req.getRequestURI();
		String path = req.getPathInfo();
		String servletPath = uri.substring(0, uri.length() - path.length());
		
		// FIXME
		System.out.println("uri=" + uri + "\npath=" + path + "->" + servletPath);
		
		return servletPath;
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
		return this.apps;
	}
	
	public ServletContext getServletContextFromInit() {
		return this.servletContext;
	}
	
	public Map<String,String> getWebXmlInitParameter() {
		return this.initParams;
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
		try {
			super.init(servletConfig);
		} catch(ServletException e) {
			throw new RuntimeException("Could not initialise super servlet", e);
		}
		
		/**
		 * Configuration option in web.xml to select class for logging back-end,
		 * which must be an implementation of ILoggerFactorySPI.
		 */
		this.loggerFactory = servletConfig.getInitParameter("loggerFactory");
		if(this.loggerFactory != null) {
			// try to instantiate
			try {
				Class<?> loggerFactoryClass = Class.forName(this.loggerFactory);
				try {
					Constructor<?> constructor = loggerFactoryClass.getConstructor();
					try {
						Object instance = constructor.newInstance();
						try {
							ILoggerFactorySPI spi = (ILoggerFactorySPI)instance;
							LoggerFactory.setLoggerFactorySPI(spi);
						} catch(ClassCastException e) {
							throw new RuntimeException(
							        "Given loggerFactory class is not an implementation of ILoggerFactorySPI",
							        e);
						}
					} catch(IllegalArgumentException e) {
						throw new RuntimeException("Could not instantiate loggerFactory class", e);
					} catch(InstantiationException e) {
						throw new RuntimeException("Could not instantiate loggerFactory class", e);
					} catch(IllegalAccessException e) {
						throw new RuntimeException("Could not instantiate loggerFactory class", e);
					} catch(InvocationTargetException e) {
						throw new RuntimeException("Could not instantiate loggerFactory class", e);
					}
				} catch(SecurityException e) {
					throw new RuntimeException("Could not get constructor of loggerFactory class",
					        e);
				} catch(NoSuchMethodException e) {
					throw new RuntimeException(
					        "Found no parameterless constructor in loggerFactory class", e);
				}
				
			} catch(ClassNotFoundException e) {
				throw new RuntimeException("Could not load loggerFactory class", e);
			}
		}
		
		log = LoggerFactory.getLogger(Restless.class);
		log.info("Restless init. Using loggerFactory '" + this.loggerFactory + "'...");
		
		/** provide servletContext object for other parts of the application */
		this.servletContext = servletConfig.getServletContext();
		
		/** copy init parameters for others to use */
		Enumeration<?> enumeration = servletConfig.getInitParameterNames();
		while(enumeration.hasMoreElements()) {
			String key = (String)enumeration.nextElement();
			String value = servletConfig.getInitParameter(key);
			this.initParams.put(key, value);
		}
		
		this.initParams.put("context:contextPath", servletConfig.getServletContext()
		        .getContextPath());
		this.initParams.put("context:realPath of '/'", servletConfig.getServletContext()
		        .getRealPath("/"));
		this.initParams.put("context:servletContextName", servletConfig.getServletContext()
		        .getServletContextName());
		this.initParams
		        .put("context:serverInfo", servletConfig.getServletContext().getServerInfo());
		
		/** invoke restless(this,'/') on configured application class */
		this.apps = servletConfig.getInitParameter("app");
		
		List<String> appClassNames = parseToList(this.apps);
		for(String appClassName : appClassNames) {
			log.info("Loading restless app '" + appClassName + "'...");
			instatiateAndInit(appClassName);
			log.info("... done loading restless app '" + appClassName + "'.");
		}
		
		log.info(">>> Done Restless init at context path '"
		        + this.initParams.get("context:contextPath") + "'. Admin interface at '"
		        + this.initParams.get("context:contextPath") + "/admin/restless'");
		if(log.isDebugEnabled()) {
			for(RestlessMethod rm : this.methods) {
				log.debug("Mapping " + rm.httpMethod + " " + rm.pathTemplate.getRegex() + " --> "
				        + instanceOrClass_className(rm.instanceOrClass) + "#" + rm.methodName
				        + " access:" + (rm.adminOnly ? "ADMIN ONLY" : "PUBLIC"));
			}
			
		}
	}
	
	private void instatiateAndInit(String appClassName) {
		try {
			Class<?> clazz = Class.forName(appClassName);
			try {
				Constructor<?> cons = clazz.getConstructor();
				try {
					Object appInstance = cons.newInstance();
					try {
						Method restlessMethod = clazz.getMethod("restless", Restless.class,
						        String.class);
						try {
							restlessMethod.invoke(appInstance, this, "");
						} catch(IllegalArgumentException e) {
							throw new RuntimeException("Class '" + appClassName
							        + ".restless(Restless,String prefix)' failed", e);
						} catch(IllegalAccessException e) {
							throw new RuntimeException("Class '" + appClassName
							        + ".restless(Restless,String prefix)' failed", e);
						} catch(InvocationTargetException e) {
							throw new RuntimeException("Class '" + appClassName
							        + ".restless(Restless,String prefix)' failed", e);
						}
					} catch(NoSuchMethodException e) {
						log.warn("Class '"
						        + this.apps
						        + "' has no restless( Restless restless, String prefix ) method. Relying on static initializer.");
						log.debug("Configured with " + clazz.getName());
					}
				} catch(IllegalArgumentException e) {
					throw new RuntimeException("new '" + appClassName + "() failed", e);
				} catch(InstantiationException e) {
					throw new RuntimeException("new '" + appClassName + "() failed", e);
				} catch(IllegalAccessException e) {
					throw new RuntimeException("new '" + appClassName + "() failed", e);
				} catch(InvocationTargetException e) {
					throw new RuntimeException("new '" + appClassName + "() failed", e);
				}
			} catch(SecurityException e) {
				throw new RuntimeException("Class '" + appClassName + " failed to get constructor",
				        e);
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("Class '" + appClassName
				        + " has no parameterless constructor", e);
			}
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Class '" + appClassName + "' not found");
		}
	}
	
	/**
	 * @param commaSeparatedClassnames
	 * @return a list of classnames in order of appearance
	 */
	private List<String> parseToList(String commaSeparatedClassnames) {
		
		List<String> list = new ArrayList<String>();
		if(commaSeparatedClassnames == null) {
			return list;
		}
		
		String[] parts = commaSeparatedClassnames.split(",");
		for(int i = 0; i < parts.length; i++) {
			String classname = parts[i].trim();
			assert !classname.contains(",");
			list.add(classname);
		}
		return list;
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
		for(RestlessMethod restlessMethod : this.methods) {
			// if path matches
			if(restlessMethod.pathTemplate.matches(path)) {
				foundPath = true;
				// and HTTP method matches
				if(httpMethod.equalsIgnoreCase(restlessMethod.httpMethod)) {
					foundMethod = true;
					if(restlessMethod.adminOnly) {
						// check security
						if(requestIsViaAdminUrl(req)) {
							// calling from potentially secured url, run
							mayAccess = true;
							try {
								restlessMethod.run(this, req, res);
							} catch(IOException e) {
								throw new RuntimeException(e);
							}
						} else {
							// access denied
							mayAccess = false;
							log.warn("Someone tried to access '" + path + "'");
						}
					} else {
						mayAccess = true;
						// just run
						try {
							restlessMethod.run(this, req, res);
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
				res.sendError(
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
	
	private boolean requestIsViaAdminUrl(HttpServletRequest req) {
		return req.getRequestURI().startsWith(ADMIN_ONLY_URL_PREFIX);
	}
}

package org.xydra.restless;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.NanoClock;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.XmlUtils;


/**
 * A minimalistic servlet to help using servlets.
 * 
 * Most configuration happens in Java code.
 * 
 * Usage:
 * 
 * <p>
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
 * </p>
 * 
 * <p>
 * 2) create a corresponding class org.xydra.example.ExampleApp. See
 * org.xydra.restless.example.ExampleApp in test sources.
 * </p>
 * 
 * <p>
 * 3) The ExampleApp initialises the resources of your application. More
 * precisely, it calls
 * 
 * <pre>
 * public static void restless(Restless restless, String path) { ... }
 * </pre>
 * 
 * where path is the empty string if called from web.xml and a path with form
 * "/path" (by convention) if called from other Restless Apps.
 * 
 * See org.xydra.restless.example.ExampleApp in test sources for an example.
 * </p>
 * 
 * <p>
 * 4) The configuration can always be accessed at the URL "/admin/restless"
 * </p>
 * 
 * @author voelkel
 * 
 */

@ThreadSafe
public class Restless extends HttpServlet {
	
	private static Logger log;
	
	/**
	 * Gets notified before a request is send to a Java method (
	 * {@link #onRequestStarted(IRestlessContext)}) and after the Java method
	 * finished processing the request (
	 * {@link #onRequestFinished(IRestlessContext)}).
	 * 
	 * Via {@link IRestlessContext#getRequestIdentifier()} a correlation from
	 * start to finish can be achieved.
	 * 
	 * Implementations should have valid {@link #hashCode()} and
	 * {@link #equals(Object)} methods.
	 * 
	 * @author xamde
	 */
	public static interface IRequestListener {
		void onRequestFinished(IRestlessContext restlessContext);
		
		void onRequestStarted(IRestlessContext restlessContext);
	}
	
	/**
	 * Methods registered with the
	 * {@link #addMethod(String, String, Object, String, boolean, RestlessParameter...)}
	 * with the boolean set to TRUE are executed only if accessed visa a URL
	 * starting with this prefix.
	 */
	public static final String ADMIN_ONLY_URL_PREFIX = "/admin";
	
	public static final String CONTENT_TYPE_CHARSET_UTF8 = "utf-8";
	/**
	 * If true, unhandled requests (for which no mapping is found) are delegated
	 * to the 'default' servlet of the container.
	 * 
	 * Default is false.
	 */
	public static boolean DELEGATE_UNHANDLED_TO_DEFAULT = false;
	
	public static final String INIT_PARAM_APP = "app";
	public static final String INIT_PARAM_XYDRA_LOG_BACKEND = "loggerFactory";
	public static final String INIT_PARAM_404URL = "error404";
	public static final String JAVA_ENCODING_UTF8 = "utf-8";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_XHTML = "application/xhtml+xml";
	
	private static final long serialVersionUID = -1906300614203565189L;
	/**
	 * See http://en.wikipedia.org/wiki/X-Frame-Options#Frame-Options
	 * 
	 * Legal values are: 'deny', 'sameorigin'
	 */
	public static String X_FRAME_OPTIONS_DEFAULT = "sameorigin";
	
	public static final String X_FRAME_OPTIONS_HEADERNAME = "X-Frame-Options";
	
	/** Only effective on localhost for security reasons. Helps testing */
	public static final String X_HOST_Override = "X-HTTP-Host-Override";
	
	public static final String X_HTTP_Method_Override = "X-HTTP-Method-Override";
	
	public static final String XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
	
	public static final String XHTML_NS = "xmlns=\"http://www.w3.org/1999/xhtml\"";
	
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	private static final String INTROSPECTION_PATH = "/admin/restless";
	
	/**
	 * @param req HttpServletRequest, @NeverNull
	 * @return "/foo/" for a request uri of "/foo/bar" with a pathInfo of "bar"
	 */
	public static String getServletPath(@NeverNull HttpServletRequest req) {
		String uri = req.getRequestURI();
		String path = req.getPathInfo();
		String servletPath = uri.substring(0, uri.length() - path.length());
		log.debug("uri=" + uri + "\npath=" + path + "->" + servletPath);
		return servletPath;
	}
	
	/** =========== Utilities ================ */
	
	/**
	 * 
	 * @param instanceOrClass @NeverNull
	 * @return
	 */
	protected static final String instanceOrClass_className(@NeverNull Object instanceOrClass) {
		if(instanceOrClass instanceof Class<?>) {
			return ((Class<?>)instanceOrClass).getCanonicalName();
		} else {
			return instanceOrClass.getClass().getName();
		}
	}
	
	/**
	 * @param clazz Class from which to get the method reference @NeverNull
	 * @param methodName Name of Java method to get @NeverNull
	 * @return a java.lang.reflect.{@link Method} from a Class with a given
	 *         methodName
	 */
	public static Method methodByName(@NeverNull Class<?> clazz, @NeverNull String methodName) {
		for(Method method : clazz.getMethods()) {
			if(method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * @param instanceOrClass an instance or class in which to search methodName @NeverNull
	 * @param methodName e.g. 'getName' @NeverNull
	 * @return a java.lang.reflect.{@link Method} from a String
	 */
	public static Method methodByName(@NeverNull Object instanceOrClass,
	        @NeverNull String methodName) {
		return methodByName(toClass(instanceOrClass), methodName);
	}
	
	/**
	 * @param commaSeparatedClassnames @CanBeNull
	 * @return a list of classnames in order of appearance
	 */
	private static List<String> parseToList(@CanBeNull String commaSeparatedClassnames) {
		
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
	
	private static boolean requestIsViaAdminUrl(@NeverNull HttpServletRequest req) {
		return req.getRequestURI().startsWith(ADMIN_ONLY_URL_PREFIX);
	}
	
	/**
	 * 
	 * @param instanceOrClass @NeverNull
	 * @return
	 */
	public static Class<?> toClass(@NeverNull Object instanceOrClass) {
		if(instanceOrClass instanceof Class<?>) {
			return (Class<?>)instanceOrClass;
		} else {
			return instanceOrClass.getClass();
		}
	}
	
	/** =========== Instance code ================ */
	
	private String apps;
	
	/*
	 * TODO access to the lists of listeners also needs to be synchronized on
	 * the listeners themselves, since they might be shared between different
	 * lists.
	 */
	
	final List<RestlessExceptionHandler> exceptionHandlers = new LinkedList<RestlessExceptionHandler>();
	
	/** Filled from web.xml */
	private final Map<String,String> initParams = new HashMap<String,String>();
	
	/**
	 * Simulates the servlet context when run outside a servlet container
	 */
	private HashMap<String,Object> localContext;
	
	private String loggerFactory;
	
	/** All publicly exposed methods */
	private final List<RestlessMethod> methods = new LinkedList<RestlessMethod>();
	
	private final Set<IRequestListener> requestListeners = new HashSet<IRequestListener>();
	
	private ServletContext servletContext;
	
	/**
	 * Register a handler that will receive exceptions thrown by the executed
	 * REST methods.
	 * 
	 * @param handler a non-null {@link RestlessExceptionHandler} @NeverNull
	 */
	public void addExceptionHandler(@NeverNull RestlessExceptionHandler handler) {
		synchronized(this.exceptionHandlers) {
			this.exceptionHandlers.add(handler);
		}
	}
	
	/**
	 * Shortcut for adding addMethod( method = 'GET', admin-only = 'false' )
	 * 
	 * @param pathTemplate see {@link PathTemplate} for syntax @NeverNull
	 * @param instanceOrClass a Java instance or class @NeverNull
	 * @param javaMethodName a method name like 'getName', see
	 *            {@link #addMethod(String, String, Object, String, boolean, RestlessParameter...)}
	 *            for handling of this parameter @NeverNull
	 * @param parameter @NeverNull
	 */
	public void addGet(@NeverNull String pathTemplate, @NeverNull Object instanceOrClass,
	        @NeverNull String javaMethodName, @NeverNull RestlessParameter ... parameter) {
		addMethod(pathTemplate, "GET", instanceOrClass, javaMethodName, false, parameter);
	}
	
	/**
	 * If the method has a parameter of type {@link HttpServletResponse},
	 * Restless hands over the response object and ignores the method response,
	 * if any. Otherwise the method return value is converted toString() and
	 * returned as text/plain.
	 * 
	 * @param pathTemplate see {@link PathTemplate} for syntax @NeverNull
	 * @param httpMethod one of 'GET', 'PUT', 'POST', or 'DELETE' @NeverNull
	 * @param instanceOrClass Java instance to be called or Java class to be
	 *            instantiated. If a class is given, the instance is created on
	 *            first access and cached in memory from there on. @NeverNull
	 * @param javaMethodName to be called on the Java instance. This method may
	 *            not have several signatures. @NeverNull
	 * @param adminOnly
	 * @param parameter in the order in which they are used in the Java method.
	 *            The Java method may additionally use
	 *            {@link HttpServletRequest} and {@link HttpServletResponse} at
	 *            any position in the Java method. {@link HttpServletResponse}
	 *            should be used to send a response. @NeverNull
	 */
	public void addMethod(@NeverNull String pathTemplate, @NeverNull String httpMethod,
	        @NeverNull Object instanceOrClass, @NeverNull String javaMethodName, boolean adminOnly,
	        @NeverNull RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		
		synchronized(this.methods) {
			this.methods.add(new RestlessMethod(instanceOrClass, httpMethod, javaMethodName, pt,
			        adminOnly, parameter));
		}
		assert methodByName(instanceOrClass, javaMethodName) != null : "method '" + javaMethodName
		        + "' not found";
	}
	
	public void addRequestListener(IRequestListener requestListener) {
		synchronized(this.requestListeners) {
			this.requestListeners.add(requestListener);
		}
	}
	
	/**
	 * Based on
	 * http://stackoverflow.com/questions/132052/servlet-for-serving-static
	 * -content
	 * 
	 * @param req @NeverNull
	 * @param res @NeverNull
	 * @throws IOException
	 */
	private void delegateToDefaultServlet(@NeverNull HttpServletRequest req,
	        @NeverNull HttpServletResponse res) throws IOException {
		try {
			RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
			HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
				@Override
				public String getServletPath() {
					return "";
				}
			};
			
			rd.forward(wrapped, res);
		} catch(ServletException e) {
			throw new RuntimeException(e);
		}
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
	/**
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	@Override
	public void doDelete(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
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
	/**
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	@Override
	public void doGet(@NeverNull final HttpServletRequest req,
	        @NeverNull final HttpServletResponse res) {
		final String uri = req.getRequestURI();
		if(uri.startsWith(INTROSPECTION_PATH)) {
			doIntrospection(req, res);
		} else {
			restlessService(req, res);
		}
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
	/**
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	@Override
	public void doHead(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		try {
			super.doHead(req, res);
		} catch(ServletException e) {
			throw new RuntimeException(e);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Print the current mapping from URL patterns to Java methods as a web
	 * page.
	 * 
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	private void doIntrospection(@NeverNull HttpServletRequest req,
	        @NeverNull HttpServletResponse res) {
		String servletPath = getServletPath(req);
		
		ServletUtils.headers(res, MIME_XHTML);
		try {
			Writer w = res.getWriter();
			w.write(XHTML_DOCTYPE);
			
			w.write("<html " + XHTML_NS + ">\n" +
			
			"<head>\n" +
			
			"<title>Restless Configuration</title>\n" +
			
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset="
			        + Restless.CONTENT_TYPE_CHARSET_UTF8 + "\" />\n" +
			        
			        /* styling */
			        "<style type='text/css'> \n" +
			        
			        "body { font-family: Verdana,sans-serif; }" + "\n"
			        
			        + "</style>\n" +
			        
			        "</head><body><div>");
			w.write("<h3>Restless configuration</h3>\n");
			w.write("<ol>");
			
			synchronized(this.methods) {
				for(RestlessMethod rm : this.methods) {
					w.write("<li>");
					String url = servletPath + XmlUtils.xmlEncode(rm.getPathTemplate().getRegex());
					w.write((rm.adminOnly ? "ADMIN ONLY" : "PUBLIC")
					        + " resource <b class='resource'>" + url + "</b>: "
					        + rm.getHttpMethod() + " =&gt; ");
					w.write(instanceOrClass_className(rm.getInstanceOrClass()) + "#"
					        + rm.getMethodName());
					
					/* list parameters */
					w.write("<form action='" + url + "' method='"
					        + rm.getHttpMethod().toLowerCase() + "'><div>");
					for(RestlessParameter parameter : rm.getRequiredNamedParameter()) {
						w.write(parameter.name + " <input type='text' name='" + parameter.name
						        + "' value='" + parameter.defaultValue + "' />");
					}
					w.write("<input type='submit' value='Send' /></div></form>");
					
					w.write("</li>\n");
				}
			}
			w.write("</ol>");
			HtmlUtils.endHtmlPage(w);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
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
	/**
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	@Override
	public void doPost(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
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
	/**
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	@Override
	public void doPut(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/**
	 * 
	 * @param restlessContext @NeverNull
	 */
	protected void fireRequestFinished(@NeverNull IRestlessContext restlessContext) {
		synchronized(this.requestListeners) {
			for(IRequestListener requestListener : this.requestListeners) {
				requestListener.onRequestFinished(restlessContext);
			}
		}
	}
	
	/**
	 * 
	 * @param restlessContext @NeverNull
	 */
	protected void fireRequestStarted(@NeverNull IRestlessContext restlessContext) {
		synchronized(this.requestListeners) {
			for(IRequestListener requestListener : this.requestListeners) {
				requestListener.onRequestStarted(restlessContext);
			}
		}
	}
	
	/**
	 * @return the configured Application classes as a comma-separated string
	 */
	public String getApp() {
		return this.apps;
	}
	
	/**
	 * Helper method to make writing JUnit tests easier.
	 * 
	 * @param key attribute name @CanBeNull
	 * @return when run in a servlet container, this method is simply a
	 *         short-cut for getServletContext().getAttribute(key,value).
	 *         Otherwise a local hash-map is used that can be set via
	 *         {@link #setServletContextAttribute(String, Object)}
	 */
	public Object getServletContextAttribute(@CanBeNull String key) {
		try {
			ServletContext sc = this.getServletContext();
			return sc.getAttribute(key);
		} catch(NullPointerException e) {
			// deal with lazy init
			if(this.localContext == null) {
				return null;
			}
			/*
			 * TODO do we need a lock here? (local context)
			 * 
			 * It's a hashmap, maybe use a thread-safe implementation from
			 * Google Guava
			 */
			return this.localContext.get(key);
		}
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
	/**
	 * @param servletConfig @NeverNull
	 */
	@Override
	public void init(@NeverNull ServletConfig servletConfig) {
		/*
		 * this is only run once, so it doesn't need to be thread-safe!
		 */
		
		/* measure boot performance */
		NanoClock clock = new NanoClock();
		clock.start();
		try {
			super.init(servletConfig);
		} catch(ServletException e) {
			throw new RuntimeException("Could not initialise super servlet", e);
		}
		clock.stopAndStart("super.init");
		
		/**
		 * Configuration option in web.xml to select class for logging back-end,
		 * which must be an implementation of ILoggerFactorySPI.
		 */
		this.loggerFactory = servletConfig.getInitParameter(INIT_PARAM_XYDRA_LOG_BACKEND);
		if(this.loggerFactory != null) {
			initLoggerFactory();
		}
		clock.stopAndStart("logger-init");
		
		/*
		 * TODO get thread-safe logger!
		 */
		log = LoggerFactory.getLogger(Restless.class);
		log.info("Restless: Init. Using loggerFactory '" + this.loggerFactory + "'...");
		
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
		this.apps = this.initParams.get(INIT_PARAM_APP);
		
		List<String> appClassNames = parseToList(this.apps);
		clock.stop("param-parsing");
		for(String appClassName : appClassNames) {
			log.info("Restless: Loading restless app '" + appClassName + "'...");
			try {
				clock.start();
				String stats = instatiateAndInit(appClassName);
				clock.stop("init-app-" + appClassName);
				clock.append(" { ").append(stats).append(" } ");
				log.debug("Restless: ... done loading restless app '" + appClassName + "'.");
			} catch(Exception e) {
				log.error("Failed to init Restless app '" + appClassName + "' ", e);
			}
		}
		
		if(log.isDebugEnabled()) {
			for(RestlessMethod rm : this.methods) {
				log.debug("Mapping " + rm.getHttpMethod() + " " + rm.getPathTemplate().getRegex()
				        + " --> " + instanceOrClass_className(rm.getInstanceOrClass()) + "#"
				        + rm.getMethodName() + " access:"
				        + (rm.adminOnly ? "ADMIN ONLY" : "PUBLIC"));
			}
			
		}
		log.info(">>> Done Restless init at context path '"
		        + this.initParams.get("context:contextPath") + "'. Admin interface at '"
		        + this.initParams.get("context:contextPath") + "/admin/restless'. "
		        
		        + "Init performance " + clock.getStats());
	}
	
	private void initLoggerFactory() {
		if(LoggerFactory.hasLoggerFactorySPI()) {
			return;
		}
		
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
				throw new RuntimeException("Could not get constructor of loggerFactory class", e);
			} catch(NoSuchMethodException e) {
				throw new RuntimeException(
				        "Found no parameterless constructor in loggerFactory class", e);
			}
			
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Could not load loggerFactory class", e);
		}
	}
	
	/**
	 * IMPROVE make this faster
	 * 
	 * @param appClassName fully qualified java class name
	 * @return a String with statistics
	 * @throws RuntimeException for many reflection-related issues
	 */
	private String instatiateAndInit(String appClassName) throws RuntimeException {
		NanoClock clock = new NanoClock();
		clock.start();
		try {
			Class<?> clazz = Class.forName(appClassName);
			try {
				Constructor<?> cons = clazz.getConstructor();
				try {
					Object appInstance = cons.newInstance();
					clock.stop(appClassName + "-newinstance");
					try {
						clock.start();
						Method restlessMethod = clazz.getMethod("restless", Restless.class,
						        String.class);
						clock.stop(appClassName + "-get-restless-method");
						try {
							clock.start();
							restlessMethod.invoke(appInstance, this, "");
							clock.stop(appClassName + "-invoke-restless-method");
						} catch(IllegalArgumentException e) {
							throw new RuntimeException(
							        "Class '"
							                + appClassName
							                + ".restless(Restless,String prefix)' failed with IllegalArgumentException",
							        e);
						} catch(IllegalAccessException e) {
							throw new RuntimeException(
							        "Class '"
							                + appClassName
							                + ".restless(Restless,String prefix)' failed with IllegalAccessException",
							        e);
						} catch(InvocationTargetException e) {
							throw new RuntimeException(
							        "Class '"
							                + appClassName
							                + ".restless(Restless,String prefix)' failed with InvocationTargetException",
							        e);
						}
					} catch(NoSuchMethodException e) {
						log.warn("Class '"
						        + this.apps
						        + "' has no restless( Restless restless, String prefix ) method. Relying on static initializer.");
						log.debug("Configured with " + clazz.getName());
					}
				} catch(IllegalArgumentException e) {
					throw new RuntimeException("new '" + appClassName + "() failed with "
					        + e.getClass() + ":" + e.getMessage(), e);
				} catch(InstantiationException e) {
					throw new RuntimeException("new '" + appClassName + "() failed with "
					        + e.getClass() + ":" + e.getMessage(), e);
				} catch(IllegalAccessException e) {
					throw new RuntimeException(
					        "new '" + appClassName + "() failed with " + e.getClass() + ":"
					                + e.getMessage() + " caused by " + e.getCause() == null ? "--"
					                : e.getCause().getClass() + ":" + e.getCause().getMessage(), e);
				} catch(InvocationTargetException e) {
					throw new RuntimeException("new '" + appClassName + "() failed with "
					        + e.getClass() + ":" + e.getMessage(), e);
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
		return clock.getStats();
	}
	
	/**
	 * @param requestListener @CanBenNull
	 */
	public void removeRequestListener(@CanBeNull IRequestListener requestListener) {
		synchronized(this.requestListeners) {
			this.requestListeners.remove(requestListener);
		}
	}
	
	/**
	 * Generic method to map incoming web requests to mapped
	 * {@link RestlessMethod}. Match path and HTTP method.
	 * 
	 * @param req @NeverNull
	 * @param res @NeverNull
	 */
	protected void restlessService(@NeverNull final HttpServletRequest req,
	        @NeverNull final HttpServletResponse res) {
		/*
		 * TODO recheck that this is actually thread-safe now
		 */
		
		NanoClock requestClock = new NanoClock().start();
		
		/* If running on localhost, we might tweak the host */
		boolean runningOnLocalhost = TweakedRequest.isLocalhost(req.getServerName());
		final HttpServletRequest reqHandedDown = runningOnLocalhost ? new TweakedRequest(req) : req;
		
		// find class mapped to path
		String path = reqHandedDown.getPathInfo();
		if(path == null) {
			path = "/";
		}
		
		boolean foundPath = false;
		boolean foundMethod = false;
		
		/* Determine HTTP method --------------------- */
		// look in HTTP header
		String httpMethod = reqHandedDown.getHeader(X_HTTP_Method_Override);
		// look in query/post param
		if(httpMethod == null) {
			httpMethod = reqHandedDown.getParameter(X_HTTP_Method_Override);
		}
		// use given HTTP method
		if(httpMethod == null) {
			httpMethod = reqHandedDown.getMethod();
		}
		
		/* Find RestlessMethod to be called ------------------ */
		// look through all registered methods
		final boolean reqViaAdminUrl = requestIsViaAdminUrl(reqHandedDown);
		boolean couldStartMethod = false;
		
		synchronized(this.methods) {
			for(RestlessMethod restlessMethod : this.methods) {
				/*
				 * if secure access, ignore all public methods. if insecure
				 * access, ignore all secure methods: Skip all restlessMethods
				 * that may not be accessed
				 */
				if(reqViaAdminUrl == restlessMethod.adminOnly) {
					// if path matches
					if(restlessMethod.getPathTemplate().matches(path)) {
						foundPath = true;
						// and HTTP method matches
						if(httpMethod.equalsIgnoreCase(restlessMethod.getHttpMethod())) {
							foundMethod = true;
							try {
								couldStartMethod = restlessMethod.run(this, reqHandedDown, res,
								        requestClock);
							} catch(IOException e) {
								throw new RuntimeException(e);
							}
							if(couldStartMethod) {
								break;
							}
						}
					}
				}
			}
		}
		
		if(!foundMethod) {
			if(DELEGATE_UNHANDLED_TO_DEFAULT) {
				try {
					delegateToDefaultServlet(reqHandedDown, res);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				// produce better error message
				String msg = "No handler matched your "
				        + reqHandedDown.getMethod()
				        + "-request path '"
				        + path
				        + "'. "
				        + (foundPath ? "Found at least one path mapping (wrong HTTP method or missing parameters)."
				                : "Found not even a path mapping. Check your Restless App and web.xml.");
				log.warn(msg);
				try {
					res.sendError(404, msg);
				} catch(IOException e) {
				}
			}
		}
		
		// make super-sure all buffers are flushed
		try {
			res.flushBuffer();
		} catch(IOException e) {
		}
		
		long requestTime = requestClock.stop("done").getDurationSinceStart();
		log.info("Request time: " + requestTime + " causes: " + requestClock.getStats());
	}
	
	/**
	 * Helper method to make writing JUnit tests easier.
	 * 
	 * When run in a servlet container, this method is simply a short-cut for
	 * getServletContext().setAttribute(key,value). Otherwise a local hash-map
	 * is used.
	 * 
	 * @param key attribute name @NeverNull
	 * @param value attribute value @NeverNull
	 */
	public void setServletContextAttribute(@NeverNull String key, @NeverNull Object value) {
		try {
			ServletContext sc = this.getServletContext();
			sc.setAttribute(key, value);
		} catch(NullPointerException e) {
			// lazy init
			if(this.localContext == null) {
				this.localContext = new HashMap<String,Object>();
			}
			this.localContext.put(key, value);
		}
	}
	
	/*
	 * TODO make sure that the destroy() method is only called when all running
	 * threads are finished
	 */
}

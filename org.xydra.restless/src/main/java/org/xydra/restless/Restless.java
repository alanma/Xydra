package org.xydra.restless;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.spi.ILoggerFactorySPI;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.NanoClock;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.XmlUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

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

	/* Exposed to RestlessInternals */
	static Logger log;

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

	public static final String INIT_PARAM_404RESOURCE = "error404";

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

	/** =========== Instance code ================ */

	private String apps;

	final List<RestlessExceptionHandler> exceptionHandlers = new LinkedList<RestlessExceptionHandler>();

	/** Filled from web.xml */
	private final Map<String, String> initParams = new HashMap<String, String>();

	/**
	 * Simulates the servlet context when run outside a servlet container
	 */
	private HashMap<String, Object> localContext;

	private String loggerFactory;

	/** All publicly exposed methods */
	private final List<RestlessMethod> methods = new LinkedList<RestlessMethod>();

	private final Set<IRequestListener> requestListeners = new HashSet<IRequestListener>();

	private ServletContext servletContext;

	private Object serviceLock = new Object();
	private int serviceCounter = 0;
	private boolean shuttingDown = false;

	private String error404resourceClassname = null;

	public boolean hasCustomError404HandlerDefined() {
		return this.error404resourceClassname != null;
	}

	/**
	 * Register a handler that will receive exceptions thrown by the executed
	 * REST methods.
	 * 
	 * @param handler
	 *            a non-null {@link RestlessExceptionHandler} @NeverNull
	 */
	public void addExceptionHandler(@NeverNull RestlessExceptionHandler handler) {
		synchronized (this.exceptionHandlers) {
			this.exceptionHandlers.add(handler);
		}
	}

	/**
	 * Shortcut for adding addMethod( method = 'GET', admin-only = 'false' )
	 * 
	 * @param pathTemplate
	 *            see {@link PathTemplate} for syntax @NeverNull
	 * @param instanceOrClass
	 *            a Java instance or class @NeverNull
	 * @param javaMethodName
	 *            a method name like 'getName', see
	 *            {@link #addMethod(String, String, Object, String, boolean, RestlessParameter...)}
	 *            for handling of this parameter @NeverNull
	 * @param parameter
	 *            @NeverNull
	 */
	public void addGet(@NeverNull String pathTemplate, @NeverNull Object instanceOrClass,
			@NeverNull String javaMethodName, @NeverNull RestlessParameter... parameter) {
		addMethod(pathTemplate, "GET", instanceOrClass, javaMethodName, false, parameter);
	}

	/**
	 * If the method has a parameter of type {@link HttpServletResponse},
	 * Restless hands over the response object and ignores the method response,
	 * if any. Otherwise the method return value is converted toString() and
	 * returned as text/plain.
	 * 
	 * @param pathTemplate
	 *            see {@link PathTemplate} for syntax @NeverNull
	 * @param httpMethod
	 *            one of 'GET', 'PUT', 'POST', or 'DELETE' @NeverNull
	 * @param instanceOrClass
	 *            Java instance to be called or Java class to be instantiated.
	 *            If a class is given, the instance is created on first access
	 *            and cached in memory from there on. @NeverNull
	 * @param javaMethodName
	 *            to be called on the Java instance. This method may not have
	 *            several signatures. @NeverNull
	 * @param adminOnly
	 * @param parameter
	 *            in the order in which they are used in the Java method. The
	 *            Java method may additionally use {@link HttpServletRequest}
	 *            and {@link HttpServletResponse} at any position in the Java
	 *            method. {@link HttpServletResponse} should be used to send a
	 *            response. @NeverNull
	 */
	public void addMethod(@NeverNull String pathTemplate, @NeverNull String httpMethod,
			@NeverNull Object instanceOrClass, @NeverNull String javaMethodName, boolean adminOnly,
			@NeverNull RestlessParameter... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);

		synchronized (this.methods) {
			RestlessMethod restlessMethod = new RestlessMethod(instanceOrClass, httpMethod,
					javaMethodName, pt, adminOnly, parameter);
			this.methods.add(restlessMethod);
			log.debug("Add method " + restlessMethod);
		}
		assert RestlessStatic.methodByName(instanceOrClass, javaMethodName) != null : "method '"
				+ javaMethodName + "' not found";
	}

	public void addRequestListener(IRequestListener requestListener) {
		synchronized (this.requestListeners) {
			this.requestListeners.add(requestListener);
		}
	}

	/**
	 * Based on
	 * http://stackoverflow.com/questions/132052/servlet-for-serving-static
	 * -content
	 * 
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 * @throws IOException
	 */
	public void delegateToDefaultServlet(@NeverNull HttpServletRequest req,
			@NeverNull HttpServletResponse res) throws IOException {
		try {
			ServletContext sc = this.getServletContext();

			synchronized (sc) {
				RequestDispatcher rd = sc.getNamedDispatcher("default");
				if (rd == null) {
					// for newer Google AppEngine versions
					rd = sc.getNamedDispatcher("_ah_default");
				}
				HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
					@Override
					public String getServletPath() {
						return "";
					}
				};

				rd.forward(wrapped, res);
			}
		} catch (ServletException e) {
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
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	@Override
	public void doDelete(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		if (log.isDebugEnabled()) {
			Thread.currentThread().setName("Restless DELETE " + req.getRequestURI());
		}
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
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	@Override
	public void doGet(@NeverNull final HttpServletRequest req,
			@NeverNull final HttpServletResponse res) {
		final String uri = req.getRequestURI();
		if (log.isDebugEnabled()) {
			Thread.currentThread().setName("Restless GET " + uri);
		}
		if (uri.startsWith(INTROSPECTION_PATH)) {
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
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	@Override
	public void doHead(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		if (log.isDebugEnabled()) {
			Thread.currentThread().setName("Restless HEAD " + req.getRequestURI());
		}
		try {
			super.doHead(req, res);
		} catch (ServletException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Print the current mapping from URL patterns to Java methods as a web
	 * page.
	 * 
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	private void doIntrospection(@NeverNull HttpServletRequest req,
			@NeverNull HttpServletResponse res) {
		String servletPath = RestlessStatic.getServletPath(req);

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

			synchronized (this.methods) {
				for (RestlessMethod rm : this.methods) {
					w.write("<li>");
					String url = servletPath + XmlUtils.xmlEncode(rm.getPathTemplate().getRegex());
					w.write((rm.isAdminOnly() ? "ADMIN ONLY" : "PUBLIC")
							+ " resource <b class='resource'>" + url + "</b>: "
							+ rm.getHttpMethod() + " =&gt; ");
					w.write(RestlessStatic.instanceOrClass_className(rm.getInstanceOrClass()) + "#"
							+ rm.getMethodName());

					/* list parameters */
					w.write("<form action='" + url + "' method='"
							+ rm.getHttpMethod().toLowerCase() + "'><div>");
					for (RestlessParameter parameter : rm.getRequiredNamedParameter()) {
						w.write(parameter.getName() + " <input type='text' name='"
								+ parameter.getName() + "' value='" + parameter.getDefaultValue()
								+ "' />");
					}
					w.write("<input type='submit' value='Send' /></div></form>");

					w.write("</li>\n");
				}
			}
			w.write("</ol>");
			HtmlUtils.endHtmlPage(w);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String toConfigDebug() {
		synchronized (this.methods) {
			StringBuilder b = new StringBuilder();
			for (RestlessMethod rm : this.methods) {
				String url = XmlUtils.xmlEncode(rm.getPathTemplate().getRegex());
				b.append((rm.isAdminOnly() ? "ADMIN ONLY" : "PUBLIC") + " [" + url + "] "
						+ rm.getHttpMethod() + " => ");
				b.append(RestlessStatic.instanceOrClass_className(rm.getInstanceOrClass()) + "#"
						+ rm.getMethodName() + "\n");

				/* list parameters */
				b.append("  ");
				for (RestlessParameter parameter : rm.getRequiredNamedParameter()) {
					b.append(parameter.getName() + "='" + parameter.getDefaultValue() + "' ");
				}
				b.append("\n");
			}
			return b.toString();
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
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	@Override
	public void doPost(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		if (log.isDebugEnabled()) {
			Thread.currentThread().setName("Restless POST " + req.getRequestURI());
		}
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
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	@Override
	public void doPut(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res) {
		if (log.isDebugEnabled()) {
			Thread.currentThread().setName("Restless PUT " + req.getRequestURI());
		}
		restlessService(req, res);
	}

	/**
	 * 
	 * @param restlessContext
	 *            @NeverNull
	 */
	protected void fireRequestFinished(@NeverNull IRestlessContext restlessContext) {
		synchronized (this.requestListeners) {
			for (IRequestListener requestListener : this.requestListeners) {
				requestListener.onRequestFinished(restlessContext);
			}
		}
	}

	/**
	 * 
	 * @param restlessContext
	 *            @NeverNull
	 */
	protected void fireRequestStarted(@NeverNull IRestlessContext restlessContext) {
		synchronized (this.requestListeners) {
			for (IRequestListener requestListener : this.requestListeners) {
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
	 * @param key
	 *            attribute name @CanBeNull
	 * @return when run in a servlet container, this method is simply a
	 *         short-cut for getServletContext().getAttribute(key,value).
	 *         Otherwise a local hash-map is used that can be set via
	 *         {@link #setServletContextAttribute(String, Object)}
	 */
	public Object getServletContextAttribute(@CanBeNull String key) {
		try {
			ServletContext sc = this.getServletContext();
			synchronized (sc) {
				return sc.getAttribute(key);
			}
		} catch (NullPointerException e) {
			// deal with lazy init
			if (this.localContext == null) {
				return null;
			}

			synchronized (this.localContext) {
				return this.localContext.get(key);
			}
		}
	}

	/*
	 * TODO maybe a "setServletContextAttribute if..." method might be a good
	 * idea, because the get and set methods aren't synchronized as one big
	 * method, so checking something before changing might not be atomic.
	 */

	/**
	 * Attention: Please always synchronize access on the returned
	 * ServletContext on itself, to ensure that the access on it is always
	 * correctly synchronized over all objects that share it.
	 * 
	 * Note: This doesn't really return the servlectContext in the state it was
	 * during init(), since there's only one context per servlet (as far as I
	 * know), which might be changed by other methods.
	 * 
	 * @return the context which was also filled with init params from web.xml
	 */
	public ServletContext getServletContextFromInit() {
		return this.servletContext;
	}

	public Map<String, String> getWebXmlInitParameter() {
		return this.initParams;
	}

	/**
	 * Called from the servlet environment if
	 * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; is in web.xml set for
	 * the Restless servlet.
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 * @param servletConfig
	 *            @NeverNull
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
		} catch (ServletException e) {
			throw new RuntimeException("Could not initialise super servlet", e);
		}
		clock.stopAndStart("super.init");

		/**
		 * Configuration option in web.xml to select class for logging back-end,
		 * which must be an implementation of ILoggerFactorySPI.
		 */
		this.loggerFactory = servletConfig.getInitParameter(INIT_PARAM_XYDRA_LOG_BACKEND);
		if (this.loggerFactory != null) {
			initLoggerFactory();
		}
		clock.stopAndStart("logger-init");

		log = LoggerFactory.getThreadSafeLogger(Restless.class);
		log.info("Restless: Init. Logging runs. Loading apps...");
		// ========== CONF Logging done.

		/** provide servletContext object for other parts of the application */
		this.servletContext = servletConfig.getServletContext();

		/** copy init parameters for others to use */
		Enumeration<?> enumeration = servletConfig.getInitParameterNames();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			String value = servletConfig.getInitParameter(key);
			this.initParams.put(key, value);
		}
		/** provide some synthetic properties */
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

		this.error404resourceClassname = this.initParams.get(INIT_PARAM_404RESOURCE);

		List<String> appClassNames = RestlessStatic.parseToList(this.apps);
		clock.stop("param-parsing");
		for (String appClassName : appClassNames) {
			log.info("Restless: Loading restless app '" + appClassName + "'...");
			try {
				clock.start();
				String stats = instatiateAndInit(appClassName);
				clock.stop("init-app-" + appClassName);
				clock.append(" { ").append(stats).append(" } ");
				log.debug("Restless: ... done loading restless app '" + appClassName + "'.");
			} catch (Exception e) {
				log.error("Failed to init Restless app '" + appClassName + "' ", e);
			}
		}

		if (log.isDebugEnabled()) {
			for (RestlessMethod rm : this.methods) {
				log.debug("Mapping " + rm.getHttpMethod() + " " + rm.getPathTemplate().getRegex()
						+ " --> "
						+ RestlessStatic.instanceOrClass_className(rm.getInstanceOrClass()) + "#"
						+ rm.getMethodName() + " access:"
						+ (rm.isAdminOnly() ? "ADMIN ONLY" : "PUBLIC"));
			}

		}
		log.info(">>> Done Restless init at context path '"
				+ this.initParams.get("context:contextPath") + "'. Admin interface at '"
				+ this.initParams.get("context:contextPath") + "/admin/restless'. "

				+ "Init performance " + clock.getStats());
	}

	private void initLoggerFactory() {
		/*
		 * this is only called during init(), so sync isn't necessary here,
		 * since init() is executed before anything else happens on the servlet.
		 */

		if (LoggerFactory.hasLoggerFactorySPI()) {
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
						ILoggerFactorySPI spi = (ILoggerFactorySPI) instance;
						LoggerFactory.setLoggerFactorySPI(spi, "Restless from web.xml/param:"
								+ INIT_PARAM_XYDRA_LOG_BACKEND);
					} catch (ClassCastException e) {
						throw new RuntimeException(
								"Given loggerFactory class is not an implementation of ILoggerFactorySPI",
								e);
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Could not instantiate loggerFactory class", e);
				} catch (InstantiationException e) {
					throw new RuntimeException("Could not instantiate loggerFactory class", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Could not instantiate loggerFactory class", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Could not instantiate loggerFactory class", e);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Could not get constructor of loggerFactory class", e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(
						"Found no parameterless constructor in loggerFactory class", e);
			}

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not load loggerFactory class", e);
		}
	}

	private static final Class<?>[] RESTLESS_METHOD_PARAMETERS = new Class[] { Restless.class,
			String.class };

	/**
	 * IMPROVE make this faster
	 * 
	 * @param appClassName
	 *            fully qualified java class name
	 * @return a String with statistics
	 * @throws RuntimeException
	 *             for many reflection-related issues
	 */
	private String instatiateAndInit(String appClassName) throws RuntimeException {
		NanoClock clock = new NanoClock();
		RestlessStatic.invokeStaticMethod(clock, appClassName, "restless",
				RESTLESS_METHOD_PARAMETERS, new Object[] { this, "" });
		return clock.getStats();
	}

	/**
	 * @param requestListener
	 *            @CanBeNull
	 */
	public void removeRequestListener(@CanBeNull IRequestListener requestListener) {
		synchronized (this.requestListeners) {
			this.requestListeners.remove(requestListener);
		}
	}

	/*
	 * the following code was basically taken from
	 * http://docs.oracle.com/javaee/6/tutorial/doc/bnags.html and ensures that
	 * the destroy() method makes sure that it waits until all threads which are
	 * currently executing a service on this servlet are finished.
	 */
	@Override
	protected void service(@NeverNull HttpServletRequest req, @NeverNull HttpServletResponse res)
			throws ServletException, IOException {
		enteringServiceMethod();
		try {
			super.service(req, res);
		} finally {
			leavingServiceMethod();
		}
	}

	private void enteringServiceMethod() {
		synchronized (this.serviceLock) {
			this.serviceCounter += 1;
		}
	}

	private void leavingServiceMethod() {
		synchronized (this.serviceLock) {
			this.serviceCounter -= 1;
		}
	}

	private int numServices() {
		synchronized (this.serviceLock) {
			return this.serviceCounter;
		}
	}

	private void setShuttingDown() {
		this.shuttingDown = true;
	}

	/**
	 * Tells whether this servlet is in the process of being destroyed.
	 * Potentially long running tasks should query this method in regular
	 * intervals to check whether the servlet is being shut down or not and act
	 * accordingly (i.e. try to finish in a safe way as fast as possible when
	 * it's shutting down).
	 * 
	 * @return true, if the servlet is being shut down/destroyed.
	 */
	public boolean isShuttingDown() {
		return this.shuttingDown;
	}

	@Override
	public void destroy() {
		/*
		 * Check to see whether there are still service methods running, and if
		 * there are, tell them to stop.
		 */
		if (numServices() > 0) {
			setShuttingDown();
		}

		/* Wait for the service methods to stop. */
		try {
			while (numServices() > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		} finally {
			/*
			 * Custom destroy code (if necessary) goes here
			 */

			super.destroy();
		}
	}

	/**
	 * Generic method to map incoming web requests to mapped
	 * {@link RestlessMethod}. Match path and HTTP method.
	 * 
	 * @param req
	 *            @NeverNull
	 * @param res
	 *            @NeverNull
	 */
	protected void restlessService(@NeverNull final HttpServletRequest req,
			@NeverNull final HttpServletResponse res) {

		NanoClock requestClock = new NanoClock().start();

		/* If running on localhost, we might tweak the host */
		boolean runningOnLocalhost = TweakedRequest.isLocalhost(req.getServerName());
		final HttpServletRequest reqHandedDown = runningOnLocalhost ? new TweakedRequest(req) : req;

		// find class mapped to path
		String path = reqHandedDown.getPathInfo();
		if (path == null) {
			path = "/";
		}

		boolean foundPath = false;
		boolean foundMethod = false;

		/* Determine HTTP method --------------------- */
		// look in HTTP header
		String httpMethod = reqHandedDown.getHeader(X_HTTP_Method_Override);
		// look in query/post param
		if (httpMethod == null) {
			httpMethod = reqHandedDown.getParameter(X_HTTP_Method_Override);
		}
		// use given HTTP method
		if (httpMethod == null) {
			httpMethod = reqHandedDown.getMethod();
		}

		/* Find RestlessMethod to be called ------------------ */
		// look through all registered methods
		final boolean reqViaAdminUrl = RestlessStatic.requestIsViaAdminUrl(reqHandedDown);

		/*
		 * Searching and executing the correct method is split up in two blocks
		 * so that the list of methods is only blocked during the
		 * search-process.
		 * 
		 * Find the correct method, get the necessary parameters...
		 */
		RestlessMethodExecutionParameters params = null;
		RestlessMethod restlessMethod = null;
		synchronized (this.methods) {
			for (RestlessMethod m : this.methods) {
				/*
				 * if secure access, ignore all public methods. if insecure
				 * access, ignore all secure methods: Skip all restlessMethods
				 * that may not be accessed
				 */
				if (reqViaAdminUrl == m.isAdminOnly()) {
					// if path matches
					if (m.getPathTemplate().matches(path)) {
						foundPath = true;
						// and HTTP method matches
						if (httpMethod.equalsIgnoreCase(m.getHttpMethod())) {
							try {
								params = m.prepareMethodExecution(this, reqHandedDown, res,
										requestClock);

							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							if (params != null) {
								/*
								 * non-null params imply that the correct method
								 * was found.
								 */
								restlessMethod = m;
								foundMethod = true;
								break;
							}
						}
					}
				}
			}
		}

		/*
		 * ... and execute the method if it was found.
		 */
		if (restlessMethod != null) {
			assert params != null;
			assert foundMethod == true;
			try {
				restlessMethod.execute(params, this, reqHandedDown, res);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (!foundMethod) {
			if (hasCustomError404HandlerDefined()) {
				log.info("Launching custom error404 handler: " + this.error404resourceClassname
						+ " for request on path '" + path + "'");
				// define context
				IRestlessContext restlessContext = new RestlessContextImpl(this, req, res, "error-"
						+ UUID.randomUUID());
				RestlessStatic.invokeStaticMethod(this.error404resourceClassname, "error404",
						new Class[] { IRestlessContext.class }, new Object[] { restlessContext });
			} else if (DELEGATE_UNHANDLED_TO_DEFAULT) {
				log.info("Delegateto default");
				try {
					delegateToDefaultServlet(reqHandedDown, res);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				// produce even better error message?
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
				} catch (IOException e) {
				}
			}
		}

		// make super-sure all buffers are flushed
		try {
			res.flushBuffer();
		} catch (IOException e) {
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
	 * @param key
	 *            attribute name @NeverNull
	 * @param value
	 *            attribute value @NeverNull
	 */

	public void setServletContextAttribute(@NeverNull String key, @NeverNull Object value) {
		try {
			ServletContext sc = this.getServletContext();
			synchronized (sc) {
				sc.setAttribute(key, value);
			}
		} catch (NullPointerException e) {
			// lazy init
			if (this.localContext == null) {
				this.localContext = new HashMap<String, Object>();
			}

			synchronized (this.localContext) {
				this.localContext.put(key, value);
			}
		}
	}

}

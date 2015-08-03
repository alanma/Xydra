package org.xydra.gwt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.coreimpl.sysout.DefaultLoggerFactorySPI;

/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
 * required to run the webapp.
 *
 * This class requires to build and assemble the web app: call
 * <code>mvn clean compile gwt:compile gwt:mergewebxml war:war -Dmaven.test.skip=true -o
 * </code>
 *
 * <p>
 * If only the client source code changed, use <code>mvn gwt:compile</code>
 *
 * <p>
 * If the web.xml changed use
 * <code>mvn gwt:mergwebxml war:war to update the web.xml in target/iba-1.0.0-SNAPSHOT/WEB-INF
 *
 * <p>If only static files have been modified, no call is neccesary as this Jetty is configured to load the directly from src/main/webapp.
 *
 * @author xamde
 *
 */
public class GwtTestServer {

	private static Logger log = getLogger();

	private static Logger getLogger() {
		LoggerFactory.setLoggerFactorySPI(new DefaultLoggerFactorySPI(), "GwtTestServer");
		return LoggerFactory.getLogger(GwtTestServer.class);
	}

	private final int port;

	private Server server;

	private WebAppContext webapp;

	public GwtTestServer() {
		this(8888);
	}

	public GwtTestServer(final int port) {
		this.port = port;
	}

	private URI startServer() throws Exception {

		// Create an instance of the jetty server.
		this.server = new Server(this.port);

		final String contextPath = "/";

		// Where to server files from.
		final File docRoot = new File("target/gwt-0.1.4-SNAPSHOT");
		final File webappRoot = new File("src/main/webapp");

		/*
		 * Create the webapp. This will load the servlet configuration from
		 * docRoot + WEB-INF/web.xml
		 */
		this.webapp = new WebAppContext(webappRoot.getAbsolutePath(), contextPath);

		// Add the servlets.
		this.webapp.setClassLoader(Thread.currentThread().getContextClassLoader());

		final HandlerList hl = new HandlerList();

		// Add a handler serving static files directly from src/main/webapp
		final ResourceHandler publicDocs = new ResourceHandler() {
			// nothing
		};
		publicDocs.setResourceBase(webappRoot.getAbsolutePath());
		hl.addHandler(publicDocs);

		// Add a handler serving static files directly from src/main/webapp
		final ResourceHandler gwtFiles = new ResourceHandler() {
			// nothing
		};
		gwtFiles.setResourceBase(docRoot.getAbsolutePath());
		hl.addHandler(gwtFiles);

		this.webapp.setHandler(hl);

		final FilterHolder filterHolder = new FilterHolder();
		filterHolder.setFilter(new Filter() {

			@Override
			public void destroy() {
				// do nothing
			}

			@Override
			public void doFilter(final ServletRequest request, final ServletResponse response,
					final FilterChain filterChain) throws IOException, ServletException {
				System.out.println("Jetty GET " + ((HttpServletRequest) request).getRequestURI());
				final HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
						(HttpServletResponse) response);

				// Modify the servlet which serves png and gif files so that it
				// explicitly sets the Pragma, Cache-Control, and Expires
				// headers. The Pragma and Cache-Control headers should be
				// removed. The Expires header should be set according to the
				// caching recommendations mentioned in the previous section.

				final Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);

				final SimpleDateFormat dateFormatter = new SimpleDateFormat(
						"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
				final TimeZone tz = TimeZone.getTimeZone("GMT");
				dateFormatter.setTimeZone(tz);
				final String rfc1123 = dateFormatter.format(cal.getTime());
				((HttpServletResponse) response).addHeader("Expires", rfc1123);
				((HttpServletResponse) response).addHeader("Cache-Control",
						"public; max-age=31536000");
				filterChain.doFilter(request, responseWrapper);
			}

			@Override
			public void init(final FilterConfig filterConfig) {
				// do nothing
			}
		});
		this.webapp.addFilter(filterHolder, "*.png", EnumSet.allOf(DispatcherType.class));
		this.webapp.addFilter(filterHolder, "*.gif", EnumSet.allOf(DispatcherType.class));
		this.webapp.addFilter(filterHolder, ".cache.*", EnumSet.allOf(DispatcherType.class));

		// Add the webapp to the server.
		this.server.setHandler(this.webapp);

		try {
			this.server.start();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		try {
			URI uri = new URI("http://localhost:" + this.port + "/");
			if (!contextPath.equals("") && !contextPath.equals("/")) {
				uri = uri.resolve(contextPath + "/");
			}
			return uri;
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

	public static void main(final String[] args) throws Exception {

		final TestServer testServer = new TestServer();

		final URI store = testServer.startXydraServer(new File("src/test/resources/webapp"));

		final URI ui = new GwtTestServer().startServer();

		log.info("Started servers.");
		log.info(" - Backend is at " + store);
		log.info(" - User interface is at " + ui + "XydraEditor.html");

	}
}

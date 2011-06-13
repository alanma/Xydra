package org.xydra.gwt;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
 * @author voelkel
 * 
 */
public class Jetty {
	
	private static Logger log = LoggerFactory.getLogger(Jetty.class);
	
	private int port;
	
	private Server server;
	
	private WebAppContext webapp;
	
	public Jetty() {
		this(8888);
	}
	
	public Jetty(int port) {
		this.port = port;
	}
	
	private void startServer() throws Exception {
		
		// Create an instance of the jetty server.
		this.server = new Server(this.port);
		
		String contextPath = "/xclient";
		
		// Where to server files from.
		File docRoot = new File("target/gwt-0.1.4-SNAPSHOT");
		File webappRoot = new File("src/main/webapp");
		
		/*
		 * Create the webapp. This will load the servlet configuration from
		 * docRoot + WEB-INF/web.xml
		 */
		this.webapp = new WebAppContext(webappRoot.getAbsolutePath(), contextPath);
		
		// Add the servlets.
		this.webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
		
		HandlerList hl = new HandlerList();
		
		// Add a handler serving static files directly from src/main/webapp
		ResourceHandler publicDocs = new ResourceHandler() {
			// nothing
		};
		publicDocs.setResourceBase(webappRoot.getAbsolutePath());
		hl.addHandler(publicDocs);
		
		// Add a handler serving static files directly from src/main/webapp
		ResourceHandler gwtFiles = new ResourceHandler() {
			// nothing
		};
		gwtFiles.setResourceBase(docRoot.getAbsolutePath());
		hl.addHandler(gwtFiles);
		
		this.webapp.addHandler(hl);
		
		FilterHolder filterHolder = new FilterHolder();
		filterHolder.setFilter(new Filter() {
			
			public void destroy() {
				// do nothing
			}
			
			public void doFilter(ServletRequest request, ServletResponse response,
			        FilterChain filterChain) throws IOException, ServletException {
				System.out.println("Jetty GET " + ((HttpServletRequest)request).getRequestURI());
				HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
				        (HttpServletResponse)response);
				
				// Modify the servlet which serves png and gif files so that it
				// explicitly sets the Pragma, Cache-Control, and Expires
				// headers. The Pragma and Cache-Control headers should be
				// removed. The Expires header should be set according to the
				// caching recommendations mentioned in the previous section.
				
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat(
				        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
				TimeZone tz = TimeZone.getTimeZone("GMT");
				dateFormatter.setTimeZone(tz);
				String rfc1123 = dateFormatter.format(cal.getTime());
				((HttpServletResponse)response).addHeader("Expires", rfc1123);
				((HttpServletResponse)response).addHeader("Cache-Control",
				        "public; max-age=31536000");
				filterChain.doFilter(request, responseWrapper);
			}
			
			public void init(FilterConfig filterConfig) {
				// do nothing
			}
		});
		this.webapp.addFilter(filterHolder, "*.png", Handler.ALL);
		this.webapp.addFilter(filterHolder, "*.gif", Handler.ALL);
		this.webapp.addFilter(filterHolder, ".cache.*", Handler.ALL);
		
		// Add the webapp to the server.
		this.server.setHandler(this.webapp);
		
		try {
			this.server.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		log.info("Started embedded Jetty server. User interface is at http://localhost:"
		        + this.port + "/");
	}
	
	public static void main(String[] args) throws Exception {
		Jetty jetty = new Jetty();
		jetty.startServer();
	}
	
}

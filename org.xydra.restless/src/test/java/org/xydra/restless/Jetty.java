package org.xydra.restless;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
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
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp.
 * 
 * @author voelkel
 * 
 */
public class Jetty {
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(Jetty.class);
	
	private int port;
	
	private Server server;
	
	private WebAppContext webapp;
	
	public Jetty() {
		this(8080);
	}
	
	public Jetty(int port) {
		this.port = port;
	}
	
	public URI startServer(String contextPath, File docRoot) {
		
		if(this.server != null)
			throw new RuntimeException("server is already startet");
		
		// Create an instance of the jetty server.
		this.server = new Server(this.port);
		
		/*
		 * Create the webapp. This will load the servlet configuration from
		 * docRoot + WEB-INF/web.xml
		 */
		this.webapp = new WebAppContext(docRoot.getAbsolutePath(), contextPath);
		
		// make sure 'com.google.appengine.tools.appstats.AppstatsFilter' is on
		// the classpath
		
		// Add the servlets.
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		
		this.webapp.setClassLoader(classloader);
		FilterHolder filterHolder = new FilterHolder();
		
		// caching
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
		
		/*
		 * Add simple security handler that puts anybody with the name 'admin'
		 * into the admin role.
		 */
		this.webapp.getSecurityHandler().setUserRealm(new UserRealm() {
			
			public boolean reauthenticate(Principal user) {
				return true;
			}
			
			public Principal pushRole(Principal user, String role) {
				return user;
			}
			
			public Principal popRole(Principal user) {
				return user;
			}
			
			public void logout(Principal user) {
			}
			
			public boolean isUserInRole(Principal user, String role) {
				return user.getName().equalsIgnoreCase("admin");
			}
			
			public Principal getPrincipal(final String username) {
				return new Principal() {
					public String getName() {
						return username;
					}
				};
			}
			
			public String getName() {
				return "dummyRealm";
			}
			
			public void disassociate(Principal user) {
			}
			
			public Principal authenticate(String username, Object credentials, Request request) {
				return getPrincipal(username);
			}
		});
		
		// Add the webapp to the server.
		this.server.setHandler(this.webapp);
		
		try {
			this.server.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			return new URI("http://localhost:" + this.port + "/").resolve(contextPath + "/");
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void stopServer() {
		if(this.server != null) {
			try {
				this.server.stop();
				this.server.destroy();
			} catch(Exception e) {
				throw new RuntimeException("error stopping server", e);
			}
			this.server = null;
		}
	}
	
}

package org.xydra.log.howto.ongae;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * FIXME The current maven config (pom.xml) conflicts GAE with GWT settings,
 * gae:deploy fails.
 * 
 * @author xamde
 * 
 */
public class ServletWithLogging extends HttpServlet {
	
	private static final long serialVersionUID = -7941734636749249244L;
	
	private static final Logger log = LoggerFactory.getLogger(ServletWithLogging.class);
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.getWriter().write("Starting log test ...");
		res.getWriter().flush();
		log();
		res.getWriter().write("Done with log test.");
		res.getWriter().flush();
	}
	
	public static void log() {
		log.trace("this is trace");
		log.debug("this is debug");
		log.info("this is info");
		log.warn("this is warn");
		log.error("this is error");
		System.out.println("this is sysout");
		System.err.println("this is syserr");
		
		System.err.println("Log is traceEnabled? " + log.isTraceEnabled());
		System.err.println("Log is debugEnabled? " + log.isDebugEnabled());
		System.err.println("Log is infoEnabled? " + log.isInfoEnabled());
		System.err.println("Log is warnEnabled? " + log.isWarnEnabled());
		System.err.println("Log is errorEnabled? " + log.isErrorEnabled());
	}
	
	/* Just for fun: You can also call all these logs this way */
	public static void main(String[] args) {
		log();
	}
	
}

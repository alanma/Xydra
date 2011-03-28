package org.xydra.gaemyadmin;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.Page.Form;
import org.xydra.restless.utils.Page.METHOD;
import org.xydra.restless.utils.Page.UnsortedList;
import org.xydra.restless.utils.ServletUtils;


/**
 * Configure logging settings via browser
 * 
 * @author xamde
 */
public class LogConfResource {
	
	private static String URL;
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix + "/", "GET", LogConfResource.class, "index", true);
		restless.addMethod(prefix + "/", "POST", LogConfResource.class, "post", true);
		URL = "/admin" + prefix + "/";
	}
	
	public void index(HttpServletResponse res) throws IOException {
		HtmlUtils.startHtmlPage(res, "Logging Configuration");
		
		Form form = new Form(null, METHOD.POST, URL);
		UnsortedList ul = form.unsortedList();
		
		LogManager lm = LogManager.getLogManager();
		Enumeration<String> enu = lm.getLoggerNames();
		while(enu.hasMoreElements()) {
			String loggerName = enu.nextElement();
			java.util.logging.Logger logger = lm.getLogger(loggerName);
			Level currentLevel = logger.getLevel();
			ul.li().inputText("Logger '" + loggerName + "'", "logger-" + loggerName,
			        currentLevel == null ? "" : currentLevel.getName());
		}
		ul.li().inputSubmit("Set");
		
		HtmlUtils.writeContent(res, form.toHtml("  "));
		HtmlUtils.endHtmlPage(res);
	}
	
	public void post(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// set each logger to the given level, if different from before
		
		// TODO persist settings in datastore and load from there
		
		Map<String,String> params = ServletUtils.getRequestparametersAsMap(req);
		LogManager lm = LogManager.getLogManager();
		for(Map.Entry<String,String> param : params.entrySet()) {
			String key = param.getKey();
			String loggerName = key.substring("logger-".length());
			Logger logger = lm.getLogger(loggerName);
			if(logger == null) {
				throw new IllegalArgumentException("Logger '" + loggerName + "' was null");
			}
			Level current = logger.getLevel();
			Level planned = null;
			try {
				planned = Level.parse(param.getValue());
				if(current == null || !current.equals(planned)) {
					logger.setLevel(planned);
				}
			} catch(IllegalArgumentException e) {
			}
		}
		
		// redirect to self
		res.sendRedirect(URL);
	}
	
}

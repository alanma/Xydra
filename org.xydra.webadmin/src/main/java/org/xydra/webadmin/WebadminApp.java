package org.xydra.webadmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInJava;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;


@RunsInAppEngine
@RunsInJava
public class WebadminApp {
	
	public static final Logger log = LoggerFactory.getLogger(WebadminApp.class);
	
	public static void restless(String prefix) {
		
		Restless.addExceptionHandler(new RestlessExceptionHandler() {
			
			public boolean handleException(Throwable t, HttpServletRequest req,
			        HttpServletResponse res) {
				log.error(t.getLocalizedMessage(), t);
				// eat all exceptions
				return true;
			}
		});
		
		WebadminApp webadminApp = new WebadminApp();
		Restless.addGet("/backup", webadminApp, "backup");
		
	}
	
	public void backup(HttpServletRequest req, HttpServletResponse res) {
		
	}
	
}

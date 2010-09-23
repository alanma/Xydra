package org.xydra.server.impl.gae;

import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.server.rest.XydraRestServer;


public class LogTestResource {
	
	public void restless(Restless restless, String prefix) {
		
		restless.addGet(prefix + "/logtest", this, "init");
		
	}
	
	public void init(HttpServletResponse res) {
		
		Logtest.log();
		
		String result = "result: ";
		
		Logger logger = java.util.logging.Logger.getLogger("dummy");
		for(Handler handler : logger.getHandlers()) {
			result += " HANDLER " + handler.getClass().getName();
		}
		Logger parent = logger.getParent();
		int depth = 0;
		while(parent != null && depth < 10) {
			for(Handler handler : parent.getHandlers()) {
				result += " PARENT-HANDLER " + handler.getClass().getName();
			}
			parent = logger.getParent();
			depth++;
		}
		
		XydraRestServer.textResponse(res, HttpServletResponse.SC_OK, result);
	}
	
}

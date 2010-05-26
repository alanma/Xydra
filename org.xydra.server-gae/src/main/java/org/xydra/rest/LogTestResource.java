package org.xydra.rest;

import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.xydra.server.gae.util.Logtest;



@Path("logtest")
public class LogTestResource {
	
	@GET
	@Produces("text/plain")
	public String init() {
		
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
		
		return result;
	}
	
}

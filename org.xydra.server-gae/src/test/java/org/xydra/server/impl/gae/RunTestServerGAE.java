package org.xydra.server.impl.gae;

import org.xydra.server.test.TestServer;


public class RunTestServerGAE {
	
	public static void main(String[] args) throws Exception {
		
		GaeTestfixer.enable();
		
		TestServer.main(args);
	}
	
}

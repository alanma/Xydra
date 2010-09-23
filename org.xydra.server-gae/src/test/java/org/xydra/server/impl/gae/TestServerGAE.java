package org.xydra.server.impl.gae;

import org.xydra.server.test.TestServer;


public class TestServerGAE {
	
	public static void main(String[] args) throws Exception {
		
		GaeTestfixer.enable();
		
		TestServer.main(args);
	}
	
}

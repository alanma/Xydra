package org.xydra.server;

import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.test.TestServer;


public class TestServerGAE {
	
	public static void main(String[] args) throws Exception {
		
		GaeTestfixer.enable();
		
		TestServer.main(args);
	}
	
}

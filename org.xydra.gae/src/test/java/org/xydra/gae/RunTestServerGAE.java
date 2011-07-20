package org.xydra.gae;

import org.xydra.server.test.TestServer;
import org.xydra.store.impl.gae.GaeTestfixer;


public class RunTestServerGAE {
	
	public static void main(String[] args) throws Exception {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		TestServer.main(args);
	}
	
}

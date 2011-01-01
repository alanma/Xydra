package org.xydra.testgae;

import org.xydra.restless.Restless;


public class EchoResource {
	
	public static void restless(Restless r) {
		r.addGet("/echo", EchoResource.class, "get");
	}
	
	public String get() {
		return "It is " + System.currentTimeMillis();
	}
	
}

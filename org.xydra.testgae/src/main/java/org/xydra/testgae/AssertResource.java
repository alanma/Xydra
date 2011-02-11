package org.xydra.testgae;

import org.xydra.restless.Restless;


public class AssertResource {
	
	public static void restless(Restless r) {
		r.addGet("/assert", AssertResource.class, "get");
	}
	
	public String get() {
		try {
			assert false : "vm assertions are on";
			return "If you can read this, 'assert' is off";
		} catch(AssertionError e) {
			return "Assertions are on";
		}
	}
	
}

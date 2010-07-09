package org.xydra.restless;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.xydra.restless.PathTemplate;
import org.xydra.restless.Restless;


public class RestlessTest {
	
	@Test
	@Ignore
	// TODO need stub objects for req and res
	public void testMatching() {
		// setup
		Restless.addGet("/my/path", new RestlessTest(), "getMe");
		// execution
		Restless restless = new Restless();
		restless.restlessService(null, null);
	}
	
	public void getMe() {
		
	}
	
	public void testVariableExtraction() {
		PathTemplate pe = new PathTemplate("/way/{id}/step");
		System.out.println(pe.toString());
		assert "/way/13/step".matches(pe.getRegex());
		assert "/way/13/step/".matches(pe.getRegex());
		assert "/way/13/step/this".matches(pe.getRegex());
		assert "/way/13/step/this/".matches(pe.getRegex());
		List<String> var = pe.extractVariables("/way/13/step/olé");
		assert var.size() == 1;
		assert var.get(0).equals("13");
		System.out.println(var);
	}
	
}

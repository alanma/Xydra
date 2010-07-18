package org.xydra.restless;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;


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
		assertTrue("/way/13/step".matches(pe.getRegex()));
		assertTrue("/way/13/step/".matches(pe.getRegex()));
		// FIXME why should this match?
		assertFalse("/way/13/step/this".matches(pe.getRegex()));
		assertFalse("/way/13/step/this/".matches(pe.getRegex()));
		List<String> var = pe.extractVariables("/way/13/");
		assert var.size() == 1;
		assert var.get(0).equals("13");
		System.out.println(var);
	}
}

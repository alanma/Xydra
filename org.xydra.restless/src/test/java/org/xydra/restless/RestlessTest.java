package org.xydra.restless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
	
	@Test
	public void testVariableExtraction() {
		PathTemplate pe = new PathTemplate("/way/{id}/step");
		System.out.println(pe.toString());
		assertTrue("/way/13/step".matches(pe.getRegex()));
		assertTrue("/way/13/step/".matches(pe.getRegex()));
		// FIXME why should this match?
		assertFalse("/way/13/step/this".matches(pe.getRegex()));
		assertFalse("/way/13/step/this/".matches(pe.getRegex()));
		List<String> var = pe.extractVariables("/way/13/step");
		assert var.size() == 1;
		assert var.get(0).equals("13");
		System.out.println(var);
	}
	
	@Test
	public void testQueryParamParsing() {
		Map<String,SortedSet<String>> map = Restless
		        .getQueryStringAsMap("foo=bar&a=b&a=c&d=&this=that&enc=aa%3Dbb%26cc%3Ddd%26%C3%A4%C3%B6%C3%BC");
		assertTrue(map.containsKey("foo"));
		assertTrue(map.containsKey("a"));
		assertTrue(map.containsKey("d"));
		assertTrue(map.containsKey("this"));
		assertTrue(map.containsKey("enc"));
		assertEquals(1, map.get("foo").size());
		assertEquals(2, map.get("a").size());
		assertEquals(1, map.get("d").size());
		assertEquals(1, map.get("this").size());
		assertEquals(1, map.get("enc").size());
		String dec = "aa=bb&cc=dd&äöü";
		assertEquals(dec, map.get("enc").iterator().next());
	}
}

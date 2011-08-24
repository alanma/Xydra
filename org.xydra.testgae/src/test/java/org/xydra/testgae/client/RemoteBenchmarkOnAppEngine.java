package org.xydra.testgae.client;

import org.junit.Before;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnAppEngine extends RemoteBenchmark {
	
	@Before
	public void setup() {
		this.absoluteUrl = "http://testgae.latest.xydra-live.appspot.com/logged";
		this.path = "gae";
	}
}

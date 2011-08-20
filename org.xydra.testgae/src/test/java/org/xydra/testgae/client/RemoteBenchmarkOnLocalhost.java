package org.xydra.testgae.client;

import org.junit.Before;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnLocalhost extends RemoteBenchmark {
	
	@Before
	public void setup() {
		this.absoluteUrl = "http://localhost:8787";
		this.path = "local";
	}
}

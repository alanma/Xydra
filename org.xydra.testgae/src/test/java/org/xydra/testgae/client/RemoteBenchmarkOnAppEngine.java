package org.xydra.testgae.client;

import java.io.IOException;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnAppEngine {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		RemoteBenchmark.runBenchmark("http://testgae.latest.xydra-live.appspot.com");
	}
	
}

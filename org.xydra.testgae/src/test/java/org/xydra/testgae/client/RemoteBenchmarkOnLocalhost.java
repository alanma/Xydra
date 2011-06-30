package org.xydra.testgae.client;

import java.io.IOException;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnLocalhost {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		RemoteBenchmark.runBenchmark("http://localhost:8787");
	}
	
}

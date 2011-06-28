package org.xydra.testgae.client;



/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnAppEngine {
	
	public static void main(String[] args) throws InterruptedException {
		RemoteBenchmark.runBenchmark("http://localhost:8787");
	}
	
}

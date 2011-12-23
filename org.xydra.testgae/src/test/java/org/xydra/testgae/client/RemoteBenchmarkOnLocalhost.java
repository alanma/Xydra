package org.xydra.testgae.client;



/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnLocalhost extends RemoteBenchmark {
	
	public RemoteBenchmarkOnLocalhost(String absoluteUrl, String path, int iterations,
	        int maxAmount, Integer[] range) {
		super(absoluteUrl, path, iterations, maxAmount, range);
	}
}

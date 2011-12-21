package org.xydra.testgae.client;

import org.junit.Before;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnAppEngine extends RemoteBenchmark {
	
	public RemoteBenchmarkOnAppEngine(String absoluteUrl, String path, int iterations,
	        int maxAmount, Integer[] range) {
		this.absoluteUrl = absoluteUrl;
		this.path = path;
		this.iterations = iterations;
		this.maxAmount = maxAmount;
		this.range = range;
	}
	
	public void executeAllBenchmarks() {
		benchmarkAddingOneWishOneThread();
		benchmarkDeletingOneWishOneThread();
		benchmarkEditingOneWishOneThread();
		benchmarkAddingMultipleWishesInTransaction();
		benchmarkAddingWishesInTransactionWithInitialWishes();
		benchmarkEditingOneWishOneThreadWithInitialWishes();
		
	}
	
	@Before
	public void setup() {
		
	}
}

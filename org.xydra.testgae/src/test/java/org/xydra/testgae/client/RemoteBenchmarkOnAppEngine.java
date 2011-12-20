package org.xydra.testgae.client;

import org.junit.Before;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmarkOnAppEngine extends RemoteBenchmark {
	
	public RemoteBenchmarkOnAppEngine(String absoluteUrl, String path, int iterations, int maxAmount) {
		this.absoluteUrl = absoluteUrl;
		this.path = path;
		this.iterations = iterations;
		this.maxAmount = maxAmount;
	}
	
	public void executeAllBenchmarks() {
		testBenchmarkAddingOneWishOneThread();
		testBenchmarkDeletingOneWishOneThread();
		testBenchmarkEditingOneWishOneThread();
		testAddingMultipleWishesInTransaction();
		testAddingWishesInTransactionWithInitialWishes();
		testBenchmarkEditingOneWishOneThreadWithInitialWishes();
		
	}
	
	@Before
	public void setup() {
		
	}
}

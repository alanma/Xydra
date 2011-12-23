package org.xydra.testgae.client;

/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * @author Kaidel
 * 
 */
public class RemoteBenchmarkOnAppEngine extends RemoteBenchmark {
	
	public RemoteBenchmarkOnAppEngine(String absoluteUrl, String path, int iterations,
	        int maxAmount, Integer[] range) {
		super(absoluteUrl, path, iterations, maxAmount, range);
	}
	
	public void executeAllBenchmarks() {
		benchmarkAddingOneWishOneThread();
		benchmarkDeletingOneWishOneThread();
		benchmarkEditingOneWishOneThread();
		benchmarkAddingMultipleWishesInTransaction();
		benchmarkAddingWishesInTransactionWithInitialWishes();
		benchmarkEditingOneWishInTransactionWithInitialWishes();
		
	}
}

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
	
	public void executeAllSingleThreadBenchmarks() {
		benchmarkAddingOneWishOneThread();
		benchmarkDeletingOneWishOneThread();
		benchmarkEditingOneWishOneThread();
		benchmarkAddingMultipleWishesInTransactionOneThread();
		benchmarkAddingWishesInTransactionWithInitialWishesOneThread();
		benchmarkEditingOneWishInTransactionWithInitialWishesOneThread();
		
	}
	
	public void executeAllMultiThreadedBenchmarks(int threads) {
		benchmarkAddingOneWishMultipleThreads(threads);
		benchmarkDeletingOneWishMultipleThreads(threads);
		benchmarkEditingOneWishMultipleThreads(threads);
		benchmarkAddingMultipleWishesInTransactionMultipleThreads(threads);
		benchmarkAddingWishesInTransactionWithInitialWishesMultipleThreads(threads);
		benchmarkEditingOneWishInTransactionWithInitialWishesMultipleThreads(threads);
		
	}
}

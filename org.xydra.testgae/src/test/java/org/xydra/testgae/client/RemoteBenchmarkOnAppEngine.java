package org.xydra.testgae.client;

/**
 * Benchmark test for a remote server
 *
 * @author xamde
 * @author kaidel
 *
 */
public class RemoteBenchmarkOnAppEngine extends RemoteBenchmark {

	public RemoteBenchmarkOnAppEngine(final String absoluteUrl, final String path, final int iterations,
			final int maxAmount, final Integer[] range) {
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

	public void executeAllMultiThreadedBenchmarks(final int threads) {
		benchmarkAddingOneWishMultipleThreads(threads);
		benchmarkDeletingOneWishMultipleThreads(threads);
		benchmarkEditingOneWishMultipleThreads(threads);
		benchmarkAddingMultipleWishesInTransactionMultipleThreads(threads);
		benchmarkAddingWishesInTransactionWithInitialWishesMultipleThreads(threads);
		benchmarkEditingOneWishInTransactionWithInitialWishesMultipleThreads(threads);

	}
}

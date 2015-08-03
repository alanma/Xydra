package org.xydra.testgae.client;

/**
 * Benchmark test for a remote server
 *
 * @author xamde
 *
 */
public class RemoteBenchmarkOnLocalhost extends RemoteBenchmark {

	public RemoteBenchmarkOnLocalhost(final String absoluteUrl, final String path, final int iterations,
			final int maxAmount, final Integer[] range) {
		super(absoluteUrl, path, iterations, maxAmount, range);
	}
}

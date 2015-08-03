package org.xydra.testgae.client.experimental;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class StressTest {

	private static final Logger log = LoggerFactory.getLogger(StressTest.class);

	@Test
	public void testConcurrentRequests() {
		long threads = 1;
		final long THREADS_PER_MINUTE_MAX = 1;
		final long start = System.currentTimeMillis();
		while (true) {
			final long now = System.currentTimeMillis();
			final double threadsPerMinute = (now - start) / threads;
			if (threadsPerMinute < THREADS_PER_MINUTE_MAX) {
				final Worker worker = new Worker(threads++);
				worker.start();
				log.info("Running threads: " + threads);
			}
			Thread.yield();
		}
	}

	class Worker extends Thread {

		public Worker(final long id) {
			this.id = id;
		}

		long id;

		long count = 0;

		@Override
		public void run() {
			while (true) {
				assertUrl("id " + this.id + " has count " + this.count++,
						"http://localhost:8787/test1");
				try {
					sleep((long) (Math.random() * 1000));
				} catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	public static void assertUrl(final String logMsg, final String absoluteUrl) {
		log.info(logMsg + " trying url " + absoluteUrl);
		try {
			final DefaultHttpClient httpclient = new DefaultHttpClient();

			final HttpGet httpget = new HttpGet(absoluteUrl);

			final HttpResponse res = httpclient.execute(httpget);

			final int status = res.getStatusLine().getStatusCode();

			assertEquals(absoluteUrl, 200, status);
		} catch (final Exception e) {
			throw new RuntimeException("Failed on " + absoluteUrl, e);
		}
	}

}

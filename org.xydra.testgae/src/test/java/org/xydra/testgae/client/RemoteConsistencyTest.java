package org.xydra.testgae.client;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.NanoClock;

@SuppressWarnings("unused")
public class RemoteConsistencyTest {

	private static final Logger log = LoggerFactory.getLogger(RemoteConsistencyTest.class);

	// your url here
	private static final String SERVER_ROOT = "http://testgae20120918.xydra-1.appspot.com/";
	// "testgae.latest.xydra-live.appspot.com";

	public static final int THREADS = 10;

	public static final int ROUNDS = 100;

	public static void main(String[] args) {
		NanoClock c = new NanoClock().start();
		for (int i = 0; i < THREADS; i++) {
			ConsistencyTestClient client = new ConsistencyTestClient(SERVER_ROOT, "thread-" + i
					+ "-", ROUNDS);
			client.start();
		}
		// wait for all threads to finish
		while (ConsistencyTestClient.done < THREADS) {
			Thread.yield();
		}

		long d = c.stopAndGetDuration("Threads: " + THREADS + " x Rounds: " + ROUNDS);
		long runs = THREADS * ROUNDS;
		System.out.println("==== " + c.getStats());
		System.out.println("Ran " + THREADS + " for up to " + ROUNDS + " rounds");
		System.out.println("Average: " + (d / runs) + " ms per run");
		System.out.println("Read  errors: " + ConsistencyTestClient.failedReads);
		System.out.println("Write errors: " + ConsistencyTestClient.failedWrites);
	}
}

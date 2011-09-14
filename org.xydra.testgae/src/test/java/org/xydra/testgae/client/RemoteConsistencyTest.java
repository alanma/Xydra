package org.xydra.testgae.client;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.Clock;


@SuppressWarnings("unused")
public class RemoteConsistencyTest {
	
	private static final Logger log = LoggerFactory.getLogger(RemoteConsistencyTest.class);
	
	private static final String SERVER_ROOT = "http://testgae.latest.xydra-live.appspot.com";
	
	public static final int THREADS = 10;
	
	public static final int ROUNDS = 10;
	
	public static void main(String[] args) {
		Clock c = new Clock().start();
		for(int i = 0; i < THREADS; i++) {
			ConsistencyTestClient client = new ConsistencyTestClient(SERVER_ROOT, "thread-" + i
			        + "-", ROUNDS);
			client.start();
		}
		// wait for all threads to finish
		while(ConsistencyTestClient.done < THREADS) {
			Thread.yield();
		}
		
		long d = c.stopAndGetDuration("Threads: " + THREADS + " x Rounds: " + ROUNDS);
		long runs = THREADS * ROUNDS;
		System.out.println("==== " + c.getStats());
		System.out.println("Average: " + (d / runs) + " ms per run");
		System.out.println("Errors: " + ConsistencyTestClient.failedWrites);
	}
}

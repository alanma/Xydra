package org.xydra.testgae.client;

import org.xydra.testgae.shared.SimulatedUser;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmark {
	
	public static void runBenchmark(String absoluteUrl) throws InterruptedException {
		SimulatedUser u1 = new SimulatedUser(absoluteUrl, "repo1");
		u1.start();
		// tell other thread after some seconds to stop.
		Thread.sleep(60 * 1000);
		u1.pleaseStopSoon();
	}
	
}

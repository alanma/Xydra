package org.xydra.testgae.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.xydra.testgae.shared.SimulatedUser;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmark {
	
	public static void runBenchmark(String absoluteUrl) throws InterruptedException, IOException {
		System.out.println("---- Running benchmark1 -----");
		SimulatedUser u1 = new SimulatedUser(absoluteUrl, "repo1");
		Writer w = new OutputStreamWriter(System.out);
		u1.doBenchmark1(w);
		
		runLoadTest(absoluteUrl, "repo2", 20, 3 * 60 * 1000);
	}
	
	public static void runLoadTest(String absoluteUrl, String repoId, int users, int testDurationMs)
	        throws InterruptedException {
		System.out.println("---- " + (testDurationMs / 1000) + "s of random actions by " + users
		        + " users -----");
		SimulatedUser[] u = new SimulatedUser[users];
		for(int i = 0; i < u.length; i++) {
			u[i] = new SimulatedUser(absoluteUrl, repoId);
		}
		// starting them slowly
		long startPhaseMs = testDurationMs / 2;
		long startPerUserMs = startPhaseMs / users;
		for(int i = 0; i < u.length; i++) {
			u[i].start();
			System.out.println("Running " + (i + 1) + " users");
			Thread.sleep(startPerUserMs);
		}
		System.out.println("Running ALL " + users + " users for " + startPhaseMs + "ms now...");
		Thread.sleep(startPhaseMs);
		
		System.out.println("Shutting down users ...");
		for(int i = 0; i < u.length; i++) {
			u[i].pleaseStopSoon();
		}
	}
	
}

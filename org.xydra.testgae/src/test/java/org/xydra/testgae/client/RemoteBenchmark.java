package org.xydra.testgae.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.junit.Ignore;
import org.junit.Test;
import org.xydra.testgae.shared.HttpUtils;
import org.xydra.testgae.shared.SimulatedUser;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public class RemoteBenchmark {
	protected String absoluteUrl;
	
	@Test
	@Ignore
	public void testRunBenchmark() {
		System.out.println("---- Running benchmark1 -----");
		SimulatedUser u1 = new SimulatedUser(this.absoluteUrl, "repo1", new Exchanger<Exception>());
		Writer w = new OutputStreamWriter(System.out);
		try {
			u1.doBenchmark1(w);
		} catch(Exception e) {
			fail(e.toString());
		}
		
	}
	
	@Test
	@Ignore
	public void testRunLoadTest() {
		try {
			runLoadTest(this.absoluteUrl, "repo2", 20, 3 * 60 * 1000);
		} catch(Exception e) {
			assertFalse(e.toString(), true);
		}
	}
	
	public void runLoadTest(String absoluteUrl, String repoId, int users, int testDurationMs)
	        throws InterruptedException {
		System.out.println("---- " + (testDurationMs / 1000) + "s of random actions by " + users
		        + " users -----");
		SimulatedUser[] u = new SimulatedUser[users];
		List<Exchanger<Exception>> exchangers = new ArrayList<Exchanger<Exception>>();
		for(int i = 0; i < u.length; i++) {
			// TODO Document exchangers!
			
			Exchanger<Exception> exchanger = new Exchanger<Exception>();
			exchangers.add(i, exchanger);
			u[i] = new SimulatedUser(absoluteUrl, repoId, exchanger);
		}
		// starting them slowly
		long startPhaseMs = testDurationMs / 2;
		long startPerUserMs = startPhaseMs / users;
		for(int i = 0; i < u.length; i++) {
			try {
				u[i].start();
			} catch(Exception e) {
				assertFalse(true);
			}
			System.out.println("Running " + (i + 1) + " users");
			Thread.sleep(startPerUserMs);
		}
		System.out.println("Running ALL " + users + " users for " + startPhaseMs + "ms now...");
		Thread.sleep(startPhaseMs);
		
		System.out.println("Shutting down users ...");
		for(int i = 0; i < u.length; i++) {
			u[i].pleaseStopSoon();
		}
		
		// check exchangers
		for(Exchanger<Exception> ex : exchangers) {
			Exception exception = ex.exchange(null);
			
			if(exception != null) {
				fail("There was at least one exception: " + exception.toString());
			}
		}
	}
	
	@Test
	public void testAddTooManyWishes() {
		try {
			HttpUtils.makeGetRequest(this.absoluteUrl + "/xmas/repo1/add?lists=1&wishes=10000");
		} catch(Exception e) {
			fail("Exception! " + e.toString());
		}
	}
	
}

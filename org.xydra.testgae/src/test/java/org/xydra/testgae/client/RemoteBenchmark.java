package org.xydra.testgae.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.junit.Before;
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
	protected String dataUrl;
	
	@Before
	public void setupBenchmark() {
		
	}
	
	@Ignore
	@Test
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
	
	@Ignore
	@Test
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
	
	@Ignore
	@Test
	public void testAddTooManyWishes() {
		try {
			assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl
			        + "/xmas/repo1/add?lists=1&wishes=10"));
			
			assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl
			        + "/xmas/repo1/add?lists=1&wishes=1000"));
		} catch(Exception e) {
			fail("Exception! " + e.toString());
		}
	}
	
	public static final int RUNS = 1000;
	
	@Test
	public void testBenchmarkAddingOneWish() {
		List<Long> times = new LinkedList<Long>();
		
		String listStr = addList("/repo1");
		
		for(int i = 0; i < RUNS; i++) {
			if(i % (RUNS / 10) == 0) {
				System.out.println("Adding " + i + "th wish.");
			}
			
			long time = System.currentTimeMillis();
			assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes=1"));
			time = System.currentTimeMillis() - time;
			
			times.add(time);
			
			assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/clear"));
		}
		
		double avgTime = 0l;
		for(long time : times) {
			avgTime += time;
		}
		
		avgTime = avgTime / RUNS;
		
		System.out.println("Average time (in ms) to add one wish: " + avgTime);
	}
	
	@Test
	public void testBenchmarkDeletingOneWish() {
		List<Long> times = new LinkedList<Long>();
		
		String listStr = addList("/repo1");
		
		for(int i = 0; i < 1000; i++) {
			if(i % 100 == 0) {
				System.out.println("Deleting " + i + "th wish.");
			}
			
			String wishStr = addWishToEmptyList(this.absoluteUrl + listStr);
			
			long time = System.currentTimeMillis();
			assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete"));
			time = System.currentTimeMillis() - time;
			
			times.add(time);
			
		}
		
		double avgTime = 0l;
		for(long time : times) {
			avgTime += time;
		}
		
		avgTime = avgTime / 1000;
		
		System.out.println("Average time (in ms) to delete one wish: " + avgTime);
	}
	
	// Helper Methods
	private String addList(String repoIdStr) {
		String response = HttpUtils.getRequestAsStringResponse(this.absoluteUrl + "/xmas"
		        + repoIdStr + "/add?lists=1&wishes=0");
		
		String[] lines = response.split("\n");
		
		String list = null;
		boolean found = false;
		for(int i = 0; i < lines.length && !found; i++) {
			if(lines[i].startsWith("<a href=\"/xmas/")) {
				String[] contents = lines[i].split("\"");
				list = contents[1];
				
				found = true;
			}
		}
		assertNotNull(list);
		assert list != null;
		assertTrue(list.startsWith("/xmas/repo1/"));
		
		return list;
	}
	
	private String addWishToEmptyList(String listUrlStr) {
		assertTrue(HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1"));
		
		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls");
		
		String[] lines = response.split("\n");
		
		return lines[0];
	}
}

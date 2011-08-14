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
import org.xydra.testgae.Operation;
import org.xydra.testgae.OperationWorker;
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
	
	@Test
	public void testAddTooManyWishes() {
		assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + "/xmas/repo1/add?lists=1&wishes=10"));
		
		assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl
		        + "/xmas/repo1/add?lists=1&wishes=1000"));
	}
	
	@Ignore
	@Test
	public void testBenchmarkAddingOneWish() {
		
		// Multiple Threads
		
		int numberOfWorkers = 10;
		int operationCount = 100;
		
		OperationWorker[] workers = new OperationWorker[numberOfWorkers];
		
		System.out.println("Starting threads");
		for(int i = 0; i < workers.length; i++) {
			String listStr = addList("/repo1");
			workers[i] = new OperationWorker(operationCount, new AddOperation(this.absoluteUrl
			        + listStr));
			
			// System.out.println("Starting thread " + i + ".");
			workers[i].start();
		}
		
		boolean workersFinished = false;
		
		do {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ie) {
				// do nothing
			}
			
			workersFinished = true;
			for(int i = 0; i < workers.length; i++) {
				workersFinished &= !workers[i].isAlive();
			}
			
		} while(!workersFinished);
		
		double threadsAvgTime = 0l;
		int threadsAddExceptions = 0;
		int threadsClearExceptions = 0;
		for(int i = 0; i < workers.length; i++) {
			AddOperation operation = (AddOperation)workers[i].getOperation();
			threadsAvgTime += operation.getTimesSum();
			threadsAddExceptions += operation.getAddExceptions();
			threadsClearExceptions += operation.getClearExceptions();
		}
		
		int threadsSuccessfulOperations = (numberOfWorkers * operationCount) - threadsAddExceptions;
		
		threadsAvgTime = threadsAvgTime / threadsSuccessfulOperations;
		
		// Single Thread
		
		// Results
		
		System.out.println(" -------- Multiple Threads ---------");
		System.out.println("Added " + threadsSuccessfulOperations
		        + " wishes sequentially. Average time (in ms) to add one wish: " + threadsAvgTime);
		System.out.println("Number of exceptions while adding wishes: " + threadsAddExceptions);
		System.out.println("Number of exceptions while clearing the list: "
		        + threadsClearExceptions);
	}
	
	@Ignore
	@Test
	public void testBenchmarkAddingOneWishOneThread() {
		String listStr = addList("/repo1");
		int addExceptions = 0;
		int clearExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < 1000; i++) {
			try {
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes=1"));
				time = System.currentTimeMillis() - time;
				
				System.out.println(time);
				
				avgTime += time;
				counter++;
			} catch(Exception e) {
				addExceptions++;
			}
			
			try {
				assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/clear"));
			} catch(Exception e) {
				clearExceptions++;
			}
		}
		
		avgTime = avgTime / (1000 - addExceptions);
		
		System.out.println(" -------- Single Thread ---------");
		System.out.println("Added " + counter
		        + " wishes sequentially. Average time (in ms) to add one wish: " + avgTime);
		System.out.println("Number of exceptions while adding wishes: " + addExceptions);
		System.out.println("Number of exceptions while clearing the list: " + clearExceptions);
	}
	
	@Ignore
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
	
	private class AddOperation implements Operation {
		private String listUrl;
		private int addExceptions;
		private int clearExceptions;
		private List<Long> times;
		
		public AddOperation(String listUrl) {
			this.listUrl = listUrl;
			this.times = new LinkedList<Long>();
		}
		
		public void doOperation() {
			try {
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.listUrl + "/add?wishes=1"));
				time = System.currentTimeMillis() - time;
				
				System.out.println(time);
				
				this.times.add(time);
			} catch(Exception e) {
				this.addExceptions++;
			}
			
			try {
				assertTrue(HttpUtils.makeGetRequest(this.listUrl + "/clear"));
			} catch(Exception e) {
				this.clearExceptions++;
			}
		}
		
		public long getTimesSum() {
			long result = 0l;
			for(long time : this.times) {
				result += time;
			}
			return result;
		}
		
		public int getAddExceptions() {
			return this.addExceptions;
		}
		
		public int getClearExceptions() {
			return this.clearExceptions;
		}
	}
}

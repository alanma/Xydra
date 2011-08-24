package org.xydra.testgae.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
	protected String path;
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	final static String lineSeparator = System.getProperty("line.separator");
	
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
	
	@Test
	public void testBenchmarkAddingOneWish() {
		this.benchmarkOperation(OperationEnum.ADD, 10, 50, this.path + "testBenchmarkAddingOneWish"
		        + ".txt");
	}
	
	@Test
	public void testBenchmarkDeletingOneWish() {
		this.benchmarkOperation(OperationEnum.DELETE, 10, 1, this.path
		        + "testBenchmarkAddingOneWish" + ".txt");
	}
	
	private enum OperationEnum {
		ADD, DELETE;
	}
	
	public void benchmarkOperation(OperationEnum operation, int numberOfThreads,
	        int operationsPerThread, String filePath) {
		
		OperationWorker[] workers = new OperationWorker[numberOfThreads];
		
		log.info("Starting threads");
		for(int i = 0; i < workers.length; i++) {
			String listStr = addList("/repo1");
			
			switch(operation) {
			case ADD:
				workers[i] = new OperationWorker(operationsPerThread, new AddWishOperation(
				        this.absoluteUrl + listStr));
				break;
			case DELETE:
				workers[i] = new OperationWorker(operationsPerThread, new DeleteWishOperation(
				        this.absoluteUrl + listStr));
				break;
			}
			
			log.info("Starting thread " + i + ".");
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
		int threadsOperationExceptions = 0;
		int threadsOtherExceptions = 0;
		for(int i = 0; i < workers.length; i++) {
			Operation tempOp = workers[i].getOperation();
			threadsAvgTime += tempOp.getTimesSum();
			threadsOperationExceptions += tempOp.getOperationExceptions();
			threadsOtherExceptions += tempOp.getOtherExceptions();
		}
		
		int threadsSuccessfulOperations = (numberOfThreads * operationsPerThread)
		        - threadsOperationExceptions;
		
		threadsAvgTime = threadsAvgTime / threadsSuccessfulOperations;
		
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
			
			out.write("#Threads:, " + numberOfThreads + ", ");
			out.write("#Operations:, " + operationsPerThread + ", ");
			out.write("#Successful Operations:, " + threadsSuccessfulOperations + ", ");
			out.write("Average Time (ms):, " + threadsAvgTime + ", ");
			out.write("#Operation Exceptions:, " + threadsOperationExceptions + ", ");
			out.write("#Other Exceptions:, " + threadsOtherExceptions);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBenchmarkAddingOneWishOneThread() {
		addingWishesOneThreadInTransaction(1, 10000, "BenchmarkAddingOneWishOneThread.txt");
	}
	
	@Test
	public void testCompareAddingOneWishTransactionAndSequential() {
		for(int i = 1; i < 100; i += 50) {
			addingWishesOneThreadInTransaction(i, 1000, "CompareAddingOneWishInTransaction.txt");
			addingWishesOneThreadInTransaction(i, 1000, "CompareAddingOneWishSequential.txt");
		}
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, String filePath) {
		String listStr = addList("/repo1");
		
		int addExceptions = 0;
		int clearExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes="
				        + wishes));
				time = System.currentTimeMillis() - time;
				
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
		
		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;
		
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.path
			        + "BenchmarkAddingOneWishOneThread.txt", true));
			out.write("#Operations:, " + operations + ", ");
			out.write("#Successful Operations:, " + successfulOperations + ", ");
			out.write("Average Time (ms):, " + avgTime + ", ");
			out.write("#Operation Exceptions:, " + addExceptions + ", ");
			out.write("#Other Exceptions:, " + clearExceptions);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addingWishesOneThreadSequentially(int wishes, int operations, String filePath) {
		String listStr = addList("/repo1");
		
		int addExceptions = 0;
		int clearExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				long time = System.currentTimeMillis();
				for(int j = 0; i < wishes; j++) {
					assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + listStr
					        + "/add?wishes=1"));
				}
				
				time = System.currentTimeMillis() - time;
				
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
		
		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;
		
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.path
			        + "BenchmarkAddingOneWishOneThread.txt", true));
			out.write("#Operations:, " + operations + ", ");
			out.write("#Successful Operations:, " + successfulOperations + ", ");
			out.write("Average Time (ms):, " + avgTime + ", ");
			out.write("#Operation Exceptions:, " + addExceptions + ", ");
			out.write("#Other Exceptions:, " + clearExceptions);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// ----------------- Helper Methods ------------------------
	
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
	
	private class AddWishOperation implements Operation {
		private String listUrl;
		private int addExceptions;
		private int clearExceptions;
		private long timesSum;
		
		public AddWishOperation(String listUrl) {
			this.listUrl = listUrl;
		}
		
		public void doOperation() {
			try {
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.listUrl + "/add?wishes=1"));
				time = System.currentTimeMillis() - time;
				
				this.timesSum += time;
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
			return this.timesSum;
		}
		
		public int getOperationExceptions() {
			return this.addExceptions;
		}
		
		public int getOtherExceptions() {
			return this.clearExceptions;
		}
	}
	
	private class DeleteWishOperation implements Operation {
		private String listUrl;
		private int deleteExceptions;
		private int clearExceptions;
		private long timesSum;
		
		public DeleteWishOperation(String listUrl) {
			this.listUrl = listUrl;
		}
		
		public void doOperation() {
			try {
				// add wish
				String wishStr = addWishToEmptyList(this.listUrl);
				
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.listUrl + wishStr + "/delete"));
				time = System.currentTimeMillis() - time;
				
				this.timesSum += time;
			} catch(Exception e) {
				this.deleteExceptions++;
			}
			
			try {
				assertTrue(HttpUtils.makeGetRequest(this.listUrl + "/clear"));
			} catch(Exception e) {
				this.clearExceptions++;
			}
		}
		
		public long getTimesSum() {
			return this.timesSum;
		}
		
		public int getOperationExceptions() {
			return this.deleteExceptions;
		}
		
		public int getOtherExceptions() {
			return this.clearExceptions;
		}
	}
}

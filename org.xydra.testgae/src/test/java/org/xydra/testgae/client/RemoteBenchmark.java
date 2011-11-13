package org.xydra.testgae.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.testgae.shared.HttpUtils;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */
public abstract class RemoteBenchmark {
	protected String absoluteUrl;
	protected String path;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	final static String lineSeparator = System.getProperty("line.separator");
	
	@Test
	public void testBenchmarkAddingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			addingWishesOneThreadInTransaction(1, 1, "BenchmarkAddingOneWishOneThread.txt");
		}
	}
	
	@Test
	public void testBenchmarkDeletingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			deletingWishesOneThreadInTransaction(1, this.path
			        + "BenchmarkDeletingOneWishOneThread.txt");
		}
	}
	
	@Test
	public void testBenchmarkEditingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			editingWishesOneThreadInTransaction(1, 0, this.path
			        + "BenchmarkEditingOneWishOneThread.txt");
		}
	}
	
	@Test
	public void testCompareAddingMultipleWishesInTransaction() {
		for(int i = 0; i < 100; i++) {
			for(int X = 10; X <= 80; X *= 2) {
				addingWishesOneThreadInTransaction(X, 1, 0, this.path
				        + "AddingMultipleWishesInTransaction" + X + ".txt");
			}
			
			for(int X = 8; X <= 1024; X *= 2) {
				addingWishesOneThreadInTransaction(X, 1, 0, this.path
				        + "AddingMultipleWishesInTransaction" + X + ".txt");
			}
		}
	}
	
	@Test
	public void testAddingWishesInTransactionWithInitialWishes() {
		for(int i = 0; i < 100; i++) {
			for(int X = 10; X <= 80; X *= 2) {
				addingWishesOneThreadInTransaction(10, 1, X, this.path
				        + "AddingWishesInTransactionWithInitialWishes" + X + ".txt");
			}
			
			for(int X = 8; X <= 1024; X *= 2) {
				addingWishesOneThreadInTransaction(10, 1, X, this.path
				        + "AddingWishesInTransactionWithInitialWishes" + X + ".txt");
			}
		}
	}
	
	@Test
	public void testBenchmarkEditingOneWishOneThreadWithInitialWishes() {
		for(int i = 0; i < 100; i++) {
			for(int X = 10; X <= 80; X *= 2) {
				editingWishesOneThreadInTransaction(1, X, this.path
				        + "EditingOneWishInTransactionWithInitialWishes" + X + ".txt");
			}
			
			for(int X = 8; X <= 1024; X *= 2) {
				editingWishesOneThreadInTransaction(1, X, this.path
				        + "EditingOneWishInTransactionWithInitialWishes" + X + ".txt");
			}
		}
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, String filePath) {
		addingWishesOneThreadInTransaction(wishes, operations, 0, filePath);
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, int initialWishes,
	        String filePath) {
		String listStr = addList("/repo1", initialWishes);
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList("/repo1", initialWishes);
		}
		
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
		outputResults(filePath, initialWishes, operations, wishes, successfulOperations, avgTime,
		        addExceptions, clearExceptions);
		
	}
	
	public void addingWishesOneThreadSequentially(int wishes, int operations, String filePath) {
		String listStr = addList("/repo1");
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList("/repo1");
		}
		
		assertNotNull(listStr);
		
		int addExceptions = 0;
		int clearExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				long time = System.currentTimeMillis();
				for(int j = 0; j < wishes; j++) {
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
		outputResults(filePath, 0, operations, wishes, successfulOperations, avgTime,
		        addExceptions, clearExceptions);
	}
	
	public void deletingWishesOneThreadInTransaction(int operations, String filePath) {
		String listStr = addList("/repo1");
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList("/repo1");
		}
		
		int addExceptions = 0;
		int clearExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				String wishStr = addWishToEmptyList(this.absoluteUrl + listStr);
				
				long time = System.currentTimeMillis();
				assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete"));
				time = System.currentTimeMillis() - time;
				
				avgTime += time;
				counter++;
			} catch(Exception e) {
				addExceptions++;
			}
		}
		
		int successfulOperations = (operations - addExceptions);
		if(successfulOperations == 0) {
			fail();
		}
		avgTime = avgTime / successfulOperations;
		
		// Output Results in a simple CSV format
		outputResults(filePath, 0, operations, 1, successfulOperations, avgTime, addExceptions,
		        clearExceptions);
		
	}
	
	public void editingWishesOneThreadInTransaction(int operations, int initialWishes,
	        String filePath) {
		String listStr = addList("/repo1", initialWishes);
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList("/repo1", initialWishes);
		}
		
		double avgTime;
		
		do {
			
			int addExceptions = 0;
			int clearExceptions = 0;
			int counter = 0;
			
			avgTime = 0;
			for(int i = 0; i < operations; i++) {
				try {
					String wishStr = addWish(this.absoluteUrl + listStr);
					
					long time = System.currentTimeMillis();
					assertTrue(HttpUtils.makeGetRequest(this.absoluteUrl + wishStr
					        + "/editName?name=performanceTest"));
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
			
			if(!Double.isNaN(avgTime)) {
				// Output Results in a simple CSV format
				outputResults(filePath, initialWishes, operations, 0, successfulOperations,
				        avgTime, addExceptions, clearExceptions);
			} else {
				System.out.println("Was NaN - Starting over...");
			}
		} while(Double.isNaN(avgTime));
		
	}
	
	// ----------------- Helper Methods ------------------------
	
	private void outputResults(String filePath, int initialWishes, int operations, int wishes,
	        int successfulOps, double avgTime, int opExceps, int otherExceps) {
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true));
			out.write("#Initial Wishes:, " + initialWishes + ", ");
			out.write("#Operations:, " + operations + ", ");
			out.write("#Wishes per Op.:, " + wishes + ", ");
			out.write("#Successful Operations:, " + successfulOps + ", ");
			out.write("Average Time (ms):, " + avgTime + ", ");
			out.write("#Operation Exceptions:, " + opExceps + ", ");
			out.write("#Other Exceptions:, " + otherExceps);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private String addList(String repoIdStr) {
		return addList(repoIdStr, 0);
	}
	
	private String addList(String repoIdStr, int initialWishes) {
		String response = null;
		try {
			response = HttpUtils.getRequestAsStringResponse(this.absoluteUrl + "/xmas" + repoIdStr
			        + "/add?lists=1&wishes=" + initialWishes);
		} catch(Exception e) {
			return null;
		}
		assertNotNull(response);
		
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
	
	private String addWish(String listUrlStr) {
		assertTrue(HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1"));
		
		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls");
		
		String[] lines = response.split("\n");
		
		return lines[lines.length - 1];
	}
}

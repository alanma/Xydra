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
	protected String currentRepo;
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	final static String lineSeparator = System.getProperty("line.separator");
	
	@Test
	public void testBenchmarkAddingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			addingWishesOneThreadInTransaction(1, 1, "AddingOneWishOneThread", i);
		}
	}
	
	@Test
	public void testBenchmarkDeletingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			deletingWishesOneThreadInTransaction(1, 0, "DeletingOneWishOneThread", i);
		}
	}
	
	@Test
	public void testBenchmarkEditingOneWishOneThread() {
		for(int i = 0; i < 100; i++) {
			editingWishesOneThreadInTransaction(1, 0, "EditingOneWishOneThread", i);
		}
	}
	
	@Test
	public void testAddingMultipleWishesInTransaction() {
		for(int i = 0; i < 100; i++) {
			System.out.println("Iteration: " + i);
			for(int X = 10; X <= 80; X *= 2) {
				System.out.println("X = " + X);
				addingWishesOneThreadInTransaction(X, 1, 0,
				        "AddingMultipleWishesInTransaction" + X, i);
			}
			
			for(int X = 8; X <= 256; X *= 2) {
				System.out.println("X = " + X);
				addingWishesOneThreadInTransaction(X, 1, 0,
				        "AddingMultipleWishesInTransaction" + X, i);
			}
		}
	}
	
	@Test
	public void testAddingWishesInTransactionWithInitialWishes() {
		for(int i = 0; i < 100; i++) {
			System.out.println("Iteration: " + i);
			for(int X = 10; X <= 80; X *= 2) {
				System.out.println("X = " + X);
				addingWishesOneThreadInTransaction(10, 1, X,
				        "AddingWishesInTransactionWithInitialWishes" + X, i);
			}
			
			// TODO Check 128,256, 512 and 1024 initial wishes again... seems
			// like
			// that's
			// too much!
			for(int X = 8; X < 128; X *= 2) {
				System.out.println("X = " + X);
				addingWishesOneThreadInTransaction(10, 1, X,
				        "AddingWishesInTransactionWithInitialWishes" + X, i);
			}
		}
	}
	
	@Test
	public void testBenchmarkEditingOneWishOneThreadWithInitialWishes() {
		for(int i = 0; i < 100; i++) {
			System.out.println("Iteration: " + i);
			for(int X = 10; X <= 80; X *= 2) {
				System.out.println("X = " + X);
				editingWishesOneThreadInTransaction(1, X,
				        "EditingOneWishInTransactionWithInitialWishes" + X, i);
			}
			
			for(int X = 8; X <= 1024; X *= 2) {
				System.out.println("X = " + X);
				editingWishesOneThreadInTransaction(1, X,
				        "EditingOneWishInTransactionWithInitialWishes" + X, i);
			}
		}
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, String filePath,
	        int iteration) {
		addingWishesOneThreadInTransaction(wishes, operations, 0, filePath, iteration);
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, int initialWishes,
	        String filePath, int iteration) {
		this.currentRepo = "/repo" + System.currentTimeMillis();
		
		String listStr = addList(this.currentRepo, initialWishes);
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo, initialWishes);
		}
		
		int addExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				long time = 0l;
				boolean succGet = false;
				
				while(!succGet) {
					
					time = System.currentTimeMillis();
					succGet = (HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes="
					        + wishes));
					
					if(!succGet) {
						System.out.println("addingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while adding " + wishes + " wishes");
						
						this.outputCriticalErrors(filePath, iteration, initialWishes, wishes);
						this.wait(100);
					} else {
						time = System.currentTimeMillis() - time;
					}
				}
				
				avgTime += time;
				counter++;
			} catch(Exception e) {
				addExceptions++;
			}
		}
		
		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;
		
		// Output Results in a simple CSV format
		outputResults(filePath, initialWishes, operations, wishes, successfulOperations, avgTime,
		        addExceptions);
		
	}
	
	public void deletingWishesOneThreadInTransaction(int operations, int initialWishes,
	        String filePath, int iteration) {
		// TODO implement initial wishes mechanic
		this.currentRepo = "/repo" + System.currentTimeMillis();
		String listStr = addList(this.currentRepo);
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo);
		}
		
		int addExceptions = 0;
		int counter = 0;
		
		double avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				String wishStr = addWishToEmptyList(this.absoluteUrl + listStr);
				
				long time = 0l;
				boolean succGet = false;
				
				while(!succGet) {
					
					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete");
					
					if(!succGet) {
						System.out.println("deletingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while deleting 1 wish");
						this.outputCriticalErrors(filePath, iteration, initialWishes, 1);
						this.wait(100);
					} else {
						time = System.currentTimeMillis() - time;
					}
				}
				
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
		outputResults(filePath, 0, operations, 1, successfulOperations, avgTime, addExceptions);
		
	}
	
	public void editingWishesOneThreadInTransaction(int operations, int initialWishes,
	        String filePath, int iteration) {
		this.currentRepo = "/repo" + System.currentTimeMillis();
		String listStr = addList(this.currentRepo, initialWishes);
		
		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for(int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo, initialWishes);
		}
		
		double avgTime;
		
		int addExceptions = 0;
		int counter = 0;
		
		avgTime = 0;
		for(int i = 0; i < operations; i++) {
			try {
				String wishStr = addWish(this.absoluteUrl + listStr);
				
				long time = 0l;
				boolean succGet = false;
				
				while(!succGet) {
					
					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr
					        + "/editName?name=performanceTest");
					
					if(!succGet) {
						System.out.println("editingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while editing 1 wish");
						this.outputCriticalErrors(filePath, iteration, initialWishes, 1);
						this.wait(100);
					} else {
						time = System.currentTimeMillis() - time;
					}
				}
				
				avgTime += time;
				counter++;
			} catch(Exception e) {
				addExceptions++;
			}
		}
		
		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;
		
		// Output Results in a simple CSV format
		outputResults(filePath, initialWishes, operations, 0, successfulOperations, avgTime,
		        addExceptions);
		
	}
	
	// ----------------- Helper Methods ------------------------
	
	private void outputResults(String filePath, int initialWishes, int operations, int wishes,
	        int successfulOps, double avgTime, int opExceps) {
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.path + filePath + ".txt",
			        true));
			out.write("#Initial Wishes:, " + initialWishes + ", ");
			out.write("#Operations:, " + operations + ", ");
			out.write("#Wishes per Op.:, " + wishes + ", ");
			out.write("#Successful Operations:, " + successfulOps + ", ");
			out.write("Average Time (ms):, " + avgTime + ", ");
			out.write("#Operation Exceptions:, " + opExceps + ", ");
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void outputCriticalErrors(String filePath, int iteration, int initialWishes, int wishes) {
		// Output Results in a simple CSV format
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(this.path + filePath
			        + "CriticalErrors.txt", true));
			out.write("#Initial Wishes:, " + initialWishes + ", ");
			out.write("#Wishes per Op.:, " + wishes + ", ");
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
		assertTrue(list.startsWith("/xmas" + this.currentRepo + "/"));
		
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

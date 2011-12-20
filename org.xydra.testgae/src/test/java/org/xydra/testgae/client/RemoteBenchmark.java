package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.testgae.shared.HttpUtils;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * 
 */

/**
 * TODO there's a lot of copied code -> refactor
 * 
 * TODO comment
 */
public abstract class RemoteBenchmark {
	protected String absoluteUrl;
	protected String path;
	protected String currentRepo;
	protected int iterations;
	protected int maxAmount;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	final static String lineSeparator = System.getProperty("line.separator");
	
	public void testBenchmarkAddingOneWishOneThread() {
		String fileName = "AddingOneWishOneThread";
		
		int amount = this.getAmountOfMeasuredData(fileName);
		
		for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			addingWishesOneThreadInTransaction(1, 1, fileName, i);
		}
	}
	
	public void testBenchmarkDeletingOneWishOneThread() {
		String fileName = "DeletingOneWishOneThread";
		
		int amount = this.getAmountOfMeasuredData(fileName);
		
		for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			deletingWishesOneThreadInTransaction(1, 0, fileName, i);
		}
	}
	
	public void testBenchmarkEditingOneWishOneThread() {
		String fileName = "EditingOneWishOneThread";
		
		int amount = this.getAmountOfMeasuredData(fileName);
		
		for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			editingWishesOneThreadInTransaction(1, 0, "EditingOneWishOneThread", i);
		}
	}
	
	public void testAddingMultipleWishesInTransaction() {
		String fileName = "AddingMultipleWishesInTransaction";
		
		for(int X = 10; X <= 80; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
		
		for(int X = 8; X <= 1024; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
	}
	
	public void testAddingWishesInTransactionWithInitialWishes() {
		String fileName = "AddingWishesInTransactionWithInitialWishes";
		
		for(int X = 10; X <= 80; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
		
		for(int X = 8; X <= 1024; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
	}
	
	public void testBenchmarkEditingOneWishOneThreadWithInitialWishes() {
		String fileName = "EditingOneWishInTransactionWithInitialWishes";
		
		for(int X = 10; X <= 80; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
		
		for(int X = 8; X <= 1024; X *= 2) {
			
			int amount = this.getAmountOfMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
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
					time = System.currentTimeMillis() - time;
					
					if(!succGet) {
						System.out.println("addingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while adding " + wishes + " wishes");
						
						this.outputResults(filePath, initialWishes, operations, initialWishes, 0,
						        Double.NaN, 1);
						this.outputCriticalErrors(filePath, iteration, initialWishes, wishes);
						return;
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
				
				if(wishStr == null) {
					System.out.println("deletingWishes: Failed addWish-GET-Request at iteration "
					        + iteration + ", " + initialWishes
					        + " initial wishes in DeleteWishes-Test");
					this.outputResults(filePath, 0, operations, 1, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath, iteration, initialWishes, 1);
					return;
				}
				
				long time = 0l;
				boolean succGet = false;
				
				while(!succGet) {
					
					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete");
					time = System.currentTimeMillis() - time;
					
					if(!succGet) {
						System.out.println("deletingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while deleting 1 wish");
						this.outputResults(filePath, 0, operations, 1, 0, Double.NaN, 1);
						this.outputCriticalErrors(filePath, iteration, initialWishes, 1);
						return;
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
				
				if(wishStr == null) {
					System.out
					        .println("editingWishes: Failed addWish-GET-Request at iteration "
					                + iteration + ", " + initialWishes
					                + " initial wishes in EditWish-Test");
					this.outputResults(filePath, initialWishes, operations, 0, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath + initialWishes, iteration, initialWishes, 1);
					return;
				}
				
				long time = 0l;
				boolean succGet = false;
				
				while(!succGet) {
					
					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr
					        + "/editName?name=performanceTest");
					time = System.currentTimeMillis() - time;
					
					if(!succGet) {
						System.out.println("editingWishes: Failed GET-Request at iteration "
						        + iteration + ", " + initialWishes
						        + " initial wishes while editing 1 wish");
						this.outputResults(filePath, initialWishes, operations, 0, 0, Double.NaN, 1);
						this.outputCriticalErrors(filePath + initialWishes, iteration,
						        initialWishes, 1);
						return;
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
			File f = new File(this.path + filePath + ".txt");
			if(!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
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
	
	private int getAmountOfMeasuredData(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(this.path + filePath + ".txt"));
			
			String currentLine = in.readLine();
			int count = 0;
			while(currentLine != null) {
				count++;
				currentLine = in.readLine();
			}
			
			return count;
			
		} catch(IOException e) {
			// no data measured
			return 0;
		}
		
	}
	
	private void outputCriticalErrors(String filePath, int iteration, int initialWishes, int wishes) {
		// Output Results in a simple CSV format
		try {
			File f = new File(this.path + filePath + "CriticalErrors.txt");
			if(!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
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
		assert response != null;
		
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
		assert list != null;
		assert list.startsWith("/xmas" + this.currentRepo + "/");
		
		return list;
	}
	
	private String addWishToEmptyList(String listUrlStr) {
		boolean succ = HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1");
		if(!succ) {
			return null;
		}
		
		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls");
		if(response == null) {
			return null;
		}
		
		String[] lines = response.split("\n");
		
		return lines[0];
	}
	
	private String addWish(String listUrlStr) {
		boolean succ = HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1");
		if(!succ) {
			return null;
		}
		
		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls");
		if(response == null) {
			return null;
		}
		
		String[] lines = response.split("\n");
		
		return lines[lines.length - 1];
	}
}

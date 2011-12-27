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
import org.xydra.testgae.shared.Operations;


/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * @author Kaidel
 * 
 */

/**
 * TODO there's a lot of copied code -> refactor
 * 
 * TODO comment
 * 
 */
public abstract class RemoteBenchmark {
	
	/**
	 * url of the instance which is to be tested (i.e. the link to the GAE
	 * version)
	 */
	protected String absoluteUrl;
	
	/**
	 * filepath were measured data is to be saved
	 */
	protected String path;
	
	/**
	 * currently used repository
	 */
	protected String currentRepo;
	
	/**
	 * number of benchmark iterations
	 */
	protected int iterations;
	
	/**
	 * maximum allowed amount of data to measure. Considers the already measured
	 * & stored data. For example, if one benchmark already has n datasets saved
	 * and the given maxAmount is 2*n, no more than n additional datasets will
	 * be measured, even if the {@link RemoteBenchmark#iterations} is greater
	 * than n.
	 */
	protected int maxAmount;
	
	/**
	 * Some benchmarks need additional values X to determine how many initial
	 * wishes should be in the lists they're using or how many wishes they add
	 * etc. - see the specific benchmarks for more information. This parameter
	 * contains all values X which are to be used.
	 */
	protected Integer[] range;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	final static String lineSeparator = System.getProperty("line.separator");
	
	public RemoteBenchmark(String absoluteUrl, String path, int iterations, int maxAmount,
	        Integer[] range) {
		this.absoluteUrl = absoluteUrl;
		this.path = path;
		this.iterations = iterations;
		this.maxAmount = maxAmount;
		this.range = range;
	}
	
	// - Benchmarks -
	
	/**
	 * Benchmarks adding one single wish to an empty list.
	 */
	public void benchmarkAddingOneWishOneThread() {
		runSingleOperationBenchmark(Operations.ADD, "AddingOneWishOneThread");
	}
	
	/**
	 * Benchmarks deleting one single wish from a list with exactly one wish.
	 */
	public void benchmarkDeletingOneWishOneThread() {
		runSingleOperationBenchmark(Operations.DELETE, "DeletingOneWishOneThread");
	}
	
	/**
	 * Benchmarks editing one wish single wish in a list with exactly one wish.
	 */
	public void benchmarkEditingOneWishOneThread() {
		runSingleOperationBenchmark(Operations.EDIT, "EditingOneWishOneThread");
	}
	
	/**
	 * Benchmarks adding X wishes to an empty list in a transaction for all
	 * values given in the 'range'-parameter of the constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkAddingMultipleWishesInTransaction() {
		String fileName = "AddingMultipleWishesInTransaction";
		
		for(int X : this.range) {
			
			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesOneThreadInTransaction(X, 1, 0, fileName + X, i);
			}
		}
	}
	
	/**
	 * Benchmarks adding one wish in a transaction to a list with X initial
	 * wishes for all values X given in the 'range'-parameter of the constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkAddingWishesInTransactionWithInitialWishes() {
		String fileName = "AddingWishesInTransactionWithInitialWishes";
		this.runSingleOperationWithInitialWishesBenchmark(Operations.ADD, fileName);
	}
	
	/**
	 * Benchmarks editing one wish in a list with X initial wishes in a
	 * transaction for all values X given in the 'range'-parameter of the
	 * constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkEditingOneWishInTransactionWithInitialWishes() {
		String fileName = "EditingOneWishInTransactionWithInitialWishes";
		this.runSingleOperationWithInitialWishesBenchmark(Operations.EDIT, fileName);
	}
	
	// - Benchmark Execution Code -
	
	/**
	 * Used to run the "single operation" type benchmarks, for example the "add
	 * one single wish to an empty list" benchmark
	 * {@link RemoteBenchmark#benchmarkAddingWishesInTransactionWithInitialWishes()}
	 * 
	 * @param operation the operation which "single operation"-type benchmark is
	 *            to be executed
	 * @param fileName the name of the file in which the measured data is to be
	 *            saved
	 */
	private void runSingleOperationBenchmark(Operations operation, String fileName) {
		int amount = this.getAmountOfAlreadyMeasuredData(fileName);
		
		for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			switch(operation) {
			case ADD:
				addingWishesOneThreadInTransaction(1, 1, fileName, i);
				break;
			
			case DELETE:
				deletingWishesOneThreadInTransaction(1, 0, fileName, i);
				break;
			case EDIT:
				editingWishesOneThreadInTransaction(1, 0, fileName, i);
				break;
			}
		}
	}
	
	/**
	 * Used to run the "with initial wishes"-type benchmarks, for example the
	 * "add one single wish to a list with X initial wishes" benchmark
	 * {@link RemoteBenchmark#benchmarkAddingWishesInTransactionWithInitialWishes()}
	 * 
	 * @param operation the operation which "with initial wishes"-type benchmark
	 *            is to be executed
	 * @param fileName the name of the file in which the measured data is to be
	 *            saved
	 */
	private void runSingleOperationWithInitialWishesBenchmark(Operations operation, String fileName) {
		for(int X : this.range) {
			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);
			
			for(int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				switch(operation) {
				case ADD:
					addingWishesOneThreadInTransaction(10, 1, X, fileName + X, i);
					break;
				case DELETE:
					break; // no such benchmark implemented at this time
				case EDIT:
					editingWishesOneThreadInTransaction(1, X, fileName + X, i);
					break;
				}
			}
		}
	}
	
	public void addingWishesOneThreadInTransaction(int wishes, int operations, String filePath,
	        int iteration) {
		addingWishesOneThreadInTransaction(wishes, operations, 0, filePath, iteration);
	}
	
	// TODO Maybe the following three benchmarks can be merged to one generic
	// benchmark?
	
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
				
				time = System.currentTimeMillis();
				succGet = (HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes="
				        + wishes));
				time = System.currentTimeMillis() - time;
				
				if(!succGet) {
					System.out.println("addingWishes: Failed GET-Request at iteration " + iteration
					        + ", " + initialWishes + " initial wishes while adding " + wishes
					        + " wishes");
					
					this.outputResults(filePath, initialWishes, operations, initialWishes, 0,
					        Double.NaN, 1);
					this.outputCriticalErrors(filePath, initialWishes, wishes);
					return;
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
					this.outputCriticalErrors(filePath, initialWishes, 1);
					return;
				}
				
				long time = 0l;
				boolean succGet = false;
				
				time = System.currentTimeMillis();
				succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete");
				time = System.currentTimeMillis() - time;
				
				if(!succGet) {
					System.out.println("deletingWishes: Failed GET-Request at iteration "
					        + iteration + ", " + initialWishes
					        + " initial wishes while deleting 1 wish");
					this.outputResults(filePath, 0, operations, 1, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath, initialWishes, 1);
					return;
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
					this.outputCriticalErrors(filePath + initialWishes, initialWishes, 1);
					return;
				}
				
				long time = 0l;
				boolean succGet = false;
				
				time = System.currentTimeMillis();
				succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr
				        + "/editName?name=performanceTest");
				time = System.currentTimeMillis() - time;
				
				if(!succGet) {
					System.out.println("editingWishes: Failed GET-Request at iteration "
					        + iteration + ", " + initialWishes
					        + " initial wishes while editing 1 wish");
					this.outputResults(filePath, initialWishes, operations, 0, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath + initialWishes, initialWishes, 1);
					return;
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
	
	/**
	 * Outputs the given data to a file at {@link RemoteBenchmark#path} /
	 * {@link outputResults#fileName}.txt.
	 * 
	 * The new data will be appended to the end of the file in the following
	 * CSV-type in a single new line:
	 * 
	 * #Initial Wishes:, {@link outputResults#initialWishes}, #Wishes per Op.:,
	 * {@link outputResults#wishes},#Successful Operations:,
	 * {@link outputResults#successfulOps}, Average Time (ms):,
	 * {@link outputResults#avgTime}, #Operation Exceptions:,
	 * {@link outputResults#opExceps}
	 * 
	 * 
	 * @param fileName the name of the file
	 */
	private void outputResults(String fileName, int initialWishes, int operations, int wishes,
	        int successfulOps, double avgTime, int opExceps) {
		// Output Results in a simple CSV format
		try {
			File f = new File(this.path + fileName + ".txt");
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
	
	/**
	 * Used to output to a special critical error file when such an error
	 * (usually a 505) occurs during one benchmark
	 * 
	 * Data will be appended to the file {@link RemoteBenchmark#path}/
	 * {@link outputCriticalErrors#fileName}.txt in the following CSV-type
	 * format in a new single line:
	 * 
	 * #Initial Wishes, {@link outputCriticalErrors#initialWishes}, #Wishes per
	 * Op., {@link outputCriticalErrors#wishes},
	 */
	private void outputCriticalErrors(String fileName, int initialWishes, int wishes) {
		// Output Results in a simple CSV format
		try {
			File f = new File(this.path + fileName + "CriticalErrors.txt");
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
	
	/**
	 * Measures the amount of data stored in the given file
	 * {@link RemoteBenchmark#range}/
	 * {@link getAmountOfAlreadyMeasuredData#fileName}.txt (corresponds with the
	 * amount of lines in the file for the output format used by these
	 * benchmarks)
	 * 
	 * @return the amount of already measured data
	 */
	private int getAmountOfAlreadyMeasuredData(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(this.path + fileName + ".txt"));
			
			String currentLine = in.readLine();
			int count = 0;
			while(currentLine != null) {
				count++;
				currentLine = in.readLine();
			}
			
			return count;
			
		} catch(IOException e) {
			// no data measured until now
			return 0;
		}
		
	}
	
	/**
	 * Adds a new empty list to the repository at
	 * {@link RemoteBenchmark#absoluteUrl}/xmas/{@link addList#repoIdStr} and
	 * returns its url.
	 * 
	 * @param repoIdStr The repository id
	 * @return the url of the repository or null if the operation fails
	 */
	private String addList(String repoIdStr) {
		return addList(repoIdStr, 0);
	}
	
	/**
	 * Adds a new list with the given amount of wishes to the repository at
	 * {@link RemoteBenchmark#absoluteUrl}/xmas/{@link addList#repoIdStr} and
	 * returns its url.
	 * 
	 * @param repoIdStr The repository id
	 * @param initialWishes amount of initial wishes for the new list
	 * @return the url of the repository or null if the operation fails
	 */
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
	
	/**
	 * Adds a new wish to the list with the given url and returns the url of the
	 * new wish.
	 * 
	 * @param listUrlStr the url of the list to which a new wish is to be added
	 * @return the url of the new wish. (returning the correct url only works
	 *         when the list was empty before executing this method, if the list
	 *         already contained some wishes the returned url may be the url of
	 *         another wish) Returns null if operations fails.
	 */
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

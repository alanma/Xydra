package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.testgae.shared.HttpUtils;
import org.xydra.testgae.shared.Operations;

/**
 * Benchmark test for a remote server
 * 
 * @author xamde
 * @author kaidel
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

	public static final int SINGLETHREADONLY = -1;

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
		runSingleOperationBenchmarkOneThread(Operations.ADD, "AddingOneWishOneThread");
	}

	public void benchmarkAddingOneWishMultipleThreads(int threads) {
		runSingleOperationBenchmarkMultipleThreads(Operations.ADD, "AddingOneWish" + threads
				+ "Threads", threads);
	}

	/**
	 * Benchmarks deleting one single wish from a list with exactly one wish.
	 */
	public void benchmarkDeletingOneWishOneThread() {
		runSingleOperationBenchmarkOneThread(Operations.DELETE, "DeletingOneWishOneThread");
	}

	public void benchmarkDeletingOneWishMultipleThreads(int threads) {
		runSingleOperationBenchmarkMultipleThreads(Operations.DELETE, "DeletingOneWish" + threads
				+ "Threads", threads);
	}

	/**
	 * Benchmarks editing one wish single wish in a list with exactly one wish.
	 */
	public void benchmarkEditingOneWishOneThread() {
		runSingleOperationBenchmarkOneThread(Operations.EDIT, "EditingOneWishOneThread");
	}

	public void benchmarkEditingOneWishMultipleThreads(int threads) {
		runSingleOperationBenchmarkMultipleThreads(Operations.EDIT, "EditingOneWish" + threads
				+ "Threads", threads);
	}

	/**
	 * Benchmarks adding X wishes to an empty list in a transaction for all
	 * values given in the 'range'-parameter of the constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkAddingMultipleWishesInTransactionOneThread() {
		String fileName = "AddingMultipleWishesInTransactionOneThread";

		for (int X : this.range) {

			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);

			for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesInTransactionOneThread(X, 1, 0, fileName + X, i);
			}
		}
	}

	public void benchmarkAddingMultipleWishesInTransactionMultipleThreads(int threads) {
		String fileName = "AddingMultipleWishesInTransaction" + threads + "Threads";

		for (int X : this.range) {

			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);

			for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				addingWishesInTransactionMultipleThreads(X, 1, 0, fileName + X, i, threads);
			}
		}
	}

	/**
	 * Benchmarks adding one wish in a transaction to a list with X initial
	 * wishes for all values X given in the 'range'-parameter of the constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkAddingWishesInTransactionWithInitialWishesOneThread() {
		String fileName = "AddingWishesInTransactionWithInitialWishesOneThread";
		this.runSingleOperationWithInitialWishesBenchmarkOneThread(Operations.ADD, fileName);
	}

	public void benchmarkAddingWishesInTransactionWithInitialWishesMultipleThreads(int threads) {
		String fileName = "AddingWishesInTransactionWithInitialWishes" + threads + "Threads";
		this.runSingleOperationWithInitialWishesBenchmarkMultipleThreads(Operations.ADD, fileName,
				threads);
	}

	/**
	 * Benchmarks editing one wish in a list with X initial wishes in a
	 * transaction for all values X given in the 'range'-parameter of the
	 * constructor
	 * {@link RemoteBenchmark#RemoteBenchmark(String, String, int, int, Integer[])}
	 */
	public void benchmarkEditingOneWishInTransactionWithInitialWishesOneThread() {
		String fileName = "EditingOneWishInTransactionWithInitialWishesOneThread";
		this.runSingleOperationWithInitialWishesBenchmarkOneThread(Operations.EDIT, fileName);
	}

	public void benchmarkEditingOneWishInTransactionWithInitialWishesMultipleThreads(int threads) {
		String fileName = "EditingOneWishInTransactionWithInitialWishes" + threads + "Threads";
		this.runSingleOperationWithInitialWishesBenchmarkMultipleThreads(Operations.EDIT, fileName,
				threads);
	}

	// - Benchmark Execution Code -

	/**
	 * Used to run the "single operation" type benchmarks, for example the "add
	 * one single wish to an empty list" benchmark
	 * {@link RemoteBenchmark#benchmarkAddingWishesInTransactionWithInitialWishesOneThread()}
	 * 
	 * @param operation
	 *            the operation which "single operation"-type benchmark is to be
	 *            executed
	 * @param fileName
	 *            the name of the file in which the measured data is to be saved
	 */
	private void runSingleOperationBenchmarkOneThread(Operations operation, String fileName) {
		int amount = this.getAmountOfAlreadyMeasuredData(fileName);

		for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			switch (operation) {
			case ADD:
				addingWishesOneThreadInTransaction(1, 1, fileName, i);
				break;

			case DELETE:
				deletingWishesOneThreadInTransaction(1, 0, fileName, i);
				break;
			case EDIT:
				editingWishesOneThreadInTransaction(1, 0, fileName, i);
				break;
			default:
				break;
			}
		}
	}

	private void runSingleOperationBenchmarkMultipleThreads(Operations operation, String fileName,
			int threads) {
		int amount = this.getAmountOfAlreadyMeasuredData(fileName);

		for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
			switch (operation) {
			case ADD:
				addingWishesMultipleThreadsInTransaction(1, 1, fileName, i, threads);
				break;

			case DELETE:
				deletingWishesInTransactionMultipleThreads(1, 0, fileName, i, threads);
				break;
			case EDIT:
				editingWishesInTransactionMultipleThreads(1, 0, fileName, i, threads);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Used to run the "with initial wishes"-type benchmarks, for example the
	 * "add one single wish to a list with X initial wishes" benchmark
	 * {@link RemoteBenchmark#benchmarkAddingWishesInTransactionWithInitialWishesOneThread()}
	 * 
	 * @param operation
	 *            the operation which "with initial wishes"-type benchmark is to
	 *            be executed
	 * @param fileName
	 *            the name of the file in which the measured data is to be saved
	 */
	private void runSingleOperationWithInitialWishesBenchmarkOneThread(Operations operation,
			String fileName) {
		for (int X : this.range) {
			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);

			for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				switch (operation) {
				case ADD:
					addingWishesInTransactionOneThread(10, 1, X, fileName + X, i);
					break;
				case DELETE:
					break; // no such benchmark implemented at this time
				case EDIT:
					editingWishesOneThreadInTransaction(1, X, fileName + X, i);
					break;
				default:
					break;
				}
			}
		}
	}

	private void runSingleOperationWithInitialWishesBenchmarkMultipleThreads(Operations operation,
			String fileName, int threads) {
		for (int X : this.range) {
			int amount = this.getAmountOfAlreadyMeasuredData(fileName + X);

			for (int i = 0; i < this.iterations && (amount + i) < this.maxAmount; i++) {
				switch (operation) {
				case ADD:
					addingWishesInTransactionMultipleThreads(10, 1, X, fileName + X, i, threads);
					break;
				case DELETE:
					break; // no such benchmark implemented at this time
				case EDIT:
					editingWishesInTransactionMultipleThreads(1, X, fileName + X, i, threads);
					break;
				default:
					break;
				}
			}
		}
	}

	public void addingWishesOneThreadInTransaction(int wishes, int operations, String filePath,
			int iteration) {
		addingWishesInTransactionOneThread(wishes, operations, 0, filePath, iteration);
	}

	// TODO Maybe the following three benchmarks can be merged to one generic
	// benchmark?

	public void addingWishesInTransactionOneThread(int wishes, int operations, int initialWishes,
			String filePath, int iteration) {
		this.currentRepo = "/repo" + System.currentTimeMillis();

		String listStr = addList(this.currentRepo, initialWishes);

		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for (int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo, initialWishes);
		}

		int addExceptions = 0;

		double avgTime = 0;
		for (int i = 0; i < operations; i++) {
			try {
				long time = 0l;
				boolean succGet = false;

				time = System.currentTimeMillis();
				succGet = (HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes="
						+ wishes));
				time = System.currentTimeMillis() - time;

				if (!succGet) {
					System.out.println("addingWishes: Failed GET-Request at iteration " + iteration
							+ ", " + initialWishes + " initial wishes while adding " + wishes
							+ " wishes");

					this.outputResults(filePath, initialWishes, operations, initialWishes, 0,
							Double.NaN, 1);
					this.outputCriticalErrors(filePath, initialWishes, wishes);
					return;
				}

				avgTime += time;
			} catch (Exception e) {
				addExceptions++;
			}
		}

		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;

		// Output Results in a simple CSV format
		outputResults(filePath, initialWishes, operations, wishes, successfulOperations, avgTime,
				addExceptions);

	}

	public void addingWishesMultipleThreadsInTransaction(int wishes, int operations,
			String filePath, int iteration, int threads) {
		addingWishesInTransactionMultipleThreads(wishes, operations, 0, filePath, iteration,
				threads);
	}

	public void addingWishesInTransactionMultipleThreads(int wishes, int operations,
			int initialWishes, String filePath, int iteration, int threads) {
		this.currentRepo = "/repo" + System.currentTimeMillis();

		for (int i = 0; i < threads; i++) {
			System.out.println("addingWishesInTransactionMultipleThreads: Starting thread nr. " + i
					+ " with " + initialWishes + " initial wishes.");
			Thread t = new Thread(new AddingWishesRunnable(this.currentRepo, this.absoluteUrl,
					initialWishes, operations, iteration, wishes, i, filePath, this));

			t.start();
		}
	}

	private class AddingWishesRunnable implements Runnable {
		@SuppressWarnings("hiding")
		private String currentRepo;
		@SuppressWarnings("hiding")
		private String absoluteUrl;
		private int initialWishes;
		private int operations;
		private int iteration;
		private int wishes;
		private int threadNr;
		private String filePath;
		private RemoteBenchmark benchmark;

		public AddingWishesRunnable(String currentRepo, String absoluteUrl, int initialWishes,
				int operations, int iteration, int wishes, int threadNumber, String filePath,
				RemoteBenchmark benchmark) {
			this.currentRepo = currentRepo;
			this.absoluteUrl = absoluteUrl;
			this.initialWishes = initialWishes;
			this.operations = operations;
			this.iteration = iteration;
			this.wishes = wishes;
			this.threadNr = threadNumber;
			this.filePath = filePath;
			this.benchmark = benchmark;
		}

		@Override
		public void run() {
			String listStr = this.benchmark.addList(this.currentRepo, this.initialWishes,
					this.threadNr);

			// in total 6 tries to create a list... if it doesn't work, this
			// test
			// fails
			for (int i = 0; i < 5 & listStr == null; i++) {
				listStr = this.benchmark.addList(this.currentRepo, this.initialWishes,
						this.threadNr);
			}

			int addExceptions = 0;

			double avgTime = 0;
			for (int i = 0; i < this.operations; i++) {
				try {
					long time = 0l;
					boolean succGet = false;

					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + listStr + "/add?wishes="
							+ this.wishes, this.threadNr);
					time = System.currentTimeMillis() - time;

					if (!succGet) {
						System.out.println("addingWishes: Failed GET-Request at iteration "
								+ this.iteration + ", " + this.initialWishes
								+ " initial wishes while adding " + this.wishes + " wishes");

						this.benchmark.outputResults(this.filePath, this.initialWishes,
								this.operations, this.initialWishes, 0, Double.NaN, 1,
								this.threadNr);
						this.benchmark.outputCriticalErrors(this.filePath, this.initialWishes,
								this.wishes, this.threadNr);
						return;
					}

					avgTime += time;
				} catch (Exception e) {
					addExceptions++;
				}
			}

			int successfulOperations = (this.operations - addExceptions);
			avgTime = avgTime / successfulOperations;

			// Output Results in a simple CSV format
			this.benchmark.outputResults(this.filePath, this.initialWishes, this.operations,
					this.wishes, successfulOperations, avgTime, addExceptions, this.threadNr);

		}

	}

	public void deletingWishesOneThreadInTransaction(int operations, int initialWishes,
			String filePath, int iteration) {
		// TODO implement initial wishes mechanic
		this.currentRepo = "/repo" + System.currentTimeMillis();
		String listStr = addList(this.currentRepo);

		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for (int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo);
		}

		int addExceptions = 0;
		double avgTime = 0;
		for (int i = 0; i < operations; i++) {
			try {
				String wishStr = addWishToEmptyList(this.absoluteUrl + listStr);

				if (wishStr == null) {
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

				if (!succGet) {
					System.out.println("deletingWishes: Failed GET-Request at iteration "
							+ iteration + ", " + initialWishes
							+ " initial wishes while deleting 1 wish");
					this.outputResults(filePath, 0, operations, 1, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath, initialWishes, 1);
					return;
				}

				avgTime += time;
			} catch (Exception e) {
				addExceptions++;
			}
		}

		int successfulOperations = (operations - addExceptions);

		avgTime = avgTime / successfulOperations;

		// Output Results in a simple CSV format
		outputResults(filePath, 0, operations, 1, successfulOperations, avgTime, addExceptions);

	}

	public void deletingWishesInTransactionMultipleThreads(int operations, int initialWishes,
			String filePath, int iteration, int threads) {
		// TODO implement initial wishes mechanic
		this.currentRepo = "/repo" + System.currentTimeMillis();

		for (int i = 0; i < threads; i++) {
			System.out.println("deletingWishesInTransactionMultipleThreads: Starting thread nr. "
					+ i + ".");
			Thread t = new Thread(new DeletingWishesRunnable(this.currentRepo, this.absoluteUrl,
					initialWishes, operations, iteration, i, filePath, this));

			t.start();
		}

	}

	private class DeletingWishesRunnable implements Runnable {
		@SuppressWarnings("hiding")
		private String currentRepo;
		@SuppressWarnings("hiding")
		private String absoluteUrl;
		private int initialWishes;
		private int operations;
		private int iteration;
		private int threadNr;
		private String filePath;
		private RemoteBenchmark benchmark;

		public DeletingWishesRunnable(String currentRepo, String absoluteUrl, int initialWishes,
				int operations, int iteration, int threadNumber, String filePath,
				RemoteBenchmark benchmark) {
			this.currentRepo = currentRepo;
			this.absoluteUrl = absoluteUrl;
			this.initialWishes = initialWishes;
			this.operations = operations;
			this.iteration = iteration;
			this.threadNr = threadNumber;
			this.filePath = filePath;
			this.benchmark = benchmark;
		}

		// TODO implement initial wishes mechanic

		@Override
		public void run() {
			String listStr = this.benchmark.addList(this.currentRepo, 0, this.threadNr);

			// in total 6 tries to create a list... if it doesn't work, this
			// test
			// fails
			for (int i = 0; i < 5 & listStr == null; i++) {
				listStr = this.benchmark.addList(this.currentRepo, 0, this.threadNr);
			}

			int addExceptions = 0;
			double avgTime = 0;
			for (int i = 0; i < this.operations; i++) {
				try {

					String wishStr = addWishToEmptyList(this.absoluteUrl + listStr, this.threadNr);

					if (wishStr == null) {
						System.out
								.println("deletingWishes: Failed addWish-GET-Request at iteration "
										+ this.iteration + ", " + this.initialWishes
										+ " initial wishes in DeleteWishes-Test");
						this.benchmark.outputResults(this.filePath, 0, this.operations, 1, 0,
								Double.NaN, 1, this.threadNr);
						this.benchmark.outputCriticalErrors(this.filePath, this.initialWishes, 1,
								this.threadNr);
						return;
					}

					long time = 0l;
					boolean succGet = false;

					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr + "/delete",
							this.threadNr);
					time = System.currentTimeMillis() - time;

					if (!succGet) {
						System.out.println("deletingWishes: Failed GET-Request at iteration "
								+ this.iteration + ", " + this.initialWishes
								+ " initial wishes while deleting 1 wish");
						this.benchmark.outputResults(this.filePath, 0, this.operations, 1, 0,
								Double.NaN, 1, this.threadNr);
						this.benchmark.outputCriticalErrors(this.filePath, this.initialWishes, 1,
								this.threadNr);
						return;
					}

					avgTime += time;
				} catch (Exception e) {
					addExceptions++;
				}
			}

			int successfulOperations = (this.operations - addExceptions);

			avgTime = avgTime / successfulOperations;

			// Output Results in a simple CSV format
			this.benchmark.outputResults(this.filePath, 0, this.operations, 1,
					successfulOperations, avgTime, addExceptions, this.threadNr);
		}

	}

	public void editingWishesOneThreadInTransaction(int operations, int initialWishes,
			String filePath, int iteration) {
		this.currentRepo = "/repo" + System.currentTimeMillis();
		String listStr = addList(this.currentRepo, initialWishes);

		// in total 6 tries to create a list... if it doesn't work, this test
		// fails
		for (int i = 0; i < 5 & listStr == null; i++) {
			listStr = addList(this.currentRepo, initialWishes);
		}

		double avgTime;

		int addExceptions = 0;

		avgTime = 0;
		for (int i = 0; i < operations; i++) {
			try {
				String wishStr = addWish(this.absoluteUrl + listStr);

				if (wishStr == null) {
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

				if (!succGet) {
					System.out.println("editingWishes: Failed GET-Request at iteration "
							+ iteration + ", " + initialWishes
							+ " initial wishes while editing 1 wish");
					this.outputResults(filePath, initialWishes, operations, 0, 0, Double.NaN, 1);
					this.outputCriticalErrors(filePath + initialWishes, initialWishes, 1);
					return;
				}

				avgTime += time;
			} catch (Exception e) {
				addExceptions++;
			}
		}

		int successfulOperations = (operations - addExceptions);
		avgTime = avgTime / successfulOperations;

		// Output Results in a simple CSV format
		outputResults(filePath, initialWishes, operations, 0, successfulOperations, avgTime,
				addExceptions);

	}

	public void editingWishesInTransactionMultipleThreads(int operations, int initialWishes,
			String filePath, int iteration, int threads) {
		this.currentRepo = "/repo" + System.currentTimeMillis();

		for (int i = 0; i < threads; i++) {
			System.out.println("editingWishesInTransactionMultipleThreads: Starting thread nr. "
					+ i + " with " + initialWishes + " initial wishes.");
			Thread t = new Thread(new EditingWishesRunnable(this.currentRepo, this.absoluteUrl,
					initialWishes, operations, iteration, i, filePath, this));

			t.start();
		}

	}

	private class EditingWishesRunnable implements Runnable {
		@SuppressWarnings("hiding")
		private String currentRepo;
		@SuppressWarnings("hiding")
		private String absoluteUrl;
		private int initialWishes;
		private int operations;
		private int iteration;
		private int threadNr;
		private String filePath;
		private RemoteBenchmark benchmark;

		public EditingWishesRunnable(String currentRepo, String absoluteUrl, int initialWishes,
				int operations, int iteration, int threadNumber, String filePath,
				RemoteBenchmark benchmark) {
			this.currentRepo = currentRepo;
			this.absoluteUrl = absoluteUrl;
			this.initialWishes = initialWishes;
			this.operations = operations;
			this.iteration = iteration;
			this.threadNr = threadNumber;
			this.filePath = filePath;
			this.benchmark = benchmark;
		}

		@Override
		public void run() {
			String listStr = this.benchmark.addList(this.currentRepo, this.initialWishes,
					this.threadNr);

			// in total 6 tries to create a list... if it doesn't work, this
			// test
			// fails
			for (int i = 0; i < 5 & listStr == null; i++) {
				listStr = this.benchmark.addList(this.currentRepo, this.initialWishes,
						this.threadNr);
			}

			double avgTime;

			int addExceptions = 0;

			avgTime = 0;
			for (int i = 0; i < this.operations; i++) {
				try {
					String wishStr = addWish(this.absoluteUrl + listStr, this.threadNr);

					if (wishStr == null) {
						System.out
								.println("editingWishes: Failed addWish-GET-Request at iteration "
										+ this.iteration + ", " + this.initialWishes
										+ " initial wishes in EditWish-Test");
						this.benchmark.outputResults(this.filePath, this.initialWishes,
								this.operations, 0, 0, Double.NaN, 1, this.threadNr);
						this.benchmark.outputCriticalErrors(this.filePath + this.initialWishes,
								this.initialWishes, 1, this.threadNr);
						return;
					}

					long time = 0l;
					boolean succGet = false;

					time = System.currentTimeMillis();
					succGet = HttpUtils.makeGetRequest(this.absoluteUrl + wishStr
							+ "/editName?name=performanceTest", this.threadNr);
					time = System.currentTimeMillis() - time;

					if (!succGet) {
						System.out.println("editingWishes: Failed GET-Request at iteration "
								+ this.iteration + ", " + this.initialWishes
								+ " initial wishes while editing 1 wish");
						this.benchmark.outputResults(this.filePath, this.initialWishes,
								this.operations, 0, 0, Double.NaN, 1, this.threadNr);
						this.benchmark.outputCriticalErrors(this.filePath + this.initialWishes,
								this.initialWishes, 1, this.threadNr);
						return;
					}

					avgTime += time;
				} catch (Exception e) {
					addExceptions++;
				}
			}

			int successfulOperations = (this.operations - addExceptions);
			avgTime = avgTime / successfulOperations;

			// Output Results in a simple CSV format
			this.benchmark.outputResults(this.filePath, this.initialWishes, this.operations, 0,
					successfulOperations, avgTime, addExceptions, this.threadNr);

		}

	}

	// ----------------- Helper Methods ------------------------

	private Object outputResultsLock = new Object();

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
	 * @param fileName
	 *            the name of the file
	 */
	private void outputResults(String fileName, int initialWishes, int operations, int wishes,
			int successfulOps, double avgTime, int opExceps) {
		synchronized (this.outputResultsLock) {
			outputResults(fileName, initialWishes, operations, wishes, successfulOps, avgTime,
					opExceps, RemoteBenchmark.SINGLETHREADONLY);
		}
	}

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
	 * @param fileName
	 *            the name of the file
	 */
	private void outputResults(String fileName, int initialWishes, int operations, int wishes,
			int successfulOps, double avgTime, int opExceps, int threadNumber) {
		synchronized (this.outputResultsLock) {
			// Output Results in a simple CSV format
			try {
				File f = new File(this.path + fileName + ".txt");
				if (!f.exists()) {
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

				if (threadNumber >= 0) {
					out.write("Thread Nr.:, " + threadNumber + ", ");
				} else {
					out.write("Thread Nr.:, - , ");
				}

				out.write(lineSeparator);

				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Object outputCriticalErrorsLock = new Object();

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
		synchronized (this.outputCriticalErrorsLock) {
			outputCriticalErrors(fileName, initialWishes, wishes, RemoteBenchmark.SINGLETHREADONLY);
		}
	}

	private void outputCriticalErrors(String fileName, int initialWishes, int wishes,
			int threadNumber) {
		synchronized (this.outputCriticalErrorsLock) {
			// Output Results in a simple CSV format
			try {
				File f = new File(this.path + fileName + "CriticalErrors.txt");
				if (!f.exists()) {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
				out.write("#Initial Wishes:, " + initialWishes + ", ");
				out.write("#Wishes per Op.:, " + wishes + ", ");

				if (threadNumber >= 0) {
					out.write("Thread Nr.:, " + threadNumber + ", ");
				} else {
					out.write("Thread Nr.:, - , ");
				}

				out.write(lineSeparator);

				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		synchronized (this.outputResultsLock) {
			try {
				BufferedReader in = new BufferedReader(
						new FileReader(this.path + fileName + ".txt"));

				String currentLine = in.readLine();
				int count = 0;
				while (currentLine != null) {
					count++;
					currentLine = in.readLine();
				}

				in.close();

				return count;

			} catch (IOException e) {
				// no data measured until now
				return 0;
			}

		}

	}

	/**
	 * Adds a new empty list to the repository at
	 * {@link RemoteBenchmark#absoluteUrl}/xmas/{@link addList#repoIdStr} and
	 * returns its url.
	 * 
	 * @param repoIdStr
	 *            The repository id
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
	 * @param repoIdStr
	 *            The repository id
	 * @param initialWishes
	 *            amount of initial wishes for the new list
	 * @return the url of the repository or null if the operation fails
	 */
	private String addList(String repoIdStr, int initialWishes) {
		return addList(repoIdStr, initialWishes, RemoteBenchmark.SINGLETHREADONLY);
	}

	private String addList(String repoIdStr, int initialWishes, int threadNr) {
		String response = null;

		if (threadNr < 0) {
			System.out.println("What the...?");
		}

		try {
			response = HttpUtils.getRequestAsStringResponse(this.absoluteUrl + "/xmas" + repoIdStr
					+ "/add?lists=1&wishes=" + initialWishes, threadNr);
		} catch (Exception e) {
			return null;
		}
		assert response != null;

		String[] lines = response.split("\n");

		String list = null;
		boolean found = false;
		for (int i = 0; i < lines.length && !found; i++) {
			if (lines[i].startsWith("<a href=\"/xmas/")) {
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
	 * @param listUrlStr
	 *            the url of the list to which a new wish is to be added
	 * @return the url of the new wish. (returning the correct url only works
	 *         when the list was empty before executing this method, if the list
	 *         already contained some wishes the returned url may be the url of
	 *         another wish) Returns null if operations fails.
	 */
	private static String addWishToEmptyList(String listUrlStr) {
		return addWishToEmptyList(listUrlStr, RemoteBenchmark.SINGLETHREADONLY);
	}

	private static String addWishToEmptyList(String listUrlStr, int threadNr) {
		boolean succ = HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1", threadNr);
		if (!succ) {
			return null;
		}

		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls",
				threadNr);
		if (response == null) {
			return null;
		}

		String[] lines = response.split("\n");

		return lines[0];
	}

	private static String addWish(String listUrlStr) {
		return addWish(listUrlStr, RemoteBenchmark.SINGLETHREADONLY);
	}

	private static String addWish(String listUrlStr, int threadNr) {
		boolean succ = HttpUtils.makeGetRequest(listUrlStr + "/add?wishes=1");
		if (!succ) {
			return null;
		}

		String response = HttpUtils.getRequestAsStringResponse(listUrlStr + "?format=urls",
				threadNr);
		if (response == null) {
			return null;
		}

		String[] lines = response.split("\n");

		return lines[lines.length - 1];
	}
}

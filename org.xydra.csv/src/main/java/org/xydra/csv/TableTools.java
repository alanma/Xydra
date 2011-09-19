package org.xydra.csv;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.csv.impl.memory.SparseTable;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A set of tools for {@link CsvTable}.
 * 
 * @author voelkel
 * 
 */
public class TableTools {
	
	/**
	 * Processes a file and writes results to a shared {@link CsvTable}.
	 * 
	 * @author voelkel
	 */
	public interface IFileProcessor {
		/**
		 * @param table a shared result table (shared by all calls to 'process')
		 * @param f a file to be processes
		 * @throws IOException from reading f
		 */
		public void process(ICsvTable table, File f) throws IOException;
	}
	
	/**
	 * Represents a compound key consisting of <i>n</i> columns.
	 * 
	 * The order of keys implies a sorting order.
	 * 
	 * @author voelkel
	 */
	private static class NKeys implements Iterable<String> {
		final List<String> keys;
		
		public NKeys(List<String> keys) {
			this.keys = keys;
		}
		
		@SuppressWarnings("unused")
		public boolean contains(String key) {
			return this.keys.contains(key);
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof NKeys) {
				NKeys otherKey = (NKeys)other;
				if(otherKey.keys.size() == this.keys.size()) {
					for(int i = 0; i < this.keys.size(); i++) {
						if(!this.keys.get(i).equals(otherKey.keys.get(i)))
							return false;
					}
					return true;
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			int hash = 0;
			for(int i = 0; i < this.keys.size(); i++) {
				hash += this.keys.get(i).hashCode();
			}
			return hash;
		}
		
		@Override
		public Iterator<String> iterator() {
			return this.keys.iterator();
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			for(int i = 0; i < this.keys.size(); i++) {
				buf.append(this.keys.get(i));
				if(i + 1 < this.keys.size()) {
					buf.append("--");
				}
			}
			return buf.toString();
		}
	}
	
	/**
	 * Represents a compound key consisting of two columns.
	 * 
	 * @author voelkel
	 * 
	 */
	private static class TwoKey {
		final String a;
		final String b;
		
		public TwoKey(String a, String b) {
			this.a = a;
			this.b = b;
		}
		
		@Override
		public boolean equals(Object other) {
			return other instanceof TwoKey && this.a.equals(((TwoKey)other).a)
			        && this.b.equals(((TwoKey)other).b);
		}
		
		@Override
		public int hashCode() {
			return this.a.hashCode() + this.b.hashCode();
		}
	}
	
	private static Logger log = LoggerFactory.getLogger(TableTools.class);
	
	/**
	 * TODO generalize into n keys
	 * 
	 * @param keyColumnName1 never null
	 * @param keyColumnName2 never null
	 * @return an {@link IRowInsertionHandler} that aggregates all rows into one
	 *         where the values of the key column names are equal between rows.
	 *         If the key column names are "a" and "b", two rows with (a=3, b=5,
	 *         c=7) and (a=3, b=5,c=10) would be merged into (a=3, b=5, c=17).
	 *         Numbers are added, String values are concatenated.
	 */
	public static IRowInsertionHandler createGroupByTwoKeysRowInsertionHandler(
	        final String keyColumnName1, final String keyColumnName2) {
		IRowInsertionHandler rowInsertionHandler = new IRowInsertionHandler() {
			
			long aggregated = 0;
			// temporary index of occurring keys and their row
			Map<TwoKey,Row> compoundKeys2row = new HashMap<TwoKey,Row>(100);
			TwoKey keyColumnNames = new TwoKey(keyColumnName1, keyColumnName2);
			// Collection<String> rowKeysToBeRemoved = new LinkedList<String>();
			long processed = 0;
			
			@Override
			public void afterRowInsertion(IRow row) {
			}
			
			@Override
			public boolean beforeRowInsertion(Row row) {
				this.processed++;
				if(this.processed % 1000 == 0) {
					log.info("Read aggregating processed " + this.processed + " rows, aggregated "
					        + this.aggregated);
				}
				
				/*
				 * Calculate compound key of this row. If one or several of the
				 * key-columns are empty, then this row is deleted.
				 */
				String valueA = row.getValue(this.keyColumnNames.a);
				if(valueA == null) {
					return false;
				}
				String valueB = row.getValue(this.keyColumnNames.b);
				if(valueB == null) {
					return false;
				}
				TwoKey compoundKey = new TwoKey(valueA, valueB);
				
				/* If we have previously seen a row with the same compundKey... */
				if(this.compoundKeys2row.containsKey(compoundKey)) {
					/* ... we aggregate this row into the existing row */
					IRow masterRow = this.compoundKeys2row.get(compoundKey);
					masterRow.aggregate(row, new String[] {

					keyColumnName1, keyColumnName2

					});
					this.aggregated++;
					return false;
				} else {
					/*
					 * record the row as a new potential master row and add it
					 * also (return=true)
					 */
					this.compoundKeys2row.put(compoundKey, row);
					return true;
				}
			}
		};
		return rowInsertionHandler;
	}
	
	/**
	 * Group sourceTable by keys. For each group, calculate sum, average and
	 * count for the given columns.
	 * 
	 * TODO improve docu for this function
	 * 
	 * Only rows listed in sum, average, or range list are present in resulting
	 * table.
	 * 
	 * @param sourceTable which table to process
	 * @param keyList create groups of rows where columns have same values
	 * @param sumList sum these columns up, for each group
	 * @param averageList calculate the average of these columns, for each group
	 * @param rangeList counts entries, for each group
	 * @param targetTable to which to write the result (which is a much smaller
	 *            table, with one row for each used combination of keys)
	 */
	public static void groupBy(ICsvTable sourceTable, List<String> keyList, List<String> sumList,
	        List<String> averageList, List<String> rangeList, ICsvTable targetTable) {
		NKeys keys = new NKeys(keyList);
		NKeys sum = new NKeys(sumList);
		NKeys average = new NKeys(averageList);
		NKeys range = new NKeys(rangeList);
		
		log.info("Re-sorting table into group tables.");
		/* re-sort table into group tables */
		long serialNumber = 1;
		Map<NKeys,CsvTable> groups = new HashMap<NKeys,CsvTable>();
		for(IRow sourceRow : sourceTable) {
			/* create compound key */
			List<String> compoundRowNameList = new LinkedList<String>();
			for(String key : keys) {
				compoundRowNameList.add(sourceRow.getValue(key));
			}
			NKeys compoundRowName = new NKeys(compoundRowNameList);
			CsvTable groupTable = groups.get(compoundRowName);
			if(groupTable == null) {
				groupTable = new CsvTable();
				groups.put(compoundRowName, groupTable);
			}
			/* copy all columns which will later be used */
			Set<String> relevantCols = new HashSet<String>();
			relevantCols.addAll(sum.keys);
			relevantCols.addAll(average.keys);
			relevantCols.addAll(range.keys);
			IRow groupRow = groupTable.getOrCreateRow("" + serialNumber++, true);
			for(String relevantCol : relevantCols) {
				groupRow.setValue(relevantCol, sourceRow.getValue(relevantCol), true);
			}
		}
		
		log.info("Aggregate group tables to target table.");
		/* aggregate to targetTable */
		for(NKeys groupKey : groups.keySet()) {
			IRow targetRow = targetTable.getOrCreateRow(groupKey.toString(), true);
			// set key parts
			for(int i = 0; i < keys.keys.size(); i++) {
				targetRow.setValue(keys.keys.get(i), groupKey.keys.get(i), true);
			}
			ICsvTable groupTable = groups.get(groupKey);
			// count
			targetRow.setValue("count", groupTable.rowCount(), true);
			
			// sum
			for(String sumCol : sum) {
				long longResult = 0;
				double doubleResult = 0;
				for(IRow groupRow : groupTable) {
					try {
						longResult += groupRow.getValueAsLong(sumCol);
					} catch(WrongDatatypeException e) {
						try {
							doubleResult += groupRow.getValueAsDouble(sumCol);
						} catch(WrongDatatypeException e2) {
							log.warn("Could not calculate sum in col " + sumCol, e2);
						}
					}
				}
				if(doubleResult == 0) {
					targetRow.setValue(sumCol + "--sum", "" + ((long)(longResult + doubleResult)),
					        true);
				} else {
					targetRow.setValue(sumCol + "--sum", "" + (longResult + doubleResult), true);
				}
				
			}
			// average
			for(String averageCol : average) {
				long longResult = 0;
				double doubleResult = 0;
				for(IRow groupRow : groupTable) {
					try {
						longResult += groupRow.getValueAsLong(averageCol);
					} catch(WrongDatatypeException e) {
						try {
							doubleResult += groupRow.getValueAsDouble(averageCol);
						} catch(WrongDatatypeException e2) {
							log.warn("Could not calculate average in col " + averageCol, e2);
						}
					}
				}
				targetRow.setValue(averageCol + "--average", ""
				        + ((long)((longResult + doubleResult) / groupTable.rowCount())), true);
			}
			// count value range
			for(String rangeCol : range) {
				Set<String> seenValues = new HashSet<String>();
				for(IRow groupRow : groupTable) {
					seenValues.add(groupRow.getValue(rangeCol));
				}
				targetRow.setValue(rangeCol + "--range", "" + seenValues.size(), true);
			}
		}
	}
	
	/**
	 * Merges the content of two CSV-files into one resulting CSV-table. This is
	 * a memory-efficient operation.
	 * 
	 * @param a source file
	 * @param b source file
	 * @param mergeFile result file
	 * @throws IOException from file I/O
	 */
	public static void merge(File a, File b, File mergeFile) throws IOException {
		ICsvTable table = new CsvTable();
		table.readFrom(a);
		table.readFrom(b);
		table.writeTo(mergeFile);
	}
	
	/**
	 * Starts in 'startDirectory' and traverses all sub-directories. On the way,
	 * all files matching the 'filenameFilter' are processed using the given
	 * 'processor'. A {@link CsvTable} is used to persist any results the
	 * processor can generate.
	 * 
	 * @param startDirectory the root of the directory traversal
	 * @param table to store results of processing
	 * @param filenameFilter only matching files are processed
	 * @param processor an {@link IFileProcessor}
	 * @throws IOException from read/write files
	 */
	public static void process(File startDirectory, ICsvTable table, FilenameFilter filenameFilter,
	        IFileProcessor processor) throws IOException {
		for(File f : startDirectory.listFiles(filenameFilter)) {
			processor.process(table, f);
		}
		for(File f : startDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		})) {
			process(f, table, filenameFilter, processor);
		}
	}
	
	public static void transpose(SparseTable sourceTable, SparseTable targetTable) {
		for(String sourceColumnName : sourceTable.getColumnNames()) {
			for(IRow sourceRow : sourceTable) {
				String value = sourceRow.getValue(sourceColumnName);
				IRow targetRow = targetTable.getOrCreateRow(sourceColumnName, true);
				targetRow.setValue(sourceRow.getKey(), value, true);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		CsvTable test = new CsvTable();
		Row row1 = test.getOrCreateRow("row-1", true);
		row1.setValue("colA", "A1", true);
		row1.setValue("colB", "B1", true);
		row1.setValue("colC", "C1", true);
		Row row2 = test.getOrCreateRow("row-2", true);
		row2.setValue("colA", "A2", true);
		row2.setValue("colB", "B2", true);
		row2.setValue("colC", "C2", true);
		Row row3 = test.getOrCreateRow("row-3", true);
		row3.setValue("colA", "A3", true);
		row3.setValue("colB", "B3", true);
		row3.setValue("colC", "C3", true);
		
		test.dump();
		CsvTable t = new CsvTable();
		TableTools.transpose(test, t);
		t.dump();
	}
	
}

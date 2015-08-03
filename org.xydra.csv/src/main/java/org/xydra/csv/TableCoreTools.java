package org.xydra.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.csv.impl.memory.SparseTable;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class TableCoreTools {

	private static Logger log = LoggerFactory.getLogger(TableCoreTools.class);

	/**
	 * Represents a compound key consisting of <i>n</i> columns.
	 *
	 * The order of keys implies a sorting order.
	 *
	 * @author xamde
	 */
	private static class CompoundKey implements Iterable<String>, Comparable<CompoundKey> {
		final List<String> keyParts;

		public CompoundKey(final List<String> keys) {
			this.keyParts = keys;
		}

		// @SuppressWarnings("unused")
		// public boolean contains(String key) {
		// return this.keys.contains(key);
		// }

		@Override
		public boolean equals(final Object other) {
			if (other instanceof CompoundKey) {
				final CompoundKey otherKey = (CompoundKey) other;
				if (otherKey.keyParts.size() == this.keyParts.size()) {
					for (int i = 0; i < this.keyParts.size(); i++) {
						if (!this.keyParts.get(i).equals(otherKey.keyParts.get(i))) {
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 0;
			for (int i = 0; i < this.keyParts.size(); i++) {
				hash += this.keyParts.get(i).hashCode();
			}
			return hash;
		}

		@Override
		public Iterator<String> iterator() {
			return this.keyParts.iterator();
		}

		@Override
		public String toString() {
			final StringBuffer buf = new StringBuffer();
			for (int i = 0; i < this.keyParts.size(); i++) {
				buf.append(this.keyParts.get(i));
				if (i + 1 < this.keyParts.size()) {
					buf.append("--");
				}
			}
			return buf.toString();
		}

		@Override
		public int compareTo(final CompoundKey other) {
			return ALPHA_NUMERIC_COMPARATOR.compare(toString(), other.toString());
		}
	}

	public static Comparator<String> ALPHA_NUMERIC_COMPARATOR = new Comparator<String>() {

		@Override
		public int compare(final String a, final String b) {
			if (a == null) {
				if (b == null) {
					return 0;
				} else {
					return 1;
				}
			} else if (b == null) {
				return -1;
			}

			// experimental: try to treat both as numbers
			try {
				final long aNum = Long.parseLong(a);
				final long bNum = Long.parseLong(b);
				return (int) (aNum - bNum);
			} catch (final NumberFormatException e) {
			}

			// else
			return a.compareTo(b);
		}
	};

	/**
	 * Represents a compound key consisting of two columns.
	 *
	 * @author xamde
	 *
	 */
	private static class TwoKey {
		final String a;
		final String b;

		public TwoKey(final String a, final String b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(final Object other) {
			return other instanceof TwoKey && this.a.equals(((TwoKey) other).a)
					&& this.b.equals(((TwoKey) other).b);
		}

		@Override
		public int hashCode() {
			return this.a.hashCode() + this.b.hashCode();
		}
	}

	/**
	 * IMPROVE generalize into n keys
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
		final IRowInsertionHandler rowInsertionHandler = new IRowInsertionHandler() {

			long aggregated = 0;
			// temporary index of occurring keys and their row
			Map<TwoKey, Row> compoundKeys2row = new HashMap<TwoKey, Row>(100);
			TwoKey keyColumnNames = new TwoKey(keyColumnName1, keyColumnName2);
			// Collection<String> rowKeysToBeRemoved = new LinkedList<String>();
			long processed = 0;

			@Override
			public void afterRowInsertion(final IRow row) {
			}

			@Override
			public boolean beforeRowInsertion(final Row row) {
				this.processed++;
				if (this.processed % 1000 == 0) {
					log.info("Read aggregating processed " + this.processed + " rows, aggregated "
							+ this.aggregated);
				}

				/*
				 * Calculate compound key of this row. If one or several of the
				 * key-columns are empty, then this row is deleted.
				 */
				final String valueA = row.getValue(this.keyColumnNames.a);
				if (valueA == null) {
					return false;
				}
				final String valueB = row.getValue(this.keyColumnNames.b);
				if (valueB == null) {
					return false;
				}
				final TwoKey compoundKey = new TwoKey(valueA, valueB);

				/* If we have previously seen a row with the same compundKey... */
				if (this.compoundKeys2row.containsKey(compoundKey)) {
					/* ... we aggregate this row into the existing row */
					final IRow masterRow = this.compoundKeys2row.get(compoundKey);
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
	 * Group sourceTable by keys. I.e. for a table with four columns, A, B, C,
	 * and D a groupBy with keyList= A and B would result in a map with
	 * {@link CompoundKey} consisting of A and B and sub-tables containing only
	 * columns C and D.
	 *
	 * There are as many resulting tables as there are different combinations of
	 * A-B pairs.
	 *
	 * @param sourceTable which table to process
	 * @param keyList create groups of rows where columns have same values
	 * @return a Map with the resulting tables. The resulting tables have the
	 *         columns listed in keyList merged into the {@link CompoundKey} and
	 *         have been removed from the sub-tables.
	 */
	public static Map<CompoundKey, CsvTable> groupBy(final ICsvTable sourceTable, final List<String> keyList) {
		final CompoundKey keys = new CompoundKey(keyList);

		log.info("Re-sorting table into group tables.");
		/* re-sort table into group tables */
		long serialNumber = 1;
		final Map<CompoundKey, CsvTable> groups = new TreeMap<CompoundKey, CsvTable>();
		for (final IRow sourceRow : sourceTable) {
			/* create compound key */
			final List<String> compoundRowNameList = new LinkedList<String>();
			for (final String key : keys) {
				compoundRowNameList.add(sourceRow.getValue(key));
			}
			final CompoundKey compoundRowName = new CompoundKey(compoundRowNameList);
			CsvTable groupTable = groups.get(compoundRowName);
			if (groupTable == null) {
				groupTable = new CsvTable();
				groups.put(compoundRowName, groupTable);
			}
			/* copy all columns which will later be used */
			final Set<String> relevantCols = new HashSet<String>();
			relevantCols.addAll(sourceTable.getColumnNames());
			final IRow groupRow = groupTable.getOrCreateRow("" + serialNumber++, true);
			for (final String relevantCol : relevantCols) {
				groupRow.setValue(relevantCol, sourceRow.getValue(relevantCol), true);
			}
		}
		return groups;
	}

	/**
	 * Group sourceTable by keys. For each group, calculate sum, average and
	 * count for the given columns.
	 *
	 * TODO improve docu for this function
	 *
	 * Only columns listed in sum, average, or range list are present in
	 * resulting table.
	 *
	 * @param sourceTable which table to process
	 * @param keyList create groups of rows where columns have same values
	 * @param sumList sum these columns up, for each group
	 * @param averageList calculate the average of these columns, for each group
	 * @param rangeList counts entries, for each group
	 * @param targetTable to which to write the result (which is a much smaller
	 *            table, with one row for each used combination of keys)
	 */
	public static void groupBy(final ICsvTable sourceTable, final List<String> keyList, final List<String> sumList,
			final List<String> averageList, final List<String> rangeList, final ICsvTable targetTable) {
		final CompoundKey keys = new CompoundKey(keyList);
		final CompoundKey sum = new CompoundKey(sumList);
		final CompoundKey average = new CompoundKey(averageList);
		final CompoundKey range = new CompoundKey(rangeList);

		log.info("Re-sorting table into group tables.");
		/* re-sort table into group tables */
		long serialNumber = 1;
		final Map<CompoundKey, CsvTable> groups = new TreeMap<CompoundKey, CsvTable>();
		for (final IRow sourceRow : sourceTable) {
			/* create compound key */
			final List<String> compoundRowNameList = new LinkedList<String>();
			for (final String key : keys) {
				compoundRowNameList.add(sourceRow.getValue(key));
			}
			final CompoundKey compoundRowName = new CompoundKey(compoundRowNameList);
			CsvTable groupTable = groups.get(compoundRowName);
			if (groupTable == null) {
				groupTable = new CsvTable();
				groups.put(compoundRowName, groupTable);
			}
			/* copy all columns which will later be used */
			final Set<String> relevantCols = new HashSet<String>();
			relevantCols.addAll(sum.keyParts);
			relevantCols.addAll(average.keyParts);
			relevantCols.addAll(range.keyParts);
			final IRow groupRow = groupTable.getOrCreateRow("" + serialNumber++, true);
			for (final String relevantCol : relevantCols) {
				groupRow.setValue(relevantCol, sourceRow.getValue(relevantCol), true);
			}
		}

		log.info("Aggregate group tables to target table.");
		/* aggregate to targetTable */
		for (final CompoundKey groupKey : groups.keySet()) {
			log.info("Use groupKey " + groupKey);
			final IRow targetRow = targetTable.getOrCreateRow(groupKey.toString(), true);
			// set key parts
			for (int i = 0; i < keys.keyParts.size(); i++) {
				targetRow.setValue(keys.keyParts.get(i), groupKey.keyParts.get(i), true);
			}
			final ICsvTable groupTable = groups.get(groupKey);
			// count
			targetRow.setValue("count", groupTable.rowCount(), true);

			// sum
			for (final String sumCol : sum) {
				long longResult = 0;
				double doubleResult = 0;
				for (final IRow groupRow : groupTable) {
					try {
						longResult += groupRow.getValueAsLong(sumCol);
					} catch (final WrongDatatypeException e) {
						try {
							doubleResult += groupRow.getValueAsDouble(sumCol);
						} catch (final WrongDatatypeException e2) {
							log.warn("Could not calculate sum in col " + sumCol, e2);
						}
					}
				}
				if (doubleResult == 0) {
					targetRow.setValue(sumCol + "--sum", "" + (long) (longResult + doubleResult),
							true);
				} else {
					targetRow.setValue(sumCol + "--sum", "" + (longResult + doubleResult), true);
				}

			}
			// average + stdev
			for (final String averageCol : average) {
				// average
				long longResult = 0;
				double doubleResult = 0;
				for (final IRow groupRow : groupTable) {
					try {
						longResult += groupRow.getValueAsLong(averageCol);
					} catch (final WrongDatatypeException e) {
						try {
							doubleResult += groupRow.getValueAsDouble(averageCol);
						} catch (final WrongDatatypeException e2) {
							log.warn("Could not calculate average in col " + averageCol, e2);
						}
					}
				}
				final double ave = (longResult + doubleResult) / groupTable.rowCount();

				// rounding to #.### format
				final long rAve = (long) (ave * 1000);

				targetRow.setValue(averageCol + "--average", "" + rAve / 1000 + "." + rAve % 1000,
						true);

				// standard derivation
				double _sum = 0;
				long n = 0;
				for (final IRow groupRow : groupTable) {
					n++;
					double delta = 0;
					try {
						delta = groupRow.getValueAsLong(averageCol) - ave;
					} catch (final WrongDatatypeException e) {
						try {
							delta = groupRow.getValueAsDouble(averageCol) - ave;
						} catch (final WrongDatatypeException e2) {
							log.warn("Could not calculate average in col " + averageCol, e2);
						}
					}
					delta = delta * delta;
					_sum += delta;
				}
				final double stdev = Math.sqrt(_sum / n);

				// rounding to #.### format
				final long rStdev = (long) (stdev * 1000);

				targetRow.setValue(averageCol + "--stdev",
						"" + rStdev / 1000 + "." + rStdev % 1000, true);

			}
			// count value range
			for (final String rangeCol : range) {
				final Set<String> seenValues = new HashSet<String>();
				for (final IRow groupRow : groupTable) {
					seenValues.add(groupRow.getValue(rangeCol));
				}
				targetRow.setValue(rangeCol + "--range", "" + seenValues.size(), true);
			}
		}
	}

	public static void transpose(final SparseTable sourceTable, final SparseTable targetTable) {
		for (final String sourceColumnName : sourceTable.getColumnNames()) {
			for (final IRow sourceRow : sourceTable) {
				final String value = sourceRow.getValue(sourceColumnName);
				final IRow targetRow = targetTable.getOrCreateRow(sourceColumnName, true);
				targetRow.setValue(sourceRow.getKey(), value, true);
			}
		}
	}

	public static List<Row> sortByColumn(final ISparseTable table, final String sortKey) {
		// get all rows
		final List<Row> rowList = new ArrayList<Row>();
		for (final Row row : table) {
			rowList.add(row);
		}
		Collections.sort(rowList, new RowByColumnComparator(sortKey));
		return rowList;
	}

	public static class RowByColumnComparator implements Comparator<IRow> {

		public RowByColumnComparator(final String sortCol) {
			this.sortCol = sortCol;
		}

		private final String sortCol;

		@Override
		public int compare(final IRow a, final IRow b) {
			final String aVal = a.getValue(this.sortCol);
			final String bVal = b.getValue(this.sortCol);
			return TableCoreTools.ALPHA_NUMERIC_COMPARATOR.compare(aVal, bVal);
		}

	}

}

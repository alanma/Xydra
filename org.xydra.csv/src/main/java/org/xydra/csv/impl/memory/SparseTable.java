package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;
import org.xydra.csv.IRowInsertionHandler;
import org.xydra.csv.IRowVisitor;
import org.xydra.csv.ISparseTable;
import org.xydra.csv.RowFilter;
import org.xydra.csv.WrongDatatypeException;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.iterator.ReadOnlyIterator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Maintains a sparse table, organised by rows and columns.
 *
 * Insertion order is preserved.
 *
 * @author xamde
 */
public class SparseTable implements ISparseTable {

	public static final int EXCEL_MAX_COLS = 255;

	public static final int EXCEL_MAX_ROWS = 65535;

	private static Logger log = LoggerFactory.getLogger(SparseTable.class);

	boolean aggregateStrings = true;

	Set<String> columnNames;

	/**
	 * Older Excel limits: You are trying to open a file that contains more than
	 * 65,536 rows or 256 columns. Default: false
	 */
	boolean restrictToExcelSize = false;

	private IRowInsertionHandler rowInsertionHandler;

	// ----------------- row handling

	/* maintain any row insertion order */
	private final List<String> rowNames = new LinkedList<String>();

	/* automatically sorted */
	private final Map<String, Row> table = new TreeMap<String, Row>();

	private void insertRow(final String rowName, final Row row) {
		if (rowCount() == EXCEL_MAX_ROWS) {
			log.warn("Adding the " + EXCEL_MAX_ROWS + "th row - that is Excels limit");
			if (this.restrictToExcelSize) {
				throw new ExcelLimitException("Row limit reached");
			}
		}

		/* new key? */
		if (this.table.containsKey(rowName)) {
			assert this.rowNames.contains(rowName);
			// attempt a merge
			final IRow existingRow = getOrCreateRow(rowName, false);

			for (final Map.Entry<String, ICell> entry : row.entrySet()) {
				try {
					existingRow.setValue(entry.getKey(), entry.getValue().getValue(), true);
				} catch (final IllegalStateException ex) {
					throw new IllegalStateException("Table contains already a row named '"
							+ rowName + "' and for key '" + entry.getKey()
							+ "' there was already a value.", ex);
				}
			}
		} else {
			assert !this.rowNames.contains(rowName);
			this.rowNames.add(rowName);
			this.table.put(rowName, row);
		}
	}

	protected Iterable<String> rowNamesIterable() {
		return this.rowNames;
	}

	protected Iterator<String> subIterator(final int startRow, final int endRow) {
		return this.rowNames.subList(startRow, endRow).iterator();
	}

	@Override
	public Iterator<Row> iterator() {
		return new ReadOnlyIterator<Row>(this.table.values().iterator());
	}

	@Override
	public Iterator<Row> getDataRows() {
		final Iterator<Row> it = this.table.values().iterator();
		if (it.hasNext()) {
			it.next();
			return new ReadOnlyIterator<Row>(it);
		} else {
			return Iterators.none();
		}
	}

	@Override
	public Row getHeaderRow() {
		return this.table.values().iterator().next();
	}

	// -----------------

	public SparseTable() {
		this.columnNames = new TreeSet<String>();
	}

	/**
	 * @param maintainColumnInsertionOrder if true, the insertion order of
	 *            columns is maintained. If false, the default automatic
	 *            alphabetic sorting is on.
	 */
	public SparseTable(final boolean maintainColumnInsertionOrder) {
		if (maintainColumnInsertionOrder) {
			this.columnNames = new LinkedHashSet<String>();
		} else {
			this.columnNames = new TreeSet<String>();
		}
	}

	/**
	 * Add all values from the other table into this table
	 *
	 * @param other never null
	 */
	public void addAll(final SparseTable other) {
		for (final Entry<String, Row> entry : other.table.entrySet()) {
			final Row thisRow = getOrCreateRow(entry.getKey(), true);
			for (final Entry<String, ICell> rowEntry : entry.getValue().entrySet()) {
				thisRow.setValue(rowEntry.getKey(), rowEntry.getValue().getValue());
			}
		}
	}

	@Override
	public void addColumnName(final String columnName) {
		this.columnNames.add(columnName);
	}

	/**
	 * Add if {@link IRowInsertionHandler} null or does not object
	 *
	 * @param rowName
	 * @param row
	 */
	protected void addRow(final String rowName, final Row row) {
		boolean insert = true;
		if (this.rowInsertionHandler != null) {
			insert &= this.rowInsertionHandler.beforeRowInsertion(row);
		}
		if (insert) {
			insertRow(rowName, row);
		}
	}

	@Override
	public void aggregate(final String[] keyColumnNames) {
		// temporary index of occurring keys and their row
		final Map<String, Row> compoundKeys2row = new HashMap<String, Row>(rowCount());
		final Iterator<Row> rowIt = this.table.values().iterator();
		// Collection<String> rowKeysToBeRemoved = new LinkedList<String>();
		long processed = 0;
		long aggregated = 0;
		while (rowIt.hasNext()) {
			final Row row = rowIt.next();

			// calculate compound key
			// IMPROVE performance takes 50% of performance
			final StringBuffer compoundKeyBuffer = new StringBuffer(100);
			for (final String keyColumnName : keyColumnNames) {
				final String value = row.getValue(keyColumnName);
				if (value != null) {
					compoundKeyBuffer.append(value);
				}
			}
			final String compoundKey = compoundKeyBuffer.toString();

			if (compoundKeys2row.containsKey(compoundKey)) {
				// aggregate row into existing row
				final IRow masterRow = compoundKeys2row.get(compoundKey);
				masterRow.aggregate(row, keyColumnNames);
				aggregated++;

				// delete row
				rowIt.remove();
				this.rowNames.remove(row.getKey());
			} else {
				// just mark key as seen
				compoundKeys2row.put(compoundKey, row);
			}

			processed++;
			if (processed % 1000 == 0) {
				log.info("Aggregate processed " + processed + " rows, aggregated " + aggregated);
			}
		}
		log.info(rowCount() + " rows with aggregated data remain");
	}

	/**
	 * Used only by tests.
	 *
	 * @param row
	 * @param column
	 * @param s
	 * @param maximalFieldLength
	 */
	void appendString(final String row, final String column, final String s, final int maximalFieldLength) {
		final Row r = getOrCreateRow(row, true);
		final ICell c = r.getOrCreateCell(column, true);
		c.appendString(s, maximalFieldLength);
	}

	@Override
	public void clear() {
		this.rowNames.clear();
		this.table.clear();
	}

	protected int colCount() {
		return this.columnNames.size();
	}

	@Override
	public ISparseTable dropColumn(final String columnName, final String value) {
		final ISparseTable target = new SparseTable();
		for (final String rowName : this.rowNames) {
			final IRow sourceRow = getOrCreateRow(rowName, false);
			if (!sourceRow.getValue(columnName).equals(value)) {
				// copy row
				final IRow targetRow = target.getOrCreateRow(rowName, true);
				for (final String colName : sourceRow.getColumnNames()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}

	@Override
	public ISparseTable filter(final String key, final String value) {
		final ISparseTable target = new SparseTable();
		for (final String rowName : this.rowNames) {
			final IRow sourceRow = getOrCreateRow(rowName, false);
			if (sourceRow.getValue(key).equals(value)) {
				// copy row
				final IRow targetRow = target.getOrCreateRow(rowName, true);
				for (final String colName : sourceRow.getColumnNames()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}

	@Override
	public Set<String> getColumnNames() {
		return this.columnNames;
	}

	@Override
	public Row getOrCreateRow(final String rowName, final boolean create) {
		Row row = this.table.get(rowName);
		if (row == null) {
			assert !this.rowNames.contains(rowName);
			if (create) {
				row = new Row(rowName, this);
				insertRow(rowName, row);
			}
		}
		return row;
	}

	@Override
	public boolean getParamAggregateStrings() {
		return this.aggregateStrings;
	}

	@Override
	public boolean getParamRestrictToExcelSize() {
		return this.restrictToExcelSize;
	}

	@Override
	public String getValue(final String row, final String column) {
		final Row r = getOrCreateRow(row, false);
		if (r == null) {
			return null;
		}
		final ICell c = r.getOrCreateCell(column, false);
		if (c == null) {
			return null;
		}
		return c.getValue();
	}

	@Override
	public void incrementValue(final String row, final String column, final int increment)
			throws WrongDatatypeException {
		final Row r = getOrCreateRow(row, true);
		final ICell c = r.getOrCreateCell(column, true);
		c.incrementValue(increment);
	}

	@Override
	public void removeRowsMatching(final RowFilter rowFilter) {
		final Iterator<Entry<String, Row>> it = this.table.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, Row> entry = it.next();
			final String rowName = entry.getKey();
			if (rowFilter.matches(entry.getValue())) {
				it.remove();
				this.rowNames.remove(rowName);
			}
		}
	}

	@Override
	public int rowCount() {
		return this.table.size();
	}

	@Override
	public void setParamAggregateStrings(final boolean aggregateStrings) {
		this.aggregateStrings = aggregateStrings;
	}

	@Override
	public void setParamRestrictToExcelSize(final boolean b) {
		this.restrictToExcelSize = b;
	}

	/**
	 * Set an {@link IRowInsertionHandler} which is called before and after each
	 * row insertion (either during read or create).
	 *
	 * @see IRowInsertionHandler
	 * @param rowInsertionHandler never null
	 */
	public void setRowInsertionHandler(final IRowInsertionHandler rowInsertionHandler) {
		this.rowInsertionHandler = rowInsertionHandler;
	}

	@Override
	public void setValueInitial(final String rowName, final String columnName, final String value)
			throws IllegalStateException {
		final IRow row = getOrCreateRow(rowName, true);
		row.setValue(columnName, value, true);
	}

	@Override
	public void setValueInitial(final String rowName, final String columnName, final long value)
			throws IllegalStateException {
		final IRow row = getOrCreateRow(rowName, true);
		row.setValue(columnName, value, true);
	}

	@Override
	public Map<String, SparseTable> split(final String colName) {
		final Map<String, SparseTable> map = new HashMap<String, SparseTable>();

		for (final String rowName : this.rowNames) {
			final IRow row = getOrCreateRow(rowName, false);
			final String currentValue = row.getValue(colName);

			SparseTable table = map.get(currentValue);
			if (table == null) {
				table = new SparseTable();
				map.put(currentValue, table);
			}

			final IRow copyRow = table.getOrCreateRow(rowName, true);
			copyRow.addAll(row);
		}

		return map;
	}

	@Override
	public void visitRows(final IRowVisitor rowVisitor) {
		for (final IRow row : this) {
			rowVisitor.visit(row);
		}
	}

	@Override
	public void handleRow(final String rowName, final IReadableRow readableRow) {
		final Row row = new Row(rowName, this);
		// copy content
		for (final Entry<String, ICell> entry : readableRow.entrySet()) {
			row.setValue(entry.getKey(), entry.getValue().getValue());
		}
		addRow(rowName, row);
	}

	@Override
	public Iterable<String> getColumnNamesSorted() {
		return this.columnNames;
		// if(this.columnNames instanceof TreeSet<?>) {
		// } else {
		// return null;
		// }
	}

	@Override
	public void handleHeaderRow(final Collection<String> columnNames) {
		this.columnNames.addAll(columnNames);
	}
}

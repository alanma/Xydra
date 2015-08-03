package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;
import org.xydra.csv.ISparseTable;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class Row extends AbstractRow implements IRow {

	private static final Logger log = LoggerFactory.getLogger(Row.class);

	private static final long serialVersionUID = -1859613946021005526L;

	private final HashMap<String, ICell> map = new HashMap<String, ICell>();

	private final ISparseTable table;

	/**
	 * @param table
	 *            used to read configuration and column names
	 */
	Row(final String key, final ISparseTable table) {
		super(key);
		assert table != null;
		this.table = table;
	}


	@Override
	public void aggregate(final IReadableRow row, final String[] keyColumnNames) {
		assert row != null;
		for (final String colName : this.table.getColumnNames()) {
			if (colName.equals(ROW_KEY)) {
				// skip row key
			} else if (!contains(keyColumnNames, colName)) {
				// skip key columns
			} else {
				final String thisValue = getValue(colName);
				final String otherValue = row.getValue(colName);
				if (otherValue == null || otherValue.equals("")) {
					// ignore
				} else {
					// try as number
					try {
						long thisLong;
						if (thisValue == null) {
							thisLong = 0;
						} else {
							thisLong = Long.parseLong(thisValue);
						}
						final long otherLong = Long.parseLong(otherValue);
						final long sum = thisLong + otherLong;
						if (sum == 0) {
							removeValue(colName);
						} else {
							this.setValue(colName, "" + sum);
						}
					} catch (final NumberFormatException e) {
						// string concatenation
						if (otherValue.equals("")) {
							// do nothing
						} else {
							if (thisValue == null || thisValue.equals("")) {
								this.setValue(colName, otherValue);
							} else if (thisValue.equals(otherValue)) {
								// do nothing
							} else {
								if (this.table.getParamAggregateStrings()) {
									if (!Shared.contains(thisValue, otherValue)) {
										final String concat = thisValue + "|" + otherValue;
										this.setValue(colName, concat);
									}
								}
							}
						}
					}
				}
			}
		}
	}


	@Override
	public Set<Entry<String, ICell>> entrySet() {
		return this.map.entrySet();
	}

	// public String getKey() {
	// return this.getValue(ROW_KEY);
	// }
	//
	// public void setKey(String rowKey) {
	// setValue(ROW_KEY, rowKey);
	// }


	@Override
	public Collection<String> getColumnNames() {
		return this.map.keySet();
	}

	@Override
	public ICell getOrCreateCell(final String column, final boolean create) {
		ICell c = this.map.get(column);
		if (c == null && create) {
			if (this.table.getColumnNames().size() == SparseTable.EXCEL_MAX_COLS) {
				log.warn("Cannot add the " + SparseTable.EXCEL_MAX_COLS
						+ "th column - that is Excels limit");
				if (this.table.getParamRestrictToExcelSize()) {
					throw new ExcelLimitException("Column limit reached");
				}
			}
			c = new Cell();
			this.map.put(column, c);
			// maintain index of column name
			this.table.addColumnName(column);
		}
		return c;
	}


	@Override
	public String getValue(final String columnName) {
		final ICell cell = getOrCreateCell(columnName, false);
		return cell == null ? "null" : cell.getValue();
	}


	@Override
	public Iterator<ICell> iterator() {
		return this.map.values().iterator();
	}

	@Override
	protected void removeValue(final String colName) {
		this.map.remove(colName);
		// IMPROVE: colName remains indexed, we don't know where else the
		// colName is used
	}
}

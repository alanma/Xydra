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


public class Row implements IRow {
	
	private static final String ROW_KEY = "ROW";
	
	private static final long serialVersionUID = -1859613946021005526L;
	
	private HashMap<String,ICell> map = new HashMap<String,ICell>();
	
	private final ISparseTable table;
	
	/**
	 * @param table used to read configuration and column names
	 */
	Row(ISparseTable table) {
		this.table = table;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.csv.impl.memory.IRow#addAll(org.xydra.csv.impl.memory.IRow)
	 */
	public void addAll(IReadableRow otherRow) {
		for(String colName : otherRow.getColumnNames()) {
			this.setValue(colName, otherRow.getValue(colName), true);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.csv.impl.memory.IRow#aggregate(org.xydra.csv.impl.memory.IRow,
	 * java.lang.String[])
	 */
	public void aggregate(IReadableRow row, String[] keyColumnNames) {
		assert row != null;
		for(String colName : this.table.getColumnNames()) {
			if(colName.equals(ROW_KEY)) {
				// skip row key
			} else if(!contains(keyColumnNames, colName)) {
				// skip key columns
			} else {
				String thisValue = this.getValue(colName);
				String otherValue = row.getValue(colName);
				if(otherValue == null || otherValue.equals("")) {
					// ignore
				} else {
					// try as number
					try {
						long thisLong;
						if(thisValue == null) {
							thisLong = 0;
						} else {
							thisLong = Long.parseLong(thisValue);
						}
						long otherLong = Long.parseLong(otherValue);
						long sum = thisLong + otherLong;
						if(sum == 0) {
							this.removeValue(colName);
						} else {
							this.setValue(colName, "" + sum);
						}
					} catch(NumberFormatException e) {
						// string concatenation
						if(otherValue.equals("")) {
							// do nothing
						} else {
							if(thisValue == null || thisValue.equals("")) {
								this.setValue(colName, otherValue);
							} else {
								if(this.table.getParamAggregateStrings()) {
									String concat = thisValue + "|" + otherValue;
									this.setValue(colName, concat);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#appendValue(java.lang.String,
	 * java.lang.String, int)
	 */
	public void appendValue(String columnName, String value, int maximalFieldLength) {
		if(value == null || value.equals("")) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.appendString(value, maximalFieldLength);
			} catch(IllegalStateException e) {
				throw new IllegalStateException("Could not append value in column (" + columnName
				        + "). Stored was " + this.getValue(columnName), e);
			}
		}
	}
	
	/**
	 * @param array
	 * @param value
	 * @return true if array of String contains value
	 */
	private boolean contains(String[] array, String value) {
		for(int i = 0; i < array.length; i++) {
			if(array[i].equals(value))
				return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#entrySet()
	 */
	public Set<Entry<String,ICell>> entrySet() {
		return this.map.entrySet();
	}
	
	// public String getKey() {
	// return this.getValue(ROW_KEY);
	// }
	//
	// public void setKey(String rowKey) {
	// setValue(ROW_KEY, rowKey);
	// }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#getColumnNames()
	 */
	public Collection<String> getColumnNames() {
		return this.map.keySet();
	}
	
	ICell getOrCreateCell(String column, boolean create) {
		ICell c = this.map.get(column);
		if(c == null && create) {
			if(this.table.getColumnNames().size() == CsvTable.EXCEL_MAX_COLS) {
				CsvTable.log.warn("Cannot add the " + CsvTable.EXCEL_MAX_COLS
				        + "th column - that is Excels limit");
				if(this.table.getParamRestrictToExcelSize())
					throw new ExcelLimitException("Column limit reached");
			}
			c = new Cell();
			this.map.put(column, c);
			// maintain index of column name
			this.table.addColumnName(column);
		}
		return c;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#getValue(java.lang.String)
	 */
	public String getValue(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);
		return cell == null ? "null" : cell.getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#getValueAsDouble(java.lang.String)
	 */
	public double getValueAsDouble(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);
		
		if(cell == null) {
			return 0;
		} else {
			return cell.getValueAsDouble();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#getValueAsLong(java.lang.String)
	 */
	public long getValueAsLong(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);
		
		if(cell == null) {
			return 0;
		} else {
			return cell.getValueAsLong();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#incrementValue(java.lang.String, int)
	 */
	public void incrementValue(String columnName, int increment) {
		if(increment == 0) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.incrementValue(increment);
			} catch(IllegalStateException e) {
				throw new IllegalStateException("Could not increment value in column ("
				        + columnName + "). Stored was " + this.getValue(columnName), e);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#iterator()
	 */
	public Iterator<ICell> iterator() {
		return this.map.values().iterator();
	}
	
	private void removeValue(String colName) {
		this.map.remove(colName);
		// IMPROVE: colName remains indexed, we don't know where else the
		// colName is used
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#setValue(java.lang.String, long,
	 * boolean)
	 */
	public void setValue(String columnName, long value, boolean initial) {
		if(value == 0) {
			// nothing to set, keep table sparse
		} else {
			setValue(columnName, "" + value, initial);
		}
	}
	
	void setValue(String columnName, String value) {
		ICell c = getOrCreateCell(columnName, true);
		c.setValue(value, false);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#setValue(java.lang.String,
	 * java.lang.String, boolean)
	 */
	public void setValue(String columnName, String value, boolean initial) {
		if(value == null) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.setValue(value, initial);
			} catch(IllegalStateException e) {
				throw new IllegalStateException("Could not set value in column (" + columnName
				        + ")", e);
			}
		}
	}
}

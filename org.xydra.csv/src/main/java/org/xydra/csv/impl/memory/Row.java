package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.WrongDatatypeException;


/**
 * A Row is a sparse representation of a table row.
 * 
 * @author voelkel
 */
public class Row extends HashMap<String,org.xydra.csv.impl.memory.Cell> {
	
	/**
     * 
     */
	private final SparseTable table;
	
	/**
	 * @param table
	 */
	Row(SparseTable table) {
		this.table = table;
	}
	
	private static final String ROW_KEY = "ROW";
	
	private static final long serialVersionUID = -1859613946021005526L;
	
	/**
	 * Add all values from the otherRow to this Row
	 * 
	 * @param otherRow from which to add all cells
	 */
	public void addAll(Row otherRow) {
		for(String colName : otherRow.getColumnNames()) {
			this.setValue(colName, otherRow.getValue(colName), true);
		}
	}
	
	/**
	 * Aggregate given row into this row
	 * 
	 * @param row never null
	 */
	public void aggregate(Row row, String[] keyColumnNames) {
		assert row != null;
		for(String colName : this.table.columnNames) {
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
								if(this.table.aggregateStrings) {
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
	
	public void appendValue(String columnName, String value, int maximalFieldLength) {
		if(value == null || value.equals("")) {
			// nothing to set, keep table sparse
		} else {
			Cell cell = getOrCreateCell(columnName, true);
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
	
	public Collection<String> getColumnNames() {
		return this.keySet();
	}
	
	// public String getKey() {
	// return this.getValue(ROW_KEY);
	// }
	//
	// public void setKey(String rowKey) {
	// setValue(ROW_KEY, rowKey);
	// }
	
	Cell getOrCreateCell(String column, boolean create) {
		Cell c = get(column);
		if(c == null && create) {
			if(this.table.columnNames.size() == CsvTable.EXCEL_MAX_COLS) {
				CsvTable.log.warn("Cannot add the " + CsvTable.EXCEL_MAX_COLS
				        + "th column - that is Excels limit");
				if(this.table.restrictToExcelSize)
					throw new ExcelLimitException("Column limit reached");
			}
			c = new Cell();
			put(column, c);
			// maintain index of column name
			this.table.columnNames.add(column);
		}
		return c;
	}
	
	/**
	 * @param columnName for which to get the value in this row
	 * @return the value or the String "null"
	 */
	public String getValue(String columnName) {
		Cell cell = getOrCreateCell(columnName, false);
		return cell == null ? "null" : cell.getValue();
	}
	
	public double getValueAsDouble(String columnName) {
		Cell cell = getOrCreateCell(columnName, false);
		
		if(cell == null) {
			return 0;
		} else {
			return cell.getValueAsDouble();
		}
	}
	
	/**
	 * @param columnName never null
	 * @return 0 if value is not set.
	 * @throws WrongDatatypeException if value was set, but could not be parsed
	 *             as long.
	 */
	public long getValueAsLong(String columnName) {
		Cell cell = getOrCreateCell(columnName, false);
		
		if(cell == null) {
			return 0;
		} else {
			return cell.getValueAsLong();
		}
	}
	
	public void incrementValue(String columnName, int increment) {
		if(increment == 0) {
			// nothing to set, keep table sparse
		} else {
			Cell cell = getOrCreateCell(columnName, true);
			try {
				cell.incrementValue(increment);
			} catch(IllegalStateException e) {
				throw new IllegalStateException("Could not increment value in column ("
				        + columnName + "). Stored was " + this.getValue(columnName), e);
			}
		}
	}
	
	public Iterator<Cell> iterator() {
		return this.values().iterator();
	}
	
	private void removeValue(String colName) {
		this.remove(colName);
		// IMPROVE: colName remains indexed, we don't know where else the
		// colName is used
	}
	
	public void setValue(String columnName, long value, boolean initial) {
		if(value == 0) {
			// nothing to set, keep table sparse
		} else {
			setValue(columnName, "" + value, initial);
		}
	}
	
	void setValue(String columnName, String value) {
		Cell c = getOrCreateCell(columnName, true);
		c.setValue(value, false);
	}
	
	/**
	 * @param columnName never null
	 * @param value may be null
	 * @param initial if false and there was a already a value.
	 * @throws IllegalStateException if 'initial' is true and there was a
	 *             previous value
	 */
	public void setValue(String columnName, String value, boolean initial) {
		if(value == null) {
			// nothing to set, keep table sparse
		} else {
			Cell cell = getOrCreateCell(columnName, true);
			try {
				cell.setValue(value, initial);
			} catch(IllegalStateException e) {
				throw new IllegalStateException("Could not set value in column (" + columnName
				        + ")", e);
			}
		}
	}
}

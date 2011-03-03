package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;
import org.xydra.csv.ISparseTable;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class Row extends AbstractRow implements IRow {
	
	private static final Logger log = LoggerFactory.getLogger(Row.class);
	
	private static final long serialVersionUID = -1859613946021005526L;
	
	private HashMap<String,ICell> map = new HashMap<String,ICell>();
	
	private final ISparseTable table;
	
	/**
	 * @param table used to read configuration and column names
	 */
	Row(String key, ISparseTable table) {
		super(key);
		assert table != null;
		this.table = table;
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
	
	@Override
	public ICell getOrCreateCell(String column, boolean create) {
		ICell c = this.map.get(column);
		if(c == null && create) {
			if(this.table.getColumnNames().size() == CsvTable.EXCEL_MAX_COLS) {
				log.warn("Cannot add the " + CsvTable.EXCEL_MAX_COLS
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
	@Override
	public String getValue(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);
		return cell == null ? "null" : cell.getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#iterator()
	 */
	public Iterator<ICell> iterator() {
		return this.map.values().iterator();
	}
	
	@Override
	protected void removeValue(String colName) {
		this.map.remove(colName);
		// IMPROVE: colName remains indexed, we don't know where else the
		// colName is used
	}
}

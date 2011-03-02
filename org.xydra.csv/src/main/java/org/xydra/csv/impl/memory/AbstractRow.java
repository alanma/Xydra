package org.xydra.csv.impl.memory;

import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;


public abstract class AbstractRow extends AbstractReadableRow implements IRow {
	
	AbstractRow(String key) {
		super(key);
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
	
	protected abstract void removeValue(String colName);
	
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

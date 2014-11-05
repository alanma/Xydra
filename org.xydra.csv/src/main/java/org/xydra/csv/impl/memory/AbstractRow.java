package org.xydra.csv.impl.memory;

import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;

public abstract class AbstractRow extends AbstractReadableRow implements IRow {

	private static final long serialVersionUID = -2543133872138623905L;

	AbstractRow(String key) {
		super(key);
	}

	
	@Override
	public void addAll(IReadableRow otherRow) {
		for (String colName : otherRow.getColumnNames()) {
			this.setValue(colName, otherRow.getValue(colName), true);
		}
	}

	protected abstract void removeValue(String colName);

	
	@Override
	public void appendValue(String columnName, String value, int maximalFieldLength) {
		if (value == null || value.equals("")) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.appendString(value, maximalFieldLength);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not append value in column (" + columnName
						+ "). Stored was " + this.getValue(columnName), e);
			}
		}
	}

	
	@Override
	public void incrementValue(String columnName, int increment) {
		if (increment == 0) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.incrementValue(increment);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not increment value in column ("
						+ columnName + "). Stored was " + this.getValue(columnName), e);
			}
		}
	}

	
	@Override
	public void setValue(String columnName, long value, boolean initial) {
		if (value == 0) {
			// nothing to set, keep table sparse
		} else {
			setValue(columnName, "" + value, initial);
		}
	}

	void setValue(String columnName, String value) {
		ICell c = getOrCreateCell(columnName, true);
		c.setValue(value, false);
	}

	
	@Override
	public void setValue(String columnName, String value, boolean initial) {
		if (value == null) {
			// nothing to set, keep table sparse
		} else {
			ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.setValue(value, initial);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("Could not set value in column (" + columnName
						+ ")", e);
			}
		}
	}

	@Override
	public void setValue(String columnName, double value, boolean initial) {
		if (value == 0) {
			// nothing to set, keep table sparse
		} else {
			String vStr = "" + value;
			String germanValueString = vStr.replace(".", ",");
			setValue(columnName, germanValueString, initial);
		}
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IRow && ((IRow) other).getKey().equals(getKey());
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
}

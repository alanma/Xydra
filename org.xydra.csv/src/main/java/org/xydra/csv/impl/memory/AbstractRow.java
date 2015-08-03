package org.xydra.csv.impl.memory;

import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;

public abstract class AbstractRow extends AbstractReadableRow implements IRow {

	private static final long serialVersionUID = -2543133872138623905L;

	AbstractRow(final String key) {
		super(key);
	}


	@Override
	public void addAll(final IReadableRow otherRow) {
		for (final String colName : otherRow.getColumnNames()) {
			this.setValue(colName, otherRow.getValue(colName), true);
		}
	}

	protected abstract void removeValue(String colName);


	@Override
	public void appendValue(final String columnName, final String value, final int maximalFieldLength) {
		if (value == null || value.equals("")) {
			// nothing to set, keep table sparse
		} else {
			final ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.appendString(value, maximalFieldLength);
			} catch (final IllegalStateException e) {
				throw new IllegalStateException("Could not append value in column (" + columnName
						+ "). Stored was " + getValue(columnName), e);
			}
		}
	}


	@Override
	public void incrementValue(final String columnName, final int increment) {
		if (increment == 0) {
			// nothing to set, keep table sparse
		} else {
			final ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.incrementValue(increment);
			} catch (final IllegalStateException e) {
				throw new IllegalStateException("Could not increment value in column ("
						+ columnName + "). Stored was " + getValue(columnName), e);
			}
		}
	}


	@Override
	public void setValue(final String columnName, final long value, final boolean initial) {
		if (value == 0) {
			// nothing to set, keep table sparse
		} else {
			setValue(columnName, "" + value, initial);
		}
	}

	void setValue(final String columnName, final String value) {
		final ICell c = getOrCreateCell(columnName, true);
		c.setValue(value, false);
	}


	@Override
	public void setValue(final String columnName, final String value, final boolean initial) {
		if (value == null) {
			// nothing to set, keep table sparse
		} else {
			final ICell cell = getOrCreateCell(columnName, true);
			try {
				cell.setValue(value, initial);
			} catch (final IllegalStateException e) {
				throw new IllegalStateException("Could not set value in column (" + columnName
						+ ")", e);
			}
		}
	}

	@Override
	public void setValue(final String columnName, final double value, final boolean initial) {
		if (value == 0) {
			// nothing to set, keep table sparse
		} else {
			final String vStr = "" + value;
			final String germanValueString = vStr.replace(".", ",");
			setValue(columnName, germanValueString, initial);
		}
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof IRow && ((IRow) other).getKey().equals(getKey());
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
}

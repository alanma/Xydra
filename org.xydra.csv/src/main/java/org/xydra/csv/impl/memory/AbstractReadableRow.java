package org.xydra.csv.impl.memory;

import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;

public abstract class AbstractReadableRow implements IReadableRow {

	protected static final String ROW_KEY = "ROW";

	public static final long serialVersionUID = -1859613946021005526L;

	private final String key;

	/**
	 * @param key
	 *            (the row name)
	 */
	AbstractReadableRow(final String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	/**
	 * @param array
	 * @param value
	 * @return true if array of String contains value
	 */
	protected boolean contains(String[] array, String value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value))
				return true;
		}
		return false;
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

	public abstract ICell getOrCreateCell(String columnName, boolean create);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.IRow#getValueAsDouble(java.lang.String)
	 */
	@Override
	public double getValueAsDouble(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);

		if (cell == null) {
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
	@Override
	public long getValueAsLong(String columnName) {
		ICell cell = getOrCreateCell(columnName, false);

		if (cell == null) {
			return 0;
		} else {
			return cell.getValueAsLong();
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ \n");
		for (String colName : getColumnNames()) {
			buf.append("  " + colName + ": '" + getValue(colName) + "', \n");
		}
		buf.append(" }");
		return buf.toString();
	}

}

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
	protected boolean contains(final String[] array, final String value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public String getValue(final String columnName) {
		final ICell cell = getOrCreateCell(columnName, false);
		return cell == null ? "null" : cell.getValue();
	}

	public abstract ICell getOrCreateCell(String columnName, boolean create);


	@Override
	public double getValueAsDouble(final String columnName) {
		final ICell cell = getOrCreateCell(columnName, false);

		if (cell == null) {
			return 0;
		} else {
			return cell.getValueAsDouble();
		}
	}


	@Override
	public long getValueAsLong(final String columnName) {
		final ICell cell = getOrCreateCell(columnName, false);

		if (cell == null) {
			return 0;
		} else {
			return cell.getValueAsLong();
		}
	}

	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append("{ \n");
		for (final String colName : getColumnNames()) {
			buf.append("  " + colName + ": '" + getValue(colName) + "', \n");
		}
		buf.append(" }");
		return buf.toString();
	}

}

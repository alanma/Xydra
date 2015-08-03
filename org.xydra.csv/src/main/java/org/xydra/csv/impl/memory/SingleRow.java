package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;

public class SingleRow extends AbstractReadableRow implements IReadableRow {

	private static final long serialVersionUID = -7970004628196813272L;

	private final Map<String, ICell> map = new HashMap<String, ICell>();

	/**
	 * @param key
	 *            (the row name)
	 */
	public SingleRow(final String key) {
		super(key);
	}

	public SingleRow(final String key, final String[][] arrayOfPairs) {
		this(key);
		for (final String[] pair : arrayOfPairs) {
			assert pair.length == 2;
			this.map.put(pair[0], new Cell(pair[1]));
		}
	}

	public SingleRow(final String key, final Map<String, String> map) {
		this(key);
		for (final Entry<String, String> entry : map.entrySet()) {
			this.map.put(entry.getKey(), new Cell(entry.getValue()));
		}
	}

	@Override
	public Collection<String> getColumnNames() {
		return this.map.keySet();
	}

	@Override
	public Iterator<ICell> iterator() {
		return this.map.values().iterator();
	}

	protected void removeValue(final String colName) {
		this.map.remove(colName);
	}

	@Override
	public ICell getOrCreateCell(final String columnName, final boolean create) {
		ICell cell = this.map.get(columnName);
		if (cell == null && create) {
			cell = new Cell();
			this.map.put(columnName, cell);
		}
		return cell;
	}

	@Override
	public Set<Entry<String, ICell>> entrySet() {
		return this.map.entrySet();
	}

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

}

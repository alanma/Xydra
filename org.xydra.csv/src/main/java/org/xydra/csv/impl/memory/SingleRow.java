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
	
	private Map<String,ICell> map = new HashMap<String,ICell>();
	
	public SingleRow() {
	}
	
	public SingleRow(String[][] arrayOfPairs) {
		for(String[] pair : arrayOfPairs) {
			assert pair.length == 2;
			this.map.put(pair[0], new Cell(pair[1]));
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
	
	protected void removeValue(String colName) {
		this.map.remove(colName);
	}
	
	public ICell getOrCreateCell(String columnName, boolean create) {
		ICell cell = this.map.get(columnName);
		if(cell == null && create) {
			cell = new Cell();
			this.map.put(columnName, cell);
		}
		return cell;
	}
	
	@Override
	public Set<Entry<String,ICell>> entrySet() {
		return this.map.entrySet();
	}
	
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

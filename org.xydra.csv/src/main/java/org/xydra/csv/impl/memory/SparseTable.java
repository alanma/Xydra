package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.IRowInsertionHandler;
import org.xydra.csv.IRowVisitor;
import org.xydra.csv.ISparseTable;
import org.xydra.csv.RowFilter;
import org.xydra.csv.WrongDatatypeException;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Maintains a sparse table, organised by rows and columns.
 * 
 * Insertion order is preserved.
 * 
 * @author voelkel
 */
public class SparseTable implements ISparseTable {
	
	public static final int EXCEL_MAX_COLS = 255;
	
	public static final int EXCEL_MAX_ROWS = 65535;
	
	static Logger log = LoggerFactory.getLogger(SparseTable.class);
	
	boolean aggregateStrings = true;
	
	Collection<String> columnNames;
	
	/**
	 * Older Excel limits: You are trying to open a file that contains more than
	 * 65,536 rows or 256 columns.
	 */
	boolean restrictToExcelSize = false;
	
	private IRowInsertionHandler rowInsertionHandler;
	
	/* maintain row insertion order */
	protected List<String> rowNames = new LinkedList<String>();
	
	protected Map<String,Row> table = new HashMap<String,Row>();
	
	public SparseTable() {
		this.columnNames = new TreeSet<String>();
	}
	
	/**
	 * @param maintainColumnInsertionOrder if true, the insertion order of
	 *            columns is maintained. If false, the default automatic
	 *            alphabetic sorting is on.
	 */
	public SparseTable(boolean maintainColumnInsertionOrder) {
		if(maintainColumnInsertionOrder) {
			this.columnNames = new LinkedHashSet<String>();
		} else {
			this.columnNames = new TreeSet<String>();
		}
	}
	
	/**
	 * Add all values from the other table into this table
	 * 
	 * @param other never null
	 */
	public void addAll(SparseTable other) {
		for(Entry<String,Row> entry : other.table.entrySet()) {
			Row thisRow = this.getOrCreateRow(entry.getKey(), true);
			for(Entry<String,Cell> rowEntry : entry.getValue().entrySet()) {
				thisRow.setValue(rowEntry.getKey(), rowEntry.getValue().getValue());
			}
		}
	}
	
	/**
	 * Add checked
	 * 
	 * @param rowName
	 * @param row
	 */
	protected void addRow(String rowName, Row row) {
		boolean insert = true;
		if(this.rowInsertionHandler != null) {
			insert &= this.rowInsertionHandler.beforeRowInsertion(row);
		}
		if(insert) {
			this.insertRow(rowName, row);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#aggregate(java.lang.String[])
	 */
	public void aggregate(String[] keyColumnNames) {
		// temporary index of occurring keys and their row
		Map<String,Row> compoundKeys2row = new HashMap<String,Row>(this.rowCount());
		Iterator<Row> rowIt = this.table.values().iterator();
		// Collection<String> rowKeysToBeRemoved = new LinkedList<String>();
		long processed = 0;
		long aggregated = 0;
		while(rowIt.hasNext()) {
			Row row = rowIt.next();
			
			// calculate compound key FIXME takes 50% of performance
			StringBuffer compoundKeyBuffer = new StringBuffer(100);
			for(String keyColumnName : keyColumnNames) {
				String value = row.getValue(keyColumnName);
				if(value != null) {
					compoundKeyBuffer.append(value);
				}
			}
			String compoundKey = compoundKeyBuffer.toString();
			
			if(compoundKeys2row.containsKey(compoundKey)) {
				// aggregate row into existing row
				Row masterRow = compoundKeys2row.get(compoundKey);
				masterRow.aggregate(row, keyColumnNames);
				aggregated++;
				
				// delete row
				rowIt.remove();
				// rowKeysToBeRemoved.add(row.getKey());
			} else {
				// just mark key as seen
				compoundKeys2row.put(compoundKey, row);
			}
			
			processed++;
			if(processed % 1000 == 0) {
				log.info("Aggregate processed " + processed + " rows, aggregated " + aggregated);
			}
		}
		// // delete
		// log.info("Deleting " + rowKeysToBeRemoved.size() + " rows");
		// for (String rowKeyToBeRemoved : rowKeysToBeRemoved) {
		// this.table.remove(rowKeyToBeRemoved);
		// }
		log.info(this.rowCount() + " rows with aggregated data remain");
	}
	
	/**
	 * Used only by tests.
	 * 
	 * @param row
	 * @param column
	 * @param s
	 * @param maximalFieldLength
	 */
	void appendString(String row, String column, String s, int maximalFieldLength) {
		Row r = getOrCreateRow(row, true);
		Cell c = r.getOrCreateCell(column, true);
		c.appendString(s, maximalFieldLength);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#clear()
	 */
	public void clear() {
		this.rowNames.clear();
		this.table.clear();
	}
	
	protected int colCount() {
		return this.columnNames.size();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#drop(java.lang.String, java.lang.String)
	 */
	public ISparseTable drop(String key, String value) {
		ISparseTable target = new SparseTable();
		for(String rowName : this.rowNames) {
			Row sourceRow = this.getOrCreateRow(rowName, false);
			if(!sourceRow.getValue(key).equals(value)) {
				// copy row
				Row targetRow = target.getOrCreateRow(rowName, true);
				for(String colName : sourceRow.keySet()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#filter(java.lang.String, java.lang.String)
	 */
	public ISparseTable filter(String key, String value) {
		ISparseTable target = new SparseTable();
		for(String rowName : this.rowNames) {
			Row sourceRow = this.getOrCreateRow(rowName, false);
			if(sourceRow.getValue(key).equals(value)) {
				// copy row
				Row targetRow = target.getOrCreateRow(rowName, true);
				for(String colName : sourceRow.keySet()) {
					targetRow.setValue(colName, sourceRow.getValue(colName), true);
				}
			}
		}
		return target;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#getOrCreateRow(java.lang.String, boolean)
	 */
	public Row getOrCreateRow(String rowName, boolean create) {
		Row row = this.table.get(rowName);
		if(row == null && create) {
			row = new Row(this);
			this.insertRow(rowName, row);
		}
		return row;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#getValue(java.lang.String, java.lang.String)
	 */
	public String getValue(String row, String column) {
		Row r = getOrCreateRow(row, false);
		if(r == null) {
			return null;
		}
		Cell c = r.getOrCreateCell(column, false);
		if(c == null) {
			return null;
		}
		return c.getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#incrementValue(java.lang.String,
	 * java.lang.String, int)
	 */
	public void incrementValue(String row, String column, int increment)
	        throws WrongDatatypeException {
		Row r = getOrCreateRow(row, true);
		Cell c = r.getOrCreateCell(column, true);
		c.incrementValue(increment);
	}
	
	private void insertRow(String rowName, Row row) {
		if(this.table.size() == EXCEL_MAX_ROWS) {
			log.warn("Adding the " + EXCEL_MAX_ROWS + "th row - that is Excels limit");
			if(this.restrictToExcelSize)
				throw new ExcelLimitException("Row limit reached");
		}
		/* new key? */
		if(this.table.containsKey(rowName)) {
			// attempt a merge
			Row existingRow = this.getOrCreateRow(rowName, false);
			
			for(Map.Entry<String,Cell> entry : row.entrySet()) {
				try {
					existingRow.setValue(entry.getKey(), entry.getValue().getValue(), true);
				} catch(IllegalStateException ex) {
					throw new IllegalStateException("Table contains already a row named '"
					        + rowName + "' and for key '" + entry.getKey()
					        + "' there was already a value.", ex);
				}
			}
		} else {
			assert !this.rowNames.contains(rowName);
			this.rowNames.add(rowName);
			this.table.put(rowName, row);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#iterator()
	 */
	public Iterator<Row> iterator() {
		return this.table.values().iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#removeRowsMatching(org.xydra.csv.RowFilter)
	 */
	public void removeRowsMatching(RowFilter rowFilter) {
		Iterator<Entry<String,Row>> it = this.table.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String,Row> entry = it.next();
			if(rowFilter.matches(entry.getValue())) {
				it.remove();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#rowCount()
	 */
	public int rowCount() {
		return this.table.size();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#setParamAggregateStrings(boolean)
	 */
	public void setParamAggregateStrings(boolean aggregateStrings) {
		this.aggregateStrings = aggregateStrings;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#setParamRestrictToExcelSize(boolean)
	 */
	public void setParamRestrictToExcelSize(boolean b) {
		this.restrictToExcelSize = b;
	}
	
	/**
	 * Set an {@link IRowInsertionHandler} which is called before and after each
	 * row insertion (either during read or create).
	 * 
	 * @see IRowInsertionHandler
	 * @param rowInsertionHandler never null
	 */
	public void setRowInsertionHandler(IRowInsertionHandler rowInsertionHandler) {
		this.rowInsertionHandler = rowInsertionHandler;
	}
	
	/**
	 * Set an initial value to cell (rowName, columnName)
	 * 
	 * @param rowName never null
	 * @param columnName never null
	 * @param value may be null
	 * @throws IllegalStateException if there was already a value
	 */
	public void setValueInitial(String rowName, String columnName, String value)
	        throws IllegalStateException {
		Row row = getOrCreateRow(rowName, true);
		row.setValue(columnName, value, true);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#split(java.lang.String)
	 */
	public Map<String,SparseTable> split(String colName) {
		Map<String,SparseTable> map = new HashMap<String,SparseTable>();
		
		for(String rowName : this.rowNames) {
			Row row = this.getOrCreateRow(rowName, false);
			String currentValue = row.getValue(colName);
			
			SparseTable table = map.get(currentValue);
			if(table == null) {
				table = new SparseTable();
				map.put(currentValue, table);
			}
			
			Row copyRow = table.getOrCreateRow(rowName, true);
			copyRow.addAll(row);
		}
		
		return map;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#visitRows(org.xydra.csv.IRowVisitor)
	 */
	public void visitRows(IRowVisitor rowVisitor) {
		for(Row row : this) {
			rowVisitor.visit(row);
		}
	}
	
}

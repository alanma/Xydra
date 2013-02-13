package org.xydra.csv.impl.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.ICell;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;
import org.xydra.csv.IRowInsertionHandler;
import org.xydra.csv.IRowVisitor;
import org.xydra.csv.ISparseTable;
import org.xydra.csv.RowFilter;
import org.xydra.csv.WrongDatatypeException;
import org.xydra.index.iterator.ReadOnlyIterator;
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
    
    private static Logger log = LoggerFactory.getLogger(SparseTable.class);
    
    boolean aggregateStrings = true;
    
    Set<String> columnNames;
    
    /**
     * Older Excel limits: You are trying to open a file that contains more than
     * 65,536 rows or 256 columns.
     */
    boolean restrictToExcelSize = false;
    
    private IRowInsertionHandler rowInsertionHandler;
    
    // ----------------- row handling
    
    /* maintain any row insertion order */
    private List<String> rowNames = new LinkedList<String>();
    
    /* automatically sorted */
    private Map<String,Row> table = new TreeMap<String,Row>();
    
    private void insertRow(String rowName, Row row) {
        if(this.rowCount() == EXCEL_MAX_ROWS) {
            log.warn("Adding the " + EXCEL_MAX_ROWS + "th row - that is Excels limit");
            if(this.restrictToExcelSize)
                throw new ExcelLimitException("Row limit reached");
        }
        
        /* new key? */
        if(this.table.containsKey(rowName)) {
            assert this.rowNames.contains(rowName);
            // attempt a merge
            IRow existingRow = this.getOrCreateRow(rowName, false);
            
            for(Map.Entry<String,ICell> entry : row.entrySet()) {
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
    
    protected Iterable<String> rowNamesIterable() {
        return this.rowNames;
    }
    
    protected Iterator<String> subIterator(int startRow, int endRow) {
        return this.rowNames.subList(startRow, endRow).iterator();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#iterator()
     */
    @Override
    public Iterator<Row> iterator() {
        return new ReadOnlyIterator<Row>(this.table.values().iterator());
    }
    
    // -----------------
    
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
            for(Entry<String,ICell> rowEntry : entry.getValue().entrySet()) {
                thisRow.setValue(rowEntry.getKey(), rowEntry.getValue().getValue());
            }
        }
    }
    
    @Override
    public void addColumnName(String columnName) {
        this.columnNames.add(columnName);
    }
    
    /**
     * Add if {@link IRowInsertionHandler} null or does not object
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
    @Override
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
                IRow masterRow = compoundKeys2row.get(compoundKey);
                masterRow.aggregate(row, keyColumnNames);
                aggregated++;
                
                // delete row
                rowIt.remove();
                this.rowNames.remove(row.getKey());
            } else {
                // just mark key as seen
                compoundKeys2row.put(compoundKey, row);
            }
            
            processed++;
            if(processed % 1000 == 0) {
                log.info("Aggregate processed " + processed + " rows, aggregated " + aggregated);
            }
        }
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
        ICell c = r.getOrCreateCell(column, true);
        c.appendString(s, maximalFieldLength);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#clear()
     */
    @Override
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
    @Override
    public ISparseTable dropColumn(String columnName, String value) {
        ISparseTable target = new SparseTable();
        for(String rowName : this.rowNames) {
            IRow sourceRow = this.getOrCreateRow(rowName, false);
            if(!sourceRow.getValue(columnName).equals(value)) {
                // copy row
                IRow targetRow = target.getOrCreateRow(rowName, true);
                for(String colName : sourceRow.getColumnNames()) {
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
    @Override
    public ISparseTable filter(String key, String value) {
        ISparseTable target = new SparseTable();
        for(String rowName : this.rowNames) {
            IRow sourceRow = this.getOrCreateRow(rowName, false);
            if(sourceRow.getValue(key).equals(value)) {
                // copy row
                IRow targetRow = target.getOrCreateRow(rowName, true);
                for(String colName : sourceRow.getColumnNames()) {
                    targetRow.setValue(colName, sourceRow.getValue(colName), true);
                }
            }
        }
        return target;
    }
    
    @Override
    public Set<String> getColumnNames() {
        return this.columnNames;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#getOrCreateRow(java.lang.String, boolean)
     */
    @Override
    public Row getOrCreateRow(String rowName, boolean create) {
        Row row = this.table.get(rowName);
        if(row == null) {
            assert !this.rowNames.contains(rowName);
            if(create) {
            row = new Row(rowName, this);
            this.insertRow(rowName, row);
        }
        }
        return row;
    }
    
    @Override
    public boolean getParamAggregateStrings() {
        return this.aggregateStrings;
    }
    
    @Override
    public boolean getParamRestrictToExcelSize() {
        return this.restrictToExcelSize;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(String row, String column) {
        Row r = getOrCreateRow(row, false);
        if(r == null) {
            return null;
        }
        ICell c = r.getOrCreateCell(column, false);
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
    @Override
    public void incrementValue(String row, String column, int increment)
            throws WrongDatatypeException {
        Row r = getOrCreateRow(row, true);
        ICell c = r.getOrCreateCell(column, true);
        c.incrementValue(increment);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#removeRowsMatching(org.xydra.csv.RowFilter)
     */
    @Override
    public void removeRowsMatching(RowFilter rowFilter) {
        Iterator<Entry<String,Row>> it = this.table.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String,Row> entry = it.next();
            String rowName = entry.getKey();
            if(rowFilter.matches(entry.getValue())) {
                it.remove();
                this.rowNames.remove(rowName);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#rowCount()
     */
    @Override
    public int rowCount() {
        return this.table.size();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#setParamAggregateStrings(boolean)
     */
    @Override
    public void setParamAggregateStrings(boolean aggregateStrings) {
        this.aggregateStrings = aggregateStrings;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#setParamRestrictToExcelSize(boolean)
     */
    @Override
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
    
    @Override
    public void setValueInitial(String rowName, String columnName, String value)
            throws IllegalStateException {
        IRow row = getOrCreateRow(rowName, true);
        row.setValue(columnName, value, true);
    }
    
    @Override
    public void setValueInitial(String rowName, String columnName, long value)
            throws IllegalStateException {
        IRow row = getOrCreateRow(rowName, true);
        row.setValue(columnName, value, true);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#split(java.lang.String)
     */
    @Override
    public Map<String,SparseTable> split(String colName) {
        Map<String,SparseTable> map = new HashMap<String,SparseTable>();
        
        for(String rowName : this.rowNames) {
            IRow row = this.getOrCreateRow(rowName, false);
            String currentValue = row.getValue(colName);
            
            SparseTable table = map.get(currentValue);
            if(table == null) {
                table = new SparseTable();
                map.put(currentValue, table);
            }
            
            IRow copyRow = table.getOrCreateRow(rowName, true);
            copyRow.addAll(row);
        }
        
        return map;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#visitRows(org.xydra.csv.IRowVisitor)
     */
    @Override
    public void visitRows(IRowVisitor rowVisitor) {
        for(IRow row : this) {
            rowVisitor.visit(row);
        }
    }
    
    @Override
    public void handleRow(String rowName, IReadableRow readableRow) {
        Row row = new Row(rowName, this);
        // copy content
        for(Entry<String,ICell> entry : readableRow.entrySet()) {
            row.setValue(entry.getKey(), entry.getValue().getValue());
        }
        this.addRow(rowName, row);
    }
    
    @Override
    public Iterable<String> getColumnNamesSorted() {
        if(this.columnNames instanceof TreeSet<?>) {
            return this.columnNames;
        } else {
            return null;
        }
    }
    
    @Override
    public void handleHeaderRow(Collection<String> columnNames) {
        this.columnNames.addAll(columnNames);
    }
}

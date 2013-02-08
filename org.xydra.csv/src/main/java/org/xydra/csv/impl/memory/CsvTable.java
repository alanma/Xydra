package org.xydra.csv.impl.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.csv.ExcelLimitException;
import org.xydra.csv.ICsvTable;
import org.xydra.csv.ICsvTableFactory;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRow;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Maintains a sparse table, organised by rows and columns.
 * 
 * Insertion order is preserved.
 * 
 * @author voelkel
 */
public class CsvTable extends CsvCoreTable implements ICsvTable, ICsvTableFactory {
    
    private static Logger log = LoggerFactory.getLogger(CsvTable.class);
    
    public CsvTable(boolean maintainColumnInsertionOrder) {
        super(maintainColumnInsertionOrder);
    }
    
    public CsvTable() {
        super();
    }
    
    /**
     * Dump table to System.out
     * 
     * @throws IOException from System.out
     */
    @Override
    public void dump() throws IOException {
        Writer writer = new OutputStreamWriter(System.out);
        writeTo(writer);
        writer.flush();
    }
    
    /**
     * Dump table to System.out in LaTeX syntax
     * 
     * @throws IOException from System.out
     */
    @Override
    public void dumpToLaTeX() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(System.out);
        toLaTeX(osw);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#readFrom(java.io.File)
     */
    @Override
    public void readFrom(File f) throws IOException {
        log.info("Reading CSV table from " + f.getAbsolutePath() + " Before: " + this.rowCount()
                + " rows and " + this.colCount() + " columns");
        FileInputStream fos = new FileInputStream(f);
        Reader reader = new InputStreamReader(fos, Charset.forName(this.defaultEncoding));
        readFrom(reader, true);
        reader.close();
        // remove "NULL" and "ROW"-columns
        this.columnNames.remove("NULL");
        this.columnNames.remove("ROW");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#readFrom(java.io.Reader)
     */
    @Override
    public void readFrom(Reader r, boolean create) throws IOException {
        CsvReader csvReader = new CsvReader(r, this.readMaxRows);
        Collection<String> columnNames = csvReader.readHeaders();
        assert this.columnNames != null;
        this.columnNames.addAll(columnNames);
        IReadableRow row = csvReader.readDataRow();
        while(row != null) {
            Row tableRow = this.getOrCreateRow(row.getKey(), true);
            tableRow.addAll(row);
            row = csvReader.readDataRow();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#toLaTeX(java.io.Writer)
     */
    @Override
    public void toLaTeX(Writer w) throws IOException {
        // determine padding
        Map<String,Integer> colName2maxLength = new HashMap<String,Integer>();
        for(IRow row : this) {
            for(String colName : this.columnNames) {
                int oldmax = colName2maxLength.get(colName) == null ? 0 : colName2maxLength
                        .get(colName);
                String value = row.getValue(colName);
                int newmax = Math.max(oldmax, value.length());
                colName2maxLength.put(colName, newmax);
            }
        }
        
        // table control header
        w.write("\\begin{tabular}{|");
        
        for(int i = 0; i < this.columnNames.size(); i++) {
            w.write("l|");
        }
        w.write("}\n");
        // table header row
        w.write("\\hline\n");
        
        // calculate the last columns name
        String lastColumnName = null;
        Iterator<String> colNameIt = this.columnNames.iterator();
        while(colNameIt.hasNext()) {
            lastColumnName = colNameIt.next();
        }
        
        for(String colName : this.columnNames) {
            w.write(latexEncode(colName, colName2maxLength.get(colName)));
            if(colName.equals(lastColumnName)) {
                w.write(" \\\\\n");
            } else {
                w.write(" & ");
            }
        }
        w.write("\\hline\n");
        
        for(String rowName : this.rowNames) {
            IRow row = this.getOrCreateRow(rowName, false);
            for(String colName : this.columnNames) {
                w.write(latexEncode(row.getValue(colName), colName2maxLength.get(colName)));
                if(colName.equals(lastColumnName)) {
                    w.write(" \\\\\n");
                } else {
                    w.write(" & ");
                }
            }
        }
        w.write("\\hline\n");
        w.write("\\end{tabular}\n");
        w.flush();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#writeTo(java.io.File)
     */
    @Override
    public void writeTo(File f) throws FileNotFoundException {
        writeTo(f, false);
    }
    
    /**
     * @param f
     * @param append if append is true, the new records must match exactly the
     *            format of the old ones. Otherwise just a big mess is produced.
     * @throws FileNotFoundException
     */
    public void writeTo(File f, boolean append) throws FileNotFoundException {
        log.info("Writing CSV table to " + f.getAbsolutePath());
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(f, append);
        } catch(FileNotFoundException e) {
            // Java's way of saying, Windows locked the file
            File f2 = new File(f.getAbsolutePath() + "-COULD-NOT-"
                    + (append ? "APPEND" : "OVERWRITE"));
            fos = new FileOutputStream(f2);
        }
        Writer writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
        try {
            if(!append && this.splitWhenWritingLargeFiles) {
                log.info("Will write as " + ((this.table.size() / EXCEL_MAX_ROWS) + 1) + " file(s)");
                int startRow = 0;
                int endRow = Math.min(EXCEL_MAX_ROWS, this.table.size());
                writeTo(writer, startRow, endRow);
                int writtenRows = endRow - startRow;
                int fileNumber = 1;
                while(writtenRows < this.table.size()) {
                    // split in several files
                    writer.flush();
                    writer.close();
                    
                    // shift start
                    startRow += EXCEL_MAX_ROWS;
                    endRow = Math.min(endRow + EXCEL_MAX_ROWS, this.table.size());
                    
                    File f1 = new File(f.getAbsolutePath() + "-part-" + fileNumber);
                    fos = new FileOutputStream(f1);
                    writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
                    fileNumber++;
                    writeTo(writer, startRow, endRow);
                    writtenRows += endRow - startRow;
                }
                
            } else {
                writeTo(writer);
            }
        } catch(IOException e) {
            log.warn("", e);
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch(IOException e1) {
                log.warn("", e1);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#writeTo(java.io.Writer)
     */
    @Override
    public void writeTo(Writer w) throws IOException {
        log.info("Writing " + this.table.size() + " rows with " + this.columnNames.size()
                + " columns");
        writeTo(w, 0, this.rowCount());
    }
    
    private boolean oversizeWarning = false;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xydra.csv.ICsvTable#writeTo(java.io.Writer, int, int)
     */
    @Override
    public void writeTo(Writer w, int startRow, int endRow) throws IOException, ExcelLimitException {
        if(!this.oversizeWarning && endRow - startRow > EXCEL_MAX_ROWS) {
            log.warn("Exceeding Excels limit of " + EXCEL_MAX_ROWS
                    + " rows - older versions of Excel cannot read it");
            this.oversizeWarning = true;
        }
        log.debug("Writing rows " + startRow + " to " + endRow + " of " + this.rowCount()
                + " rows with " + this.colCount() + " columns");
        int writtenRows = 0;
        
        // UTF-8 BOM
        w.write('\uFEFF');
        
        // header
        writeHeaderRow(w, this.columnNames);
        writtenRows++;
        
        // data
        List<String> selectedRowNames = this.rowNames.subList(startRow, endRow);
        Iterator<String> rowIt = selectedRowNames.iterator();
        while(rowIt.hasNext() && writtenRows < EXCEL_MAX_ROWS) {
            String rowName = rowIt.next();
            IReadableRow row = this.getOrCreateRow(rowName, false);
            writeRow(w, this.columnNames, rowName, row);
            writtenRows++;
        }
    }
    
    /**
     * Write the given iterator to a CSV writer
     * <em>with synthetic row names</em> (ascending numbers).
     * 
     * @param w writer
     * @param columNames only columns listed here are written
     * @param rowIt from which to read rows
     * @throws IOException from the writer
     */
    public static void writeTable(Writer w, List<String> columNames, Iterator<IReadableRow> rowIt)
            throws IOException {
        // fetch first row to know column names
        if(!rowIt.hasNext()) {
            log.warn("No rows in rowIt, writing empty table");
            return;
        }
        IReadableRow firstRow = rowIt.next();
        
        int writtenRows = 0;
        // header
        writeHeaderRow(w, columNames);
        writtenRows++;
        
        // first data row
        while(writtenRows < EXCEL_MAX_ROWS) {
            String rowName = "" + writtenRows;
            writeRow(w, columNames, rowName, firstRow);
            writtenRows++;
        }
        
        // more data
        while(rowIt.hasNext() && writtenRows < EXCEL_MAX_ROWS) {
            IReadableRow row = rowIt.next();
            String rowName = "" + writtenRows;
            writeRow(w, columNames, rowName, row);
            writtenRows++;
        }
    }
    
    public static void writeHeaderRow(Writer w, Collection<String> columnNames) throws IOException {
        w.write(CsvCodec.excelEncode(COLUMNNAME_ROW));
        Iterator<String> colIt = columnNames.iterator();
        int writtenCols = 0;
        while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
            String columnName = colIt.next();
            w.write(CsvCodec.CELL_DELIMITER + CsvCodec.excelEncode(columnName));
            writtenCols++;
            if(writtenCols == EXCEL_MAX_COLS) {
                log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
            }
        }
        w.write(CsvCodec.CELL_DELIMITER + "\n");
    }
    
    public static void writeRow(Writer w, Collection<String> columnNames, String rowName,
            IReadableRow row) throws IOException {
        assert row != null;
        w.write(CsvCodec.excelEncode(rowName));
        Iterator<String> colIt = columnNames.iterator();
        int writtenCols = 0;
        while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
            String columnName = colIt.next();
            w.write(CsvCodec.CELL_DELIMITER + CsvCodec.excelEncode(row.getValue(columnName)));
            writtenCols++;
            if(writtenCols == EXCEL_MAX_COLS) {
                log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
            }
        }
        w.write(CsvCodec.CELL_DELIMITER + "\n");
    }
    
    @Override
    public ICsvTable createTable() {
        return new CsvTable();
    }
    
    @Override
    public ICsvTable createTable(boolean maintainColumnInsertionOrder) {
        return new CsvTable(maintainColumnInsertionOrder);
    }
    
}

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
public class CsvTable extends SparseTable implements Iterable<Row>, ICsvTable {
	
	private static Logger log = LoggerFactory.getLogger(CsvTable.class);
	
	private static final String COLUMNNAME_ROW = "ROW";
	
	private static final String SEMICOLON = ";";
	
	public static String excelDecode(String encoded) {
		if(encoded.equals("") || encoded.equals("\"\"")) {
			return null;
		}
		
		String decoded = encoded;
		if(encoded.startsWith("\"") && encoded.endsWith("\"")) {
			decoded = decoded.substring(1, encoded.length() - 1);
		}
		// unescape
		decoded = decoded.replace("\"\"", "\"");
		return decoded;
	}
	
	/**
	 * Use safe CSV encoding, that is, surround by " and escape all quotes.
	 * 
	 * Enclosing all fields in quotes is not strictly required, but lets Excel
	 * open an CSV file with default settings without mangling e.g. dates.
	 * 
	 * @param value to be encoded
	 */
	public static String excelEncode(String value) {
		if(value == null) {
			return "\"\"";
		}
		
		String escaped;
		if(value.contains("\"")) {
			escaped = value.replace("\"", "\"\"");
		} else {
			escaped = value;
		}
		
		escaped = escaped.replace("\n", "§N");
		escaped = escaped.replace("\r", "§R");
		
		return "\"" + escaped + "\"";
	}
	
	/**
	 * @param s
	 * @param fieldLength number of characters to be used to write the filed,
	 *            excess characters are truncated, missing characters are filled
	 *            with spaces.
	 * @return a String in LaTeX-safe encoding
	 */
	static String latexEncode(String s, int fieldLength) {
		String s2 = s;
		if(s == null) {
			s2 = "";
		} else if(s.equals("null")) {
			s2 = "";
		}
		
		while(s2.length() < fieldLength) {
			s2 = " " + s2;
		}
		
		return s2;
	}
	
	// TODO make setParam-able
	private String defaultEncoding = "ISO-8859-1";
	
	private int readMaxRows = -1;
	
	private boolean splitWhenWritingLargeFiles = false;
	
	public CsvTable() {
		super();
	}
	
	/**
	 * @param maintainColumnInsertionOrder if true, the insertion order of
	 *            columns is maintained. If false, the default automatic
	 *            alphabetic sorting is on.
	 */
	public CsvTable(boolean maintainColumnInsertionOrder) {
		super(maintainColumnInsertionOrder);
	}
	
	/**
	 * Dump table to System.out
	 * 
	 * @throws IOException from System.out
	 */
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
	public void dumpToLaTeX() throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		toLaTeX(osw);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#readFrom(java.io.File)
	 */
	public void readFrom(File f) throws IOException {
		log.info("Reading CSV table from " + f.getAbsolutePath() + " Before: " + this.rowCount()
		        + " rows and " + this.colCount() + " columns");
		FileInputStream fos = new FileInputStream(f);
		Reader reader = new InputStreamReader(fos, Charset.forName(this.defaultEncoding));
		readFrom(reader, true);
		reader.close();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#readFrom(java.io.Reader)
	 */
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
	 * @see org.xydra.csv.ICsvTable#setParamReadMaxRows(int)
	 */
	public void setParamReadMaxRows(int readMaxRows) {
		this.readMaxRows = readMaxRows;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#setParamSplitWhenWritingLargeFiles(boolean)
	 */
	public void setParamSplitWhenWritingLargeFiles(boolean b) {
		this.splitWhenWritingLargeFiles = b;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#toLaTeX(java.io.Writer)
	 */
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
	public void writeTo(File f) throws FileNotFoundException {
		log.info("Writing CSV table to " + f.getAbsolutePath());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
		} catch(FileNotFoundException e) {
			// Java's way of saying, Windows locked the file
			File f2 = new File(f.getAbsolutePath() + "-COULD-NOT-OVERWRITE");
			fos = new FileOutputStream(f2);
		}
		Writer writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
		try {
			if(this.splitWhenWritingLargeFiles) {
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
	public void writeTo(Writer w) throws IOException {
		log.info("Writing " + this.table.size() + " rows with " + this.columnNames.size()
		        + " columns");
		writeTo(w, 0, this.rowCount());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.ICsvTable#writeTo(java.io.Writer, int, int)
	 */
	public void writeTo(Writer w, int startRow, int endRow) throws IOException, ExcelLimitException {
		if(endRow - startRow > EXCEL_MAX_ROWS) {
			throw new ExcelLimitException("Exceeding Excels limit of " + EXCEL_MAX_ROWS + " rows");
		}
		log.debug("Writing rows " + startRow + " to " + endRow + " of " + this.rowCount()
		        + " rows with " + this.colCount() + " columns");
		int writtenRows = 0;
		
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
		w.write(excelEncode(COLUMNNAME_ROW));
		Iterator<String> colIt = columnNames.iterator();
		int writtenCols = 0;
		while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
			String columnName = colIt.next();
			w.write(SEMICOLON + excelEncode(columnName));
			writtenCols++;
			if(writtenCols == EXCEL_MAX_COLS) {
				log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
			}
		}
		w.write(SEMICOLON + "\n");
	}
	
	public static void writeRow(Writer w, Collection<String> columnNames, String rowName,
	        IReadableRow row) throws IOException {
		w.write(excelEncode(rowName));
		Iterator<String> colIt = columnNames.iterator();
		int writtenCols = 0;
		while(colIt.hasNext() && writtenCols < EXCEL_MAX_COLS) {
			String columnName = colIt.next();
			w.write(SEMICOLON + excelEncode(row.getValue(columnName)));
			writtenCols++;
			if(writtenCols == EXCEL_MAX_COLS) {
				log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns");
			}
		}
		w.write(SEMICOLON + "\n");
	}
	
}

package org.xydra.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


public interface ICsvTable extends ISparseTable {
	
	/**
	 * Dump table to System.out
	 * 
	 * @throws IOException from System.out
	 */
	void dump() throws IOException;
	
	/**
	 * Dump table to System.out in LaTeX syntax
	 * 
	 * @throws IOException from System.out
	 */
	void dumpToLaTeX() throws IOException;
	
	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param f from which to read
	 * @throws IOException if file reading fails
	 */
	void readFrom(File f) throws IOException;
	
	/**
	 * Add the content given in the reader as CSV to this CvsTable. Multiple
	 * CvsTable can be read in, the result is a merge.
	 * 
	 * The first row of the CVS file is interpreted as header names. The first
	 * column is interpreted as row names.
	 * 
	 * @param r from which to read
	 * @throws IOException from the underlying reader
	 */
	void readFrom(Reader r) throws IOException;
	
	/**
	 * How many rows should maximally be read? Default is -1 = unlimited. Note
	 * that more rows can be added later.
	 * 
	 * @param readMaxRows set to -1 for unlimited (default)
	 */
	void setParamReadMaxRows(int readMaxRows);
	
	/**
	 * @param b Default is false. If true, file writing is split into files with
	 *            65535 records each, so that Excel can handle it.
	 */
	void setParamSplitWhenWritingLargeFiles(boolean b);
	
	void toLaTeX(Writer w) throws IOException;
	
	void writeTo(File f) throws FileNotFoundException;
	
	/**
	 * Writes a non-sparse CSV version of this tables contents
	 * 
	 * @param w to which to write the CSV
	 * @throws IOException from the writer
	 */
	void writeTo(Writer w) throws IOException;
	
	/**
	 * Writes a non-sparse CSV version of this tables contents. Writes only the
	 * data in [startRow,endRow).
	 * 
	 * @param w to which to write the CSV
	 * @param startRow inclusive
	 * @param endRow exclusive
	 * @throws IOException from the writer
	 * @throws ExcelLimitException if using more than 65535 rows or more than
	 *             255 columns.
	 */
	void writeTo(Writer w, int startRow, int endRow) throws IOException, ExcelLimitException;
}

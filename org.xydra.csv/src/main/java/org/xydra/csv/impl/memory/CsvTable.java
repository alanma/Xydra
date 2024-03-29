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
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Maintains a sparse table, organised by rows and columns.
 *
 * Insertion order is preserved.
 *
 * @author xamde
 */
public class CsvTable extends CsvCoreTable implements ICsvTable, ICsvTableFactory {

	private static Logger log = LoggerFactory.getLogger(CsvTable.class);

	public CsvTable(final boolean maintainColumnInsertionOrder) {
		super(maintainColumnInsertionOrder);
	}

	public CsvTable() {
		super();
	}

	/**
	 * Dump table to System.out
	 *
	 * @throws IOException
	 *             from System.out
	 */
	@Override
	public void dump() throws IOException {
		final Writer writer = new OutputStreamWriter(System.out);
		writeTo(writer);
		writer.flush();
	}

	/**
	 * Dump table to System.out in LaTeX syntax
	 *
	 * @throws IOException
	 *             from System.out
	 */
	@Override
	public void dumpToLaTeX() throws IOException {
		final OutputStreamWriter osw = new OutputStreamWriter(System.out);
		toLaTeX(osw);
	}


	@Override
	public void readFrom(final File f) throws IOException {
		readFrom(f, this.defaultEncoding);
	}

	@Override
	public void readFrom(final File f, final String encoding) throws IOException {
		log.info("Reading CSV table from " + f.getAbsolutePath() + " Before: " + rowCount()
				+ " rows and " + colCount() + " columns");
		final FileInputStream fos = new FileInputStream(f);
		final Reader reader = new InputStreamReader(fos, Charset.forName(encoding));
		readFrom(reader, true);
		reader.close();
		// remove "NULL" and "ROW"-columns
		this.columnNames.remove("NULL");
		this.columnNames.remove("ROW");
	}


	@Override
	public void readFrom(final Reader r, final boolean create) throws IOException {
		final CsvReader csvReader = new CsvReader(r, this.readMaxRows);
		final Collection<String> columnNames = csvReader.readHeaders();
		assert this.columnNames != null;
		this.columnNames.addAll(columnNames);
		IReadableRow row = csvReader.readDataRow();
		while (row != null) {
			final Row tableRow = getOrCreateRow(row.getKey(), true);
			tableRow.addAll(row);
			row = csvReader.readDataRow();
		}
	}


	@Override
	public void toLaTeX(final Writer w) throws IOException {
		// determine padding
		final Map<String, Integer> colName2maxLength = new HashMap<String, Integer>();
		for (final IRow row : this) {
			for (final String colName : this.columnNames) {
				final int oldmax = colName2maxLength.get(colName) == null ? 0 : colName2maxLength
						.get(colName);
				final String value = row.getValue(colName);
				final int newmax = Math.max(oldmax, value.length());
				colName2maxLength.put(colName, newmax);
			}
		}

		// table control header
		w.write("\\begin{tabular}{|");

		for (int i = 0; i < this.columnNames.size(); i++) {
			w.write("l|");
		}
		w.write("}\n");
		// table header row
		w.write("\\hline\n");

		// calculate the last columns name
		String lastColumnName = null;
		final Iterator<String> colNameIt = this.columnNames.iterator();
		while (colNameIt.hasNext()) {
			lastColumnName = colNameIt.next();
		}

		for (final String colName : this.columnNames) {
			w.write(latexEncode(colName, colName2maxLength.get(colName)));
			if (colName.equals(lastColumnName)) {
				w.write(" \\\\\n");
			} else {
				w.write(" & ");
			}
		}
		w.write("\\hline\n");

		for (final String rowName : rowNamesIterable()) {
			final IRow row = getOrCreateRow(rowName, false);
			for (final String colName : this.columnNames) {
				w.write(latexEncode(row.getValue(colName), colName2maxLength.get(colName)));
				if (colName.equals(lastColumnName)) {
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


	@Override
	public void writeTo(final File f) throws FileNotFoundException {
		writeTo(f, false);
	}

	/**
	 * @param f
	 * @param append
	 *            if append is true, the new records must match exactly the
	 *            format of the old ones. Otherwise just a big mess is produced.
	 * @throws FileNotFoundException
	 */
	public void writeTo(final File f, final boolean append) throws FileNotFoundException {
		log.info("Writing CSV table to " + f.getAbsolutePath());
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f, append);
		} catch (final FileNotFoundException e) {
			// Java's way of saying, Windows locked the file
			final File f2 = new File(f.getAbsolutePath() + "-COULD-NOT-"
					+ (append ? "APPEND" : "OVERWRITE"));
			fos = new FileOutputStream(f2);
		}
		Writer writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
		try {
			if (!append && this.splitWhenWritingLargeFiles) {
				log.info("Will write as " + (rowCount() / EXCEL_MAX_ROWS + 1) + " file(s)");
				int startRow = 0;
				int endRow = Math.min(EXCEL_MAX_ROWS, rowCount());
				writeTo(writer, startRow, endRow);
				int writtenRows = endRow - startRow;
				int fileNumber = 1;
				while (writtenRows < rowCount()) {
					// split in several files
					writer.flush();
					writer.close();

					// shift start
					startRow += EXCEL_MAX_ROWS;
					endRow = Math.min(endRow + EXCEL_MAX_ROWS, rowCount());

					final File f1 = new File(f.getAbsolutePath() + "-part-" + fileNumber);
					fos = new FileOutputStream(f1);
					writer = new OutputStreamWriter(fos, Charset.forName(this.defaultEncoding));
					fileNumber++;
					writeTo(writer, startRow, endRow);
					writtenRows += endRow - startRow;
				}

			} else {
				writeTo(writer);
			}
		} catch (final IOException e) {
			log.warn("", e);
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch (final IOException e1) {
				log.warn("", e1);
			}
		}
	}


	@Override
	public void writeTo(final Writer w) throws IOException {
		log.info("Writing " + rowCount() + " rows with " + this.columnNames.size()
				+ " columns");
		writeTo(w, 0, rowCount());
	}

	private boolean oversizeWarning = false;


	@Override
	public void writeTo(final Writer w, final int startRow, final int endRow) throws IOException, ExcelLimitException {
		if (!this.oversizeWarning && endRow - startRow > EXCEL_MAX_ROWS) {
			log.warn("Exceeding Excels limit of " + EXCEL_MAX_ROWS
					+ " rows - older versions of Excel cannot read it");
			this.oversizeWarning = true;
		}
		log.debug("Writing rows " + startRow + " to " + endRow + " of " + rowCount()
				+ " rows with " + colCount() + " columns");
		int writtenRows = 0;

		// UTF-8 BOM
		w.write('\uFEFF');

		// header
		writeHeaderRow(w, this.columnNames);
		writtenRows++;

		// data
		final Iterator<String> rowIt = super.subIterator(startRow, endRow);
		while (rowIt.hasNext() && (!this.restrictToExcelSize || writtenRows < EXCEL_MAX_ROWS)) {
			final String rowName = rowIt.next();
			final IReadableRow row = getOrCreateRow(rowName, false);
			if (row == null) {
				log.warn("Encountered null-row named '" + rowName + "', skipping.");
			} else {
				writeRow(w, this.columnNames, rowName, row);
				writtenRows++;
			}
		}
	}

	/**
	 * Write the given iterator to a CSV writer
	 * <em>with synthetic row names</em> (ascending numbers).
	 *
	 * @param w
	 *            writer
	 * @param columNames
	 *            only columns listed here are written
	 * @param rowIt
	 *            from which to read rows
	 * @throws IOException
	 *             from the writer
	 */
	public static void writeTable(final Writer w, final List<String> columNames, final Iterator<IReadableRow> rowIt)
			throws IOException {
		// fetch first row to know column names
		if (!rowIt.hasNext()) {
			log.warn("No rows in rowIt, writing empty table");
			return;
		}
		final IReadableRow firstRow = rowIt.next();

		int writtenRows = 0;
		// header
		writeHeaderRow(w, columNames);
		writtenRows++;

		// first data row
		while (writtenRows < EXCEL_MAX_ROWS) {
			final String rowName = "" + writtenRows;
			writeRow(w, columNames, rowName, firstRow);
			writtenRows++;
		}

		// more data
		while (rowIt.hasNext() && writtenRows < EXCEL_MAX_ROWS) {
			final IReadableRow row = rowIt.next();
			final String rowName = "" + writtenRows;
			writeRow(w, columNames, rowName, row);
			writtenRows++;
		}
	}

	public static void writeHeaderRow(final Writer w, final Collection<String> columnNames) throws IOException {
		w.write(CsvCodec.excelEncode(COLUMNNAME_ROW));
		final Iterator<String> colIt = columnNames.iterator();
		int writtenCols = 0;
		boolean warnedOnColumnNumber = false;
		while (colIt.hasNext() ){
			final String columnName = colIt.next();
			w.write(CsvCodec.CELL_DELIMITER + CsvCodec.excelEncode(columnName));
			writtenCols++;
			if (!warnedOnColumnNumber && writtenCols == EXCEL_MAX_COLS) {
				log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns. Writing more anyway.");
				warnedOnColumnNumber = true;
			}
		}
		w.write(CsvCodec.CELL_DELIMITER + "\n");
	}

	public static void writeRow(final Writer w, final Collection<String> columnNames, final String rowName,
			final IReadableRow row) throws IOException {
		assert row != null;
		w.write(CsvCodec.excelEncode(rowName));
		final Iterator<String> colIt = columnNames.iterator();
		int writtenCols = 0;
		boolean warnedOnColumnNumber = false;
		while (colIt.hasNext() ){
			final String columnName = colIt.next();
			w.write(CsvCodec.CELL_DELIMITER + CsvCodec.excelEncode(row.getValue(columnName)));
			writtenCols++;
			if (!warnedOnColumnNumber && writtenCols == EXCEL_MAX_COLS) {
				log.warn("Reached Excels limit of " + EXCEL_MAX_COLS + " columns. Writing more anyway.");
				warnedOnColumnNumber = true;
			}
		}
		w.write(CsvCodec.CELL_DELIMITER + "\n");
	}

	@Override
	public ICsvTable createTable() {
		return new CsvTable();
	}

	@Override
	public ICsvTable createTable(final boolean maintainColumnInsertionOrder) {
		return new CsvTable(maintainColumnInsertionOrder);
	}

}

package org.xydra.csv;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A set of tools for {@link CsvTable}.
 * 
 * @author voelkel
 * 
 */
public class TableTools extends TableCoreTools {
	
	/**
	 * Processes a file and writes results to a shared {@link CsvTable}.
	 * 
	 * @author voelkel
	 */
	public interface IFileProcessor {
		/**
		 * @param table a shared result table (shared by all calls to 'process')
		 * @param f a file to be processes
		 * @throws IOException from reading f
		 */
		public void process(ICsvTable table, File f) throws IOException;
	}
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TableTools.class);
	
	/**
	 * Merges the content of two CSV-files into one resulting CSV-table. This is
	 * a memory-efficient operation.
	 * 
	 * @param a source file
	 * @param b source file
	 * @param mergeFile result file
	 * @throws IOException from file I/O
	 */
	public static void merge(File a, File b, File mergeFile) throws IOException {
		ICsvTable table = new CsvTable();
		table.readFrom(a);
		table.readFrom(b);
		table.writeTo(mergeFile);
	}
	
	/**
	 * Starts in 'startDirectory' and traverses all sub-directories. On the way,
	 * all files matching the 'filenameFilter' are processed using the given
	 * 'processor'. A {@link CsvTable} is used to persist any results the
	 * processor can generate.
	 * 
	 * @param startDirectory the root of the directory traversal
	 * @param table to store results of processing
	 * @param filenameFilter only matching files are processed
	 * @param processor an {@link IFileProcessor}
	 * @throws IOException from read/write files
	 */
	public static void process(File startDirectory, ICsvTable table, FilenameFilter filenameFilter,
	        IFileProcessor processor) throws IOException {
		for(File f : startDirectory.listFiles(filenameFilter)) {
			processor.process(table, f);
		}
		for(File f : startDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		})) {
			process(f, table, filenameFilter, processor);
		}
	}
	
	public static void main(String[] args) throws IOException {
		CsvTable test = new CsvTable();
		Row row1 = test.getOrCreateRow("row-1", true);
		row1.setValue("colA", "A1", true);
		row1.setValue("colB", "B1", true);
		row1.setValue("colC", "C1", true);
		Row row2 = test.getOrCreateRow("row-2", true);
		row2.setValue("colA", "A2", true);
		row2.setValue("colB", "B2", true);
		row2.setValue("colC", "C2", true);
		Row row3 = test.getOrCreateRow("row-3", true);
		row3.setValue("colA", "A3", true);
		row3.setValue("colB", "B3", true);
		row3.setValue("colC", "C3", true);
		
		test.dump();
		CsvTable t = new CsvTable();
		TableTools.transpose(test, t);
		t.dump();
	}
	
}

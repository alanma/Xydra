package org.xydra.csv;

import java.io.File;
import java.io.IOException;

import org.xydra.csv.impl.memory.CsvTable;

/**
 * Takes a CSV file and dumps it in LaTeX syntax to System.out
 *
 * @author xamde
 */
public class LatexMain {

	public static void main(final String[] args) throws IOException {
		final File root = new File("W:\\Diss\\phdvoelkel\\data");
		final File f = new File(root, "userstudy-sessions.csv");
		final CsvTable table = new CsvTable();
		table.readFrom(f);
		table.dumpToLaTeX();
	}

}

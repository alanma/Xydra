package org.xydra.csv;

import java.io.File;
import java.io.IOException;

import org.xydra.csv.impl.memory.CsvTable;


/**
 * Takes a CSV file and dumps it in LaTeX syntax to System.out
 * 
 * @author voelkel
 */
public class LatexMain {
	
	public static void main(String[] args) throws IOException {
		File root = new File("W:\\Diss\\phdvoelkel\\data");
		File f = new File(root, "userstudy-sessions.csv");
		CsvTable table = new CsvTable();
		table.readFrom(f);
		table.dumpToLaTeX();
	}
	
}

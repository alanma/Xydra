package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.xydra.csv.HtmlTool;
import org.xydra.csv.IRow;
import org.xydra.csv.TableTools;
import org.xydra.csv.impl.memory.CsvTable;


/*
 * TODO Problems with CsvTables
 * 
 * - Output not ordered
 * 
 * - Averages cut of decimal point
 */

public class PerformanceDataAnalyzer {
	private static String[] versions = { "Version2", "gae20111105", "gae20111105-20" };
	private static String fileName;
	
	private enum Operations {
		ADD, DELETE, EDIT
	}
	
	public static void main(String args[]) {
		fileName = "Evaluation" + System.currentTimeMillis() + ".html";
		
		evaluateSingleOperationBenchmark(Operations.ADD);
		evaluateSingleOperationBenchmark(Operations.DELETE);
		evaluateSingleOperationBenchmark(Operations.EDIT);
		
		Integer[] range = new Integer[] { 8, 10, 16, 20, 32, 40, 64, 80, 128, 256 };
		
		evaluateMultipleOperations(Operations.ADD, range);
	}
	
	@SuppressWarnings("unchecked")
	public static void evaluateSingleOperationBenchmark(Operations op) {
		CsvTable results = new CsvTable(true);
		
		String path = null;
		
		// Get correct file name
		switch(op) {
		case ADD:
			path = "/AddingOneWishOneThread.txt";
			break;
		case DELETE:
			path = "/DeletingOneWishOneThread.txt";
			break;
		case EDIT:
			path = "/EditingOneWishOneThread.txt";
			break;
		}
		
		assert path != null;
		
		IRow avg = results.getOrCreateRow("avg", true);
		IRow stdev = results.getOrCreateRow("stdev", true);
		IRow excep = results.getOrCreateRow("Excep", true);
		
		avg.setValue("", "Average (ms)", true);
		stdev.setValue("", "Standard Deviation (ms)", true);
		excep.setValue("", "Average Amount of Exceptions", true);
		
		for(int i = 0; i < versions.length; i++) {
			CsvTable dataTable = new CsvTable(true);
			CsvTable excepTable = new CsvTable(true);
			
			try {
				/*
				 * Read data for the current version and write it in the CSV
				 * table
				 */
				BufferedReader in = new BufferedReader(new FileReader(".src/main/data/Performance/"
				        + versions[i] + path));
				
				String currentLine = in.readLine();
				int dataCount = 0;
				int excepCount = 0;
				
				while(currentLine != null) {
					String[] csvData = currentLine.split(",");
					
					IRow dataRow = dataTable.getOrCreateRow("" + dataCount, true);
					IRow excepRow = excepTable.getOrCreateRow("" + excepCount, true);
					
					// csv column 9 holds the data for the average time
					if(!csvData[9].contains("NaN")) {
						dataRow.setValue("X", "0", true);
						dataRow.setValue("data", csvData[9], true);
						
						dataCount++;
					}
					
					// csv column 11 holds the data for the exceptions
					excepRow.setValue("X", "0", true);
					excepRow.setValue("data", csvData[11], true);
					excepCount++;
					
					currentLine = in.readLine();
				}
				
				CsvTable dataTarget = new CsvTable();
				TableTools.groupBy(dataTable, Arrays.asList("X"), Collections.EMPTY_LIST,
				        Arrays.asList("data"), Collections.EMPTY_LIST, dataTarget);
				
				avg.setValue(versions[i], dataTarget.getValue("" + 0, "data" + "--average"), true);
				stdev.setValue(versions[i], dataTarget.getValue("" + 0, "data" + "--stdev"), true);
				
				CsvTable excepTarget = new CsvTable();
				TableTools.groupBy(excepTable, Arrays.asList("X"), Collections.EMPTY_LIST,
				        Arrays.asList("data"), Collections.EMPTY_LIST, excepTarget);
				
				excep.setValue(versions[i], excepTarget.getValue("" + 0, "data" + "--average"),
				        true);
				
				in.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			FileWriter fw = new FileWriter(new File("./PerformanceData/" + fileName), true);
			// add some CSS to have table border lines
			fw.write("<style>\n" + "  table.csv * { border: 1px solid; } \n" + "</style>\n");
			
			switch(op) {
			case ADD:
				fw.write("<h1> Adding One Wish </h1>");
				break;
			case DELETE:
				fw.write("<h1> Deleting One Wish </h1>");
				break;
			case EDIT:
				fw.write("<h1> Editing One Wish </h1>");
				break;
			}
			
			HtmlTool.writeToHtml(results, fw);
			
			// TODO write logic to only display this if Version 2 is measured
			fw.write("<b>Attention</b>: Averages etc. for Version 2 were measured over averages of 1000 operations. "
			        + "Other Versions build their average over single exceutions of the given operation.");
			
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static void evaluateMultipleOperations(Operations op, Integer[] range) {
		CsvTable dataResults = new CsvTable(true);
		CsvTable excepResults = new CsvTable(true);
		
		String path = null;
		
		// Get correct file name
		switch(op) {
		case ADD:
			path = "/AddingMultipleWishesinTransaction";
			break;
		case DELETE:
		case EDIT:
			// TODO no such tests were implemented
			return;
		}
		
		assert path != null;
		
		CsvTable dataTables[] = new CsvTable[versions.length];
		CsvTable excepTables[] = new CsvTable[versions.length];
		
		CsvTable dataTargets[] = new CsvTable[versions.length];
		CsvTable excepTargets[] = new CsvTable[versions.length];
		
		/*
		 * write data in csv table for each version to calculate averages etc -
		 * multiple tabls are needed because the amount of data is not always
		 * the same
		 */
		for(int i = 0; i < versions.length; i++) {
			dataTables[i] = new CsvTable(true);
			excepTables[i] = new CsvTable(true);
			
			for(int X : range) {
				
				try {
					/*
					 * Read data for the current version and write it in the CSV
					 * table
					 */
					BufferedReader in = new BufferedReader(new FileReader(
					        ".src/main/data/Performance/" + versions[i] + path + X + ".txt"));
					
					String currentLine = in.readLine();
					int dataCount = 0;
					int excepCount = 0;
					
					while(currentLine != null) {
						String[] csvData = currentLine.split(",");
						
						IRow dataRow = dataTables[i].getOrCreateRow(X + " " + dataCount, true);
						IRow excepRow = excepTables[i].getOrCreateRow(X + " " + excepCount, true);
						
						// csv column 9 holds the data for the average time
						if(!csvData[9].contains("NaN")) {
							dataRow.setValue("X", X, true);
							dataRow.setValue("data", csvData[9], true);
							
							dataCount++;
						}
						
						// csv column 11 holds the data for the exceptions
						excepRow.setValue("X", X, true);
						excepRow.setValue("data", csvData[11], true);
						excepCount++;
						
						currentLine = in.readLine();
					}
					
					in.close();
				} catch(FileNotFoundException e) {
					IRow dataRow = dataTables[i].getOrCreateRow(X + " " + 0, true);
					dataRow.setValue("X", X, true);
					dataRow.setValue("data", -1, true);
					
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			dataTargets[i] = new CsvTable();
			TableTools.groupBy(dataTables[i], Arrays.asList("X"), Collections.EMPTY_LIST,
			        Arrays.asList("data"), Collections.EMPTY_LIST, dataTargets[i]);
			
			excepTargets[i] = new CsvTable();
			TableTools.groupBy(excepTables[i], Arrays.asList("X"), Collections.EMPTY_LIST,
			        Arrays.asList("data"), Collections.EMPTY_LIST, excepTargets[i]);
		}
		
		// write table in final result tables
		for(int rowX : range) {
			IRow dataResultRow = dataResults.getOrCreateRow("" + rowX, true);
			IRow excepResultRow = excepResults.getOrCreateRow("" + rowX, true);
			
			dataResultRow.setValue("X", rowX, true);
			excepResultRow.setValue("X", rowX, true);
			
			for(int i = 0; i < versions.length; i++) {
				dataResultRow.setValue(versions[i] + "--average",
				        dataTargets[i].getValue("" + rowX, "data" + "--average"), true);
				
				dataResultRow.setValue(versions[i] + "--stdev", dataTargets[i].getValue("" + rowX,
				        "data" + "--stdev"), true);
				
				excepResultRow.setValue(versions[i] + "--excep",
				        excepTargets[i].getValue("" + rowX, "data" + "--average"), true);
			}
		}
		
		try {
			FileWriter fw = new FileWriter(new File("./PerformanceData/" + fileName), true);
			// add some CSS to have table border lines
			fw.write("<style>\n" + "  table.csv * { border: 1px solid; } \n" + "</style>\n");
			
			switch(op) {
			case ADD:
				fw.write("<h1> Adding Multiple Wishes </h1>");
				break;
			case DELETE:
			case EDIT:
				// TODO tests not implemented
			}
			
			HtmlTool.writeToHtml(dataResults, fw);
			fw.write("<h2> Exceptions </h2>");
			HtmlTool.writeToHtml(excepResults, fw);
			
			// TODO write logic to only display this if Version 2 is measured
			fw.write("<b>Attention</b>: Averages etc. for Version 2 were measured over averages of 1000 operations. "
			        + "Other Versions build their average over single exceutions of the given operation.");
			
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}

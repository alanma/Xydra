package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.xydra.csv.HtmlTool;
import org.xydra.csv.IRow;
import org.xydra.csv.TableTools;
import org.xydra.csv.impl.memory.CsvTable;


public class PerformanceDataAnalyzer {
	private static String[] versions = { "Version2", "gae20111105", "gae20111105-20" };
	private static String fileName;
	
	private enum Operations {
		ADD, DELETE, EDIT
	}
	
	public static void main(String args[]) {
		fileName = "Evaluation" + System.currentTimeMillis() + ".html";
		
		evaluateOneOperation(Operations.ADD);
		evaluateOneOperation(Operations.DELETE);
		evaluateOneOperation(Operations.EDIT);
	}
	
	@SuppressWarnings("unchecked")
	public static void evaluateOneOperation(Operations op) {
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
				BufferedReader in = new BufferedReader(new FileReader("./PerformanceData/"
				        + versions[i] + path));
				
				String currentLine = in.readLine();
				int dataCount = 0;
				int excepCount = 0;
				
				while(currentLine != null) {
					String[] csvData = currentLine.split(",");
					System.out.println(versions[i] + ": " + csvData[9]);
					
					IRow dataRow = dataTable.getOrCreateRow("" + dataCount, true);
					IRow excepRow = excepTable.getOrCreateRow("" + excepCount, true);
					
					// csv column 9 holds the data for the average time
					if(!csvData[9].equals("NaN")) {
						dataRow.setValue("X", "0", true);
						dataRow.setValue("data", csvData[9], true);
						
						dataCount++;
					}
					
					// csv column 11 holds the data
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
}

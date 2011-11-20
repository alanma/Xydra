package org.xydra.testgae.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.xydra.csv.IRow;
import org.xydra.csv.ISparseTable;
import org.xydra.csv.TableTools;
import org.xydra.csv.impl.memory.CsvTable;


public class PerformanceDataAnalyzer {
	private static String[] versions = { "gae20111105/" };
	private static String fileName;
	
	public static void main(String args[]) {
		fileName = "Evaluation" + System.currentTimeMillis() + ".html";
		
		evaluateAddingOneWishOneThread();
		
	}
	
	@SuppressWarnings("unchecked")
	public static void evaluateAddingOneWishOneThread() {
		CsvTable table = new CsvTable(true);
		
		for(int i = 0; i < versions.length; i++) {
			
			try {
				BufferedReader in = new BufferedReader(new FileReader("./PerformanceData/"
				        + versions[i] + "AddingOneWishOneThread.txt"));
				
				String currentLine = in.readLine();
				int count = 0;
				
				while(currentLine != null) {
					String[] csvData = currentLine.split(",");
					
					IRow row = table.getOrCreateRow("" + count, true);
					
					row.setValue("X", "0", true);
					row.setValue(versions[i], csvData[9], true);
					
					currentLine = in.readLine();
					count += 1;
				}
				
				in.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		CsvTable target = new CsvTable();
		TableTools.groupBy(table, Arrays.asList("X"), Collections.EMPTY_LIST,
		        Arrays.asList(versions), Collections.EMPTY_LIST, target);
		
		System.out.println("lol " + target.getColumnNames());
		
		try {
			FileWriter fw = new FileWriter(new File("./PerformanceData/" + fileName));
			// add some CSS to have table border lines
			fw.write("<style>\n" + "  table.csv * { border: 1px solid; } \n" + "</style>\n");
			writeToHtml(target, fw);
			fw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// TODO use htmltool instead
	public static void writeToHtml(ISparseTable table, final Writer w) throws IOException {
		w.write("<table class='csv'>\n");
		w.write("<tr>");
		
		List<String> colNames = new ArrayList<String>();
		colNames.addAll(table.getColumnNames());
		// sort alphabetically
		Collections.sort(colNames);
		
		for(String colName : colNames) {
			w.write("<th>" + htmlencode(colName) + "</th>");
		}
		w.write("</tr>\n");
		
		for(IRow row : table) {
			w.write("<tr>");
			for(String colName : colNames) {
				w.write("<td>" + htmlencode(row.getValue(colName)) + "</td>");
			}
			w.write("</tr>\n");
		}
		w.write("</table>\n");
	}
	
	public static String htmlencode(String s) {
		return s.replace("&", "&apos;").replace("<", "&lt;").replace(">", "&gt;")
		        .replace("\"", "&quot;").replace("'", "&apos;");
	}
}

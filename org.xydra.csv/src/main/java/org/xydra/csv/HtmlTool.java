package org.xydra.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HtmlTool {
	
	/**
	 * Write the given table as simple HTML to the given writer.
	 * 
	 * @param table a table, never null
	 * @param w never null
	 * @throws IOException ...
	 */
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
	
	/**
	 * @param s any string, might contain HTML-like stuff
	 * @return string with basic XML encoding to prevent broken HTML
	 */
	public static String htmlencode(String s) {
		return s.replace("&", "&apos;").replace("<", "&lt;").replace(">", "&gt;")
		        .replace("\"", "&quot;").replace("'", "&apos;");
	}
	
}

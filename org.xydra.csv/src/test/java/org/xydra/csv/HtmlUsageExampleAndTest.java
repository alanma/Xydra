package org.xydra.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.xydra.csv.impl.memory.CsvTable;

public class HtmlUsageExampleAndTest {

	public void testOutputAsHtml() throws IOException {
		/* Create a new in-memory table */
		CsvTable table = new CsvTable(true);
		/* Create a row with a unique key, using some index number X */
		int x = 10;
		int count = 0;

		// just make sure we have unique row names, string concat is just what
		// we want
		IRow row = table.getOrCreateRow("" + x + "-" + count, true);
		/* Set values */
		// also set X as a value to have in in the output
		row.setValue("X", x, true);
		// set more values
		row.setValue("bbb", "111", true);
		row.setValue("aaa", "222", true);

		// imagine we get more rows where X = 10
		count++;
		row = table.getOrCreateRow("" + x + "-" + count, true);
		row.setValue("X", x, true);
		// set more values
		row.setValue("bbb", "163", true);
		row.setValue("aaa", "242", true);

		count++;
		row = table.getOrCreateRow("" + x + "-" + count, true);
		row.setValue("X", x, true);
		// set more values
		row.setValue("bbb", "143", true);
		row.setValue("aaa", "209", true);

		count++;
		row = table.getOrCreateRow("" + x + "-" + count, true);
		row.setValue("X", x, true);
		// set more values
		row.setValue("bbb", "198", true);
		row.setValue("aaa", "289", true);

		// and some rows where X = 20

		x = 20;
		count++;
		row = table.getOrCreateRow("" + x + "-" + count, true);
		row.setValue("X", x, true);
		row.setValue("bbb", "410", true);
		row.setValue("aaa", "828", true);

		count++;
		row = table.getOrCreateRow("" + x + "-" + count, true);
		row.setValue("X", x, true);
		row.setValue("bbb", "420", true);
		row.setValue("aaa", "898", true);

		// now we process it to average all X=... something rows together
		CsvTable target = new CsvTable();
		TableTools.groupBy(table, Arrays.asList("X"), Collections.EMPTY_LIST,
				Arrays.asList("bbb", "aaa"), Collections.EMPTY_LIST, target);

		// write to HTML
		FileWriter fw = new FileWriter(new File("./target/sample.html"));
		// add some CSS to have table border lines
		fw.write("<style>\n" + "  table.csv * { border: 1px solid; } \n" + "</style>\n");
		HtmlTool.writeToHtml(table, null, fw);
		fw.write("<h2>results in</h2>");
		HtmlTool.writeToHtml(target, null, fw);
		fw.close();
	}

	public static void main(String[] args) throws IOException {
		HtmlUsageExampleAndTest h = new HtmlUsageExampleAndTest();
		h.testOutputAsHtml();
	}

}

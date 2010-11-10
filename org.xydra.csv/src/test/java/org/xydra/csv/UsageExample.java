package org.xydra.csv;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.xydra.csv.CsvTable;
import org.xydra.csv.CsvTable.Row;



public class UsageExample {
	
	@Test
	public void testSimpleUseCases() throws FileNotFoundException {
		/* A temporary file, create it'S parent directory */
		File temp = new File("./target/temp/example/table1.csv");
		temp.getParentFile().mkdirs();
		/* Create a new in-memory table */
		CsvTable table = new CsvTable();
		/* Create a row with a unique key, using the system time */
		Row row = table.getOrCreateRow("" + System.currentTimeMillis(), true);
		/* Set a value */
		row.setValue("first name", "Heiko", true);
		/* we overwrite the value */
		row.setValue("first name", "Tim", false);
		/* test exceptions */
		try {
			row.setValue("first name", "Jones", true);
			fail("we try to set the value initially, but there was already a value, so we get an exception.");
		} catch(IllegalStateException e) {
			// we expected that.
		}
		/* set another value */
		row.setValue("last name", "Doe", true);
		/* add a second row with values */
		Row row2 = table.getOrCreateRow("" + System.currentTimeMillis(), true);
		row2.setValue("last name", "Homer", true);
		row2.setValue("first name", "Simpson", true);
		
		table.writeTo(temp);
		System.out.println("Wrote table to " + temp.getAbsolutePath());
		
		/**
		 * The expected result should be: <code><pre>
"ROW";"first name";"last name";
"1269266667429";"Tim";"Doe";
"1269266667444";"Simpson";"Homer";
</pre></code>
		 */
	}
	
	@Test
	public void testColumnsInInsertionOrder() throws IOException {
		/* Create a new in-memory table */
		CsvTable table = new CsvTable(true);
		/* Create a row with a unique key, using the system time */
		Row row = table.getOrCreateRow("" + System.currentTimeMillis() + "-" + Math.random(), true);
		/* Set values */
		row.setValue("bbb", "111", true);
		row.setValue("aaa", "222", true);
		
		row = table.getOrCreateRow("" + System.currentTimeMillis() + "-" + Math.random(), true);
		row.setValue("aaa", "333", true);
		
		/* Now column order should be "bbb", "aaa" */
		table.dump();
	}
}

package org.xydra.csv;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.xydra.csv.impl.memory.CsvTable;

public class GroupByTest {

	@Test
	public void testRightHandDigit() {
		CsvTable dataTable = new CsvTable();

		IRow row = dataTable.getOrCreateRow("1", true);
		row.setValue("X", "0", true);
		row.setValue("aaa", 5.0, true);

		IRow row2 = dataTable.getOrCreateRow("2", true);
		row2.setValue("X", "0", true);
		row2.setValue("aaa", 8.0, true);

		CsvTable dataTarget = new CsvTable();
		TableTools.groupBy(dataTable, Arrays.asList("X"), Collections.EMPTY_LIST,
				Arrays.asList("aaa"), Collections.EMPTY_LIST, dataTarget);

		// 8 + 5 = 13, Average should be 6.5
		assertEquals(6.5d, Double.parseDouble(dataTarget.getValue("" + 0, "aaa--average")), 0.01d);
	}
}

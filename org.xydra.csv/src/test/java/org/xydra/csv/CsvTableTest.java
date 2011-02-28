package org.xydra.csv;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.xydra.csv.impl.memory.CsvTable;


public class CsvTableTest {
	
	@Test
	public void testGroupBy() throws IOException {
		ICsvTable ct = new CsvTable();
		ct.setValueInitial("1", "name", "Jana");
		ct.setValueInitial("1", "profession", "computer science");
		ct.setValueInitial("1", "age", "21");
		ct.setValueInitial("1", "sex", "female");
		ct.setValueInitial("1", "shoesize", "5.5");
		
		ct.setValueInitial("2", "name", "Jim");
		ct.setValueInitial("2", "profession", "computer science");
		ct.setValueInitial("2", "age", "25");
		ct.setValueInitial("2", "sex", "male");
		ct.setValueInitial("2", "shoesize", "7.5");
		
		ct.setValueInitial("3", "name", "John");
		ct.setValueInitial("3", "profession", "firefighter");
		ct.setValueInitial("3", "age", "29");
		ct.setValueInitial("3", "sex", "male");
		ct.setValueInitial("3", "shoesize", "8");
		
		ct.setValueInitial("4", "name", "Jorge");
		ct.setValueInitial("4", "profession", "computer science");
		ct.setValueInitial("4", "age", "35");
		ct.setValueInitial("4", "sex", "male");
		ct.setValueInitial("4", "shoesize", "7");
		
		ICsvTable result = new CsvTable();
		TableTools.groupBy(ct, Arrays.asList("profession", "sex"),
		        Arrays.asList("age", "shoesize"), Arrays.asList("age", "shoesize"),
		        Arrays.asList("age", "shoesize"), result);
		result.dump();
	}
	
}

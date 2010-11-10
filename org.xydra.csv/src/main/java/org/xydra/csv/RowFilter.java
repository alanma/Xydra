package org.xydra.csv;


public interface RowFilter {

	public boolean matches(CsvTable.Row row);

}

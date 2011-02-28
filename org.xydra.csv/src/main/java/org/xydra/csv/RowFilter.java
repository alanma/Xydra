package org.xydra.csv;



public interface RowFilter {

	public boolean matches(IRow row);

}

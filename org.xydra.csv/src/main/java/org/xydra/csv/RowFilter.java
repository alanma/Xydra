package org.xydra.csv;

import org.xydra.csv.impl.memory.Row;


public interface RowFilter {

	public boolean matches(Row row);

}

package org.xydra.csv;

import org.xydra.csv.CsvTable.Row;

public interface IRowVisitor {

	public void visit(Row row);

}

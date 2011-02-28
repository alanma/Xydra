package org.xydra.csv;

import org.xydra.csv.impl.memory.Row;


public interface IRowVisitor {

	public void visit(Row row);

}

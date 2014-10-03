package org.xydra.csv;

public interface ICsvTableFactory {

	ICsvTable createTable();

	ICsvTable createTable(boolean maintainColumnInsertionOrder);

}

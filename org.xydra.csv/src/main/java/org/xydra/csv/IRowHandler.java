package org.xydra.csv;

public interface IRowHandler {
	
	void handleRow(String rowName, IReadableRow readableRow);
	
}

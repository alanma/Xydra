package org.xydra.csv;

public interface IRowSink {
	
	void appendRow(String rowName, IReadableRow readableRow);
	
}

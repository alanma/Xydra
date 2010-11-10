package org.xydra.csv;

/**
 * Used to indicate a limit in Excel, i.e. 65535 maximal rows.
 * 
 * @author voelkel
 * 
 */
public class ExcelLimitException extends RuntimeException {
	
	public ExcelLimitException(String msg, Exception e) {
		super(msg, e);
	}
	
	public ExcelLimitException(String msg) {
		super(msg);
	}
	
	private static final long serialVersionUID = 9164248069465424019L;
	
}

package org.xydra.csv;

public class WrongDatatypeException extends RuntimeException {

	public WrongDatatypeException(String msg, Exception e) {
		super(msg, e);
	}

	private static final long serialVersionUID = 9164248069465424019L;

}

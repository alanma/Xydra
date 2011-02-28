package org.xydra.csv;

/**
 * A cell is a string with some utility functions.
 * 
 * @author xamde
 */
public interface ICell {
	
	/**
	 * Append 's' to current value until maximal field length is reached. Then
	 * only a sub-string is appended to fully utilise available chars.
	 * 
	 * @param s to be appended
	 * @param maximalFieldLength after appending
	 */
	void appendString(String s, int maximalFieldLength);
	
	String getValue();
	
	double getValueAsDouble() throws WrongDatatypeException;
	
	long getValueAsLong() throws WrongDatatypeException;
	
	/**
	 * Treat value as long and increment
	 * 
	 * @param increment to be added
	 * @throws WrongDatatypeException if there was no long value (null is OK,
	 *             treated as 0)
	 */
	void incrementValue(int increment) throws WrongDatatypeException;
	
	/**
	 * @param value may be null, to store a null.
	 * @param initial if true, throws an {@link IllegalStateException} if there
	 *            was already a value
	 * @throws IllegalStateException if there was already a value
	 */
	void setValue(String value, boolean initial);
	
}

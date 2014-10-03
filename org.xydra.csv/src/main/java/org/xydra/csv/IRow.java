package org.xydra.csv;

/**
 * A Row is a sparse representation of a table row.
 * 
 * @author voelkel
 */
public interface IRow extends IReadableRow {

	/**
	 * Add all values from the otherRow to this Row
	 * 
	 * @param otherRow
	 *            from which to add all cells
	 */
	void addAll(IReadableRow otherRow);

	/**
	 * Aggregate given row into this row
	 * 
	 * @param row
	 *            never null
	 * @param keyColumnNames
	 */
	void aggregate(IReadableRow row, String[] keyColumnNames);

	void appendValue(String columnName, String value, int maximalFieldLength);

	void incrementValue(String columnName, int increment) throws WrongDatatypeException;

	void setValue(String columnName, long value, boolean initial);

	/**
	 * @param columnName
	 *            never null
	 * @param value
	 *            may be null
	 * @param initial
	 *            if false and there was a already a value.
	 * @throws IllegalStateException
	 *             if 'initial' is true and there was a previous value
	 */
	void setValue(String columnName, String value, boolean initial);

	void setValue(String columnName, double value, boolean initial);

}

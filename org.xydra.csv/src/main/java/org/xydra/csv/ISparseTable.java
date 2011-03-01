package org.xydra.csv;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.csv.impl.memory.Row;


/**
 * Appends all rows, that it handles via its {@link IRowHandler}.
 * 
 * @author xamde
 * 
 */
public interface ISparseTable extends Iterable<Row>, IRowHandler {
	
	/**
	 * Adds a column name which will appear in the output but not necessarily
	 * use any space if now rows contain entries for it.
	 * 
	 * @param columnName
	 */
	void addColumnName(String columnName);
	
	/**
	 * Aggregates all rows that share the same values for the given keys.
	 * 
	 * Aggregation is simple: If the values can be parsed as numbers, they are
	 * added. Otherwise string concatenation with a separator is used.
	 * 
	 * Example: Aggregating a table with columns "a", "b" and "c" using
	 * keyColumnNames=("a","b") will add all numbers of "c" where "a" and "b"
	 * are equal. Given the table
	 * <table>
	 * <tr>
	 * <th>a</th>
	 * <th>b</th>
	 * <th>c</th>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>on</td>
	 * <td>1</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>3</td>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>off</td>
	 * <td>7</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>11</td>
	 * </tr>
	 * <tr>
	 * <td>green</td>
	 * <td>on</td>
	 * <td>13</td>
	 * </tr>
	 * </table>
	 * results in
	 * <table>
	 * <tr>
	 * <th>a</th>
	 * <th>b</th>
	 * <th>c</th>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>on</td>
	 * <td>1</td>
	 * </tr>
	 * <tr>
	 * <td>blue</td>
	 * <td>off</td>
	 * <td>7</td>
	 * </tr>
	 * <tr>
	 * <td>red</td>
	 * <td>on</td>
	 * <td>14</td>
	 * </tr>
	 * <tr>
	 * <td>green</td>
	 * <td>on</td>
	 * <td>13</td>
	 * </tr>
	 * </table>
	 * 
	 * Existing sorting order of table is ignored.
	 * 
	 * @param keyColumnNames an array of column names which is used for
	 *            aggregation.
	 */
	void aggregate(String[] keyColumnNames);
	
	/**
	 * Deletes all data, but remembers the column names. This allows to generate
	 * several tables with the same layout, even if data is cleared in between.
	 */
	void clear();
	
	/**
	 * Create a sub-table
	 * 
	 * @param key never null
	 * @param value may be null
	 * @return a sub-table with dropped rows. The resulting table has no row in
	 *         which column 'columnName' has the given 'value'.
	 */
	ISparseTable drop(String columnName, String value);
	
	/**
	 * Create a sub-table
	 * 
	 * @param columnName never null
	 * @param value may be null
	 * @return a sub-table where in each row columnName has the given value
	 */
	ISparseTable filter(String columnName, String value);
	
	Set<String> getColumnNames();
	
	/**
	 * @return sorted column names or null if column names are not sorted (see
	 *         constructor)
	 */
	Iterable<String> getColumnNamesSorted();
	
	/**
	 * Returns an existing row or creates a new one. The new row is guaranteed
	 * to be inserted into the table.
	 * 
	 * @param rowName a String that uniquely identifies a {@link Row}. Never
	 *            null.
	 * @param create if true, creates a new {@link Row} if there is none yet. If
	 *            false and there was now row, null is returned.
	 * @return a new or exiting {@link Row} or null (if 'create' was false and
	 *         no {@link Row} with given 'rowName' exists).
	 */
	IRow getOrCreateRow(String rowName, boolean create);
	
	boolean getParamAggregateStrings();
	
	boolean getParamRestrictToExcelSize();
	
	/**
	 * @param row never null
	 * @param column never null
	 * @return the table value at cell ('row','column')
	 */
	String getValue(String row, String column);
	
	/**
	 * Increments a value at (row,column). Creates the cell before, if
	 * necessary.
	 * 
	 * @param row never null
	 * @param column never null
	 * @param increment the increment, often '1' is used.
	 * @throws WrongDatatypeException if the stored value cannot be cast to a
	 *             long.
	 */
	void incrementValue(String row, String column, int increment) throws WrongDatatypeException;
	
	/* implement Iterable */
	Iterator<Row> iterator();
	
	/**
	 * Removes all rows that match the given RowFilter.
	 * 
	 * @param rowFilter never null
	 */
	void removeRowsMatching(RowFilter rowFilter);
	
	/**
	 * @return the number of rows in the table. A very fast operation.
	 */
	int rowCount();
	
	/**
	 * @param aggregateStrings Default is true. If false, strings are not
	 *            aggregated.
	 */
	void setParamAggregateStrings(boolean aggregateStrings);
	
	/**
	 * @param b if true, the table restricts itself to a size Excel can handle
	 *            and ignores too much data. Warnings are logged in this case.
	 * 
	 *            Default is false.
	 */
	void setParamRestrictToExcelSize(boolean b);
	
	/**
	 * Set an initial value to cell (rowName, columnName)
	 * 
	 * @param rowName never null
	 * @param columnName never null
	 * @param value may be null
	 * @throws IllegalStateException if there was already a value
	 */
	void setValueInitial(String rowName, String columnName, String value);
	
	/**
	 * For every change in 'colName' a new sub-table is created with the rows
	 * having the same values for 'colName'. Sorting is always performed
	 * automatically before executing this command.
	 * 
	 * @param colName never null
	 * @return a Map from String (the value of column 'colName') to
	 *         {@link ISparseTable}
	 */
	Map<String,? extends ISparseTable> split(String colName);
	
	/**
	 * Visit all rows with an {@link IRowVisitor}.
	 * 
	 * @param rowVisitor never null
	 */
	void visitRows(IRowVisitor rowVisitor);
	
}

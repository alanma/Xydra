package org.xydra.csv;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;


/**
 * A Row is a sparse representation of a table row.
 * 
 * @author voelkel
 */
public interface IReadableRow extends Iterable<ICell> {
	
	/**
	 * @return the row key (= the row name)
	 */
	String getKey();
	
	/**
	 * @return the row's content as a Set of Entrys
	 */
	Set<Entry<String,ICell>> entrySet();
	
	/**
	 * @return the column names used in this row
	 */
	Collection<String> getColumnNames();
	
	/**
	 * @param columnName for which to get the value in this row
	 * @return the value or the String "null"
	 */
	String getValue(String columnName);
	
	/**
	 * @param columnName
	 * @return getValue(columnName).getValueAsDouble()
	 */
	double getValueAsDouble(String columnName) throws WrongDatatypeException;
	
	/**
	 * @param columnName never null
	 * @return 0 if value is not set.
	 * @throws WrongDatatypeException if value was set, but could not be parsed
	 *             as long.
	 */
	long getValueAsLong(String columnName) throws WrongDatatypeException;
	
	@Override
	Iterator<ICell> iterator();
	
}

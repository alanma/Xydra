package org.xydra.csv;

import org.xydra.csv.impl.memory.Row;

/**
 * Handlers to execute before and after row insertion. The handler can reject
 * rows from being inserted by returning false in the
 * {@link #beforeRowInsertion(Row)} method.
 *
 * @author xamde
 */
public interface IRowInsertionHandler {
	void afterRowInsertion(IRow row);

	/**
	 * @param row
	 * @return if the row should really be inserted
	 */
	boolean beforeRowInsertion(Row row);

}

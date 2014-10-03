package org.xydra.csv;

import java.io.IOException;
import java.util.Collection;

public interface IRowHandler {

	void handleHeaderRow(Collection<String> columnNames) throws IOException;

	/**
	 * @param rowName
	 *            must be unique within a stream
	 * @param readableRow
	 *            from which all columns listed in
	 *            {@link #handleHeaderRow(Collection)} will be used.
	 * @throws IllegalStateException
	 *             if there was no {@link #handleHeaderRow(Collection)} yet.
	 * @throws IOException
	 *             if there is an underlying I/O exception
	 */
	void handleRow(String rowName, IReadableRow readableRow) throws IllegalStateException,
			IOException;

}

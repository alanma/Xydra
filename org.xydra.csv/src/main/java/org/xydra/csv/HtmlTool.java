package org.xydra.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.csv.impl.memory.Row;

@RunsInGWT(false)
public class HtmlTool {

	public static interface ICellRenderer {
		String html(String cellContent);
	}

	public static final ICellRenderer identityCellRenderer = new ICellRenderer() {

		@Override
		public String html(final String cellContent) {
			return htmlencode(cellContent);
		}
	};

	/**
	 * Write the given table as simple HTML to the given writer.
	 *
	 * @param table
	 *            a table, never null
	 * @param sortColName
	 *            if this is not null, resulting HTML table will be sorted by
	 *            the given column name
	 * @param w
	 *            never null
	 * @throws IOException
	 *             ...
	 */
	public static void writeToHtml(final ISparseTable table, final String sortColName, final Writer w)
			throws IOException {
		writeToHtml(table, sortColName, w, identityCellRenderer);
	}

	/**
	 * Write the given table as simple HTML to the given writer.
	 *
	 * @param table
	 *            a table, never null
	 * @param sortColName
	 *            if this is not null, resulting HTML table will be sorted by
	 *            the given column name
	 * @param w
	 *            never null
	 * @param cellRenderer
	 * @throws IOException
	 *             ...
	 */
	public static void writeToHtml(final ISparseTable table, final String sortColName, final Writer w,
			final ICellRenderer cellRenderer) throws IOException {
		w.write("<table class='csv'>\n");
		w.write("<tr>");

		final List<String> colNames = new ArrayList<String>();
		colNames.addAll(table.getColumnNames());
		// sort alphabetically
		Collections.sort(colNames);

		for (final String colName : colNames) {
			w.write("<th>" + htmlencode(colName) + "</th>");
		}
		w.write("</tr>\n");

		Iterable<Row> rows = table;
		if (sortColName != null) {
			rows = TableCoreTools.sortByColumn(table, sortColName);
		}

		for (final IRow row : rows) {
			w.write("<tr>");
			for (final String colName : colNames) {
				String value = row.getValue(colName);
				value = cellRenderer.html(value);
				w.write("<td>" + value + "</td>");
			}
			w.write("</tr>\n");
		}
		w.write("</table>\n");
	}

	/**
	 * @param s
	 *            any string, might contain HTML-like stuff
	 * @return string with basic XML encoding to prevent broken HTML
	 */
	public static String htmlencode(final String s) {
		return s.replace("&", "&apos;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&apos;");
	}

}

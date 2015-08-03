package org.xydra.server.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.sharedutils.XyAssert;

public class XydraHtmlUtils {

	/**
	 * Write events as HTML table to writer. Does not flush.
	 *
	 * @param events
	 *            never null
	 * @param w
	 *            never null
	 * @throws IOException
	 *             ...
	 */
	public static void writeEvents(@NeverNull final List<XEvent> events, final Writer w) throws IOException {
		XyAssert.xyAssert(events != null);
		assert events != null;

		w.write("<table border='1'>" +

		"<tr>"

		+ "<th>rev</th>"

		+ "<th>target</th>"

		+ "<th>type</th>"

		+ "<th>what</th>"

		+ "<th>oldRevs</th>"

		+ "<th>txn</th>"

		+ "<th>implied</th>"

		+ "</tr>");
		for (final XEvent e : events) {
			writeEventRow(e, w);
			w.flush();
			if (e.getChangeType() == ChangeType.TRANSACTION) {
				assert e instanceof XTransactionEvent;
				final XTransactionEvent te = (XTransactionEvent) e;
				for (final XEvent child : te) {
					writeEventRow(child, w);
					w.flush();
				}
			}
		}
		w.write("</table>");
	}

	private static void writeEventRow(final XEvent e, final Writer w) throws IOException {
		w.write("<tr>"

		+ "<td>" + e.getRevisionNumber() + "</td>"

		+ "<td>" + e.getTarget() + "</td> "

		+ "<td>" + e.getChangeType() + "</td>"

		+ "<td>" + e.getChangedEntity() + "</td>"

		+ "<td>" + e.getOldModelRevision() + "/" + e.getOldObjectRevision() + "/"
				+ e.getOldFieldRevision() + "</td>"

				+ "<td>" + e.inTransaction() + "</td>"

				+ "<td>" + e.isImplied() + "</td>"

				+ "</tr>\n");

	}

	/**
	 * @param xo
	 *            to be rendered as HTML
	 * @return an HTML String representation of the given XObject
	 */
	public static String toHtml(final XReadableObject xo) {
		final StringBuffer buf = new StringBuffer();
		buf.append("<b>Object '");
		buf.append(xo.getAddress());
		buf.append("' <span class='rev'>");
		buf.append(xo.getRevisionNumber());
		buf.append("</span></b>\n");
		final SortedMap<String, String> map = new TreeMap<String, String>();
		final Iterator<XId> fieldIt = xo.iterator();
		while (fieldIt.hasNext()) {
			final XId fieldId = fieldIt.next();
			final XReadableField field = xo.getField(fieldId);
			assert field != null;
			final XValue value = field.getValue();
			map.put(fieldId.toString(),
					(value == null ? "null" : SharedHtmlUtils.sanitize(value.toString()))
							+ " <span class='rev'>" + field.getRevisionNumber() + "</span>");
		}
		return buf + SharedHtmlUtils.toDefinitionList(map);
	}

}

package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;


public class XydraHtmlUtils {
	
	/**
	 * Write events as HTML table to writer. Does not flush.
	 * 
	 * @param events never null
	 * @param w never null
	 * @throws IOException ...
	 */
	public static void writeEvents(List<XEvent> events, Writer w) throws IOException {
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
		for(XEvent e : events) {
			writeEventRow(e, w);
			w.flush();
			if(e.getChangeType() == ChangeType.TRANSACTION) {
				assert (e instanceof XTransactionEvent);
				XTransactionEvent te = (XTransactionEvent)e;
				for(XEvent child : te) {
					writeEventRow(child, w);
					w.flush();
				}
			}
		}
		w.write("</table>");
	}
	
	private static void writeEventRow(XEvent e, Writer w) throws IOException {
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
	
}

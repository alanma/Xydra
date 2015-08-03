package org.xydra.store.impl.gae.changes;

import java.io.IOException;
import java.io.Writer;

import org.xydra.base.XAddress;
import org.xydra.store.impl.gae.InstanceRevisionManager;
import org.xydra.store.impl.utils.DebugFormatter;

public class ChangesUtils {

	public static void renderChangeLog(final XAddress modelAddress, final Writer w) throws IOException {
		w.write("<h2>Changelog of " + modelAddress + "</h2>\n");
		w.flush();
		int i = 0;
		final InstanceRevisionManager rm = new InstanceRevisionManager(modelAddress);
		final GaeChangesServiceImpl3 changes = new GaeChangesServiceImpl3(modelAddress, rm);
		GaeChange c = changes.getChange(i);
		w.write("<ol>");
		while (c != null) {
			w.write("<li>");
			// render c
			final String s = DebugFormatter.format(c);
			w.write(s + "<br />\n");
			w.flush();
			i++;
			c = changes.getChange(i);
			w.write("</li>");
		}
		w.write("</ol>");
		w.write("End of changelog.<br/>\n");
	}

}

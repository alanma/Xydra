package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;

public class AppConstants {

	/** Application name */
	public static final String NAME = "GaeMyAdmin";

	public static final String CSS_DEBUG_PATH = "/s/xyadmin.css";

	public static Writer startPage(final HttpServletResponse res, final String page, final String part)
			throws IOException {
		final Writer w = HtmlUtils.startHtmlPage(res, AppConstants.NAME + ":: " + page + " " + part,
				new HeadLinkStyle(AppConstants.CSS_DEBUG_PATH));
		w.write("<div class='xydebug' >");
		w.write(SharedHtmlUtils.link("..", "Up"));
		w.write(" | ");
		w.write(SharedHtmlUtils.link(".", "Back"));
		w.write("<h3>" + page + " " + part + "</h3>\n");
		return w;
	}

	public static void endPage(final Writer w) throws IOException {
		w.write("</div>");
		HtmlUtils.endHtmlPage(w);
	}

}

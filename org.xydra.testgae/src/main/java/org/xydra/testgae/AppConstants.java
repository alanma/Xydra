package org.xydra.testgae;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.gae.AboutAppEngine;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;


public class AppConstants {
	
	/** Application name */
	public static final String NAME = "TestGAE";
	
	public static final String CSS_DEBUG_PATH = "/s/xydebug.css";
	
	public static Writer startPage(HttpServletResponse res, String page, String part)
	        throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, AppConstants.NAME + ":: " + page + " " + part,
		        new HeadLinkStyle(AppConstants.CSS_DEBUG_PATH));
		w.write("<div class='xydebug' >");
		w.write(HtmlUtils.link(".", "Back"));
		w.write("Instance: " + AboutAppEngine.getInstanceId() + " Thread: "
		        + AboutAppEngine.getThreadInfo() + "<br/>\n");
		w.write("<h3>" + page + " " + part + "</h3>\n");
		return w;
	}
	
	public static void endPage(Writer w) throws IOException {
		w.write("</div>");
		HtmlUtils.endHtmlPage(w);
	}
	
}

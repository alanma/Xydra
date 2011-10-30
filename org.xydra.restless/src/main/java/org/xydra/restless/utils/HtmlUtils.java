package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;


/**
 * Genereates some simple HTML5.
 * 
 * @author xamde
 * 
 */
public class HtmlUtils extends SharedHtmlUtils {
	
	public static void writeHtmlHeaderOpenBody(Writer w, String title, HeadChild ... headChildren)
	        throws IOException {
		w.write("<!DOCTYPE html>\r\n"

		+ "<html>\r\n"

		+ "  <head>\r\n"

		+ "    <title>" + title + "</title>\r\n"

		+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n");
		
		if(headChildren != null) {
			for(HeadChild headChild : headChildren) {
				w.write("    " + headChild.toString() + "\r\n");
			}
		}
		
		w.write("  </head>\r\n"

		+ "<body><div>\r\n");
	}
	
	/**
	 * Closes the HTML and flushes the writer.
	 * 
	 * @param w writer
	 * @throws IOException from writer
	 */
	public static void writeCloseBodyHtml(Writer w) throws IOException {
		w.write("</div></body>\r\n" + "</html>");
		w.flush();
	}
	
	public static void writeHtmlPage(HttpServletResponse res, String title, String content)
	        throws IOException {
		Writer w = startHtmlPage(res, title);
		w.write(content);
		endHtmlPage(w);
	}
	
	public static void endHtmlPage(Writer w) throws IOException {
		writeCloseBodyHtml(w);
		w.close();
	}
	
	/**
	 * Creates a text/html, UTF8, non-cached HTML page header
	 * 
	 * @param res sets content type html + encoding UTF8
	 * @param title HTML head - title
	 * @return a UTF-8 writer for the result stream
	 * @throws IOException from underlying streams
	 */
	public static Writer startHtmlPage(HttpServletResponse res, String title,
	        HeadChild ... headChildren) throws IOException {
		ServletUtils.headers(res, ServletUtils.CONTENTTYPE_TEXT_HTML);
		Writer w = res.getWriter();
		writeHtmlHeaderOpenBody(w, title, headChildren);
		w.flush();
		return w;
	}
	
}

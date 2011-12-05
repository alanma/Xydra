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
	 * @param headChildren e.g. {@link SharedHtmlUtils.HeadLinkStyle}
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
	
	public static void writeInTheMiddleOfAResponse(Writer w, String messageHtml, String redirectUrl)
	        throws IOException {
		w.write("<div style='" + "position:absolute; left:20px; top:20px;" + "z-index: 1000;"
		        + "padding: 10px;" + "background-color: #FFC;" + "font-family: sans-serif;"
		        + "border: 1px solid #999;" + "max-width: 480px;" + "word-break: break-all;" +

		        "'>");
		w.write("<a style='" + "line-height: normal;" + "padding: 9px 14px 9px;"
		        + "-webkit-border-radius: 6px;" + "font-size: 30px;" + "border: 2px solid;"
		        + "text-align: center;" + "display: block;" + "min-height: 70px;"
		        + "word-wrap: break-word;" + "color: white;" + "text-decoration: none;"
		        + "border-radius: 2px;"
		        + "border-color: rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.25);"

		        + "color: white;" + "background-color: #0064CD;"

		        + "background-image: -webkit-linear-gradient(top, #049cdb, #0064cd);"
		        + "background-image: -o-linear-gradient(top, #049cdb, #0064cd);"
		        + "background-image: linear-gradient(top, #049cdb, #0064cd);"

		        + "text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25);"

		        + "' href='" + redirectUrl + "'>Oops. Click here to continue.</a><br/>");
		w.write(messageHtml);
		
		w.write("<script type='text/javascript'>\n" + "function redir() { window.location = '"
		        + redirectUrl + "'; }\n" + "setTimeout('redir()',5000);" + "</script>");
		
		// IMPROVE do automatic redirect after given time using plain js
		w.write("</div>");
		w.flush();
		HtmlUtils.endHtmlPage(w);
	}
	
}

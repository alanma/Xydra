package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;
import org.xydra.restless.Restless;

/**
 * Genereates some simple HTML5.
 * 
 * @author xamde
 * 
 */

@ThreadSafe
public class HtmlUtils extends SharedHtmlUtils {

	/*
	 * writers and headchildren shouldn't be shared between threads, so we don't
	 * need to synchronize on them.
	 */

	/*
	 * IMPROVE some of these methods could be made private, I think (since
	 * they're only used as part of other methods of this class)
	 */

	/**
	 * Writes core html content including charset header
	 * 
	 * @param w
	 * @NeverNull
	 * @param title
	 * @CanBeNull
	 * @param headChildren
	 * @CanBeNull
	 * @throws IOException
	 */
	public static void writeHtmlHeaderOpenBody(@NeverNull Writer w, @CanBeNull String title,
			@CanBeNull HeadChild... headChildren) throws IOException {

		w.write("<!DOCTYPE html>\r\n"

		+ "<html>\r\n"

		+ "  <head>\r\n"

		+ "    <title>" + title + "</title>\r\n"

		+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset="
				+ Restless.CONTENT_TYPE_CHARSET_UTF8 + "\" />\r\n");

		if (headChildren != null) {

			for (HeadChild headChild : headChildren) {
				w.write("    " + headChild.toString() + "\r\n");
			}

		}

		w.write("  </head>\r\n"

		+ "<body><div>\r\n");

	}

	/**
	 * Closes the HTML and flushes the writer.
	 * 
	 * @param w
	 *            writer @NeverNull
	 * @throws IOException
	 *             from writer
	 */
	public static void writeCloseBodyHtml(@NeverNull Writer w) throws IOException {
		w.write("</div></body>\r\n" + "</html>");
		w.flush();
	}

	/**
	 * 
	 * @param res
	 * @NeverNull
	 * @param title
	 * @CanBeNull
	 * @param content
	 * @CanBeNull
	 * @throws IOException
	 */
	public static void writeHtmlPage(@NeverNull HttpServletResponse res, @CanBeNull String title,
			@CanBeNull String content) throws IOException {
		Writer w = startHtmlPage(res, title);
		w.write(content);
		endHtmlPage(w);
	}

	/**
	 * 
	 * @param w
	 * @NeverNull
	 * @throws IOException
	 */
	public static void endHtmlPage(@NeverNull Writer w) throws IOException {

		writeCloseBodyHtml(w);
		w.close();

	}

	/**
	 * Creates a text/html, UTF8, non-cached HTML page header
	 * 
	 * @param res
	 *            sets content type html + encoding UTF8 @NeverNull
	 * @param title
	 *            HTML head - title @CanBeNull
	 * @param headChildren
	 *            e.g. {@link SharedHtmlUtils.HeadLinkStyle} @CanBeNull
	 * @return a UTF-8 writer for the result stream
	 * @throws IOException
	 *             from underlying streams
	 */
	public static Writer startHtmlPage(@NeverNull HttpServletResponse res, @CanBeNull String title,
			@CanBeNull HeadChild... headChildren) throws IOException {

		ServletUtils.headers(res, ServletUtils.CONTENTTYPE_TEXT_HTML);
		Writer w = res.getWriter();
		writeHtmlHeaderOpenBody(w, title, headChildren);
		w.flush();
		return w;
	}

	/**
	 * 
	 * @param res
	 * @NeverNull
	 * @param statusCode
	 * @NeverNull
	 * @param title
	 * @CanBeNull
	 * @param headChildren
	 * @CanBeNull
	 * @return an opened writer that has just written the start of the page
	 * @throws IOException
	 */
	public static Writer startHtmlPage(@NeverNull HttpServletResponse res, int statusCode,
			@CanBeNull String title, @CanBeNull HeadChild... headChildren) throws IOException {
		ServletUtils.headers(res, statusCode, -1, ServletUtils.CONTENTTYPE_TEXT_HTML);
		Writer w = res.getWriter();
		writeHtmlHeaderOpenBody(w, title, headChildren);
		w.flush();
		return w;
	}

	/**
	 * @param redirectUrl
	 * @NeverNull
	 * @param redirectWaitMs
	 *            >= 0 @NeverNull
	 * @return a script snippet
	 */
	public static String scriptRedirect(@NeverNull String redirectUrl, int redirectWaitMs) {
		String redirMarker = "#";
		return "<script type='text/javascript'>\n" // .
				+ "function redir() { \n" // .
				+ "if( (window.location+'').indexOf('"
				+ redirMarker
				+ "')==-1) { window.location = '" + redirectUrl + redirMarker + "'; }\n" // .
				+ "}\n" // .
				+ "setTimeout('redir()'," + redirectWaitMs + ");\n"// .
				+ "</script>\n";// .
	}

	/**
	 * @param w
	 * @NeverNull
	 * @param messageHtml
	 * @NeverNull
	 * @param redirectUrl
	 * @NeverNull
	 * @param redirectWaitMs
	 *            >= 0 @NeverNull
	 * @throws IOException
	 *             ...
	 */
	public static void writeInTheMiddleOfAResponse(@NeverNull Writer w,
			@NeverNull String messageHtml, @NeverNull String redirectUrl, int redirectWaitMs)
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
		w.write(scriptRedirect(redirectUrl, redirectWaitMs));
		w.write("</div>");
		w.flush();
		HtmlUtils.endHtmlPage(w);

	}

	public static void script(@NeverNull Writer w, String scriptCode) throws IOException {
		w.write("<script type='text/javascript'>\n");
		w.write(scriptCode);
		w.write("\n</script>\n");
	}

}

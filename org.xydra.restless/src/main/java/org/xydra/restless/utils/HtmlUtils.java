package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;


public class HtmlUtils {
	
	public static void writeHtmlHeaderOpenBody(Writer w, String title) throws IOException {
		w.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\r\n"
		        + "		          \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n"

		        + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "<head>\r\n" + "<title>"
		        + title + "</title>\r\n"
		        + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n"
		        + "</head>\r\n"

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
	
	public static String link(String url, String text) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}
	
	/**
	 * @param url href target and link label
	 * @return a HTML 'a' element using url both as label and as link target
	 */
	public static String link(String url) {
		return "<a href=\"" + url + "\">" + url + "</a>";
	}
	
	public static SubmitInput inputSubmit(String label) {
		return new SubmitInput(label);
	}
	
	public static class Input {
		
	}
	
	public static class SubmitInput extends Input {
		
		private String label;
		
		public SubmitInput(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return "<input type=\"submit\" value=\"" + this.label + "\"/>";
		}
		
	}
	
	static class KeyValueInput extends Input {
		
		protected String name;
		protected String value;
		
		public KeyValueInput(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	public static class HiddenInput extends KeyValueInput {
		
		public HiddenInput(String name, String value) {
			super(name, value);
		}
		
		@Override
		public String toString() {
			return "<input" +

			" type=\"hidden\"" +

			" name=\"" + this.name + "\"" +

			" value=\"" + this.value + "\"" +

			"/>";
		}
	}
	
	public static class TextInput extends KeyValueInput {
		
		private int size;
		
		public TextInput(String name, String value) {
			this(name, value, 30);
		}
		
		public TextInput(String name, String value, int size) {
			super(name, value);
			this.size = size;
		}
		
		@Override
		public String toString() {
			return this.name + ": <input" +

			" type=\"text\"" +

			" name=\"" + this.name + "\"" +

			" value=\"" + this.value + "\"" +

			" size=\"" + this.size + "\"" +

			"/>";
		}
		
	}
	
	public static class TextAreaInput extends Input {
		
		private int rows;
		private int cols;
		
		public TextAreaInput(String name, String value, int cols, int rows) {
			super();
			this.name = name;
			this.value = value;
			this.cols = cols;
			this.rows = rows;
		}
		
		private String name;
		private String value;
		
		@Override
		public String toString() {
			return this.name + ": <textarea name=\"" + this.name

			+ "\" cols=\"" + this.cols

			+ "\" rows=\"" + this.rows

			+ "\">" + this.value + "</textarea>";
		}
		
	}
	
	public static Form form(METHOD method, String action) {
		return new Form(method, action);
	}
	
	public static enum METHOD {
		GET, POST
	}
	
	public static class Form {
		
		private String action;
		private METHOD method;
		private List<Input> inputs = new LinkedList<HtmlUtils.Input>();
		
		public Form(METHOD method, String action) {
			this.method = method;
			this.action = action;
		}
		
		/**
		 * @param name input name
		 * @param value predefined form value
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputText(String name, String value) {
			this.inputs.add(new TextInput(name, value));
			return this;
		}
		
		/**
		 * @param name input name
		 * @param value predefined form value
		 * @return the {@link Form} for a fluent API
		 */
		public Form withHiddenInputText(String name, String value) {
			this.inputs.add(new HiddenInput(name, value));
			return this;
		}
		
		/**
		 * @param name input name
		 * @param value predefined form value
		 * @param size of text field
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputText(String name, String value, int size) {
			this.inputs.add(new TextInput(name, value, size));
			return this;
		}
		
		/**
		 * @param name form name
		 * @param value predefined form value
		 * @param cols number of columns
		 * @param rows number of rows
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputTextArea(String name, String value, int cols, int rows) {
			this.inputs.add(new TextAreaInput(name, value, cols, rows));
			return this;
		}
		
		public Form withInputSubmit(String value) {
			this.inputs.add(new SubmitInput(value));
			return this;
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("<form action='" + this.action + "' method='" + this.method + "'>");
			for(Input input : this.inputs) {
				buf.append(input.toString() + "\n");
			}
			buf.append("</form>");
			return buf.toString();
		}
		
	}
	
	/**
	 * Utility functions to turn a Map to HTML definition list
	 * 
	 * @param mapEntries input, nut null. Entries are NOT HTML-encoded or
	 *            escaped in any way. Used valid HTML inside!
	 * @return a string containing the resulting HTML
	 */
	public static String toDefinitionList(Map<String,? extends Object> mapEntries) {
		StringBuffer buf = new StringBuffer();
		buf.append("<dl>");
		for(Entry<String,? extends Object> e : mapEntries.entrySet()) {
			buf.append("<dt>" + e.getKey() + "</dt><dd>" + e.getValue() + "</dd>");
		}
		buf.append("</dl>");
		return buf.toString();
	}
	
	/**
	 * Utility functions to turn a Collection of Strings to HTML ordered list
	 * 
	 * @param listEntries input, not null
	 * @return a string containing the resulting HTML
	 */
	public static String toOrderedList(Collection<String> listEntries) {
		StringBuffer buf = new StringBuffer();
		buf.append("<ol>");
		for(String li : listEntries) {
			buf.append("<li>" + li + "</li>");
		}
		buf.append("</ol>");
		return buf.toString();
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
	public static Writer startHtmlPage(HttpServletResponse res, String title) throws IOException {
		ServletUtils.headers(res, ServletUtils.CONTENTTYPE_TEXT_HTML);
		Writer w = res.getWriter();
		writeHtmlHeaderOpenBody(w, title);
		w.flush();
		return w;
	}
	
	public static String toOrderedList(String ... listItemContent) {
		return toOrderedList(Arrays.asList(listItemContent));
	}
	
	/**
	 * @param s may be null
	 * @return a sanitised form of s that cannot have malicious side effects.
	 *         Result is XHTML compliant.
	 */
	public static String sanitize(String s) {
		if(s == null) {
			return null;
		}
		return XmlUtils.xmlEncode(s);
	}
	
	private static final String MALICIOUS_INPUT_SAMPLE = "Dirk<script>alert('test');</script>";
	
	public static void main(String[] args) {
		System.out.println(sanitize(MALICIOUS_INPUT_SAMPLE));
	}
	
}

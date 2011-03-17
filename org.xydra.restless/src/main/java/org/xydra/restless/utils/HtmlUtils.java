package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class HtmlUtils {
	
	public static void writeHtmlHeaderOpenBody(Writer w, String title) throws IOException {
		w.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\r\n"
		        + "       \"http://www.w3.org/TR/html4/loose.dtd\">\r\n"

		        + "<html>\r\n" + "<head>\r\n" + "<title>Beschreibung der Seite</title>\r\n"
		        + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n"
		        + "</head>\r\n"

		        + "<body>\r\n");
	}
	
	/**
	 * Closes the HTML and flushes the writer.
	 * 
	 * @param w writer
	 * @throws IOException from writer
	 */
	public static void writeCloseBodyHtml(Writer w) throws IOException {
		w.write("</body>\r\n" + "</html>");
		w.flush();
	}
	
	public static String link(String url, String text) {
		return "<a href=\"" + url + "\">" + text + "</a>";
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
	
	public static class TextInput extends Input {
		
		public TextInput(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		
		private String name;
		private String value;
		
		@Override
		public String toString() {
			return this.name + ": <input type=\"text\" name=\"" + this.name + "\" value=\""
			        + this.value + "\"/>";
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
		
		public Form withInputText(String name, String value) {
			this.inputs.add(new TextInput(name, value));
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
				buf.append(input.toString() + "<br />\n");
			}
			buf.append("</form>");
			return buf.toString();
		}
		
	}
	
	/**
	 * Utility functions to turn a Map to HTML definition list
	 * 
	 * @param mapEntries input, nut null
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
	
}

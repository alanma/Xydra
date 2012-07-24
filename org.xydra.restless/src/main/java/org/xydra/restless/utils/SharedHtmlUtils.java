package org.xydra.restless.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.annotation.NotThreadSafe;
import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.RunsInGWT;


/**
 * Genereates some simple HTML5.
 * 
 * Can also run in GWT.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
@NotThreadSafe
public class SharedHtmlUtils {
	
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
	
	public static class FileInput extends KeyValueInput {
		
		private int size;
		
		/**
		 * @param name ..
		 * @param size of file name field
		 */
		public FileInput(String name, int size) {
			super(name, "");
			this.size = size;
		}
		
		@Override
		public String toString() {
			return super.name + ": <input" +
			
			" type=\"file\"" +
			
			" name=\"" + super.name + "\"" +
			
			" value=\"" + this.value + "\"" +
			
			" size=\"" + this.size + "\"" +
			
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
	
	public static Table table() {
		return new Table();
	}
	
	public static enum METHOD {
		GET, POST
	}
	
	public static class Table {
		
		private List<TableRow> rows = new LinkedList<SharedHtmlUtils.Table.TableRow>();
		
		public TableRow row() {
			TableRow row = new TableRow();
			this.rows.add(row);
			return row;
		}
		
		public class TableRow {
			
			private StringBuilder b = new StringBuilder();
			
			public TableRow td(String content) {
				this.b.append("<td>").append(content).append("</td>");
				return this;
			}
			
			public TableRow th(String header) {
				this.b.append("<th>").append(header).append("</th>");
				return this;
			}
			
			public Table rowEnd() {
				return Table.this;
			}
			
		}
		
		public String toString() {
			return toStringBuilder().toString();
		}
		
		public StringBuilder toStringBuilder() {
			StringBuilder b = new StringBuilder();
			b.append("<table>");
			for(TableRow r : this.rows) {
				b.append("<tr>");
				b.append(r.b);
				b.append("</tr>");
			}
			b.append("</table>");
			return b;
		}
		
	}
	
	public static class Form {
		
		private String action;
		private METHOD method;
		private List<Input> inputs = new LinkedList<SharedHtmlUtils.Input>();
		private boolean hasFileInput = false;
		
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
		 * @param name input name
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputFile(String name) {
			this.inputs.add(new FileInput(name, 100));
			this.hasFileInput = true;
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
			buf.append("<form action='" + this.action + "' method='" + this.method + "'"
			        + (hasFileInput() ? " enctype='multipart/form-data'" : "") + ">");
			for(Input input : this.inputs) {
				buf.append(input.toString() + "\n");
			}
			buf.append("</form>");
			return buf.toString();
		}
		
		private boolean hasFileInput() {
			return this.hasFileInput;
		}
		
	}
	
	public static class HeadLink implements HeadChild {
		
		private String href;
		private @CanBeNull
		String rel;
		private String type;
		
		/**
		 * @param href @NeverNull
		 * @param rel can be null
		 * @param type @NeverNull
		 */
		public HeadLink(String href, @CanBeNull String rel, String type) {
			this.href = href;
			this.rel = rel;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return "<link"
			
			+ attribute("href", this.href) + " "
			
			+ attribute("rel", this.rel) + " "
			
			+ attribute("type", this.type)
			
			+ " />";
		}
		
	}
	
	/**
	 * Link a CSS file
	 */
	public static class HeadLinkStyle extends HeadLink {
		
		public HeadLinkStyle(String href) {
			super(href, "stylesheet", "text/css");
		}
		
	}
	
	/**
	 * Elements that can be in the html HEAD element.
	 */
	public static interface HeadChild {
	}
	
	/**
	 * Link a JavaScript file
	 */
	public static class ScriptLink implements HeadChild {
		
		private String href;
		
		public ScriptLink(String href) {
			this.href = href;
		}
		
		@Override
		public String toString() {
			return "<script src=\"" + this.href + "\"></script>";
		}
		
	}
	
	/**
	 * @param name @NeverNull
	 * @param value @CanBeNull
	 * @return ' ' name '=' '"' value '"' OR the emtpy string if value is null.
	 */
	private static String attribute(String name, @CanBeNull String value) {
		assert name != null;
		if(value != null) {
			return " " + name + "=\"" + value + "\"";
		} else {
			return "";
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
		buf.append("<dl>\n");
		for(Entry<String,? extends Object> e : mapEntries.entrySet()) {
			buf.append("<dt>" + e.getKey() + "</dt><dd>" + e.getValue() + "</dd>\n");
		}
		buf.append("</dl>\n");
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
		buf.append("<ol>\n");
		for(String li : listEntries) {
			buf.append("<li>" + li + "</li>\n");
		}
		buf.append("</ol>\n");
		return buf.toString();
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

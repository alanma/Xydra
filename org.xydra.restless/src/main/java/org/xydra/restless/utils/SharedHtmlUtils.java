package org.xydra.restless.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.NotThreadSafe;
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

	/**
	 * @param url @NeverNull
	 * @param text
	 * @CanBeNull
	 * @return HTML source code snippet for a link
	 */
	public static String link(@NeverNull final String url, @CanBeNull final String text) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}

	/**
	 * @param url href target and link label @NeverNull
	 * @return a HTML 'a' element using url both as label and as link target
	 */
	public static String link(@NeverNull final String url) {
		return "<a href=\"" + url + "\">" + url + "</a>";
	}

	/**
	 *
	 * @param label @NeverNull
	 * @return ...
	 */
	public static SubmitInput inputSubmit(@NeverNull final String label) {
		return new SubmitInput(label);
	}

	public static class Input {
		// TODO why is this a class and not an interface?
	}

	public static class SubmitInput extends Input {

		private final String label;

		/**
		 *
		 * @param label
		 * @NeverNull
		 */
		public SubmitInput(@NeverNull final String label) {
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

		/**
		 *
		 * @param name
		 * @CanBeNull
		 * @param value
		 * @CanBeNull
		 */
		public KeyValueInput(@CanBeNull final String name, @CanBeNull final String value) {
			this.name = name;
			this.value = value;
		}
	}

	public static class HiddenInput extends KeyValueInput {

		/**
		 *
		 * @param name
		 * @NeverNull
		 * @param value
		 * @NeverNull
		 */
		public HiddenInput(@NeverNull final String name, @NeverNull final String value) {
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

		private final int size;

		/**
		 * @param name .. @NeverNull
		 * @param size of file name field @NeverNull
		 */
		public FileInput(@NeverNull final String name, final int size) {
			super(name, "");
			this.size = size;
		}

		/**
		 * TODO value is never set in the constructor where size is set and vice
		 * versa
		 */

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

		private final int size;

		/**
		 *
		 * @param name
		 * @NeverNull
		 * @param value
		 * @NeverNull
		 */
		public TextInput(@NeverNull final String name, @NeverNull final String value) {
			this(name, value, 30);
		}

		/**
		 *
		 * @param name
		 * @NeverNull
		 * @param value
		 * @NeverNull
		 * @param size
		 * @NeverNull
		 */
		public TextInput(@NeverNull final String name, @NeverNull final String value, final int size) {
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

		private final int rows;
		private final int cols;

		private final String name;
		private final String value;

		/**
		 *
		 * @param name
		 * @NeverNull
		 * @param value
		 * @NeverNull
		 * @param cols
		 * @NeverNull
		 * @param rows
		 * @NeverNull
		 */
		public TextAreaInput(@NeverNull final String name, @NeverNull final String value, final int cols, final int rows) {
			this.name = name;
			this.value = value;
			this.cols = cols;
			this.rows = rows;
		}

		@Override
		public String toString() {
			return this.name + ": <textarea name=\"" + this.name

			+ "\" cols=\"" + this.cols

			+ "\" rows=\"" + this.rows

			+ "\">" + this.value + "</textarea>";
		}

	}

	/**
	 *
	 * @param method @NeverNull
	 * @param action @NeverNull
	 * @return ...
	 */
	public static Form form(@NeverNull final METHOD method, @NeverNull final String action) {
		return new Form(method, action);
	}

	public static Table table() {
		return new Table();
	}

	public static enum METHOD {
		GET, POST
	}

	public static class Table {

		private final List<TableRow> rows = new LinkedList<SharedHtmlUtils.Table.TableRow>();

		public TableRow row() {
			final TableRow row = new TableRow();
			this.rows.add(row);
			return row;
		}

		public class TableRow {

			private final StringBuilder b = new StringBuilder();

			/**
			 *
			 * @param content
			 * @CanBeNull
			 * @return ...
			 */
			public TableRow td(@CanBeNull final String content) {
				this.b.append("<td>").append(content).append("</td>");
				return this;
			}

			/**
			 *
			 * @param header
			 * @CanBeNull
			 * @return ...
			 */
			public TableRow th(@CanBeNull final String header) {
				this.b.append("<th>").append(header).append("</th>");
				return this;
			}

			public Table rowEnd() {
				return Table.this;
			}

		}

		@Override
		public String toString() {
			return toStringBuilder().toString();
		}

		public StringBuilder toStringBuilder() {
			final StringBuilder b = new StringBuilder();
			b.append("<table>");
			for (final TableRow r : this.rows) {
				b.append("<tr>");
				b.append(r.b);
				b.append("</tr>");
			}
			b.append("</table>");
			return b;
		}

	}

	public static class Form {

		private final String action;
		private final METHOD method;
		private final List<Input> inputs = new LinkedList<SharedHtmlUtils.Input>();
		private boolean hasFileInput = false;

		/**
		 *
		 * @param method
		 * @NeverNull
		 * @param action
		 * @NeverNull
		 */
		public Form(@NeverNull final METHOD method, @NeverNull final String action) {
			this.method = method;
			this.action = action;
		}

		/**
		 * @param name input name @NeverNull
		 * @param value predefined form value @NeverNull
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputText(@NeverNull final String name, @NeverNull final String value) {
			this.inputs.add(new TextInput(name, value));
			return this;
		}

		/**
		 * @param name input name @NeverNull
		 * @param value predefined form value @NeverNull
		 * @return the {@link Form} for a fluent API
		 */
		public Form withHiddenInputText(@NeverNull final String name, @NeverNull final String value) {
			this.inputs.add(new HiddenInput(name, value));
			return this;
		}

		/**
		 * @param name input name @NeverNull
		 * @param value predefined form value @NeverNull
		 * @param size of text field @NeverNull
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputText(@NeverNull final String name, @NeverNull final String value, final int size) {
			this.inputs.add(new TextInput(name, value, size));
			return this;
		}

		/**
		 * @param name input name @NeverNull
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputFile(@NeverNull final String name) {
			this.inputs.add(new FileInput(name, 100));
			this.hasFileInput = true;
			return this;
		}

		/**
		 * @param name form name @NeverNull
		 * @param value predefined form value @NeverNull
		 * @param cols number of columns @NeverNull
		 * @param rows number of rows @NeverNull
		 * @return the {@link Form} for a fluent API
		 */
		public Form withInputTextArea(@NeverNull final String name, @NeverNull final String value, final int cols,
				final int rows) {
			this.inputs.add(new TextAreaInput(name, value, cols, rows));
			return this;
		}

		/**
		 *
		 * @param value
		 * @NeverNull
		 * @return ...
		 */
		public Form withInputSubmit(@NeverNull final String value) {
			this.inputs.add(new SubmitInput(value));
			return this;
		}

		@Override
		public String toString() {
			final StringBuffer buf = new StringBuffer();
			buf.append("<form action='" + this.action + "' method='" + this.method + "'"
					+ (hasFileInput() ? " enctype='multipart/form-data'" : "") + ">");
			for (final Input input : this.inputs) {
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

		private final String href;
		private @CanBeNull
		final String rel;
		private final String type;

		/**
		 * @param href
		 * @NeverNull
		 * @param rel can be null @CanBeNull
		 * @param type
		 * @NeverNull
		 */
		public HeadLink(@NeverNull final String href, @CanBeNull final String rel, @NeverNull final String type) {
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

		/**
		 *
		 * @param href
		 * @NeverNull
		 */
		public HeadLinkStyle(@NeverNull final String href) {
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

		private final String href;

		/**
		 *
		 * @param href
		 * @NeverNull
		 */
		public ScriptLink(@NeverNull final String href) {
			this.href = href;
		}

		@Override
		public String toString() {
			return "<script src=\"" + this.href + "\"></script>";
		}

	}

	/**
	 * @param name @NeverNull
	 * @param value
	 * @CanBeNull
	 * @return ' ' name '=' '"' value '"' OR the emtpy string if value is null.
	 */
	private static String attribute(@NeverNull final String name, @CanBeNull final String value) {
		assert name != null;
		if (value != null) {
			return " " + name + "=\"" + value + "\"";
		} else {
			return "";
		}
	}

	/**
	 * Utility functions to turn a Map to HTML definition list
	 *
	 * @param mapEntries input, not null. Entries are NOT HTML-encoded or
	 *            escaped in any way. Used valid HTML inside! @NeverNull
	 * @return a string containing the resulting HTML
	 */
	public static String toDefinitionList(@NeverNull final Map<String, ? extends Object> mapEntries) {
		final StringBuffer buf = new StringBuffer();
		buf.append("<dl>\n");
		for (final Entry<String, ? extends Object> e : mapEntries.entrySet()) {
			buf.append("<dt>" + e.getKey() + "</dt><dd>" + e.getValue() + "</dd>\n");
		}
		buf.append("</dl>\n");
		return buf.toString();
	}

	/**
	 * Utility functions to turn a Collection of Strings to HTML ordered list
	 *
	 * @param listEntries input, not null @NeverNull
	 * @return a string containing the resulting HTML
	 */
	public static String toOrderedList(@NeverNull final Collection<String> listEntries) {
		final StringBuffer buf = new StringBuffer();
		buf.append("<ol>\n");
		for (final String li : listEntries) {
			buf.append("<li>" + li + "</li>\n");
		}
		buf.append("</ol>\n");
		return buf.toString();
	}

	/**
	 *
	 * @param listItemContent @NeverNull
	 * @return ...
	 */
	public static String toOrderedList(@NeverNull final String... listItemContent) {
		return toOrderedList(Arrays.asList(listItemContent));
	}

	/**
	 * @param s
	 * @CanBeNull
	 * @return a sanitised form of s that cannot have malicious side effects.
	 *         Result is XHTML compliant. @CanBeNull
	 */
	public static String sanitize(@CanBeNull final String s) {
		if (s == null) {
			return null;
		}
		return htmlEncode(s);
	}

	public static Set<String> sanitize(@CanBeNull final Set<String> unsafe) {
		if (unsafe == null) {
			return null;
		}
		final Set<String> safe = new HashSet<String>();
		for (final String u : unsafe) {
			safe.add(sanitize(u));
		}
		return safe;
	}

	private static final String MALICIOUS_INPUT_SAMPLE = "Dirk<script>alert('test');</script>";

	public static void main(final String[] args) {
		System.out.println(sanitize(MALICIOUS_INPUT_SAMPLE));
	}

	/**
	 * @param raw unencoded string @NeverNull
	 * @return the input string with HTML escaping
	 */
	public static final String htmlEncode(@NeverNull final String raw) {
		String safe = raw;

		safe = safe.replace("&", "&amp;");

		safe = safe.replace("<", "&lt;");
		// unicode equivalent
		safe = safe.replace("\u00AB", "&lt");

		safe = safe.replace(">", "&gt;");
		// unicode equivalent
		safe = safe.replace("\u00BB", "&lt");

		// http://stackoverflow.com/questions/2083754/why-shouldnt-apos-be-used-to-escape-single-quotes
		/* "'" == In X(HT)ML: &quot; In HTML: &#39; For both: do nothing */
		safe = safe.replace("'", "&#39;");

		safe = safe.replace("\"", "&quot;");

		return safe;
	}

}

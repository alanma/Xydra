package org.xydra.restless.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.restless.Restless;


/**
 * A fluent API for {@link HtmlUtils}.
 * 
 * @author xamde
 * 
 */

/*
 * TODO I don't really understand the purpose of this class... does it need to
 * be thread-safe?
 */
public class Page {
	
	private static final ToHtml BR = new HtmlNode("<br />");
	
	public static class Attribute {
		public Attribute(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		String name;
		String value;
		
		public String toHtml() {
			return " " + this.name + "=\"" + xmlEncode(this.value) + "\"";
		}
	}
	
	public static abstract class BlockElement extends ElementWithChildren implements ToHtml {
		
		public BlockElement(String tag, ToHtml parent, boolean first, Attribute ... attributes) {
			super(tag, parent, first, attributes);
		}
		
		public Html endBodyEndHtml() {
			// get up parents until we have the body
			if(this.parent == null) {
				// element is standalone
				return null;
			}
			assert this.parent != null;
			if(this.parent instanceof Body) {
				Body body = (Body)this.parent;
				return (Html)body.parent;
			} else {
				return ((BlockElement)this.parent).endBodyEndHtml();
			}
		}
		
		public Paragraph paragraph(String content) {
			Paragraph p = new Paragraph(this, content);
			this.children.add(p);
			return p;
		}
		
		public Form form(METHOD method, String action) {
			Form form = new Form(this, method, action);
			form.parent = this;
			this.children.add(form);
			return form;
		}
		
		public BlockElement inputSubmit(String label) {
			SubmitInput input = new SubmitInput(this, label);
			input.parent = this;
			this.children.add(input);
			return this;
		}
		
		/**
		 * @param label
		 * @param name form name
		 * @param value current value, can be null
		 * @return this element for fluid API chaining
		 */
		public BlockElement inputText(String label, String name, String value) {
			TextInput input = new TextInput(this, name, value);
			input.parent = this;
			this.children.add(new TextNode(label));
			this.children.add(input);
			this.children.add(BR);
			return this;
		}
		
		public DefinitionList definitionList() {
			DefinitionList dl = new DefinitionList(this);
			dl.parent = this;
			this.children.add(dl);
			return dl;
		}
		
		public UnsortedList unsortedList() {
			UnsortedList ul = new UnsortedList(this);
			ul.parent = this;
			this.children.add(ul);
			return ul;
		}
		
		@Override
		public String toHtml(String indent) {
			return Page.renderToHtml(indent, this.first, RenderMode.Block, this.tag,
			        Page.toHtml("  " + indent, this.children),
			        this.attributes.toArray(new Attribute[0]));
		}
		
	}
	
	public static class Body extends BlockElement {
		
		protected Body(Html html) {
			super("body", html, false);
		}
		
	}
	
	public static enum METHOD {
		GET, POST
	}
	
	public static class Form extends BlockElement {
		
		public Form(ToHtml parent, METHOD method, String action) {
			super("form", parent, false, new Attribute("action", action), new Attribute("method",
			        method.name()));
		}
		
	}
	
	public abstract static class Input extends InlineElement {
		public Input(String tag, ToHtml parent, Attribute ... attributes) {
			super(tag, parent, RenderMode.InlineBlock, attributes);
		}
	}
	
	public static class SubmitInput extends Input {
		public SubmitInput(ToHtml parent, String label) {
			super("input", parent, new Attribute("type", "submit"), new Attribute("value", label));
		}
	}
	
	/**
	 * Rendered as 'name: [ value ]'
	 */
	public static class TextInput extends Input {
		public TextInput(ToHtml parent, String name, String value) {
			super("input", parent, new Attribute("type", "text"), new Attribute("name",
			        urlencode(name)), new Attribute("value", urlencode(value)));
		}
		
	}
	
	// TODO this does not run in GWT; universal function is in xydra.core
	public static String urlencode(String s) {
		try {
			return URLEncoder.encode(s, Restless.JAVA_ENCODING_UTF8);
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	public static class DefinitionList extends BlockElement {
		
		public DefinitionList(ToHtml parent, Attribute ... attributes) {
			super("dl", parent, false, attributes);
		}
		
		public DefinitionList define(String term, String definition) {
			this.children.add(new DefinitionListTermDefinition(term, definition));
			return this;
		}
		
	}
	
	public static class UnsortedList extends BlockElement {
		
		public UnsortedList(ToHtml parent, Attribute ... attributes) {
			super("ul", parent, false, attributes);
		}
		
		public UnsortedList li(String content) {
			Listitem li = new Listitem(this, content);
			this.children.add(li);
			return this;
		}
		
		public Listitem li() {
			Listitem li = new Listitem(this);
			this.children.add(li);
			return li;
			
		}
		
	}
	
	public static class Listitem extends BlockElement {
		public Listitem(ToHtml parent, Attribute ... attributes) {
			super("li", parent, false, attributes);
		}
		
		public Listitem(UnsortedList parent, String content) {
			this(parent);
			this.children.add(new TextNode(content));
		}
	}
	
	public static class DefinitionListTermDefinition implements ToHtml {
		
		protected TextNode dt, dd;
		
		public DefinitionListTermDefinition(String dt, String dd) {
			this.dt = new TextNode(dt);
			this.dd = new TextNode(dd);
		}
		
		@Override
		public String toHtml(String indent) {
			return indent + "<dt>" + this.dt.toHtml("") + "</dt><dd>" + this.dd.toHtml("")
			        + "</dd>\n";
		}
		
	}
	
	public static abstract class ElementWithChildren implements ToHtml {
		protected String tag;
		protected List<ToHtml> children = new ArrayList<ToHtml>();
		protected ToHtml parent = null;
		protected List<Attribute> attributes;
		protected boolean first;
		
		/**
		 * @param tag @NeverNull
		 * @param parent may be null
		 * @param first
		 * @param attributes optional
		 */
		public ElementWithChildren(String tag, ToHtml parent, boolean first,
		        Attribute ... attributes) {
			this.tag = tag;
			this.parent = parent;
			this.first = first;
			this.attributes = Arrays.asList(attributes);
		}
		
		public void addAttribute(String name, String value) {
			this.attributes.add(new Attribute(name, value));
		}
		
	}
	
	public static class Head extends BlockElement {
		
		protected Head(Html html) {
			super("head", html, false);
		}
		
		public Body endHeadStartBody() {
			Html html = (Html)this.parent;
			Body body = new Body(html);
			html.children.add(body);
			return body;
		}
		
		public void title(String title) {
			this.children.add(new Tag("title", title));
		}
		
	}
	
	public static class Html extends BlockElement implements ToHtml {
		
		public Html() {
			super("html", null, true);
		}
		
		public Head head() {
			Head head = new Head(this);
			this.children.add(head);
			return head;
		}
		
		@Override
		public String toString() {
			return this.toHtml("");
		}
		
	}
	
	public abstract static class InlineElement extends ElementWithChildren implements ToHtml {
		private RenderMode renderMode;
		
		public InlineElement(String tag, ToHtml parent, RenderMode renderMode,
		        Attribute ... attributes) {
			super(tag, parent, false, attributes);
			this.renderMode = renderMode;
		}
		
		@Override
		public String toHtml(String indent) {
			return Page.renderToHtml(indent, false, this.renderMode, this.tag,
			        Page.toHtml("", this.children), this.attributes.toArray(new Attribute[0]));
		}
	}
	
	public static class Paragraph extends BlockElement {
		public Paragraph(BlockElement parent, String content) {
			super("p", parent, false);
			this.children.add(new TextNode(content));
		}
	}
	
	public static class Tag implements ToHtml {
		
		private ToHtml content;
		private String tag;
		
		public Tag(String tag, String content) {
			this(tag, new TextNode(content));
		}
		
		public Tag(String tag, ToHtml content) {
			this.tag = tag;
			this.content = content;
		}
		
		@Override
		public String toHtml(String indent) {
			return Page.renderToHtml(indent, false, RenderMode.InlineBlock, this.tag,
			        this.content.toHtml(""));
		}
		
	}
	
	public static class HtmlNode implements ToHtml {
		
		private String content;
		
		/**
		 * @param content will NOT be HTML-escaped
		 */
		public HtmlNode(final String content) {
			this.content = content;
		}
		
		@Override
		public String toHtml(String indent) {
			return this.content;
		}
		
	}
	
	public static class TextNode implements ToHtml {
		
		private String content;
		
		/**
		 * @param content will be HTML-escaped
		 */
		public TextNode(String content) {
			this.content = content;
		}
		
		@Override
		public String toHtml(String indent) {
			return xmlEncode(this.content);
		}
		
	}
	
	public static interface ToHtml {
		public String toHtml(String indent);
	}
	
	public static Head htmlHeadTitle(String title) {
		Html html = new Html();
		Head head = html.head();
		head.title(title);
		return head;
	}
	
	public static String toHtml(String indent, List<ToHtml> children) {
		StringBuffer buf = new StringBuffer();
		for(ToHtml child : children) {
			buf.append(child.toHtml(indent));
		}
		return buf.toString();
	}
	
	private static enum RenderMode {
		
		/** No whitespace added */
		Inline,
		
		/** Starts on a new line */
		InlineBlock,
		
		/** Starts on a new line */
		Block
	}
	
	/**
	 * @param indent
	 * @param first no newline at start
	 * @param compact no whitespace added
	 * @param block generous newlines
	 * @param tag
	 * @param content
	 * @param attributes
	 * @return
	 */
	private static String renderToHtml(String indent, boolean first, RenderMode renderMode,
	        String tag, String content, Attribute ... attributes) {
		StringBuffer buf = new StringBuffer();
		
		if(!first && renderMode != RenderMode.Inline) {
			buf.append("\n");
		}
		if(renderMode == RenderMode.Block || renderMode == RenderMode.InlineBlock) {
			buf.append(indent);
		}
		
		buf.append("<").append(tag).append(toHtmlAttributes(attributes));
		
		if(content == null || content.equals("")) {
			buf.append(" />");
		} else {
			buf.append(">");
			buf.append(content);
			if(renderMode == RenderMode.Block) {
				buf.append("\n");
				buf.append(indent);
			}
			buf.append("</").append(tag).append(">");
		}
		return buf.toString();
	}
	
	private static String toHtmlAttributes(Attribute ... attributes) {
		StringBuffer buf = new StringBuffer();
		for(Attribute attribute : attributes) {
			if(attribute.value != null)
				buf.append(attribute.toHtml());
		}
		return buf.toString();
	}
	
	public static final String xmlEncode(String raw) {
		String safe = raw.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quot;");
		return safe;
	}
	
	public static void main(String[] args) {
		String s2 = Page.htmlHeadTitle("Hello World").endHeadStartBody().paragraph("Foo")
		        .form(METHOD.GET, "/my/url").inputText("Name: ", "name", "John Doe")
		        .inputSubmit("Abschicken").endBodyEndHtml().toString();
		System.out.println(s2);
		
		Form form = new Form(null, METHOD.GET, "/my/url");
		String s3 = form.inputText("Name: ", "name", "John Doe").inputSubmit("Abschicken")
		        .toHtml("");
		System.out.println(s3);
	}
}

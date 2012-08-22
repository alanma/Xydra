package org.xydra.restless.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.NotThreadSafe;
import org.xydra.restless.Restless;


/**
 * A fluent API for {@link HtmlUtils}.
 * 
 * @author xamde
 * 
 */

@NotThreadSafe
public class Page {
	
	private static final ToHtml BR = new HtmlNode("<br />");
	
	public static class Attribute {
		/**
		 * 
		 * @param name @CanBeNull
		 * @param value @CanBeNull
		 */
		public Attribute(@CanBeNull String name, @CanBeNull String value) {
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
		
		/**
		 * 
		 * @param tag @NeverNull
		 * @param parent @CanBeNull
		 * @param first @NeverNull
		 * @param attributes @CanBeNull
		 */
		public BlockElement(@NeverNull String tag, @CanBeNull ToHtml parent,
		        @NeverNull boolean first, @CanBeNull Attribute ... attributes) {
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
		
		/**
		 * @param content @NeverNull
		 */
		public Paragraph paragraph(@NeverNull String content) {
			Paragraph p = new Paragraph(this, content);
			this.children.add(p);
			return p;
		}
		
		/**
		 * 
		 * @param method @NeverNull
		 * @param action @CanBeNull
		 */
		public Form form(@NeverNull METHOD method, @CanBeNull String action) {
			Form form = new Form(this, method, action);
			form.parent = this;
			this.children.add(form);
			return form;
		}
		
		/**
		 * 
		 * @param label @CanBeNull
		 */
		public BlockElement inputSubmit(@CanBeNull String label) {
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
		
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param method @NeverNull
		 * @param action @CanBeNull
		 */
		public Form(@CanBeNull ToHtml parent, @NeverNull METHOD method, String action) {
			super("form", parent, false, new Attribute("action", action), new Attribute("method",
			        method.name()));
		}
		
	}
	
	public abstract static class Input extends InlineElement {
		
		/**
		 * @param tag @NeverNull
		 * @param parent @CanBeNull
		 * @param attributes @CanBeNull
		 */
		public Input(@NeverNull String tag, @CanBeNull ToHtml parent,
		        @CanBeNull Attribute ... attributes) {
			super(tag, parent, RenderMode.InlineBlock, attributes);
		}
	}
	
	public static class SubmitInput extends Input {
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param label @CanBeNull
		 */
		public SubmitInput(@CanBeNull ToHtml parent, @CanBeNull String label) {
			super("input", parent, new Attribute("type", "submit"), new Attribute("value", label));
		}
	}
	
	/**
	 * Rendered as 'name: [ value ]'
	 */
	public static class TextInput extends Input {
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param name @NeverNull
		 * @param value @NeverNull
		 */
		public TextInput(@CanBeNull ToHtml parent, @NeverNull String name, @NeverNull String value) {
			super("input", parent, new Attribute("type", "text"), new Attribute("name",
			        urlencode(name)), new Attribute("value", urlencode(value)));
		}
		
	}
	
	// TODO this does not run in GWT; universal function is in xydra.core
	/**
	 * @param s @NeverNull
	 */
	public static String urlencode(@NeverNull String s) {
		try {
			return URLEncoder.encode(s, Restless.JAVA_ENCODING_UTF8);
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	public static class DefinitionList extends BlockElement {
		
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param attributes @CanBeNull
		 */
		public DefinitionList(@CanBeNull ToHtml parent, @CanBeNull Attribute ... attributes) {
			super("dl", parent, false, attributes);
		}
		
		/**
		 * 
		 * @param term @NeverNull
		 * @param definition @NeverNull
		 */
		public DefinitionList define(@NeverNull String term, @NeverNull String definition) {
			this.children.add(new DefinitionListTermDefinition(term, definition));
			return this;
		}
		
	}
	
	public static class UnsortedList extends BlockElement {
		
		/**
		 * @param parent @CanBeNull
		 * @param attributes @CanBeNull
		 */
		public UnsortedList(@CanBeNull ToHtml parent, @CanBeNull Attribute ... attributes) {
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
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param attributes @CanBeNull
		 */
		public Listitem(@CanBeNull ToHtml parent, @CanBeNull Attribute ... attributes) {
			super("li", parent, false, attributes);
		}
		
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param content @NeverNull
		 */
		public Listitem(@CanBeNull UnsortedList parent, @NeverNull String content) {
			this(parent);
			this.children.add(new TextNode(content));
		}
	}
	
	public static class DefinitionListTermDefinition implements ToHtml {
		
		protected TextNode dt, dd;
		
		/**
		 * 
		 * @param dt @NeverNull
		 * @param dd @NeverNull
		 */
		public DefinitionListTermDefinition(@NeverNull String dt, @NeverNull String dd) {
			this.dt = new TextNode(dt);
			this.dd = new TextNode(dd);
		}
		
		/**
		 * @param indent @CanBeNull
		 */
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
		 * @param parent may be null @CanBeNull
		 * @param first @NeverNull
		 * @param attributes optional @CanBeNull
		 */
		public ElementWithChildren(@NeverNull String tag, @CanBeNull ToHtml parent,
		        @NeverNull boolean first, @CanBeNull Attribute ... attributes) {
			this.tag = tag;
			this.parent = parent;
			this.first = first;
			if(attributes != null) {
				this.attributes = Arrays.asList(attributes);
			} else {
				/*
				 * create an empty array and instantiate the list with it by
				 * calling Arrays.asList so that the behavior/the class of
				 * this.attributes is the same as when the given attributes
				 * weren't null.
				 */
				
				Attribute[] emptyArray = new Attribute[0];
				this.attributes = Arrays.asList(emptyArray);
			}
		}
		
		/**
		 * 
		 * @param name @CanBeNull
		 * @param value @CanBeNull
		 */
		public void addAttribute(@CanBeNull String name, @CanBeNull String value) {
			this.attributes.add(new Attribute(name, value));
		}
		
	}
	
	public static class Head extends BlockElement {
		
		/**
		 * 
		 * @param html @CanBeNull
		 */
		protected Head(@CanBeNull Html html) {
			super("head", html, false);
		}
		
		public Body endHeadStartBody() {
			Html html = (Html)this.parent;
			Body body = new Body(html);
			html.children.add(body);
			return body;
		}
		
		/**
		 * 
		 * @param title @NeverNull
		 */
		public void title(@NeverNull String title) {
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
		
		/**
		 * 
		 * @param tag @NeverNull
		 * @param parent @CanBeNull
		 * @param renderMode @NeverNull
		 * @param attributes @CanBeNull
		 */
		public InlineElement(@NeverNull String tag, @CanBeNull ToHtml parent,
		        @NeverNull RenderMode renderMode, @CanBeNull Attribute ... attributes) {
			super(tag, parent, false, attributes);
			this.renderMode = renderMode;
		}
		
		/**
		 * @param indent @CanBeNull
		 */
		@Override
		public String toHtml(@CanBeNull String indent) {
			return Page.renderToHtml(indent, false, this.renderMode, this.tag,
			        Page.toHtml("", this.children), this.attributes.toArray(new Attribute[0]));
		}
	}
	
	public static class Paragraph extends BlockElement {
		/**
		 * 
		 * @param parent @CanBeNull
		 * @param content @NeverNull
		 */
		public Paragraph(@CanBeNull BlockElement parent, @NeverNull String content) {
			super("p", parent, false);
			this.children.add(new TextNode(content));
		}
	}
	
	public static class Tag implements ToHtml {
		
		private ToHtml content;
		private String tag;
		
		/**
		 * 
		 * @param tag @NeverNull
		 * @param content @NeverNull
		 */
		public Tag(@NeverNull String tag, @NeverNull String content) {
			this(tag, new TextNode(content));
		}
		
		/**
		 * 
		 * @param tag @NeverNull
		 * @param content @NeverNull
		 */
		public Tag(String tag, ToHtml content) {
			this.tag = tag;
			this.content = content;
		}
		
		/**
		 * @param indent @CanBeNull
		 */
		@Override
		public String toHtml(String indent) {
			return Page.renderToHtml(indent, false, RenderMode.InlineBlock, this.tag,
			        this.content.toHtml(""));
		}
		
	}
	
	public static class HtmlNode implements ToHtml {
		
		private String content;
		
		/**
		 * @param content will NOT be HTML-escaped @CanBeNull
		 */
		public HtmlNode(@CanBeNull final String content) {
			this.content = content;
		}
		
		/**
		 * @param indent @CanBeNull
		 */
		@Override
		public String toHtml(@CanBeNull String indent) {
			return this.content;
		}
		
	}
	
	public static class TextNode implements ToHtml {
		
		private String content;
		
		/**
		 * @param content will be HTML-escaped @NeverNull
		 */
		public TextNode(@NeverNull String content) {
			this.content = content;
		}
		
		/**
		 * @param indent @CanBeNull
		 */
		@Override
		public String toHtml(@CanBeNull String indent) {
			return xmlEncode(this.content);
		}
		
	}
	
	public static interface ToHtml {
		/**
		 * 
		 * @param indent @CanBeNull
		 * @return @CanBeNull
		 */
		public String toHtml(@CanBeNull String indent);
	}
	
	/**
	 * 
	 * @param title @NeverNull
	 * 
	 */
	public static Head htmlHeadTitle(@NeverNull String title) {
		Html html = new Html();
		Head head = html.head();
		head.title(title);
		return head;
	}
	
	/**
	 * 
	 * @param indent @CanBeNull
	 * @param children @CanBeNull
	 */
	public static String toHtml(@CanBeNull String indent, @CanBeNull List<ToHtml> children) {
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
	 * @param indent @CanBeNull
	 * @param first no newline at start @NeverNull
	 * @param renderMode @NeverNull
	 * @param tag @NeverNull
	 * @param content @CanBeNull
	 * @param attributes @CanBeNull
	 * @return
	 */
	private static String renderToHtml(@CanBeNull String indent, @NeverNull boolean first,
	        @NeverNull RenderMode renderMode, @NeverNull String tag, @CanBeNull String content,
	        @CanBeNull Attribute ... attributes) {
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
	
	/**
	 * 
	 * @param attributes @CanBeNull
	 */
	private static String toHtmlAttributes(@CanBeNull Attribute ... attributes) {
		StringBuffer buf = new StringBuffer();
		for(Attribute attribute : attributes) {
			if(attribute.value != null)
				buf.append(attribute.toHtml());
		}
		return buf.toString();
	}
	
	/**
	 * 
	 * @param raw @NeverNull
	 * 
	 */
	public static final String xmlEncode(@NeverNull String raw) {
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

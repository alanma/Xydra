package org.xydra.restless.utils;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


/**
 * A fluent API for {@link HtmlUtils}.
 * 
 * @author xamde
 * 
 */
public class Page {
	
	public static abstract class BlockElement extends ElementWithChildren {
		
		private BlockElement parent = null;
		
		public Html endBodyEndHtml() {
			// get up parents until we have the body
			assert this.parent != null;
			if(this.parent instanceof Body) {
				return ((Body)this.parent).html;
			} else {
				return this.parent.endBodyEndHtml();
			}
		}
		
		public Paragraph paragraph(String content) {
			Paragraph p = new Paragraph(this, content);
			this.children.add(p);
			return p;
		}
		
		public String toHtml(String indent, String tag) {
			return Page.toHtmlBlock(indent, tag, Page.toHtml("  " + indent, this.children));
		}
		
	}
	
	public static class Body extends BlockElement {
		
		private Html html;
		
		protected Body(Html html) {
			this.html = html;
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return super.toHtml(indent, "body");
		}
		
	}
	
	public class DefinitionList extends BlockElement {
		
		public DefinitionList define(String term, String definition) {
			this.children.add(new DefinitionListTermDefinition(term, definition));
			return this;
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return super.toHtml(indent, "dl");
		}
		
	}
	
	public static class DefinitionListTermDefinition implements ToHtmlBlock {
		
		protected TextNode dt, dd;
		
		public DefinitionListTermDefinition(String dt, String dd) {
			this.dt = new TextNode(dt);
			this.dd = new TextNode(dd);
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return indent + "<dt>" + this.dt.toHtmlInline() + "</dt><dd>" + this.dd.toHtmlInline()
			        + "</dd>\n";
		}
		
	}
	
	public static abstract class ElementWithChildren implements ToHtmlBlock {
		protected List<ToHtml> children = new ArrayList<Page.ToHtml>();
	}
	
	public static class Head extends ElementWithChildren {
		
		private Html html;
		
		protected Head(Html html) {
			this.html = html;
		}
		
		public Body endHeadStartBody() {
			Body body = new Body(this.html);
			this.html.body = body;
			return body;
		}
		
		public void title(String title) {
			this.children.add(new Tag("title", title));
		}
		
		public String toHtmlBlock(String indent) {
			return Page.toHtmlBlock(indent, "head", Page.toHtml("  " + indent, this.children));
		}
		
	}
	
	public static class Html implements ToHtmlBlock {
		
		private Body body;
		private Head head;
		
		public Head head() {
			Head head = new Head(this);
			this.head = head;
			return head;
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return Page.toHtmlBlock(indent, "html", this.head.toHtmlBlock("  " + indent)
			        + this.body.toHtmlBlock("  " + indent));
		}
		
	}
	
	public abstract static class InlineElement implements ToHtmlInline {
		
	}
	
	public static class Paragraph extends BlockElement {
		
		public Paragraph(BlockElement parent, String content) {
			super.parent = parent;
			this.children.add(new TextNode(content));
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return Page.toHtmlInline(indent, "p", Page.toHtml(indent, this.children));
		}
		
	}
	
	public static class Tag implements ToHtmlBlock {
		
		private ToHtmlInline content;
		private String tag;
		
		public Tag(String tag, String content) {
			this(tag, new TextNode(content));
		}
		
		public Tag(String tag, ToHtmlInline content) {
			this.tag = tag;
			this.content = content;
		}
		
		@Override
		public String toHtmlBlock(String indent) {
			return Page.toHtmlInline(indent, this.tag, this.content.toHtmlInline());
		}
		
	}
	
	public static class TextNode implements ToHtmlInline {
		
		private String content;
		
		/**
		 * @param content will be HTML-escaped
		 */
		public TextNode(String content) {
			this.content = content;
		}
		
		@Override
		public String toHtmlInline() {
			return xmlEncode(this.content);
		}
		
	}
	
	/**
	 * Marker interface
	 */
	public static interface ToHtml {
	}
	
	public static interface ToHtmlBlock extends ToHtml {
		public String toHtmlBlock(String indent);
	}
	
	public static interface ToHtmlInline extends ToHtml {
		public String toHtmlInline();
	}
	
	public static Head htmlHead(Writer w) {
		Html html = new Html();
		return html.head();
	}
	
	public static Head htmlHeadTitle(String title) {
		Html html = new Html();
		Head head = html.head();
		head.title(title);
		return head;
	}
	
	public static void main(String[] args) {
		String s2 = Page.htmlHeadTitle("Hello World").endHeadStartBody().paragraph("Foo")
		        .endBodyEndHtml().toHtmlBlock("");
		
		System.out.println(s2);
	}
	
	public static String toHtml(String indent, List<ToHtml> children) {
		StringBuffer buf = new StringBuffer();
		for(ToHtml toHtml : children) {
			
			if(toHtml instanceof ToHtmlBlock) {
				buf.append(((ToHtmlBlock)toHtml).toHtmlBlock(indent));
			} else {
				assert toHtml instanceof ToHtmlInline;
				buf.append(((ToHtmlInline)toHtml).toHtmlInline());
			}
		}
		return buf.toString();
	}
	
	private static String toHtmlBlock(String indent, String tag, String content) {
		return indent + "<" + tag + ">\n"

		+ content

		+ indent + "</" + tag + ">\n";
	}
	
	private static String toHtmlInline(String indent, String tag, String content) {
		return indent + "<" + tag + ">" + content + "</" + tag + ">\n";
	}
	
	public static final String xmlEncode(String raw) {
		String safe = raw.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quot;");
		return safe;
	}
}

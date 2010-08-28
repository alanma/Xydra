package org.xydra.core.xml.impl;

import java.util.Iterator;
import java.util.Stack;

import org.xydra.core.xml.XmlOut;


/**
 * Abstract {@link XmlOut} implementation.
 * 
 */
abstract public class AbstractXmlOut implements XmlOut {
	
	private boolean firstLine = true;
	
	private boolean inElement = false;
	
	private boolean inProcessingInstruction = false;
	
	private final Stack<String> openThings = new Stack<String>();
	
	protected void init() {
		append(XmlOut.XML_DECLARATION);
	}
	
	abstract protected void append(String str);
	
	public String getOpentags() {
		String s = "";
		Iterator<String> it = this.openThings.iterator();
		while(it.hasNext()) {
			s += "/" + it.next();
		}
		return s;
	}
	
	public void open(String elementName) {
		this.openThings.push(elementName);
		if(this.inElement) {
			append(">");
			this.inElement = false;
		}
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			append("\n");
		}
		append("<" + elementName);
		this.inElement = true;
	}
	
	public void attribute(String name, String value) {
		
		if(!(this.inElement | this.inProcessingInstruction))
			throw new IllegalStateException("Atributes are only allowed in elements. We are here "
			        + getOpentags() + " so cannot add " + name + "=" + value);
		
		append(" ");
		append(name);
		append("=\"");
		append(XmlEncoder.xmlencode(value));
		append("\"");
	}
	
	public void content(String rawContent) {
		if(this.inElement) {
			append(">");
			this.inElement = false;
		}
		append(XmlEncoder.xmlencode(rawContent));
	}
	
	public void close(String elementName) {
		String open = this.openThings.pop();
		assert open.equals(elementName) : "trying to close '" + elementName
		        + "' but last opened was '" + open + "'";
		
		if(this.inElement) {
			append("/>");
			this.inElement = false;
		} else {
			append("</" + elementName + ">");
		}
	}
	
	public void comment(String comment) {
		if(this.inElement) {
			append(">");
			this.inElement = false;
		}
		append("\n<!-- " + comment + " -->");
	}
	
	public void write(String s) {
		append(s);
	}
	
	public void openProcessingInstruction(String processingInstruction) {
		this.inProcessingInstruction = true;
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			append("\n");
		}
		append("<?" + processingInstruction);
	}
	
	public void closeProcessingInstruction() {
		this.inProcessingInstruction = false;
		append("?>");
	}
	
	public void doctype(String doctype, String publicID, String url) {
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			append("\n");
		}
		append("<!DOCTYPE " + doctype + " PUBLIC " + publicID + " " + url + ">");
	}
	
}

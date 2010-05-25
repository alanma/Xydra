package org.xydra.core.xml.impl;

import java.util.Iterator;
import java.util.Stack;

import org.xydra.core.xml.XmlOut;



/**
 * Maps {@link XmlOut} to a {@link StringBuffer}.
 * 
 * @author voelkel
 * 
 */
public class XmlOutStringBuffer implements XmlOut {
	
	private StringBuffer buf;
	
	private boolean firstLine = true;
	
	private boolean inElement = false;
	
	private boolean inProcessingInstruction = false;
	
	private Stack<String> openThings = new Stack<String>();
	
	public XmlOutStringBuffer() {
		this.buf = new StringBuffer();
		this.buf.append(XmlOut.XML_DECLARATION);
	}
	
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
			this.buf.append(">");
			this.inElement = false;
		}
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			this.buf.append("\n");
		}
		this.buf.append("<" + elementName);
		this.inElement = true;
	}
	
	public void attribute(String name, String value) {
		
		if(!(this.inElement | this.inProcessingInstruction))
			throw new IllegalStateException("Atributes are only allowed in elements. We are here "
			        + getOpentags() + " so cannot add " + name + "=" + value);
		
		this.buf.append(" ");
		this.buf.append(name);
		this.buf.append("=\"");
		this.buf.append(XmlEncoder.xmlencode(value));
		this.buf.append("\"");
	}
	
	public void content(String rawContent) {
		if(this.inElement) {
			this.buf.append(">");
			this.inElement = false;
		}
		this.buf.append(XmlEncoder.xmlencode(rawContent));
	}
	
	public void close(String elementName) {
		String open = this.openThings.pop();
		assert open.equals(elementName) : "trying to close '" + elementName
		        + "' but last opened was '" + open + "'";
		
		if(this.inElement) {
			this.buf.append(">");
			this.inElement = false;
		}
		this.buf.append("</" + elementName + ">");
	}
	
	public void comment(String comment) {
		if(this.inElement) {
			this.buf.append(">");
			this.inElement = false;
		}
		this.buf.append("\n<!-- " + comment + " -->");
	}
	
	public void write(String s) {
		this.buf.append(s);
	}
	
	public void openProcessingInstruction(String processingInstruction) {
		this.inProcessingInstruction = true;
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			this.buf.append("\n");
		}
		this.buf.append("<?" + processingInstruction);
	}
	
	public void closeProcessingInstruction() {
		this.inProcessingInstruction = false;
		this.buf.append("?>");
	}
	
	public void doctype(String doctype, String publicID, String url) {
		if(this.firstLine) {
			this.firstLine = false;
		} else {
			this.buf.append("\n");
		}
		this.buf.append("<!DOCTYPE " + doctype + " PUBLIC " + publicID + " " + url + ">");
	}
	
	public void flush() {
		// only needed for stream implementations
	}
	
	public void close() {
		// only needed for stream implementations
	}
	
	public String getXml() {
		return this.buf.toString();
	}
	
}

package org.xydra.core.xml.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;


public class XydraOutXml extends AbstractXydraOut {
	
	public XydraOutXml(OutputStream os) {
		this(os, true);
	}
	
	public XydraOutXml(Writer writer) {
		this(writer, true);
	}
	
	public XydraOutXml() {
		this(true);
	}
	
	public XydraOutXml(OutputStream os, boolean writeHeader) {
		super(os);
		init(writeHeader);
	}
	
	public XydraOutXml(Writer writer, boolean writeHeader) {
		super(writer);
		init(writeHeader);
	}
	
	public XydraOutXml(boolean writeHeader) {
		super();
		init(writeHeader);
	}
	
	private void init(boolean writeHeader) {
		
		if(!writeHeader) {
			return;
		}
		
		try {
			this.writer.write(XmlEncoder.XML_DECLARATION);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	protected void outputAttribute(Frame element, String name, String value) throws IOException {
		
		this.writer.write(" ");
		this.writer.write(name);
		this.writer.write("=\"");
		this.writer.write(XmlEncoder.encode(value));
		this.writer.write("\"");
		
	}
	
	@Override
	void outputBeginChildren(Frame element, Frame children) {
		children.depth = element.depth;
	}
	
	@Override
	void outputCloseElement(Frame container, Frame element) throws IOException {
		
		if(element.hasContent()) {
			
			if(element.getContentType() != Type.Text) {
				this.writer.write('\n');
				indent(element.depth);
			}
			this.writer.write("</");
			this.writer.write(element.name);
			this.writer.write('>');
			
		} else {
			this.writer.write(" />");
		}
		
		this.writer.write('\n');
		
	}
	
	@Override
	void outputContent(Frame element, Frame content, String data) throws IOException {
		this.writer.write(XmlEncoder.encode(data));
	}
	
	@Override
	void outputEndChildren(Frame element, Frame children) {
		// nothing to do here
	}
	
	@Override
	void outputOpenElement(Frame container, Frame element) throws IOException {
		
		element.depth = container.depth + 1;
		
		if(!container.hasContent()) {
			this.writer.write('\n');
		}
		
		indent(element.depth);
		this.writer.write('<');
		this.writer.write(element.name);
		
	}
	
	@Override
	void outputElementBeginContent(Frame element) throws IOException {
		this.writer.write(">");
	}
	
	@Override
	void outputBeginChild(Frame element, Frame child) {
		child.depth = element.depth;
	}
	
	@Override
	void outputValue(Frame container, String type, String value) throws IOException {
		
		if(!container.hasContent()) {
			this.writer.write('\n');
		}
		
		indent(container.depth + 1);
		
		if(value == null) {
			this.writer.write('<');
			this.writer.write(type);
			this.writer.write(" isNull=\"true\" />");
		} else {
			
			this.writer.write('<');
			this.writer.write(type);
			this.writer.write('>');
			this.writer.write(XmlEncoder.encode(value));
			this.writer.write("</");
			this.writer.write(type);
			this.writer.write('>');
			
		}
		
	}
}

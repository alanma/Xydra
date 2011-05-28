package org.xydra.core.xml.impl;

import org.xydra.minio.MiniWriter;


public class XydraOutXml extends AbstractXydraOut {
	
	public XydraOutXml(MiniWriter writer) {
		this(writer, true);
	}
	
	public XydraOutXml() {
		this(true);
	}
	
	public XydraOutXml(MiniWriter writer, boolean writeHeader) {
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
		
		this.writer.write(XmlEncoder.XML_DECLARATION);
	}
	
	@Override
	protected <T> void outputAttribute(Frame element, String name, T value) {
		
		this.writer.write(" ");
		this.writer.write(name);
		this.writer.write("=\"");
		this.writer.write(XmlEncoder.encode(value.toString()));
		this.writer.write("\"");
		
	}
	
	@Override
	protected void outputBeginChildren(Frame element, Frame children) {
		children.depth = element.depth;
	}
	
	@Override
	protected void outputCloseElement(Frame container, Frame element) {
		
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
	protected <T> void outputContent(Frame element, Frame content, T data) {
		this.writer.write(XmlEncoder.encode(data.toString()));
	}
	
	@Override
	protected void outputEndChildren(Frame element, Frame children) {
		// nothing to do here
	}
	
	@Override
	protected void outputOpenElement(Frame container, Frame element) {
		
		element.depth = container.depth + 1;
		
		if(!container.hasContent()) {
			this.writer.write('\n');
		}
		
		indent(element.depth);
		this.writer.write('<');
		this.writer.write(element.name);
		
	}
	
	@Override
	protected void outputElementBeginContent(Frame element) {
		this.writer.write(">");
	}
	
	@Override
	protected void outputBeginChild(Frame element, Frame child) {
		child.depth = element.depth;
	}
	
	@Override
	protected <T> void outputValue(Frame container, String type, T value) {
		
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
			String valueStr = value.toString();
			if(valueStr.isEmpty()) {
				this.writer.write(" />");
			} else {
				this.writer.write('>');
				this.writer.write(XmlEncoder.encode(valueStr));
				this.writer.write("</");
				this.writer.write(type);
				this.writer.write('>');
			}
			
		}
		
	}
	
}

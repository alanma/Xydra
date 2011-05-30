package org.xydra.core.serialize.xml;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.AbstractXydraOut;
import org.xydra.minio.MiniWriter;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlOut extends AbstractXydraOut {
	
	public XmlOut(MiniWriter writer) {
		this(writer, true);
	}
	
	public XmlOut() {
		this(true);
	}
	
	public XmlOut(MiniWriter writer, boolean writeHeader) {
		super(writer);
		init(writeHeader);
	}
	
	public XmlOut(boolean writeHeader) {
		super();
		init(writeHeader);
	}
	
	private void init(boolean writeHeader) {
		
		if(!writeHeader) {
			return;
		}
		
		this.writer.write(XmlEncoder.XML_DECLARATION);
		this.writer.write('\n');
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
		if(!element.hasContent()) {
			this.writer.write(">\n");
		}
		children.depth = element.depth;
	}
	
	@Override
	protected void outputCloseElement(Frame container, Frame element) {
		
		if(element.hasContent()) {
			if(element.getContentType() != Type.Text) {
				indent(element.depth);
				this.writer.write("</");
				this.writer.write(element.name);
				this.writer.write(">\n");
			}
		} else {
			this.writer.write("/>\n");
		}
		
	}
	
	@Override
	protected <T> void outputContent(Frame element, Frame content, T data) {
		if(data == null) {
			this.writer.write(" nullContent=\"true\"/>\n");
		} else {
			String dataStr = data.toString();
			if(dataStr.isEmpty()) {
				this.writer.write("/>\n");
			} else {
				this.writer.write('>');
				this.writer.write(XmlEncoder.encode(data.toString()));
				this.writer.write("</");
				this.writer.write(element.name);
				this.writer.write(">\n");
			}
		}
	}
	
	@Override
	protected void outputEndChildren(Frame element, Frame children) {
		// nothing to do here
	}
	
	@Override
	protected void outputOpenElement(Frame container, Frame element) {
		
		beginChild(container);
		
		element.depth = container.depth + 1;
		
		this.writer.write('<');
		this.writer.write(element.name);
		
	}
	
	@Override
	protected void outputBeginChild(Frame element, Frame child) {
		outputBeginChildren(element, child);
	}
	
	private void beginChild(Frame container) {
		
		if((container.type == Type.Element && !container.hasContent())
		        || (container.type == Type.Array && !container.hasContent() && container
		                .getAttrCount() == 0)) {
			this.writer.write(">\n");
		}
		
		indent(container.depth + 1);
		
	}
	
	@Override
	protected void outputNullElement(Frame container) {
		
		beginChild(container);
		
		if(container.element == null) {
			this.writer.write("<xnull/>\n");
		} else {
			this.writer.write('<');
			this.writer.write(container.element);
			this.writer.write(" isNull=\"true\"/>\n");
		}
		
	}
	
	@Override
	protected <T> void outputValue(Frame container, T value) {
		
		if(value == null) {
			outputNullElement(container);
		} else {
			
			beginChild(container);
			
			String type = container.element == null ? "xvalue" : container.element;
			
			this.writer.write('<');
			this.writer.write(type);
			String valueStr = value.toString();
			if(valueStr.isEmpty()) {
				this.writer.write("/>\n");
			} else {
				this.writer.write('>');
				this.writer.write(XmlEncoder.encode(valueStr));
				this.writer.write("</");
				this.writer.write(type);
				this.writer.write(">\n");
			}
			
		}
		
	}
	
	@Override
	protected void outputBeginArray(Frame container, Frame array) {
		
		array.depth = container.depth + 1;
		indent(array.depth);
		
		if(container.element == null) {
			this.writer.write("<xarray");
		} else {
			this.writer.write('<');
			this.writer.write(container.element);
		}
		
	}
	
	@Override
	protected void outputEndArray(Frame container, Frame array) {
		
		if(array.hasContent() || array.getAttrCount() > 0) {
			this.writer.write("/>\n");
		} else {
			indent(array.depth);
			if(container.element == null) {
				this.writer.write("</xarray>\n");
			} else {
				this.writer.write("</");
				this.writer.write(container.element);
				this.writer.write(">\n");
			}
		}
		
	}
	
}

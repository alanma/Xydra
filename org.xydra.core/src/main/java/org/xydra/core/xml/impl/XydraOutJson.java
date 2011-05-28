package org.xydra.core.xml.impl;

import org.xydra.minio.MiniWriter;


public class XydraOutJson extends AbstractXydraOut {
	
	public XydraOutJson(MiniWriter writer) {
		super(writer);
	}
	
	public XydraOutJson() {
		super();
	}
	
	private void outputName(Frame frame, String name) {
		
		this.writer.write(",\n");
		indent(frame.depth + 1);
		this.writer.write('"');
		this.writer.write(name);
		this.writer.write("\": ");
		
	}
	
	private <T> void output(T value) {
		
		if(value instanceof Boolean || value instanceof Float || value instanceof Integer
		        || value instanceof Double) {
			this.writer.write(value.toString());
		} else if(value instanceof Long && (Long)value >= Integer.MIN_VALUE
		        && (Long)value <= Integer.MAX_VALUE) {
			this.writer.write(value.toString());
		} else {
			this.writer.write('"');
			this.writer.write(JsonEncoder.encode(value.toString()));
			this.writer.write('"');
		}
		
	}
	
	@Override
	protected <T> void outputAttribute(Frame element, String name, T value) {
		
		outputName(element, name);
		
		output(value);
		
	}
	
	@Override
	protected void outputBeginChildren(Frame element, Frame children) {
		
		outputName(element, children.name);
		this.writer.write('[');
		
		children.depth = element.depth + 1;
		
	}
	
	@Override
	protected void outputCloseElement(Frame container, Frame element) {
		
		if(element.hasContent() || element.hasAttributes()) {
			this.writer.write("\n");
			indent(element.depth);
		} else {
			this.writer.write(' ');
		}
		this.writer.write('}');
		
	}
	
	@Override
	protected <T> void outputContent(Frame element, Frame content, T data) {
		outputAttribute(element, content.name, data);
	}
	
	@Override
	protected void outputEndChildren(Frame element, Frame children) {
		
		if(children.hasContent()) {
			this.writer.write("\n");
			indent(children.depth);
		} else {
			this.writer.write(' ');
		}
		this.writer.write(']');
		
	}
	
	@Override
	protected void outputOpenElement(Frame container, Frame element) {
		
		element.depth = container.depth + 1;
		
		if(container.type == Type.Root || container.type == Type.Children) {
			
			if(container.hasContent()) {
				// This is not the first child element
				this.writer.write(", ");
			}
			
			this.writer.write("{ \"$type\": \"");
			this.writer.write(element.name);
			this.writer.write('"');
			
		} else if(container.type == Type.Child) {
			
			outputName(container, container.name);
			this.writer.write("{ \"$type\": \"");
			this.writer.write(element.name);
			this.writer.write('"');
			
		} else {
			
			assert container.type == Type.Element;
			
			outputName(container, element.name);
			this.writer.write('{');
			
		}
		
	}
	
	@Override
	protected <T> void outputValue(Frame container, String type, T value) {
		
		if(container.hasContent() || container.hasAttributes()) {
			// This is not the first child element
			this.writer.write(',');
		}
		this.writer.write(' ');
		
		if(value == null) {
			this.writer.write("null");
		} else {
			output(value);
		}
		
	}
	
	@Override
	protected void outputElementBeginContent(Frame element) {
		// nothing to do here
	}
	
	@Override
	protected void end() {
		this.writer.write('\n');
	}
	
	@Override
	protected void outputBeginChild(Frame element, Frame child) {
		child.depth = element.depth;
	}
	
}

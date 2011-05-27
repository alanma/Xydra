package org.xydra.core.xml.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;


public class XydraOutJson extends AbstractXydraOut {
	
	public XydraOutJson(OutputStream os) {
		super(os);
	}
	
	public XydraOutJson(Writer writer) {
		super(writer);
	}
	
	public XydraOutJson() {
		super();
	}
	
	private void outputName(Frame frame, String name) throws IOException {
		
		this.writer.write(",\n");
		indent(frame.depth + 1);
		this.writer.write('"');
		this.writer.write(name);
		this.writer.write("\": ");
		
	}
	
	private void value(Frame frame, String name, String value) throws IOException {
		
		outputName(frame, name);
		
		// IMPROVE: don't escape numbers
		this.writer.write('"');
		this.writer.write(JsonEncoder.encode(value));
		this.writer.write('"');
		
	}
	
	@Override
	protected void outputAttribute(Frame element, String name, String value) throws IOException {
		value(element, name, value);
	}
	
	@Override
	void outputBeginChildren(Frame element, Frame children) throws IOException {
		
		outputName(element, children.name);
		this.writer.write('[');
		
		children.depth = element.depth + 1;
		
	}
	
	@Override
	void outputCloseElement(Frame container, Frame element) throws IOException {
		
		if(element.hasContent() || element.hasAttributes()) {
			this.writer.write("\n");
			indent(element.depth);
		} else {
			this.writer.write(' ');
		}
		this.writer.write('}');
		
	}
	
	@Override
	void outputContent(Frame element, Frame content, String data) throws IOException {
		value(element, content.name, data);
	}
	
	@Override
	void outputEndChildren(Frame element, Frame children) throws IOException {
		
		if(children.hasContent()) {
			this.writer.write("\n");
			indent(children.depth);
		} else {
			this.writer.write(' ');
		}
		this.writer.write(']');
		
	}
	
	@Override
	void outputOpenElement(Frame container, Frame element) throws IOException {
		
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
	void outputValue(Frame container, String type, String value) throws IOException {
		
		if(container.hasContent()) {
			// This is not the first child element
			this.writer.write(", ");
		}
		
		if(value == null) {
			this.writer.write("null");
		} else {
			// IMPROVE: don't escape numbers
			this.writer.write('"');
			this.writer.write(JsonEncoder.encode(value));
			this.writer.write('"');
		}
		
	}
	
	@Override
	void outputElementBeginContent(Frame element) {
		// nothing to do here
	}
	
	@Override
	protected void end() throws IOException {
		this.writer.write('\n');
	}
	
	@Override
	void outputBeginChild(Frame element, Frame child) {
		child.depth = element.depth;
	}
	
}

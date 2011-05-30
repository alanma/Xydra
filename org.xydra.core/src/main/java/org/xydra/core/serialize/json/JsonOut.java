package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XNumberValue;
import org.xydra.core.serialize.AbstractXydraOut;
import org.xydra.minio.MiniWriter;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonOut extends AbstractXydraOut {
	
	public JsonOut(MiniWriter writer) {
		super(writer);
	}
	
	public JsonOut() {
		super();
	}
	
	private void outputName(Frame frame, String name) {
		
		if(frame.getAttrCount() > 0 || frame.hasContent()) {
			this.writer.write(',');
		}
		whitespace('\n');
		indent(frame.depth + 1);
		this.writer.write('"');
		this.writer.write(name);
		this.writer.write("\":");
		whitespace(' ');
		
	}
	
	private <T> void output(T value) {
		
		if(value instanceof Boolean || value instanceof Number || value instanceof XBooleanValue
		        || value instanceof XNumberValue) {
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
		
		int t = (container.type == Type.Element || container.element != null) ? 0 : 1;
		
		if(element.hasContent() || element.getAttrCount() > t) {
			whitespace('\n');
			indent(element.depth);
		} else {
			whitespace(' ');
		}
		this.writer.write('}');
		
	}
	
	@Override
	protected <T> void outputContent(Frame element, Frame content, T data) {
		
		outputName(element, content.name);
		
		if(data == null) {
			this.writer.write("null");
		} else {
			output(data);
		}
	}
	
	@Override
	protected void outputEndChildren(Frame element, Frame children) {
		
		if(children.hasContent()) {
			whitespace('\n');
			indent(children.depth);
		} else {
			whitespace(' ');
		}
		this.writer.write(']');
		
	}
	
	@Override
	protected void outputNullElement(Frame container) {
		
		assert container.type == Type.Root || container.type == Type.Children
		        || container.type == Type.Child;
		
		beginChild(container, true);
		
		this.writer.write("null");
		
	}
	
	private void beginChild(Frame container, boolean newline) {
		
		if(container.type == Type.Children || container.type == Type.Array) {
			if(container.hasContent() || container.getAttrCount() > 0) {
				// This is not the first child element
				this.writer.write(',');
			}
			if(newline && (!container.hasContent() || container.element == null)) {
				whitespace('\n');
				indent(container.depth + 1);
			} else {
				whitespace(' ');
			}
		} else if(container.type == Type.Child) {
			outputName(container, container.name);
		}
	}
	
	@Override
	protected void outputOpenElement(Frame container, Frame element) {
		
		element.depth = container.depth + 1;
		
		if(container.type == Type.Root || container.type == Type.Children
		        || container.type == Type.Child) {
			
			beginChild(container, true);
			
			this.writer.write('{');
			if(container.element == null) {
				whitespace(' ');
				this.writer.write("\"$type\":");
				whitespace(' ');
				this.writer.write('"');
				this.writer.write(element.name);
				this.writer.write('"');
			}
			
		} else {
			
			assert container.type == Type.Element;
			
			outputName(container, element.name);
			this.writer.write('{');
			
		}
		
	}
	
	@Override
	protected <T> void outputValue(Frame container, T value) {
		
		beginChild(container, false);
		
		if(value == null) {
			this.writer.write("null");
		} else {
			output(value);
		}
	}
	
	@Override
	protected void end() {
		whitespace('\n');
	}
	
	@Override
	protected void outputBeginChild(Frame element, Frame child) {
		child.depth = element.depth;
		if(element.getAttrCount() > 0 || element.hasContent()) {
			this.writer.write(',');
		}
	}
	
	@Override
	protected void outputBeginArray(Frame container, Frame array) {
		
		beginChild(container, true);
		
		this.writer.write('[');
		
		array.depth = container.depth + 1;
	}
	
	@Override
	protected void outputEndArray(Frame container, Frame array) {
		
		if(array.hasContent()) {
			whitespace('\n');
			indent(array.depth);
		} else {
			whitespace(' ');
		}
		this.writer.write(']');
		
	}
	
}

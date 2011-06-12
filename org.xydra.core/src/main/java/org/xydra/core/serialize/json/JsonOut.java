package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XNumberValue;
import org.xydra.core.serialize.AbstractXydraOut;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonOut extends AbstractXydraOut {
	
	private final String callback;
	
	public JsonOut(MiniWriter writer) {
		this(writer, null);
	}
	
	public JsonOut() {
		this((String)null);
	}
	
	public JsonOut(MiniWriter writer, String callback) {
		super(writer);
		this.callback = callback;
		init();
	}
	
	public JsonOut(String callback) {
		super();
		this.callback = callback;
		init();
	}
	
	private void init() {
		if(this.callback != null) {
			write(this.callback);
			write('(');
		}
	}
	
	private <T> void output(T value) {
		
		if(value instanceof Boolean || value instanceof Number || value instanceof XBooleanValue
		        || value instanceof XNumberValue) {
			write(value.toString());
		} else {
			write('"');
			write(JsonEncoder.encode(value.toString()));
			write('"');
		}
		
	}
	
	@Override
	protected <T> void outputAttribute(Frame element, String name, T value) {
		
		outputName(element, name);
		
		output(value);
		
	}
	
	@Override
	protected void outputChild(Frame child) {
		
		outputName(child.parent, child.name);
		
		child.depth = child.parent.depth;
		
	}
	
	@Override
	protected void outputCloseElement(Frame element) {
		endContainer(element, '}');
	}
	
	@Override
	protected void outputNullElement(Frame container) {
		
		begin(container, true);
		
		write("null");
		
	}
	
	private void outputName(Frame frame, String name) {
		
		begin(frame, true);
		
		write('"');
		write(name);
		write("\":");
		whitespace(' ');
		
	}
	
	private void begin(Frame frame, boolean newline) {
		
		if(frame.hasContent || frame.hasSpecialContent) {
			// This is not the first child element
			write(',');
		}
		if(newline) {
			frame.hasContent = true;
		} else {
			frame.hasSpecialContent = true;
		}
		if(frame.type != Type.Root && frame.type != Type.Child && frame.type != Type.Entry) {
			if(newline) {
				whitespace('\n');
				indent(frame.depth + 1);
			} else {
				whitespace(' ');
			}
		}
	}
	
	@Override
	protected void outputOpenElement(Frame element) {
		
		boolean saveType = !element.name.equals(element.parent.getChildType());
		
		beginContainer(element, '{', saveType && element.parent.type != Type.Child);
		
		if(saveType) {
			whitespace(' ');
			write('"');
			write(JsonEncoder.PROPERTY_TYPE);
			write("\":");
			whitespace(' ');
			write('"');
			write(element.name);
			write('"');
			element.hasSpecialContent = true;
		}
		
	}
	
	@Override
	protected <T> void outputValue(Frame container, T value) {
		
		begin(container, false);
		
		if(value == null) {
			write("null");
		} else {
			output(value);
		}
	}
	
	@Override
	protected void outpuEnd() {
		if(this.callback != null) {
			write(");");
		}
		whitespace('\n');
	}
	
	@Override
	protected void outputBeginArray(Frame array) {
		beginContainer(array, '[', false);
	}
	
	@Override
	protected void outputEndArray(Frame array) {
		endContainer(array, ']');
	}
	
	@Override
	public String getContentType() {
		return "application/json";
	}
	
	@Override
	protected void outputBeginMap(Frame map) {
		beginContainer(map, '{', false);
	}
	
	private void beginContainer(Frame container, char c, boolean newline) {
		
		begin(container.parent, newline || !container.parent.hasContent);
		
		write(c);
		
		container.depth = container.parent.depth + 1;
	}
	
	@Override
	protected void outputEndMap(Frame map) {
		endContainer(map, '}');
	}
	
	private void endContainer(Frame container, char c) {
		
		if(container.hasContent) {
			whitespace('\n');
			indent(container.depth);
		} else {
			whitespace(' ');
		}
		write(c);
	}
	
	@Override
	protected void outputEntry(Frame entry) {
		outputChild(entry);
	}
	
}

package org.xydra.core.serialize;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.minio.MiniStringWriter;
import org.xydra.minio.MiniWriter;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
abstract public class AbstractXydraOut implements XydraOut {
	
	protected final MiniWriter writer;
	
	private boolean indent = true;
	private boolean whitespace = false;
	
	public void enableWhitespace(boolean whitespace, boolean idententation) {
		this.whitespace = whitespace;
		this.indent = idententation;
	}
	
	public AbstractXydraOut(MiniWriter writer) {
		this.writer = writer;
		this.stack.push(new Frame(Type.Root, "(root)"));
	}
	
	public AbstractXydraOut() {
		this(new MiniStringWriter());
	}
	
	protected enum Type {
		Element, Children, Text, Root, Child, Array
	}
	
	protected static class Frame {
		
		public final String name;
		
		public final Type type;
		
		public final String element;
		
		public int depth = -1; // to be used by subclasses
		
		private Set<String> names;
		
		private Type contentType = null;
		private int nAttributes = 0;
		
		public Frame(Type type, String name) {
			this.type = type;
			this.name = name;
			this.element = null;
		}
		
		public Frame(Type type, String name, String element) {
			this.type = type;
			this.name = name;
			this.element = element;
		}
		
		public boolean hasContent() {
			return this.contentType != null;
		}
		
		public Type getContentType() {
			return this.contentType;
		}
		
		public int getAttrCount() {
			return this.nAttributes;
		}
		
	}
	
	private void recordName(Frame frame, String name) {
		
		assert frame.type == Type.Element;
		
		if(frame.names != null) {
			if(frame.names.contains(name)) {
				error("duplicate attribute / child name: " + name);
			}
		} else {
			frame.names = new HashSet<String>();
		}
		frame.names.add(name);
	}
	
	private final Stack<Frame> stack = new Stack<Frame>();
	
	@Override
	public <T> void attribute(String name, T value) {
		
		Frame element = getCurrent();
		
		if(element.type != Type.Element) {
			error("cannot only set attributes on elements");
		} else if(element.hasContent()) {
			error("cannot set attribute for element that already has content");
		}
		
		recordName(element, name);
		
		outputAttribute(element, name, value);
		
		element.nAttributes++;
		
	}
	
	protected abstract <T> void outputAttribute(Frame element, String name, T value);
	
	public void nullElement() {
		
		Frame container = getCurrent();
		
		if(container.type == Type.Element) {
			error("must cannot add null element without a name for children");
		} else if(container.type == Type.Text) {
			error("cannot add children once text has been set");
		} else if(container.type == Type.Child && container.hasContent()) {
			error("must declare beforehand to add multiple children");
		}
		
		outputNullElement(container);
		
		container.contentType = Type.Element;
		
		closedChild(container);
	}
	
	protected abstract void outputNullElement(Frame container);
	
	@Override
	public void open(String type) {
		
		Frame container = getCurrent();
		
		if(container.element != null && !container.element.equals(type)) {
			error("error mismatched child type: " + type);
		} else if(container.contentType == Type.Root) {
			error("cannot add content to element after adding type-less children");
		} else if(container.type == Type.Element) {
			recordName(container, type);
		} else if(container.type == Type.Text) {
			error("cannot add children once text has been set");
		} else if(container.type == Type.Child && container.hasContent()) {
			error("must declare beforehand to add multiple children");
		}
		
		Frame current = new Frame(Type.Element, type);
		this.stack.push(current);
		
		outputOpenElement(container, current);
		
		container.contentType = Type.Element;
		if(container.type != Type.Element && container.element == null) {
			current.nAttributes++;
		}
	}
	
	protected abstract void outputOpenElement(Frame container, Frame element);
	
	@Override
	public <T> void value(T value) {
		
		Frame container = getCurrent();
		
		if(container.type != Type.Children && container.type != Type.Child) {
			error("can only add values to child lists");
		} else if(container.type == Type.Child && container.hasContent()) {
			error("must declare beforehand to add multiple children");
		}
		
		outputValue(container, value);
		
		container.nAttributes++;
	}
	
	protected abstract <T> void outputValue(Frame container, T value);
	
	@Override
	public void beginChildren(String name, boolean multiple) {
		beginChildren(name, multiple, null);
	}
	
	@Override
	public void beginChildren(String name, boolean multiple, String type) {
		
		Frame element = getCurrent();
		
		if(element.type == Type.Children || element.type == Type.Child) {
			error("can only start child list once");
		} else if(element.type == Type.Text) {
			error("cannot add children once content has been set");
		} else if(element.type == Type.Array) {
			error("cannot add children to array");
		} else if(element.type == Type.Root) {
			error("must open root element before starting children");
		} else if((multiple && type == null) ? element.hasContent()
		        : element.contentType == Type.Root) {
			error("must not add untyped child list after adding children");
		}
		assert element.type == Type.Element;
		
		recordName(element, name);
		
		Frame children = new Frame(multiple ? Type.Children : Type.Child, name, type);
		this.stack.push(children);
		
		if(multiple) {
			outputBeginChildren(element, children);
		} else {
			outputBeginChild(element, children);
		}
		
		element.contentType = (type == null && multiple) ? Type.Root : children.type;
	}
	
	protected abstract void outputBeginChildren(Frame element, Frame children);
	
	protected abstract void outputBeginChild(Frame element, Frame child);
	
	public void endChildren() {
		
		Frame children = getCurrent();
		
		if(children.type != Type.Child && children.type != Type.Children) {
			error("no children are open");
		}
		
		if(children.type == Type.Child) {
			if(!children.hasContent()) {
				error("missing child element");
			}
			this.stack.pop();
		} else {
			assert !this.stack.empty();
			this.stack.pop();
			outputEndChildren(this.stack.peek(), children);
		}
		
	}
	
	abstract protected void outputEndChildren(Frame element, Frame children);
	
	@Override
	public void close(String type) {
		
		Frame current = getCurrent();
		
		if(current.type == Type.Child || current.type == Type.Children
		        || current.type == Type.Array) {
			error("must end children before closing element");
		} else if(current.type == Type.Text) {
			this.stack.pop();
			current = this.stack.peek();
		} else if(current.type == Type.Root) {
			error("must open root element before closing anything");
		}
		
		assert current.type == Type.Element;
		
		if(current.name != type) {
			error("cannot close element " + type + " while element " + current.name + " is open");
		}
		
		assert !this.stack.isEmpty();
		this.stack.pop();
		Frame container = this.stack.peek();
		
		outputCloseElement(container, current);
		
		closedChild(container);
	}
	
	private void closedChild(Frame container) {
		if(container.type == Type.Root) {
			this.stack.pop();
			assert this.stack.isEmpty();
			end();
		} else {
			container.nAttributes++;
		}
	}
	
	protected abstract void outputCloseElement(Frame container, Frame element);
	
	protected void end() {
		// to be overwritten by children if needed
	}
	
	@Override
	public <T> void content(String name, T data) {
		
		Frame element = getCurrent();
		
		if(element.type != Type.Element) {
			error("cannot only set content on elements");
		}
		
		if(element.hasContent()) {
			error("cannot set content for element that already has content");
		}
		
		recordName(element, name);
		
		Frame content = new Frame(Type.Text, name);
		this.stack.push(content);
		
		outputContent(element, content, data);
		
		element.contentType = Type.Text;
	}
	
	protected abstract <T> void outputContent(Frame element, Frame content, T data);
	
	protected void indent(int count) {
		
		if(!this.indent || !this.whitespace) {
			return;
		}
		
		for(int i = 0; i < count; i++) {
			this.writer.write('\t');
		}
	}
	
	protected void whitespace(char c) {
		if(this.whitespace) {
			this.writer.write(c);
		}
	}
	
	private void error(String desc) {
		
		StringBuilder bt = new StringBuilder();
		for(Frame frame : this.stack) {
			
			switch(frame.type) {
			case Element:
				bt.append(".<");
				bt.append(frame.name);
				bt.append('>');
				break;
			case Children:
				bt.append('.');
				bt.append(frame.name);
				bt.append("[]");
				break;
			case Child:
				bt.append('.');
				bt.append(frame.name);
				break;
			case Text:
				bt.append(".content(");
				bt.append(frame.name);
				bt.append(')');
				break;
			case Root:
				bt.append("(root)");
				break;
			case Array:
				bt.append(".[]");
				break;
			}
			if(frame.element != null) {
				bt.append('{');
				bt.append(frame.element);
				bt.append('}');
			}
			
		}
		if(this.stack.isEmpty()) {
			bt.append("(end)");
		}
		
		throw new IllegalStateException(desc + "; am at " + bt.toString());
	}
	
	private Frame getCurrent() {
		
		if(this.stack.isEmpty()) {
			error("root element has ended");
		}
		
		return this.stack.peek();
	}
	
	public String getData() {
		
		if(!this.stack.empty()) {
			error("cannot get result before closing all elements");
		}
		
		if(!(this.writer instanceof MiniStringWriter)) {
			throw new IllegalStateException(
			        "can only use this method when constructed with a MiniStringWriter");
		}
		
		return this.writer.toString();
	}
	
	public boolean isClosed() {
		return this.stack.isEmpty();
	}
	
	public void flush() {
		this.writer.flush();
	}
	
	@Override
	public <T> void value(String name, String type, T value) {
		beginChildren(name, false, type);
		value(value);
		endChildren();
	}
	
	@Override
	public <T> void values(String name, String type, Iterable<T> values) {
		beginChildren(name, true, type);
		for(T value : values) {
			value(value);
		}
		endChildren();
	}
	
	@Override
	public <T> void element(String type, String name, T content) {
		open(type);
		content(name, content);
		close(type);
	}
	
	@Override
	public <T> void element(String type) {
		open(type);
		close(type);
	}
	
	@Override
	public void beginArray() {
		beginArray(null);
	}
	
	@Override
	public void beginArray(String type) {
		
		Frame container = getCurrent();
		
		if(container.type == Type.Element) {
			error("mist be in child list or array to start an array");
		} else if(container.type == Type.Text) {
			error("cannot add array once content has been set");
		} else if(container.type == Type.Root) {
			error("must open root element before starting array");
		}
		assert container.type == Type.Children || container.type == Type.Child
		        || container.type == Type.Array;
		
		Frame array = new Frame(Type.Array, type);
		this.stack.push(array);
		
		outputBeginArray(container, array);
		
		container.contentType = Type.Array;
	}
	
	protected abstract void outputBeginArray(Frame container, Frame array);
	
	@Override
	public void endArray() {
		
		Frame array = getCurrent();
		
		if(array.type != Type.Array) {
			error("no array is open");
		}
		
		assert !this.stack.empty();
		this.stack.pop();
		outputEndArray(this.stack.peek(), array);
		
	}
	
	protected abstract void outputEndArray(Frame container, Frame array);
	
}

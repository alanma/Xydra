package org.xydra.core.serialize;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xydra.minio.MiniStringWriter;
import org.xydra.minio.MiniWriter;


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
		Element, Children, Text, Root, Child
	}
	
	protected static class Frame {
		
		public final String name;
		
		public final Type type;
		
		public final String element;
		
		public int depth = -1; // to be used by subclasses
		
		private Set<String> names;
		
		private Type contentType = null;
		private boolean hasAttributes = false;
		
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
		
		public boolean hasAttributes() {
			return this.hasAttributes;
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
		}
		
		if(element.hasContent()) {
			error("cannot set attribute for element that already has content");
		}
		
		recordName(element, name);
		
		outputAttribute(element, name, value);
		
		element.hasAttributes = true;
		
	}
	
	protected abstract <T> void outputAttribute(Frame element, String name, T value);
	
	@Override
	public void open(String name) {
		
		Frame container = getCurrent();
		
		if(container.element != null && !container.element.equals(name)) {
			if(container.type == Type.Child) {
				error("missing child element, cannot open element" + name);
				this.stack.pop();
				container = this.stack.peek();
			} else if(container.type == Type.Children) {
				Frame children = container;
				assert !this.stack.empty();
				this.stack.pop();
				container = this.stack.peek();
				outputEndChildren(container, children);
			}
		}
		
		if(container.type == Type.Element) {
			recordName(container, name);
			if(!container.hasContent()) {
				outputElementBeginContent(container);
			}
		} else if(container.type == Type.Text) {
			error("cannot add children once text has been set");
		} else if(container.type == Type.Child && container.hasContent()) {
			error("must declare beforehand to add multiple children");
		}
		
		Frame current = new Frame(Type.Element, name);
		this.stack.push(current);
		
		outputOpenElement(container, current);
		
		container.contentType = Type.Element;
		if(container.type != Type.Element && container.element == null) {
			current.hasAttributes = true;
		}
	}
	
	protected abstract void outputOpenElement(Frame container, Frame element);
	
	protected abstract void outputElementBeginContent(Frame element);
	
	@Override
	public <T> void value(String type, T value) {
		
		Frame container = getCurrent();
		
		if(container.type != Type.Children && container.type != Type.Child) {
			error("can only add values to child lists");
		}
		
		if(container.type == Type.Child && container.hasContent()) {
			error("must declare beforehand to add multiple children");
		}
		
		outputValue(container, type, value);
		
		container.hasAttributes = true;
	}
	
	protected abstract <T> void outputValue(Frame container, String type, T value);
	
	@Override
	public void children(String name, boolean multiple) {
		children(name, multiple, null);
	}
	
	@Override
	public void children(String name, boolean multiple, String type) {
		
		Frame element = getCurrent();
		
		if(element.type == Type.Children || element.type == Type.Child) {
			error("can only start child list once");
		} else if(element.type == Type.Text) {
			error("cannot add children once content has been set");
		} else if(element.type == Type.Root) {
			error("must open root element before starting children");
		} else if(element.hasContent()) {
			error("must not add a child list after adding children");
		}
		assert element.type == Type.Element;
		
		recordName(element, name);
		
		Frame children = new Frame(multiple ? Type.Children : Type.Child, name, type);
		this.stack.push(children);
		
		outputElementBeginContent(element);
		if(multiple) {
			outputBeginChildren(element, children);
		} else {
			outputBeginChild(element, children);
		}
		
		element.contentType = children.type;
	}
	
	protected abstract void outputBeginChildren(Frame element, Frame children);
	
	protected abstract void outputBeginChild(Frame element, Frame child);
	
	@Override
	public void close(String name) {
		
		Frame current = getCurrent();
		
		if(current.type == Type.Child) {
			if(!current.hasContent()) {
				error("missing child element");
			}
			this.stack.pop();
			current = this.stack.peek();
		} else if(current.type == Type.Children) {
			Frame children = current;
			assert !this.stack.empty();
			this.stack.pop();
			current = this.stack.peek();
			outputEndChildren(current, children);
		} else if(current.type == Type.Text) {
			this.stack.pop();
			current = this.stack.peek();
		} else if(current.type == Type.Root) {
			error("must open root element before closing anything");
		}
		
		assert current.type == Type.Element;
		
		if(current.name != name) {
			error("cannot close element " + name + " while element " + current.name + " is open");
		}
		
		assert !this.stack.isEmpty();
		this.stack.pop();
		Frame container = this.stack.peek();
		
		outputCloseElement(container, current);
		
		if(container.type == Type.Root) {
			this.stack.pop();
			assert this.stack.isEmpty();
			end();
		} else {
			container.hasAttributes = true;
		}
	}
	
	protected abstract void outputEndChildren(Frame element, Frame children);
	
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
		
		outputElementBeginContent(element);
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
			}
			if(frame.type != null) {
				bt.append('{');
				bt.append(frame.type);
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
}

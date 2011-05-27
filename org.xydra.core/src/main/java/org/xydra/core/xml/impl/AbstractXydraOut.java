package org.xydra.core.xml.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xydra.core.xml.XydraOut;


abstract public class AbstractXydraOut implements XydraOut {
	
	protected final Writer writer;
	
	private final boolean indent = true;
	
	public AbstractXydraOut(OutputStream os) {
		this(wrap(os));
	}
	
	private static Writer wrap(OutputStream os) {
		try {
			return new OutputStreamWriter(os, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("missing UTF-8 charset", e);
		}
	}
	
	public AbstractXydraOut(Writer writer) {
		this.writer = writer;
		this.stack.push(new Frame(Type.Root, "(root)"));
	}
	
	public AbstractXydraOut() {
		this(new StringWriter());
	}
	
	protected enum Type {
		Element, Children, Text, Root, Child
	}
	
	protected static class Frame {
		
		public final String name;
		
		public final Type type;
		
		public int depth = -1; // to be used by subclasses
		
		private Set<String> names;
		
		private Type contentType = null;
		private boolean hasAttributes = false;
		
		public Frame(Type type, String name) {
			this.type = type;
			this.name = name;
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
	public void attribute(String name, String value) {
		
		Frame element = getCurrent();
		
		if(element.type != Type.Element) {
			error("cannot only set attributes on elements");
		}
		
		if(element.hasContent()) {
			error("cannot set attribute for element that already has content");
		}
		
		recordName(element, name);
		
		try {
			outputAttribute(element, name, value);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		element.hasAttributes = true;
		
	}
	
	protected abstract void outputAttribute(Frame element, String name, String value)
	        throws IOException;
	
	@Override
	public void open(String name) {
		
		Frame container = getCurrent();
		
		if(container.type == Type.Element) {
			recordName(container, name);
			if(!container.hasContent()) {
				try {
					outputElementBeginContent(container);
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		} else if(container.type == Type.Text) {
			error("cannot add children once text has been set");
		} else if(container.type == Type.Child
		        && (container.hasAttributes || container.hasContent())) {
			error("must declare beforehand to add multiple children");
		}
		
		Frame current = new Frame(Type.Element, name);
		this.stack.push(current);
		
		try {
			outputOpenElement(container, current);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		container.contentType = Type.Element;
	}
	
	abstract void outputOpenElement(Frame container, Frame element) throws IOException;
	
	abstract void outputElementBeginContent(Frame element) throws IOException;
	
	@Override
	public void value(String type, String value) {
		
		Frame container = getCurrent();
		
		if(container.type != Type.Children && container.type != Type.Child) {
			error("can only add values to child lists");
		}
		
		if(container.type == Type.Child && (container.hasAttributes || container.hasContent())) {
			error("must declare beforehand to add multiple children");
		}
		
		try {
			outputValue(container, type, value);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		container.hasAttributes = true;
	}
	
	abstract void outputValue(Frame container, String type, String value) throws IOException;
	
	@Override
	public void children(String name, boolean multiple) {
		
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
		
		Frame children = new Frame(multiple ? Type.Children : Type.Child, name);
		this.stack.push(children);
		
		try {
			outputElementBeginContent(element);
			if(multiple) {
				outputBeginChildren(element, children);
			} else {
				outputBeginChild(element, children);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		element.contentType = children.type;
	}
	
	abstract void outputBeginChildren(Frame element, Frame children) throws IOException;
	
	abstract void outputBeginChild(Frame element, Frame child) throws IOException;
	
	@Override
	public void close(String name) {
		
		Frame current = getCurrent();
		
		if(current.type == Type.Child) {
			this.stack.pop();
			current = this.stack.peek();
		} else if(current.type == Type.Children) {
			Frame children = current;
			assert !this.stack.empty();
			this.stack.pop();
			current = this.stack.peek();
			try {
				outputEndChildren(current, children);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
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
		
		try {
			outputCloseElement(container, current);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		if(container.type == Type.Root) {
			this.stack.pop();
			assert this.stack.isEmpty();
			try {
				end();
				this.writer.flush();
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	abstract void outputEndChildren(Frame element, Frame children) throws IOException;
	
	abstract void outputCloseElement(Frame container, Frame element) throws IOException;
	
	@SuppressWarnings("unused")
	protected void end() throws IOException {
		// to be overwritten by children if needed
	}
	
	@Override
	public void content(String name, String data) {
		
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
		
		try {
			outputElementBeginContent(element);
			outputContent(element, content, data);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		element.contentType = Type.Text;
	}
	
	abstract void outputContent(Frame element, Frame content, String data) throws IOException;
	
	protected void indent(int count) throws IOException {
		
		if(!this.indent) {
			return;
		}
		
		for(int i = 0; i < count; i++) {
			this.writer.write('\t');
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
			if(this.stack.isEmpty()) {
				bt.append("(end)");
			}
			
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
		
		if(!(this.writer instanceof StringWriter)) {
			throw new IllegalStateException(
			        "can only use this method when constructed with a StringWriter");
		}
		
		return ((StringWriter)this.writer).getBuffer().toString();
	}
	
	public boolean isClosed() {
		return this.stack.isEmpty();
	}
	
	public void flush() {
		
		try {
			this.writer.flush();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
	}
}

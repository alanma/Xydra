package org.xydra.core.serialize;

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
	}
	
	public AbstractXydraOut() {
		this(new MiniStringWriter());
	}
	
	protected enum Type {
		Element, Text, Root, Child, Array, Map, Entry
	}
	
	protected static class Frame {
		
		public final Frame parent;
		
		public final String name;
		
		public final Type type;
		
		private String childType = null;
		private boolean forcedChildType = false;
		
		public int depth = -1; // to be used by subclasses for indentation
		public boolean hasContent = false; // to be used by subclasses
		public boolean hasSpecialContent = false;
		
		private Type contentType = null;
		
		public Frame(Frame parent, Type type, String name) {
			this.parent = parent;
			this.type = type;
			this.name = name;
		}
		
		public String getChildType(String def) {
			return this.childType == null ? def : this.childType;
		}
		
		public String getChildType() {
			return this.childType;
		}
		
		public boolean hasChildType() {
			return this.childType != null;
		}
		
		public boolean isChildTypeForced() {
			return this.forcedChildType;
		}
		
		private boolean hasContent() {
			return this.contentType != null;
		}
		
	}
	
	private Frame current = new Frame(null, Type.Root, "(root)");
	
	@Override
	public <T> void attribute(String name, T value) {
		
		check();
		
		if(this.current.type != Type.Element) {
			error("cannot only set attributes on elements");
		} else if(this.current.hasContent()) {
			error("cannot set attribute for element that already has content");
		}
		
		outputAttribute(this.current, name, value);
		
	}
	
	protected abstract <T> void outputAttribute(Frame element, String name, T value);
	
	private void checkCanAddChild() {
		
		check();
		
		if(this.current.type == Type.Element) {
			error("must call child() before adding children to element");
		} else if(this.current.type == Type.Map) {
			error("must call entry() before adding entries to map");
		} else if(this.current.contentType == Type.Text) {
			error("cannot add children once text has been set");
		}
		
	}
	
	private void addedChild() {
		
		if(this.current.type == Type.Child || this.current.type == Type.Entry) {
			this.current = this.current.parent;
		}
		
		if(this.current.type == Type.Root) {
			this.current = null;
			outpuEnd();
		} else {
			this.current.contentType = Type.Child;
		}
	}
	
	public void nullElement() {
		
		checkCanAddChild();
		
		outputNullElement(this.current);
		
		addedChild();
	}
	
	protected abstract void outputNullElement(Frame container);
	
	@Override
	public void open(String type) {
		
		checkCanAddChild();
		
		if(this.current.forcedChildType && !this.current.childType.equals(type)) {
			error("error mismatched child type: " + type);
		}
		
		this.current = new Frame(this.current, Type.Element, type);
		
		outputOpenElement(this.current);
	}
	
	protected abstract void outputOpenElement(Frame element);
	
	@Override
	public <T> void value(T value) {
		
		checkCanAddChild();
		
		outputValue(this.current, value);
		
		addedChild();
	}
	
	protected abstract <T> void outputValue(Frame container, T value);
	
	@Override
	public void child(String name) {
		
		check();
		
		if(this.current.type == Type.Child) {
			error("must add child first before calling child again");
		} else if(this.current.type == Type.Array) {
			error("cannot add children to array");
		} else if(this.current.type == Type.Map || this.current.type == Type.Entry) {
			error("cannot add children to map");
		} else if(this.current.type == Type.Root) {
			error("must open root element before starting children");
		} else if(this.current.contentType == Type.Text) {
			error("cannot add children once content has been set");
		}
		assert this.current.type == Type.Element;
		
		this.current = new Frame(this.current, Type.Child, name);
		
		outputChild(this.current);
	}
	
	protected abstract void outputChild(Frame child);
	
	@Override
	public void close(String type) {
		
		check();
		
		if(this.current.type == Type.Root) {
			error("must open root element before closing anything");
		} else if(this.current.type != Type.Element) {
			error("must end children before closing element");
		}
		
		if(this.current.name != type) {
			error("cannot close element " + type + " while element " + this.current.name
			        + " is open");
		}
		
		outputCloseElement(this.current);
		
		this.current = this.current.parent;
		
		addedChild();
	}
	
	protected abstract void outputCloseElement(Frame element);
	
	protected abstract void outpuEnd();
	
	@Override
	public <T> void content(String name, T value) {
		child(name);
		value(value);
	}
	
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
	
	protected void write(char c) {
		this.writer.write(c);
	}
	
	protected void write(String s) {
		this.writer.write(s);
	}
	
	private void error(String desc) {
		
		StringBuilder bt = new StringBuilder();
		
		if(this.current == null) {
			bt.append("(end)");
		} else
			do {
				
				switch(this.current.type) {
				case Element:
					bt.append(".<");
					bt.append(this.current.name);
					bt.append('>');
					break;
				case Child:
				case Entry:
					bt.append('.');
					bt.append(this.current.name);
					break;
				case Text:
					assert false;
					break;
				case Root:
					bt.append("(root)");
					break;
				case Array:
					assert this.current.name == null;
					bt.append(".[]");
					break;
				case Map:
					bt.append('{');
					bt.append(this.current.name);
					bt.append('}');
					break;
				}
				if(this.current.childType != null) {
					bt.append(':');
					bt.append(this.current.childType);
				}
				
				this.current = this.current.parent;
			} while(this.current != null);
		
		throw new IllegalStateException(desc + "; am at " + bt.toString());
	}
	
	private void check() {
		
		if(this.current == null) {
			error("root element has ended");
		}
	}
	
	public String getData() {
		
		if(this.current != null) {
			error("cannot get result before closing all elements");
		}
		
		if(!(this.writer instanceof MiniStringWriter)) {
			throw new IllegalStateException(
			        "can only use this method when constructed with a MiniStringWriter");
		}
		
		return this.writer.toString();
	}
	
	public boolean isClosed() {
		return this.current == null;
	}
	
	public void flush() {
		this.writer.flush();
	}
	
	@Override
	public void setChildType(String type) {
		
		setDefaultType(type);
		this.current.forcedChildType = true;
	}
	
	@Override
	public void setDefaultType(String type) {
		
		check();
		
		if(this.current.type == Type.Element) {
			error("must call child() before setChildType()");
		} else if(this.current.childType != null) {
			error("can only set the child type once for each context");
		} else if(this.current.hasContent()) {
			error("cannot set child type after adding children");
		}
		
		this.current.childType = type;
	}
	
	@Override
	public <T> void value(String name, String type, T value) {
		child(name);
		setChildType(type);
		value(value);
	}
	
	@Override
	public <T> void values(String name, String type, Iterable<T> values) {
		child(name);
		beginArray();
		setChildType(type);
		for(T value : values) {
			value(value);
		}
		endArray();
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
		
		checkCanAddChild();
		
		this.current = new Frame(this.current, Type.Array, null);
		
		outputBeginArray(this.current);
	}
	
	protected abstract void outputBeginArray(Frame array);
	
	@Override
	public void endArray() {
		
		check();
		
		if(this.current.type != Type.Array) {
			error("no array is open");
		}
		
		outputEndArray(this.current);
		
		this.current = this.current.parent;
		
		addedChild();
	}
	
	protected abstract void outputEndArray(Frame array);
	
	@Override
	public void child(String name, String type) {
		child(name);
		setChildType(type);
	}
	
	@Override
	public void beginArray(String type) {
		beginArray();
		setChildType(type);
	}
	
	@Override
	public void beginMap(String attribute) {
		
		checkCanAddChild();
		
		this.current = new Frame(this.current, Type.Map, attribute);
		
		outputBeginMap(this.current);
	}
	
	protected abstract void outputBeginMap(Frame map);
	
	@Override
	public void endMap() {
		
		check();
		
		if(this.current.type == Type.Entry) {
			error("must add child after calling entry");
		} else if(this.current.type != Type.Map) {
			error("no map is open");
		}
		
		outputEndMap(this.current);
		
		this.current = this.current.parent;
		
		addedChild();
	}
	
	protected abstract void outputEndMap(Frame map);
	
	@Override
	public void entry(String id) {
		
		check();
		
		if(this.current.type == Type.Entry) {
			error("must add child before calling entry() again");
		} else if(this.current.type != Type.Map) {
			error("can only add entries to maps");
		}
		
		this.current = new Frame(this.current, Type.Entry, id);
		this.current.childType = this.current.parent.childType;
		this.current.forcedChildType = this.current.parent.forcedChildType;
		
		outputEntry(this.current);
		
	}
	
	protected abstract void outputEntry(Frame entry);
	
	@Override
	public void beginMap(String attribute, String type) {
		beginMap(attribute);
		setChildType(type);
	}
	
}

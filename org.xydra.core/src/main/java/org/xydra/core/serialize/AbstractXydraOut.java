package org.xydra.core.serialize;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniStringWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.sharedutils.XyAssert;

@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
abstract public class AbstractXydraOut implements XydraOut {

	protected static class Frame {
		private String childType = null;
		private Type contentType = null;
		public int depth = -1; // to be used by subclasses for indentation
		private boolean forcedChildType = false;
		public boolean hasContent = false; // to be used by subclasses
		public boolean hasSpecialContent = false;
		public final String name;
		public final Frame parent;
		public final Type type;

		public Frame(final Frame parent, final Type type, final String name) {
			this.parent = parent;
			this.type = type;
			this.name = name;
		}

		public String getChildType() {
			return this.childType;
		}

		public String getChildType(final String def) {
			return this.childType == null ? def : this.childType;
		}

		public boolean hasChildType() {
			return this.childType != null;
		}

		private boolean hasContent() {
			return this.contentType != null;
		}

		public boolean isChildTypeForced() {
			return this.forcedChildType;
		}
	}

	protected enum Type {
		Array, Child, Element, Entry, Map, Root, Text
	}

	/** the current 'frame' (like in a video file format) */
	private Frame current = new Frame(null, Type.Root, "(root)");

	private boolean indent = true;

	private boolean whitespace = false;

	protected final MiniWriter writer;

	public AbstractXydraOut() {
		this(new MiniStringWriter());
	}

	public AbstractXydraOut(final MiniWriter writer) {
		this.writer = writer;
	}

	private void addedChild() {
		if (this.current.type == Type.Child || this.current.type == Type.Entry) {
			this.current = this.current.parent;
		}

		if (this.current.type == Type.Root) {
			this.current = null;
			outpuEnd();
		} else {
			this.current.contentType = Type.Child;
		}
	}

	@Override
	public <T> void attribute(final String name, final T value) {
		check();

		if (this.current.type != Type.Element) {
			error("cannot only set attributes on elements");
		} else if (this.current.hasContent()) {
			error("cannot set attribute for element that already has content");
		}

		outputAttribute(this.current, name, value);
	}

	@Override
	public void beginArray() {
		checkCanAddChild();

		this.current = new Frame(this.current, Type.Array, null);

		outputBeginArray(this.current);
	}

	@Override
	public void beginArray(final String type) {
		beginArray();
		setChildType(type);
	}

	@Override
	public void beginMap(final String attribute) {

		checkCanAddChild();

		this.current = new Frame(this.current, Type.Map, attribute);

		outputBeginMap(this.current);
	}

	@Override
	public void beginMap(final String attribute, final String type) {
		beginMap(attribute);
		setChildType(type);
	}

	private void check() {
		if (this.current == null) {
			error("root element has ended");
		}
	}

	private void checkCanAddChild() {
		check();

		if (this.current.type == Type.Element) {
			error("must call child() before adding children to element");
		} else if (this.current.type == Type.Map) {
			error("must call entry() before adding entries to map");
		} else if (this.current.contentType == Type.Text) {
			error("cannot add children once text has been set");
		}
	}

	@Override
	public void child(final String name) {
		check();

		if (this.current.type == Type.Child) {
			error("must add child first before calling child again");
		} else if (this.current.type == Type.Array) {
			error("cannot add children to array");
		} else if (this.current.type == Type.Map || this.current.type == Type.Entry) {
			error("cannot add children to map");
		} else if (this.current.type == Type.Root) {
			error("must open root element before starting children");
		} else if (this.current.contentType == Type.Text) {
			error("cannot add children once content has been set");
		}
		XyAssert.xyAssert(this.current.type == Type.Element);

		this.current = new Frame(this.current, Type.Child, name);

		outputChild(this.current);
	}

	@Override
	public void child(final String name, final String type) {
		child(name);
		setChildType(type);
	}

	@Override
	public void close(final String type) {
		check();

		if (this.current.type == Type.Root) {
			error("must open root element before closing anything");
		} else if (this.current.type != Type.Element) {
			error("must end children before closing element");
		}

		if (!this.current.name.equals(type)) {
			error("cannot close element " + type + " while element " + this.current.name
					+ " is open");
		}

		outputCloseElement(this.current);

		this.current = this.current.parent;

		addedChild();
	}

	@Override
	public <T> void content(final String name, final T value) {
		child(name);
		value(value);
	}

	@Override
	public void element(final String type) {
		open(type);
		close(type);
	}

	@Override
	public <T> void element(final String type, final String name, final T content) {
		open(type);
		content(name, content);
		close(type);
	}

	@Override
	public void enableWhitespace(final boolean whitespace, final boolean idententation) {
		this.whitespace = whitespace;
		this.indent = idententation;
	}

	@Override
	public void endArray() {
		check();

		if (this.current.type != Type.Array) {
			error("no array is open");
		}

		outputEndArray(this.current);

		this.current = this.current.parent;

		addedChild();
	}

	@Override
	public void endMap() {
		check();

		if (this.current.type == Type.Entry) {
			error("must add child after calling entry");
		} else if (this.current.type != Type.Map) {
			error("no map is open");
		}

		outputEndMap(this.current);

		this.current = this.current.parent;

		addedChild();
	}

	@Override
	public void entry(final String id) {
		check();

		if (this.current.type == Type.Entry) {
			error("must add child before calling entry() again");
		} else if (this.current.type != Type.Map) {
			error("can only add entries to maps");
		}

		this.current = new Frame(this.current, Type.Entry, id);
		this.current.childType = this.current.parent.childType;
		this.current.forcedChildType = this.current.parent.forcedChildType;

		outputEntry(this.current);

	}

	private void error(final String desc) {
		final StringBuilder bt = new StringBuilder();

		if (this.current == null) {
			bt.append("(end)");
		} else {
			do {

				switch (this.current.type) {
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
					XyAssert.xyAssert(false);
					break;
				case Root:
					bt.append("(root)");
					break;
				case Array:
					XyAssert.xyAssert(this.current.name == null);
					bt.append(".[]");
					break;
				case Map:
					bt.append('{');
					bt.append(this.current.name);
					bt.append('}');
					break;
				}
				if (this.current.childType != null) {
					bt.append(':');
					bt.append(this.current.childType);
				}

				this.current = this.current.parent;
			} while (this.current != null);
		}

		throw new IllegalStateException(desc + "; am at " + bt.toString());
	}

	@Override
	public void flush() {
		this.writer.flush();
	}

	@Override
	public String getData() {

		if (this.current != null) {
			error("cannot get result before closing all elements");
		}

		if (!(this.writer instanceof MiniStringWriter)) {
			throw new IllegalStateException(
					"can only use this method when constructed with a MiniStringWriter");
		}

		return this.writer.toString();
	}

	protected void indent(final int count) {

		if (!this.indent || !this.whitespace) {
			return;
		}

		for (int i = 0; i < count; i++) {
			this.writer.write('\t');
		}
	}

	@Override
	public boolean isClosed() {
		return this.current == null;
	}

	@Override
	public void nullElement() {
		checkCanAddChild();
		outputNullElement(this.current);
		addedChild();
	}

	@Override
	public void open(final String type) {
		checkCanAddChild();

		if (this.current.forcedChildType && !this.current.childType.equals(type)) {
			error("error mismatched child type: " + type);
		}

		this.current = new Frame(this.current, Type.Element, type);

		outputOpenElement(this.current);
	}

	protected abstract void outpuEnd();

	protected abstract <T> void outputAttribute(Frame element, String name, T value);

	protected abstract void outputBeginArray(Frame array);

	protected abstract void outputBeginMap(Frame map);

	protected abstract void outputChild(Frame child);

	protected abstract void outputCloseElement(Frame element);

	protected abstract void outputEndArray(Frame array);

	protected abstract void outputEndMap(Frame map);

	protected abstract void outputEntry(Frame entry);

	protected abstract void outputNullElement(Frame container);

	protected abstract void outputOpenElement(Frame element);

	protected abstract <T> void outputValue(Frame container, T value);

	@Override
	public void setChildType(final String type) {

		setDefaultType(type);
		this.current.forcedChildType = true;
	}

	@Override
	public void setDefaultType(final String type) {

		check();

		if (this.current.type == Type.Element) {
			error("must call child() before setDefaultType()");
		} else if (this.current.type == Type.Entry) {
			error("cannot set the (default) child type for individual map entries");
		} else if (this.current.childType != null) {
			error("can only set the child type once for each context");
		} else if (this.current.hasContent()) {
			error("cannot set child type after adding children");
		}

		this.current.childType = type;
	}

	@Override
	public <T> void value(final String name, final String type, final T value) {
		child(name);
		setChildType(type);
		value(value);
	}

	@Override
	public <T> void value(final T value) {
		checkCanAddChild();

		outputValue(this.current, value);

		addedChild();
	}

	@Override
	public <T> void values(final String name, final String type, final Iterable<T> values) {
		child(name);
		beginArray();
		setChildType(type);
		for (final T value : values) {
			value(value);
		}
		endArray();
	}

	protected void whitespace(final char c) {
		if (this.whitespace) {
			this.writer.write(c);
		}
	}

	protected void write(final char c) {
		this.writer.write(c);
	}

	protected void write(final String s) {
		this.writer.write(s);
	}

}

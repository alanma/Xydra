package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.NeverNull;
import org.xydra.core.serialize.ParsingException;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Pair;

public class JsonElement extends AbstractJsonElement {

	private static final Iterator<XydraElement> noChildren = NoneIterator.create();
	private static final Iterator<Object> noValue = NoneIterator.create();

	protected static Iterator<Pair<String, XydraElement>> transformMap(
			final Iterator<Map.Entry<String, Object>> iterator, final String type) {
		return new AbstractTransformingIterator<Map.Entry<String, Object>, Pair<String, XydraElement>>(
				iterator) {
			@Override
			public Pair<String, XydraElement> transform(final Map.Entry<String, Object> in) {
				return new Pair<String, XydraElement>(in.getKey(), wrap(in.getValue(), type));
			}
		};
	}

	private final Map<String, Object> data;

	private final String type;

	public JsonElement(final Map<String, Object> data, final String type) {
		this.data = data;
		final Object key = this.data.get(JsonEncoder.PROPERTY_TYPE);
		if (key != null) {
			this.type = key.toString();
		} else if (type != null) {
			this.type = type;
		} else {
			this.type = XmlEncoder.XMAP_ELEMENT;
		}
	}

	@Override
	public Object getAttribute(final String name) {
		return this.data.get(name);
	}

	@Override
	public Iterator<String> getAttributes() {
		return this.data.keySet().iterator();
	}

	@Override
	public XydraElement getChild(final String name) {
		return getElement(name);
	}

	@Override
	public XydraElement getChild(final String name, final String type) {
		return this.data.containsKey(name) ? wrap(this.data.get(name), type) : null;
	}

	@Override
	public Iterator<XydraElement> getChildren(final String defaultType) {
		throw new ParsingException(this, "cannot get unnamed children from JSON object");
	}

	@Override
	public Iterator<XydraElement> getChildrenByName(final String name, final String defaultType) {
		final Object childList = this.data.get(name);
		if (childList == null || !(childList instanceof List<?>)) {
			return noChildren;
		}
		return transform(((List<?>) childList).iterator(), defaultType);
	}

	@Override
	public Object getContent() {
		throw new ParsingException(this, "cannot get unnamed content from JSON object");
	}

	@Override
	public Object getContent(final String name) {
		return getAttribute(name);
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntries(final String attribute, final String defaultType) {
		return transformMap(this.data.entrySet().iterator(), defaultType);
	}

	@Override
	public @NeverNull String getType() {
		return this.type;
	}

	@Override
	public Object getValue(final String name, final int index) {
		final Object value = this.data.get(name);
		if (value instanceof Map<?, ?> || value instanceof List<?>) {
			throw new ParsingException(this, "expected value, got container");
		}
		return value;
	}

	@Override
	public Iterator<Object> getValues() {
		throw new ParsingException(this, "cannot get unnamed values from JSON object");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Object> getValues(final String name) {
		final Object childList = this.data.get(name);
		if (childList == null || !(childList instanceof List<?>)) {
			return noValue;
		}
		return ((List<Object>) childList).iterator();
	}

	@Override
	public String toString() {
		return this.type + ": " + this.data;
	}

}

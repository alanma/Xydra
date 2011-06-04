package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.core.serialize.ParsingError;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.query.Pair;


public class JsonElement extends AbstractJsonElement {
	
	private static final Iterator<XydraElement> noChildren = new NoneIterator<XydraElement>();
	private static final Iterator<Object> noValue = new NoneIterator<Object>();
	
	private final Map<String,Object> data;
	private final String type;
	
	public JsonElement(Map<String,Object> data, String type) {
		this.data = data;
		Object key = this.data.get(JsonEncoder.PROPERTY_TYPE);
		if(key != null) {
			this.type = key.toString();
		} else if(type != null) {
			this.type = type;
		} else {
			this.type = XmlEncoder.XMAP_ELEMENT;
		}
	}
	
	@Override
	public Object getAttribute(String name) {
		return this.data.get(name);
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		return this.data.containsKey(name) ? wrap(this.data.get(name), type) : null;
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String defaultType) {
		throw new ParsingError(this, "cannot get unnamed children from JSON object");
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByName(String name, String defaultType) {
		
		Object childList = this.data.get(name);
		if(childList == null || !(childList instanceof List<?>)) {
			return noChildren;
		}
		
		return transform(((List<?>)childList).iterator(), defaultType);
	}
	
	@Override
	public XydraElement getChild(String name) {
		return getElement(name);
	}
	
	@Override
	public Object getContent(String name) {
		return getAttribute(name);
	}
	
	protected static Iterator<Pair<String,XydraElement>> transformMap(
	        Iterator<Map.Entry<String,Object>> iterator, final String type) {
		return new AbstractTransformingIterator<Map.Entry<String,Object>,Pair<String,XydraElement>>(
		        iterator) {
			@Override
			public Pair<String,XydraElement> transform(Map.Entry<String,Object> in) {
				return new Pair<String,XydraElement>(in.getKey(), wrap(in.getValue(), type));
			}
		};
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(String attribute, String defaultType) {
		return transformMap(this.data.entrySet().iterator(), defaultType);
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	@Override
	public Object getValue(String name, int index) {
		
		Object value = this.data.get(name);
		if(value instanceof Map<?,?> || value instanceof List<?>) {
			throw new ParsingError(this, "expected value, got container");
		}
		
		return value;
	}
	
	@Override
	public Iterator<Object> getValues() {
		throw new ParsingError(this, "cannot get unnamed values from JSON object");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Object> getValues(String name) {
		
		Object childList = this.data.get(name);
		if(childList == null || !(childList instanceof List<?>)) {
			return noValue;
		}
		
		return ((List<Object>)childList).iterator();
	}
	
	@Override
	public Object getContent() {
		throw new ParsingError(this, "cannot get unnamed content from JSON object");
	}
	
}

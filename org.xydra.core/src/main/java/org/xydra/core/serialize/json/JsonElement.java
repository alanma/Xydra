package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonElement implements XydraElement {
	
	private static final Iterator<XydraElement> none = new NoneIterator<XydraElement>();
	private static final Iterator<Object> noValue = new NoneIterator<Object>();
	
	private final Map<String,Object> data;
	private final String type;
	
	public JsonElement(Map<String,Object> data, String type) {
		this.data = data;
		Object key = this.data.get("$type");
		if(type != null) {
			if(key != null && !key.toString().equals(type)) {
				throw new IllegalArgumentException("conflicting type: " + type + " vs. " + key);
			}
			this.type = type;
		} else {
			if(key == null) {
				throw new IllegalArgumentException("missing type");
			}
			this.type = key.toString();
		}
	}
	
	@Override
	public Object getAttribute(String name) {
		
		Object obj = this.data.get(name);
		if(obj == null || obj instanceof Map<?,?> || obj instanceof List<?>) {
			return null;
		}
		
		return obj;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XydraElement getElement(String type) {
		
		Object obj = this.data.get(type);
		if(obj instanceof Map<?,?>) {
			return new JsonElement((Map<String,Object>)obj, type);
		} else {
			return null;
		}
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String name) {
		return getChildren(name, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<XydraElement> getChildren(String name, String type) {
		
		Object obj = this.data.get(name);
		if(obj instanceof List<?>) {
			return transform(((List<?>)obj).iterator(), type);
		} else if(obj instanceof Map<?,?>) {
			return new SingleValueIterator<XydraElement>(new JsonElement((Map<String,Object>)obj,
			        type));
		} else if(obj == null && this.data.containsKey(name)) {
			return new SingleValueIterator<XydraElement>(null);
		} else {
			return none;
		}
	}
	
	@Override
	public XydraElement getChild(String name, int index) {
		return getChild(name, null);
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		return wrap(this.data.get(name), type);
	}
	
	protected static Iterator<XydraElement> transform(Iterator<?> iterator, final String type) {
		return new AbstractTransformingIterator<Object,XydraElement>(iterator) {
			@Override
			public XydraElement transform(Object in) {
				return wrap(in, type);
			}
		};
	}
	
	@Override
	public Object getContent(String name) {
		return getAttribute(name);
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Object> getValues(String name, String type) {
		
		Object obj = this.data.get(name);
		if(!this.data.containsKey(name) || !(obj instanceof List<?>)) {
			return noValue;
		} else {
			return ((List<Object>)obj).iterator();
		}
	}
	
	@Override
	public Iterator<Object> getValues(String name) {
		return getValues(name, null);
	}
	
	@Override
	public Object getValue(String name, String type) {
		return toValue(this.data.get(name));
	}
	
	private Object toValue(Object obj) {
		if(obj instanceof Map<?,?> || obj instanceof List<?>) {
			return null;
		} else {
			return obj;
		}
	}
	
	@Override
	public Object getValue(String name, int index) {
		return getValue(name, null);
	}
	
	@Override
	public String toString() {
		return this.type + ": " + this.data;
	}
	
	@Override
	public XydraElement getChild(String name) {
		return getChild(name, 0);
	}
	
	@SuppressWarnings("unchecked")
	private static XydraElement wrap(Object obj, String type) {
		
		if(obj instanceof List<?>) {
			return new JsonArray((List<Object>)obj, type);
		} else if(obj instanceof Map<?,?>) {
			return new JsonElement((Map<String,Object>)obj, type);
		} else {
			return null;
		}
	}
	
}

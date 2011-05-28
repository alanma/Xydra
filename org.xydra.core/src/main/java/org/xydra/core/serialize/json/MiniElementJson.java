package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.core.serialize.MiniElement;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;


public class MiniElementJson implements MiniElement {
	
	private static final Iterator<MiniElement> none = new NoneIterator<MiniElement>();
	private static final Iterator<Object> noValue = new NoneIterator<Object>();
	
	private final Map<String,Object> data;
	private final String type;
	
	public MiniElementJson(Map<String,Object> data) {
		this(data, null);
	}
	
	public MiniElementJson(Map<String,Object> data, String type) {
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
	public MiniElement getChild(String type) {
		
		Object obj = this.data.get(type);
		if(obj instanceof Map<?,?>) {
			return new MiniElementJson((Map<String,Object>)obj, type);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<MiniElement> getChildren(String name) {
		
		Object obj = this.data.get(name);
		if(obj instanceof List<?>) {
			return transform(((List<Object>)obj).iterator(), null);
		} else if(obj instanceof Map<?,?>) {
			return new SingleValueIterator<MiniElement>(
			        new MiniElementJson((Map<String,Object>)obj));
		} else {
			return none;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<MiniElement> getChildrenByType(String name, String type) {
		
		Object obj = this.data.get(name);
		if(obj instanceof List<?>) {
			return transform(((List<Object>)obj).iterator(), type);
		} else if(obj instanceof Map<?,?>) {
			return new SingleValueIterator<MiniElement>(new MiniElementJson(
			        (Map<String,Object>)obj, type));
		} else {
			return none;
		}
	}
	
	private Iterator<MiniElement> transform(Iterator<Object> iterator, final String type) {
		return new AbstractTransformingIterator<Object,MiniElement>(iterator) {
			@SuppressWarnings("unchecked")
			@Override
			public MiniElement transform(Object in) {
				
				if(!(in instanceof Map<?,?>)) {
					throw new IllegalArgumentException("bad element list");
				}
				
				return new MiniElementJson((Map<String,Object>)in, type);
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
		if(!this.data.containsKey(name) || obj instanceof Map<?,?>) {
			return noValue;
		} else if(obj instanceof List<?>) {
			return ((List<Object>)obj).iterator();
		} else {
			return new SingleValueIterator<Object>(obj);
		}
	}
	
	@Override
	public String toString() {
		return this.type + ": " + this.data;
	}
	
}

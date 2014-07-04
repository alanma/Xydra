package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xydra.core.serialize.AbstractXydraElement;
import org.xydra.core.serialize.XydraElement;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.Pair;


public abstract class AbstractJsonElement extends AbstractXydraElement {
	
	protected static Iterator<XydraElement> transform(Iterator<?> iterator, final String type) {
		return new AbstractTransformingIterator<Object,XydraElement>(iterator) {
			@Override
			public XydraElement transform(Object in) {
				return wrap(in, type);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	protected static XydraElement wrap(Object obj, String type) {
		
		if(obj == null) {
			return null;
		} else if(obj instanceof List<?>) {
			return new JsonArray((List<Object>)obj, type);
		} else if(obj instanceof Map<?,?>) {
			return new JsonElement((Map<String,Object>)obj, type);
		} else {
			return new JsonValue(obj, type);
		}
	}
	
	@Override
	public XydraElement getElement(String name) {
		return getChild(name, null);
	}
	
	@Override
	public XydraElement getElement(String name, int index) {
		return getChild(name, null);
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByType(String name, String type) {
		return getChildrenByName(name, type);
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntriesByType(String attribute, String type) {
		return getEntries(attribute, type);
	}
	
	@Override
	public Object getValue(String name, String type) {
		return getValue(name, 0);
	}
	
	@Override
	public Iterator<Object> getValues(String name, String type) {
		return getValues(name);
	}
	
}

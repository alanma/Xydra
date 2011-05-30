package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.ParsingError;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonArray implements XydraElement {
	
	private final List<Object> data;
	private final String type;
	
	public JsonArray(List<Object> data, String type) {
		this.data = data;
		this.type = type == null ? "xarray" : type;
	}
	
	@Override
	public Object getAttribute(String name) {
		throw new ParsingError(this, "Unexpected array.");
	}
	
	@Override
	public XydraElement getElement(String type) {
		return getChild(null, type);
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String name) {
		return getChildren(name, null);
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String name, String type) {
		return JsonElement.transform(this.data.iterator(), type);
	}
	
	@Override
	public XydraElement getChild(String name, int index) {
		return getChild(name, null);
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		throw new ParsingError(this, "Unexpected array.");
	}
	
	@Override
	public Object getContent(String name) {
		return getAttribute(name);
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	@Override
	public Iterator<Object> getValues(String name, String type) {
		return this.data.iterator();
	}
	
	@Override
	public Iterator<Object> getValues(String name) {
		return getValues(name, null);
	}
	
	@Override
	public Object getValue(String name, String type) {
		throw new ParsingError(this, "Unexpected array.");
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
	
}

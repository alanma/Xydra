package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.ParsingError;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.index.query.Pair;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonArray extends AbstractJsonElement {
	
	private final List<Object> data;
	private final String type;
	
	public JsonArray(List<Object> data, String type) {
		this.data = data;
		this.type = type == null ? XmlEncoder.XARRAY_ELEMENT : type;
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String defaultType) {
		return transform(this.data.iterator(), defaultType);
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	@Override
	public Iterator<Object> getValues() {
		return this.data.iterator();
	}
	
	@Override
	public String toString() {
		return this.type + ": " + this.data;
	}
	
	@Override
	public Object getAttribute(String name) {
		throw new ParsingError(this, "cannot get attribute from JSON array");
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		throw new ParsingError(this, "cannot get single child from JSON array");
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByName(String name, String defaultType) {
		throw new ParsingError(this, "cannot get named children from JSON array");
	}
	
	@Override
	public XydraElement getChild(String name) {
		throw new ParsingError(this, "cannot get container from JSON array");
	}
	
	@Override
	public Object getContent(String name) {
		throw new ParsingError(this, "cannot get content from JSON array");
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(String attribute, String defaultType) {
		throw new ParsingError(this, "cannot get entries from JSON array");
	}
	
	@Override
	public Object getValue(String name, int index) {
		throw new ParsingError(this, "cannot get attribute from JSON array");
	}
	
	@Override
	public Iterator<Object> getValues(String name) {
		throw new ParsingError(this, "cannot get named values from JSON array");
	}
	
}

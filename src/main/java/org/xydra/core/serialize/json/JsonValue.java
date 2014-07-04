package org.xydra.core.serialize.json;

import java.util.Iterator;

import org.xydra.annotations.NeverNull;
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
public class JsonValue extends AbstractJsonElement {
	
	private final String type;
	private final Object value;
	
	public JsonValue(Object value, String type) {
		this.type = type == null ? XmlEncoder.XVALUE_ELEMENT : type;
		this.value = value;
	}
	
	@Override
	public @NeverNull String getType() {
		return this.type;
	}
	
	@Override
	public Object getContent() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return this.type + ": " + this.value;
	}
	
	@Override
	public Object getContent(String name) {
		throw new ParsingError(this, "cannot get named content from JSON value");
	}
	
	@Override
	public Object getAttribute(String name) {
		throw new ParsingError(this, "cannot get attribute from JSON value");
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		throw new ParsingError(this, "cannot get child form JSON value");
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String defaultType) {
		throw new ParsingError(this, "cannot get children form JSON value");
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByName(String name, String defaultType) {
		throw new ParsingError(this, "cannot get children form JSON value");
	}
	
	@Override
	public XydraElement getChild(String name) {
		throw new ParsingError(this, "cannot get container form JSON value");
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(String attribute, String defaultType) {
		throw new ParsingError(this, "cannot get entries form JSON value");
	}
	
	@Override
	public Object getValue(String name, int index) {
		throw new ParsingError(this, "cannot get named value form JSON value");
	}
	
	@Override
	public Iterator<Object> getValues() {
		throw new ParsingError(this, "cannot get values form JSON value");
	}
	
	@Override
	public Iterator<Object> getValues(String name) {
		throw new ParsingError(this, "cannot get values form JSON value");
	}
	
}

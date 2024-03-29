package org.xydra.core.serialize.json;

import java.util.Iterator;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.ParsingException;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.index.query.Pair;

@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonValue extends AbstractJsonElement {

	private final String type;
	private final Object value;

	public JsonValue(final Object value, final String type) {
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
	public Object getContent(final String name) {
		throw new ParsingException(this, "cannot get named content from JSON value");
	}

	@Override
	public Object getAttribute(final String name) {
		throw new ParsingException(this, "cannot get attribute from JSON value");
	}

	@Override
	public Iterator<String> getAttributes() {
		throw new ParsingException(this, "cannot get attributes from JSON value");
	}

	@Override
	public XydraElement getChild(final String name, final String type) {
		throw new ParsingException(this, "cannot get child form JSON value");
	}

	@Override
	public Iterator<XydraElement> getChildren(final String defaultType) {
		throw new ParsingException(this, "cannot get children form JSON value");
	}

	@Override
	public Iterator<XydraElement> getChildrenByName(final String name, final String defaultType) {
		throw new ParsingException(this, "cannot get children form JSON value");
	}

	@Override
	public XydraElement getChild(final String name) {
		throw new ParsingException(this, "cannot get container form JSON value");
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntries(final String attribute, final String defaultType) {
		throw new ParsingException(this, "cannot get entries form JSON value");
	}

	@Override
	public Object getValue(final String name, final int index) {
		throw new ParsingException(this, "cannot get named value form JSON value");
	}

	@Override
	public Iterator<Object> getValues() {
		throw new ParsingException(this, "cannot get values form JSON value");
	}

	@Override
	public Iterator<Object> getValues(final String name) {
		throw new ParsingException(this, "cannot get values form JSON value");
	}

}

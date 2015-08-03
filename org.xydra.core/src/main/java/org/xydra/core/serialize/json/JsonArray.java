package org.xydra.core.serialize.json;

import java.util.Iterator;
import java.util.List;

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
public class JsonArray extends AbstractJsonElement {

	private final List<Object> data;
	private final String type;

	public JsonArray(final List<Object> data, final String type) {
		this.data = data;
		this.type = type == null ? XmlEncoder.XARRAY_ELEMENT : type;
	}

	@Override
	public Iterator<XydraElement> getChildren(final String defaultType) {
		return transform(this.data.iterator(), defaultType);
	}

	@Override
	public @NeverNull String getType() {
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
	public Object getAttribute(final String name) {
		throw new ParsingException(this, "cannot get attribute from JSON array");
	}

	@Override
	public Iterator<String> getAttributes() {
		throw new ParsingException(this, "cannot get attributes from JSON array");
	}

	@Override
	public XydraElement getChild(final String name, final String type) {
		throw new ParsingException(this, "cannot get single child from JSON array");
	}

	@Override
	public Iterator<XydraElement> getChildrenByName(final String name, final String defaultType) {
		throw new ParsingException(this, "cannot get named children from JSON array");
	}

	@Override
	public XydraElement getChild(final String name) {
		throw new ParsingException(this, "cannot get container from JSON array");
	}

	@Override
	public Object getContent(final String name) {
		throw new ParsingException(this, "cannot get content from JSON array");
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntries(final String attribute, final String defaultType) {
		throw new ParsingException(this, "cannot get entries from JSON array");
	}

	@Override
	public Object getValue(final String name, final int index) {
		throw new ParsingException(this, "cannot get attribute from JSON array");
	}

	@Override
	public Iterator<Object> getValues(final String name) {
		throw new ParsingException(this, "cannot get named values from JSON array");
	}

	@Override
	public Object getContent() {
		throw new ParsingException(this, "cannot get content from JSON array");
	}

}

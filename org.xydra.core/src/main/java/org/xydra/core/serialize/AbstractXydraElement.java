package org.xydra.core.serialize;

import java.util.Iterator;

import org.xydra.index.query.Pair;


public abstract class AbstractXydraElement implements XydraElement {

	@Override
	public XydraElement getElement(final String name) {
		return getElement(name, 0);
	}

	@Override
	public Iterator<XydraElement> getChildren() {
		return getChildren(null);
	}

	@Override
	public Iterator<XydraElement> getChildrenByName(final String name) {
		return getChildrenByName(name, null);
	}

	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(final String attribute) {
		return getEntries(attribute, null);
	}

}

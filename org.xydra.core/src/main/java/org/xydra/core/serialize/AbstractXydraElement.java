package org.xydra.core.serialize;

import java.util.Iterator;

import org.xydra.index.query.Pair;


public abstract class AbstractXydraElement implements XydraElement {
	
	@Override
	public XydraElement getElement(String name) {
		return getElement(name, 0);
	}
	
	@Override
	public Iterator<XydraElement> getChildren() {
		return getChildren(null);
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByName(String name) {
		return getChildrenByName(name, null);
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(String attribute) {
		return getEntries(attribute, null);
	}
	
}

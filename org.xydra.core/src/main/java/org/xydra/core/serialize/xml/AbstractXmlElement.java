package org.xydra.core.serialize.xml;

import java.util.Iterator;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.AbstractXydraElement;
import org.xydra.core.serialize.XydraElement;


/**
 * Common superclass for XML-based {@link XydraElement} implementations.
 *
 * @author dscharrer
 *
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public abstract class AbstractXmlElement extends AbstractXydraElement {

	@Override
	public Iterator<XydraElement> getChildrenByName(final String name, final String defaultType) {
		return getChildren(defaultType);
	}

	@Override
	public Iterator<Object> getValues(final String name) {
		return getValues();
	}

	@Override
	public XydraElement getElement(final String name) {
		return getElement(name, 0);
	}

	@Override
	public XydraElement getChild(final String name) {
		return this;
	}

	@Override
	public Object getContent(final String name) {
		return getContent();
	}

}

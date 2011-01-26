package org.xydra.core.xml.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.core.xml.MiniElement;


/**
 * {@link MiniElement} implementation that wraps an org.w3c.dom.Element.
 * 
 * @author dscharrer
 * 
 */
@RequiresAppEngine(false)
public class MiniElementImpl implements MiniElement {
	
	Element element;
	
	protected MiniElementImpl(Element element) {
		this.element = element;
	}
	
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName))
			return this.element.getAttribute(attributeName);
		else
			return null;
	}
	
	public String getData() {
		return this.element.getTextContent();
	}
	
	public Iterator<MiniElement> getElements() {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToIterator(nodes);
	}
	
	public Iterator<MiniElement> getElementsByTagName(String elementName) {
		final NodeList nodes = this.element.getElementsByTagName(elementName);
		return nodeListToIterator(nodes);
	}
	
	public String getName() {
		return this.element.getNodeName();
	}
	
	private Iterator<MiniElement> nodeListToIterator(NodeList nodes) {
		List<MiniElement> list = new ArrayList<MiniElement>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element)
				list.add(new MiniElementImpl((Element)node));
		}
		return list.iterator();
	}
	
}

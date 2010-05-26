package org.xydra.client.gwt.xml.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.xml.MiniElement;

import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;



/**
 * {@link MiniElement} implementation that wraps an
 * com.google.gwt.xml.client.Element.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT
public class GWTMiniElementImpl implements MiniElement {
	
	Element element;
	
	protected GWTMiniElementImpl(Element element) {
		this.element = element;
	}
	
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName))
			return this.element.getAttribute(attributeName);
		else
			return null;
	}
	
	public String getData() {
		StringBuffer sb = new StringBuffer();
		final NodeList nodes = this.element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof CharacterData)
				sb.append(((CharacterData)node).getData());
		}
		return sb.toString();
	}
	
	private Iterator<MiniElement> nodeListToIterator(NodeList nodes) {
		List<MiniElement> list = new ArrayList<MiniElement>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element)
				list.add(new GWTMiniElementImpl((Element)node));
		}
		return list.iterator();
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
	
}

package org.xydra.core.serialize.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.core.serialize.MiniElement;


/**
 * {@link MiniElement} implementation that wraps an org.w3c.dom.Element.
 * 
 * @author dscharrer
 * 
 */
@RequiresAppEngine(false)
public class MiniElementXml implements MiniElement {
	
	Element element;
	
	protected MiniElementXml(Element element) {
		this.element = element;
	}
	
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
	}
	
	private Iterator<MiniElement> nodeListToIterator(NodeList nodes) {
		List<MiniElement> list = new ArrayList<MiniElement>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				list.add(new MiniElementXml((Element)node));
			}
		}
		return list.iterator();
	}
	
	@Override
	public Iterator<MiniElement> getChildren(String name) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToIterator(nodes);
	}
	
	@Override
	public Iterator<MiniElement> getChildrenByType(String name, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToIterator(nodes);
	}
	
	@Override
	public String getContent(String name) {
		return this.element.getTextContent();
	}
	
	@Override
	public MiniElement getChild(String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				return new MiniElementXml((Element)node);
			}
		}
		return null;
	}
	
	@Override
	public String getType() {
		return this.element.getNodeName();
	}
	
	@Override
	public Iterator<Object> getValues(String name, String type) {
		List<Object> objects = new ArrayList<Object>();
		final NodeList nodes = this.element.getElementsByTagName(type);
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				if(((Element)node).hasAttribute("isNull")) {
					objects.add(null);
				} else {
					objects.add(node.getTextContent());
				}
			}
		}
		return objects.iterator();
	}
	
}

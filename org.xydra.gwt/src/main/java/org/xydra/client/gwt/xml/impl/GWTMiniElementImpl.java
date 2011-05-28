package org.xydra.client.gwt.xml.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.MiniElement;

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
@RunsInGWT(true)
public class GWTMiniElementImpl implements MiniElement {
	
	Element element;
	
	protected GWTMiniElementImpl(Element element) {
		this.element = element;
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
	
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
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
		return getTextContent(this.element);
	}
	
	private String getTextContent(Node element) {
		StringBuffer sb = new StringBuffer();
		final NodeList nodes = element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof CharacterData)
				sb.append(((CharacterData)node).getData());
		}
		return sb.toString();
	}
	
	@Override
	public MiniElement getChild(String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				return new GWTMiniElementImpl((Element)node);
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
					objects.add(getTextContent(node));
				}
			}
		}
		return objects.iterator();
	}
	
}

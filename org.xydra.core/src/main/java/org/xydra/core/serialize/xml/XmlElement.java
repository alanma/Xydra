package org.xydra.core.serialize.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;


/**
 * {@link XydraElement} implementation that wraps an org.w3c.dom.Element.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(false)
@RequiresAppEngine(false)
public class XmlElement implements XydraElement {
	
	private static final String ELEMENT_XNULL = "xnull";
	private static final String ATTRIBUTE_IS_NULL = "isNull";
	private static final String ATTRIBUTE_NULL_CONTENT = "nullContent";
	
	Element element;
	
	protected XmlElement(Element element) {
		this.element = element;
	}
	
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
	}
	
	private Iterator<XydraElement> nodeListToIterator(NodeList nodes) {
		List<XydraElement> list = new ArrayList<XydraElement>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				list.add(wrap((Element)node));
			}
		}
		return list.iterator();
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String name) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToIterator(nodes);
	}
	
	@Override
	public Iterator<XydraElement> getChildren(String name, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToIterator(nodes);
	}
	
	@Override
	public String getContent(String name) {
		if(this.element.hasAttribute(ATTRIBUTE_NULL_CONTENT)
		        && Boolean.valueOf(this.element.getAttribute(ATTRIBUTE_NULL_CONTENT))) {
			return null;
		}
		return this.element.getTextContent();
	}
	
	@Override
	public XydraElement getChild(String name, int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				if(idx == index) {
					return new XmlElement((Element)node);
				} else {
					idx++;
				}
			}
		}
		return null;
	}
	
	@Override
	public XydraElement getChild(String name, String type) {
		return getElement(type);
	}
	
	@Override
	public XydraElement getElement(String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				return new XmlElement((Element)node);
			}
		}
		return null;
	}
	
	@Override
	public Object getValue(String name, int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				if(idx == index) {
					return getValue((Element)node);
				} else {
					idx++;
				}
			}
		}
		return null;
	}
	
	@Override
	public Object getValue(String name, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				return getValue((Element)node);
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
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToValues(nodes);
	}
	
	@Override
	public Iterator<Object> getValues(String name) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToValues(nodes);
	}
	
	private Iterator<Object> nodeListToValues(final NodeList nodes) {
		
		List<Object> objects = new ArrayList<Object>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element) {
				objects.add(getValue((Element)node));
			}
		}
		
		return objects.iterator();
	}
	
	static XmlElement wrap(Element element) {
		return isNull(element) ? null : new XmlElement(element);
	}
	
	static String getValue(Element element) {
		return isNull(element) ? null : element.getTextContent();
	}
	
	private static boolean isNull(Element element) {
		return (ELEMENT_XNULL.equals(element.getNodeName()) || element
		        .hasAttribute(ATTRIBUTE_IS_NULL)
		        && Boolean.valueOf(element.getAttribute(ATTRIBUTE_IS_NULL)));
	}
	
	@Override
	public String toString() {
		return this.element.toString();
	}
	
	@Override
	public XydraElement getChild(String name) {
		return getChild(name, 0);
	}
	
}
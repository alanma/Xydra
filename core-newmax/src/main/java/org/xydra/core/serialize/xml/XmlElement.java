package org.xydra.core.serialize.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.index.query.Pair;


/**
 * {@link XydraElement} implementation that wraps an {@link org.w3c.dom.Element}
 * .
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(false)
@RequiresAppEngine(false)
public class XmlElement extends AbstractXmlElement {
	
	private final Element element;
	
	protected XmlElement(Element element) {
		this.element = element;
	}
	
	@Override
	public String getAttribute(String attributeName) {
		if(this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
	}
	
	private static Iterator<XydraElement> nodeListToIterator(NodeList nodes) {
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
	public Iterator<XydraElement> getChildren(String defaultType) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToIterator(nodes);
	}
	
	@Override
	public Iterator<XydraElement> getChildrenByType(String name, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToIterator(nodes);
	}
	
	@Override
	public String getContent() {
		if(this.element.hasAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE)
		        && Boolean.valueOf(this.element.getAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE))) {
			return null;
		}
		return this.element.getTextContent();
	}
	
	@Override
	public XydraElement getElement(String name, int index) {
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
	public @NeverNull String getType() {
		return this.element.getNodeName();
	}
	
	@Override
	public Iterator<Object> getValues(String name, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToValues(nodes);
	}
	
	@Override
	public Iterator<Object> getValues() {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToValues(nodes);
	}
	
	private static Iterator<Object> nodeListToValues(final NodeList nodes) {
		
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
		return (XmlEncoder.XNULL_ELEMENT.equals(element.getNodeName()) || (element
		        .hasAttribute(XmlEncoder.NULL_ATTRIBUTE) && Boolean.valueOf(element
		        .getAttribute(XmlEncoder.NULL_ATTRIBUTE))));
	}
	
	@Override
	public String toString() {
		return this.element.toString();
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntries(String attribute, String defaultType) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToMapIterator(nodes, attribute);
	}
	
	@Override
	public Iterator<Pair<String,XydraElement>> getEntriesByType(String attribute, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToMapIterator(nodes, attribute);
	}
	
	private static Iterator<Pair<String,XydraElement>> nodeListToMapIterator(NodeList nodes,
	        String attribute) {
		List<Pair<String,XydraElement>> list = new ArrayList<Pair<String,XydraElement>>();
		for(int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if(node instanceof Element && ((Element)node).hasAttribute(attribute)) {
				Element element = (Element)node;
				
				list.add(new Pair<String,XydraElement>(element.getAttribute(attribute),
				        wrap(element)));
			}
		}
		return list.iterator();
	}
	
}

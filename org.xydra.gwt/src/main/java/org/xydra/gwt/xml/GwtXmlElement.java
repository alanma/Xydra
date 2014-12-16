package org.xydra.gwt.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.AbstractXmlElement;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Pair;

import com.google.gwt.xml.client.CharacterData;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

/**
 * {@link XydraElement} implementation that wraps an
 * {@link com.google.gwt.xml.client.Element}.
 * 
 * This is an exact character-for-character copy of XmlElement, with different
 * import statements. It uses XML classes from the com.google.gwt.xml.client
 * package instead of the standard java XML classes, which are not available in
 * GWT.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
public class GwtXmlElement extends AbstractXmlElement {

	private static final ITransformer<Node, String> NODE2NODENAME = new ITransformer<Node, String>() {

		@Override
		public String transform(Node in) {
			return in.getNodeName();
		}

	};

	private final Element element;

	protected GwtXmlElement(Element element) {
		this.element = element;
	}

	@Override
	public String getAttribute(String attributeName) {
		if (this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
	}

	@Override
	public Iterator<String> getAttributes() {
		Iterator<Node> nodeIt = new NamedNodeMap2NodeIterator(this.element.getAttributes());
		return Iterators.transform(nodeIt, NODE2NODENAME);
	}

	static class NamedNodeMap2NodeIterator implements Iterator<Node> {

		private NamedNodeMap namedNodeMap;

		public NamedNodeMap2NodeIterator(NamedNodeMap namedNodeMap) {
			this.namedNodeMap = namedNodeMap;
		}

		int nextPos = 0;

		@Override
		public boolean hasNext() {
			return this.nextPos < this.namedNodeMap.getLength();
		}

		@Override
		public Node next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			Node node = this.namedNodeMap.item(this.nextPos);
			this.nextPos++;
			return node;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static Iterator<XydraElement> nodeListToIterator(NodeList nodes) {
		List<XydraElement> list = new ArrayList<XydraElement>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				list.add(wrap((Element) node));
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
		if (this.element.hasAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE)
				&& Boolean.valueOf(this.element.getAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE))) {
			return null;
		}
		return getTextContent(this.element);
	}

	@Override
	public XydraElement getElement(String name, int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				if (idx == index) {
					return new GwtXmlElement((Element) node);
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
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				return new GwtXmlElement((Element) node);
			}
		}
		return null;
	}

	@Override
	public Object getValue(String name, int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				if (idx == index) {
					return getValue((Element) node);
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
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				return getValue((Element) node);
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
	public Iterator<Object> getValues() {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToValues(nodes);
	}

	private static Iterator<Object> nodeListToValues(final NodeList nodes) {

		List<Object> objects = new ArrayList<Object>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				objects.add(getValue((Element) node));
			}
		}

		return objects.iterator();
	}

	static GwtXmlElement wrap(Element element) {
		return isNull(element) ? null : new GwtXmlElement(element);
	}

	static String getValue(Element element) {
		return isNull(element) ? null : getTextContent(element);
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
	public Iterator<Pair<String, XydraElement>> getEntries(String attribute, String defaultType) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToMapIterator(nodes, attribute);
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntriesByType(String attribute, String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToMapIterator(nodes, attribute);
	}

	private static Iterator<Pair<String, XydraElement>> nodeListToMapIterator(NodeList nodes,
			String attribute) {
		List<Pair<String, XydraElement>> list = new ArrayList<Pair<String, XydraElement>>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof Element && ((Element) node).hasAttribute(attribute)) {
				Element element = (Element) node;

				list.add(new Pair<String, XydraElement>(element.getAttribute(attribute),
						wrap(element)));
			}
		}
		return list.iterator();
	}

	private static String getTextContent(Node element) {
		StringBuffer sb = new StringBuffer();
		final NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			if (node instanceof CharacterData)
				sb.append(((CharacterData) node).getData());
		}
		return sb.toString();
	}

}

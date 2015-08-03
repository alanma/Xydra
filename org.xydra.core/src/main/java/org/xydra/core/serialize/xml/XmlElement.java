package org.xydra.core.serialize.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.Iterators;
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

	private static final ITransformer<Node, String> NODE2NODENAME = new ITransformer<Node, String>() {

		@Override
		public String transform(final Node in) {
			return in.getNodeName();
		}

	};

	private final Element element;

	protected XmlElement(final Element element) {
		this.element = element;
	}

	@Override
	public String getAttribute(final String attributeName) {
		if (this.element.hasAttribute(attributeName)) {
			return this.element.getAttribute(attributeName);
		} else {
			return null;
		}
	}

	private static Iterator<XydraElement> nodeListToIterator(final NodeList nodes) {
		final List<XydraElement> list = new ArrayList<XydraElement>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				list.add(wrap((Element) node));
			}
		}
		return list.iterator();
	}

	@Override
	public Iterator<XydraElement> getChildren(final String defaultType) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToIterator(nodes);
	}

	@Override
	public Iterator<XydraElement> getChildrenByType(final String name, final String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToIterator(nodes);
	}

	@Override
	public String getContent() {
		if (this.element.hasAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE)
				&& Boolean.valueOf(this.element.getAttribute(XmlEncoder.NULL_CONTENT_ATTRIBUTE))) {
			return null;
		}

		// return the first text() nodes content
		final NodeList nodeList = this.element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node child = nodeList.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getNodeValue();
			}
		}

		return "";
		// was: return this.element.getNodeName()getTextContent();
	}

	@Override
	public XydraElement getElement(final String name, final int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				if (idx == index) {
					return new XmlElement((Element) node);
				} else {
					idx++;
				}
			}
		}
		return null;
	}

	@Override
	public XydraElement getChild(final String name, final String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				return new XmlElement((Element) node);
			}
		}
		return null;
	}

	@Override
	public Object getValue(final String name, final int index) {
		int idx = 0;
		final NodeList nodes = this.element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
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
	public Object getValue(final String name, final String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				return getValue((Element) node);
			}
		}
		return null;
	}

	@Override
	public @NeverNull String getType() {
		return this.element.getNodeName();
	}

	@Override
	public Iterator<Object> getValues(final String name, final String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToValues(nodes);
	}

	@Override
	public Iterator<Object> getValues() {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToValues(nodes);
	}

	private static Iterator<Object> nodeListToValues(final NodeList nodes) {

		final List<Object> objects = new ArrayList<Object>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element) {
				objects.add(getValue((Element) node));
			}
		}

		return objects.iterator();
	}

	static XmlElement wrap(final Element element) {
		return isNull(element) ? null : new XmlElement(element);
	}

	static String getValue(final Element element) {
		return isNull(element) ? null : element.getTextContent();
	}

	private static boolean isNull(final Element element) {
		return XmlEncoder.XNULL_ELEMENT.equals(element.getNodeName()) || element
				.hasAttribute(XmlEncoder.NULL_ATTRIBUTE) && Boolean.valueOf(element
				.getAttribute(XmlEncoder.NULL_ATTRIBUTE));
	}

	@Override
	public String toString() {
		String returnString = "";

		final Element element = this.element;
		returnString = examineNode(returnString, element);

		return returnString;

	}

	public static String examineNode(final String returnString, final Node element) {
		String returnString2 = returnString;
		returnString2 += "tagName: " + element.getNodeName();
		returnString2 += "\nattributes: ";
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			returnString2 += ", " + attributes.item(i);
		}
		final NodeList childNodes = element.getChildNodes();
		returnString2 += "\n\nnodes: " + childNodes.getLength();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node childNode = childNodes.item(i);
			returnString2 += "\n nodeName: " + childNode.getNodeName() + ", value: "
					+ childNode.getNodeValue();
			// somehow doesn't work...
			// returnString2 += examineNode(returnString2, childNode);

			returnString2 += "\n.attributes: ";

			attributes = childNode.getAttributes();
			if (attributes != null) {
				for (int j = 0; j < attributes.getLength(); j++) {
					returnString2 += ", " + attributes.item(j);
				}

			}
			returnString2 += "\n";
		}
		return returnString2;
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntries(final String attribute, final String defaultType) {
		final NodeList nodes = this.element.getChildNodes();
		return nodeListToMapIterator(nodes, attribute);
	}

	@Override
	public Iterator<Pair<String, XydraElement>> getEntriesByType(final String attribute, final String type) {
		final NodeList nodes = this.element.getElementsByTagName(type);
		return nodeListToMapIterator(nodes, attribute);
	}

	private static Iterator<Pair<String, XydraElement>> nodeListToMapIterator(final NodeList nodes,
			final String attribute) {
		final List<Pair<String, XydraElement>> list = new ArrayList<Pair<String, XydraElement>>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);
			if (node instanceof Element && ((Element) node).hasAttribute(attribute)) {
				final Element element = (Element) node;

				list.add(new Pair<String, XydraElement>(element.getAttribute(attribute),
						wrap(element)));
			}
		}
		return list.iterator();
	}

	@Override
	public Iterator<String> getAttributes() {
		final Iterator<Node> nodeIt = new NamedNodeMap2NodeIterator(this.element.getAttributes());
		return Iterators.transform(nodeIt, NODE2NODENAME);
	}

	static class NamedNodeMap2NodeIterator implements Iterator<Node> {

		private final NamedNodeMap namedNodeMap;

		public NamedNodeMap2NodeIterator(final NamedNodeMap namedNodeMap) {
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

			final Node node = this.namedNodeMap.item(this.nextPos);
			this.nextPos++;
			return node;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}

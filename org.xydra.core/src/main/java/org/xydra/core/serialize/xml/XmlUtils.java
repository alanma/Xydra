package org.xydra.core.serialize.xml;

import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;

@RunsInGWT(true)
public class XmlUtils {

	/**
	 * @param xydraElement
	 * @return an XML string
	 */
	public static String toString(XydraElement xydraElement) {
		StringBuilder b = new StringBuilder();

		toStringOpen(xydraElement, b);

		Iterator<XydraElement> children = xydraElement.getChildren();
		while (children.hasNext()) {
			XydraElement child = children.next();
			b.append(toString(child));
		}

		Object content = xydraElement.getContent();
		if (content != null)
			b.append(content);

		toStringClose(xydraElement, b);

		return b.toString();
	}

	private static void toStringClose(XydraElement xydraElement, StringBuilder b) {
		String type = xydraElement.getType();
		b.append("</");
		b.append(type);
		b.append(">");
	}

	private static void toStringOpen(XydraElement xydraElement, StringBuilder b) {
		b.append("<");
		String type = xydraElement.getType();
		b.append(type);
		Iterator<String> attNames = xydraElement.getAttributes();
		while (attNames.hasNext()) {
			String attName = attNames.next();
			Object attValue = xydraElement.getAttribute(attName);
			b.append(" ");
			b.append(attName);
			b.append("='");
			b.append(attValue);
			b.append("'");
		}
		b.append(">");
	}

}

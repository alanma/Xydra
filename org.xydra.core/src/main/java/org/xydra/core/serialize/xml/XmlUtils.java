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
	public static String toString(final XydraElement xydraElement) {
		final StringBuilder b = new StringBuilder();

		toStringOpen(xydraElement, b);

		final Iterator<XydraElement> children = xydraElement.getChildren();
		while (children.hasNext()) {
			final XydraElement child = children.next();
			b.append(toString(child));
		}

		final Object content = xydraElement.getContent();
		if (content != null) {
			b.append(content);
		}

		toStringClose(xydraElement, b);

		return b.toString();
	}

	private static void toStringClose(final XydraElement xydraElement, final StringBuilder b) {
		final String type = xydraElement.getType();
		b.append("</");
		b.append(type);
		b.append(">");
	}

	private static void toStringOpen(final XydraElement xydraElement, final StringBuilder b) {
		b.append("<");
		final String type = xydraElement.getType();
		b.append(type);
		final Iterator<String> attNames = xydraElement.getAttributes();
		while (attNames.hasNext()) {
			final String attName = attNames.next();
			final Object attValue = xydraElement.getAttribute(attName);
			b.append(" ");
			b.append(attName);
			b.append("='");
			b.append(attValue);
			b.append("'");
		}
		b.append(">");
	}

}

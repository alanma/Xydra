package org.xydra.core.serialize.xml;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.serialize.AbstractXydraOut;
import org.xydra.sharedutils.XyAssert;

/**
 * See {@link AbstractXydraOut#Type} = Array, Child, Element, Entry, Map, Root,
 * Text
 *
 * @author xamde
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlOut extends AbstractXydraOut {

	public static final String CONTENT_TYPE_XML = "application/xml";

	public XmlOut(final MiniWriter writer) {
		this(writer, true);
	}

	public XmlOut() {
		this(true);
	}

	public XmlOut(final MiniWriter writer, final boolean writeHeader) {
		super(writer);
		init(writeHeader);
	}

	public XmlOut(final boolean writeHeader) {
		super();
		init(writeHeader);
	}

	private void init(final boolean writeHeader) {
		if (!writeHeader) {
			return;
		}

		write(XmlEncoder.XML_DECLARATION);
		write('\n');
	}

	@Override
	protected <T> void outputAttribute(final Frame element, final String name, final T value) {
		write(" ");
		write(name);
		write("=\"");
		write(XmlEncoder.encode(value.toString()));
		write("\"");
	}

	@Override
	protected void outputChild(final Frame child) {
		child.depth = child.parent.depth;
	}

	Frame getElement(final Frame frame) {

		Frame element = frame;

		switch (element.type) {
		case Child:
			return element.parent;
		case Element:
			return element;
		case Entry:
			element = element.parent;
			//$FALL-THROUGH$
		case Array:
		case Map:
			if (element.parent.type == Type.Child && !element.parent.hasChildType()) {
				XyAssert.xyAssert(element.parent.parent.type == Type.Element);
				return element.parent.parent;
			} else {
				return element;
			}
		case Root:
			return element;
		case Text:
			XyAssert.xyAssert(false);
		}

		return null;
	}

	private void begin(final Frame frame) {

		final Frame element = getElement(frame);

		if (element.type != Type.Root) {
			if (!element.hasContent) {
				write('>');
				element.hasContent = true;
			}
			write('\n');
			indent(element.depth + 1);
		}
	}

	@Override
	protected void outputCloseElement(final Frame element) {

		if (element.hasContent) {
			if (!element.hasSpecialContent) {
				write('\n');
				indent(element.depth);
			}
			write("</");
			write(element.name);
			write(">");
		} else {
			write("/>");
		}

	}

	@Override
	protected <T> void outputValue(final Frame container, final T value) {

		if (isInlined(container)) {

			XyAssert.xyAssert(!container.parent.hasContent);

			if (value == null) {
				outputAttribute(container, XmlEncoder.NULL_CONTENT_ATTRIBUTE,
						XmlEncoder.NULL_CONTENT_VALUE);
			} else {
				final String valueStr = value.toString();
				if (!valueStr.isEmpty()) {
					write('>');
					write(XmlEncoder.encode(valueStr));
					container.parent.hasContent = true;
					container.parent.hasSpecialContent = true;
				}
			}
		} else if (value == null) {
			outputNullElement(container);
		} else {

			begin(container);

			final String type = container.getChildType(XmlEncoder.XVALUE_ELEMENT);

			write('<');
			write(type);
			outputId(container);
			final String valueStr = value.toString();
			if (valueStr.isEmpty()) {
				write("/>");
			} else {
				write('>');
				write(XmlEncoder.encode(valueStr));
				write("</");
				write(type);
				write(">");
			}

		}

	}

	private static boolean isInlined(final Frame container) {
		return container.type == Type.Child && !container.hasChildType();
	}

	@Override
	protected void outputNullElement(final Frame container) {

		begin(container);

		write('<');
		if (!container.isChildTypeForced()) {
			write(XmlEncoder.XNULL_ELEMENT);
			outputId(container);
		} else {
			write(container.getChildType());
			outputId(container);
			outputAttribute(null, XmlEncoder.NULL_ATTRIBUTE, XmlEncoder.NULL_VALUE);
		}
		write("/>");

	}

	@Override
	protected void outputOpenElement(final Frame element) {

		begin(element.parent);

		element.depth = element.parent.depth + 1;

		write('<');
		write(element.name);
		outputId(element.parent);
	}

	@Override
	protected void outputBeginArray(final Frame array) {
		beginContainer(array, XmlEncoder.XARRAY_ELEMENT);
	}

	@Override
	protected void outputEndArray(final Frame array) {
		endContainer(array, XmlEncoder.XARRAY_ELEMENT);
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE_XML;
	}

	@Override
	protected void outpuEnd() {
		write('\n');
	}

	@Override
	protected void outputBeginMap(final Frame map) {
		beginContainer(map, XmlEncoder.XMAP_ELEMENT);
	}

	private void beginContainer(final Frame container, final String type) {
		if (isInlined(container.parent)) {

			XyAssert.xyAssert(!container.parent.parent.hasContent);
			container.depth = container.parent.depth;

		} else {
			begin(container.parent);

			write('<');
			write(container.parent.getChildType(type));
			outputId(container.parent);

			container.depth = container.parent.depth + 1;
			indent(container.depth);
		}
	}

	@Override
	protected void outputEndMap(final Frame map) {
		endContainer(map, XmlEncoder.XMAP_ELEMENT);
	}

	private void endContainer(final Frame container, final String type) {

		if (isInlined(container.parent)) {
			// do nothing
		} else {

			if (!container.hasContent) {
				write("/>");
			} else {
				write('\n');
				indent(container.depth);
				write("</");
				write(container.parent.getChildType(type));
				write('>');
			}

		}
	}

	@Override
	protected void outputEntry(final Frame entry) {
		entry.depth = entry.parent.depth;
	}

	private void outputId(final Frame container) {
		if (container.type == Type.Entry) {
			outputAttribute(null, container.parent.name, container.name);
		}

	}

}

package org.xydra.gwt.xml;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniReader;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraParser;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * {@link XydraParser} implementation that uses the GWT browser-supported
 * XMLParser
 *
 * @author dscharrer
 *
 */
@RunsInGWT(true)
public class GwtXmlParser implements XydraParser {

	@Override
	public XydraElement parse(final String xml) throws IllegalArgumentException {

		Document document;
		try {
			document = XMLParser.parse(xml);
		} catch (final Exception e) {
			throw new IllegalArgumentException();
		}

		return new GwtXmlElement(document.getDocumentElement());
	}

	@Override
	public String getContentType() {
		return "application/xml";
	}

	static final int DEFAULT_BLOCK_SIZE = 4 * 1024;

	/**
	 * @param miniReader
	 * @param blockSize e.g. 4KB = 4*1024
	 * @return
	 */
	public static String toString(final MiniReader miniReader, final int blockSize) {
		final StringBuffer buf = new StringBuffer();

		final int offset = 0;
		final char[] cbuf = new char[blockSize];
		int read = 0;
		do {
			read = miniReader.read(cbuf, offset, blockSize);
			buf.append(cbuf, 0, read);
		} while (read > 0);

		return buf.toString();
	}

	public static String toString(final MiniReader miniReader) {
		return toString(miniReader, DEFAULT_BLOCK_SIZE);
	}

	@Override
	public XydraElement parse(final MiniReader miniReader) throws IllegalArgumentException {
		final String data = toString(miniReader);
		return parse(data);
	}

}

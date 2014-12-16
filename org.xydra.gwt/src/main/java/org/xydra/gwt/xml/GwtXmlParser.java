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
	public XydraElement parse(String xml) throws IllegalArgumentException {

		Document document;
		try {
			document = XMLParser.parse(xml);
		} catch (Exception e) {
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
	public static String toString(MiniReader miniReader, int blockSize) {
		StringBuffer buf = new StringBuffer();

		int offset = 0;
		char[] cbuf = new char[blockSize];
		int read = 0;
		do {
			read = miniReader.read(cbuf, offset, blockSize);
			buf.append(cbuf, 0, read);
		} while (read > 0);

		return buf.toString();
	}

	public static String toString(MiniReader miniReader) {
		return toString(miniReader, DEFAULT_BLOCK_SIZE);
	}

	@Override
	public XydraElement parse(MiniReader miniReader) throws IllegalArgumentException {
		String data = toString(miniReader);
		return parse(data);
	}

}

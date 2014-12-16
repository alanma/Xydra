package org.xydra.core.serialize.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xydra.base.minio.MiniReader;
import org.xydra.base.minio.MiniReaderToReader;
import org.xydra.core.serialize.XydraStreamParser;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class XmlStreamParser implements XydraStreamParser {

	@Override
	public boolean parse(MiniReader miniReader, XmlOut xmlOut) throws IllegalArgumentException {
		MiniReaderToReader reader = new MiniReaderToReader(miniReader);
		InputSource is = new InputSource(reader);

		XmlOutHandler handler = new XmlOutHandler(xmlOut);
		try {
			getParser().parse(is, handler);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return true;
	}

	private static final Logger log = LoggerFactory.getLogger(XmlStreamParser.class);

	static class XmlOutHandler extends DefaultHandler implements EntityResolver, DTDHandler,
			ContentHandler, ErrorHandler {
		public XmlOutHandler(XmlOut xout) {
			this.xout = xout;
		}

		private XmlOut xout;

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			log.warn("xml", exception);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			log.warn("xml", exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			log.error("xml", exception);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts)
				throws SAXException {
			maybeEmitCharacters();
			this.xout.open(qName);
			for (int i = 0; i < atts.getLength(); i++) {
				String name = atts.getQName(i);
				String value = atts.getValue(i);
				this.xout.attribute(name, value);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			maybeEmitCharacters();
			this.xout.close(qName);
		}

		private StringBuilder charBuffer = null;

		private void maybeEmitCharacters() {
			if (this.charBuffer != null) {
				this.xout.value(this.charBuffer);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (this.charBuffer == null) {
				this.charBuffer = new StringBuilder();
			}
			this.charBuffer.append(ch, start, length);
		}

	}

	private static SAXParser parser = null;

	private synchronized static SAXParser getParser() throws ParserConfigurationException,
			SAXException {
		if (parser == null) {
			parser = SAXParserFactory.newInstance().newSAXParser();
		}
		return parser;
	}

	@Override
	public String getContentType() {
		return "application/xml";
	}

}

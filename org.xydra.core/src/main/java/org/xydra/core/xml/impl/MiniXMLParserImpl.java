package org.xydra.core.xml.impl;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;


/**
 * {@link MiniXMLParser} implementation that uses a standard java XML parser.
 * 
 * @author dscharrer
 * 
 */
@RequiresAppEngine(false)
public class MiniXMLParserImpl implements MiniXMLParser {
	
	private static DocumentBuilder parser = null;
	
	private synchronized DocumentBuilder getParser() throws ParserConfigurationException {
		if(parser == null) {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		return parser;
	}
	
	public synchronized MiniElement parseXml(String string) {
		InputSource is = new InputSource(new StringReader(string));
		Document document;
		try {
			document = getParser().parse(is);
		} catch(Exception e) {
			throw new IllegalArgumentException(e);
		}
		return new MiniElementImpl(document.getDocumentElement());
	}
	
}

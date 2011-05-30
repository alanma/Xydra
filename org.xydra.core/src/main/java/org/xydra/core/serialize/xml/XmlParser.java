package org.xydra.core.serialize.xml;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraParser;


/**
 * {@link XydraParser} implementation that uses a standard java XML parser.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(false)
@RequiresAppEngine(false)
public class XmlParser implements XydraParser {
	
	private static DocumentBuilder parser = null;
	
	private synchronized DocumentBuilder getParser() throws ParserConfigurationException {
		if(parser == null) {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		return parser;
	}
	
	public synchronized XydraElement parse(String string) {
		InputSource is = new InputSource(new StringReader(string));
		Document document;
		try {
			document = getParser().parse(is);
		} catch(Exception e) {
			throw new IllegalArgumentException(e);
		}
		return XmlElement.wrap(document.getDocumentElement());
	}
	
}

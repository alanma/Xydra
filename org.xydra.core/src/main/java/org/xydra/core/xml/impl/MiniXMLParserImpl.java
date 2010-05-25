package org.xydra.core.xml.impl;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;



/**
 * {@link MiniXMLParser} implementation that uses a standard java XML parser.
 * 
 * @author dscharrer
 * 
 */
@RunsInJava
public class MiniXMLParserImpl implements MiniXMLParser {
	
	public MiniElement parseXml(String string) {
		InputSource is = new InputSource(new StringReader(string));
		Document document;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch(Exception e) {
			throw new IllegalArgumentException(e);
		}
		return new MiniElementImpl(document.getDocumentElement());
	}
	
}

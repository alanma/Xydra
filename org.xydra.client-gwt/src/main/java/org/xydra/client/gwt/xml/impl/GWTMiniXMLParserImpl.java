package org.xydra.client.gwt.xml.impl;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;



/**
 * {@link MiniXMLParser} implementation that uses the GWT browser-supported
 * XMLParser
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT
public class GWTMiniXMLParserImpl implements MiniXMLParser {
	
	public MiniElement parseXml(String xml) throws IllegalArgumentException {
		
		Document document;
		try {
			document = XMLParser.parse(xml);
		} catch(Exception e) {
			throw new IllegalArgumentException();
		}
		
		return new GWTMiniElementImpl(document.getDocumentElement());
	}
	
}

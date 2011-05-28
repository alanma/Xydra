package org.xydra.client.gwt.xml.impl;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;



/**
 * {@link MiniParser} implementation that uses the GWT browser-supported
 * XMLParser
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
public class GWTMiniXMLParserImpl implements MiniParser {
	
	public MiniElement parse(String xml) throws IllegalArgumentException {
		
		Document document;
		try {
			document = XMLParser.parse(xml);
		} catch(Exception e) {
			throw new IllegalArgumentException();
		}
		
		return new GWTMiniElementImpl(document.getDocumentElement());
	}
	
}

package org.xydra.core.xml;

/**
 * A minimal XML parser API.
 * 
 * @author dscharrer
 * 
 */
public interface MiniXMLParser {
	
	/**
	 * Parse the given String as an XML document and return the root element.
	 * 
	 * @throws IllegalArgumentException if the given string is not a valid XML
	 *             document.
	 */
	MiniElement parseXml(String xml) throws IllegalArgumentException;
	
}

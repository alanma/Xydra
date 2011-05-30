package org.xydra.core.serialize;

/**
 * A minimal XML/JSON parser for documents generated with {@link XydraOut}.
 * 
 * @author dscharrer
 * 
 */
public interface MiniParser {
	
	/**
	 * Parse the given String as a document and return the root element.
	 * 
	 * @throws IllegalArgumentException if the given string is not a valid
	 *             document.
	 */
	MiniElement parse(String data) throws IllegalArgumentException;
	
}

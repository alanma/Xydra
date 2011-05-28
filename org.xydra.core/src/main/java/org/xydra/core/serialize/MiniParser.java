package org.xydra.core.serialize;

/**
 * A minimal XML/JSON parser API.
 * 
 * @author dscharrer
 * 
 */
public interface MiniParser {
	
	/**
	 * Parse the given String as an document and return the root element.
	 * 
	 * @throws IllegalArgumentException if the given string is not a valid
	 *             document.
	 */
	MiniElement parse(String data) throws IllegalArgumentException;
	
}

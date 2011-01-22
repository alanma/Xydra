package org.xydra.core.xml;

import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;


/**
 * A minimal XML DOM-like API
 * 
 * @author voelkel
 * 
 */
@RunsInGWT
@RunsInJava
public interface MiniElement {
	
	/**
	 * @param attributeName
	 * @return the attribute value or null if it does not exist
	 */
	public String getAttribute(String attributeName);
	
	/**
	 * @return return the contained character data. If this element contains
	 *         additional children, the return value is undefined.
	 */
	public String getData();
	
	/**
	 * @return all child elements in document order
	 */
	public Iterator<MiniElement> getElements();
	
	/**
	 * @return all matching child elements in document order
	 */
	public Iterator<MiniElement> getElementsByTagName(String elementName);
	
	public String getName();
	
}

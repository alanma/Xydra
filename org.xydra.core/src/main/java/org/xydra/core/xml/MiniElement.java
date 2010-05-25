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
	
	public String getName();
	
	/**
	 * @param attributeName
	 * @return the attribute value or null if it does not exist
	 */
	public String getAttribute(String attributeName);
	
	/**
	 * @return all matching child elements in document order
	 */
	public Iterator<MiniElement> getElementsByTagName(String elementName);
	
	/**
	 * @return all child elements in document order
	 */
	public Iterator<MiniElement> getElements();
	
	/**
	 * @return return the contained character data TODO document - what about
	 *         <em>this<b>bla</b></em> - what exactly is returned?
	 */
	public String getData();
	
}

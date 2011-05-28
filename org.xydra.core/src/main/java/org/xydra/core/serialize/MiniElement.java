package org.xydra.core.serialize;

import java.util.Iterator;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * A minimal XML DOM-like API
 * 
 * @author voelkel
 * 
 */
@RunsInGWT(true)
@RequiresAppEngine(false)
public interface MiniElement {
	
	/**
	 * @param name The name of the attribute to get.
	 * @return the attribute value or null if it does not exist
	 */
	public Object getAttribute(String name);
	
	/**
	 * @return return the contained character data. If this element contains
	 *         additional children, the return value is undefined.
	 */
	public Object getContent(String name);
	
	/**
	 * @return all child elements in document order
	 */
	public Iterator<MiniElement> getChildren(String name);
	
	/**
	 * @return all matching child elements in document order
	 */
	public Iterator<MiniElement> getChildrenByType(String name, String type);
	
	public MiniElement getChild(String type);
	
	public String getType();
	
	public Iterator<Object> getValues(String name, String type);
	
}

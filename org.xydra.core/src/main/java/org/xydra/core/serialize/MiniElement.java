package org.xydra.core.serialize;

import java.util.Iterator;


/**
 * A minimal abstraction API to access XML/JSON elements created with XydraOut.
 * 
 * @author voelkel
 * @author dscharrer
 * 
 */
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
	public Iterator<MiniElement> getChildren(String name, String type);
	
	public MiniElement getChild(String name);
	
	public MiniElement getChild(String name, int index);
	
	public MiniElement getChild(String name, String type);
	
	public MiniElement getElement(String type);
	
	public String getType();
	
	public Iterator<Object> getValues(String name);
	
	public Iterator<Object> getValues(String name, String type);
	
	public Object getValue(String name, int index);
	
	public Object getValue(String name, String type);
	
}

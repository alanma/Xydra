package org.xydra.core.value;

/**
 * An {@link XValue} for storing a set of Java String values.
 * 
 * @author Kaidel
 * 
 */
public interface XStringSetValue extends XSetValue<String> {
	
	/**
	 * Returns the contents of the XStringSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XStringSetValue
	 * 
	 * @return the contents of the XStringSetValue as an array
	 */
	String[] contents();
	
	/**
	 * Create a new {@link XStringSetValue} contains all entries from this value
	 * as well as the specified entry. This value is not modified.
	 */
	XStringSetValue add(String entry);
	
	/**
	 * Create a new {@link XSetValue} contains all entries from this value
	 * except the specified entry. This value is not modified.
	 */
	XStringSetValue remove(String entry);
	
}

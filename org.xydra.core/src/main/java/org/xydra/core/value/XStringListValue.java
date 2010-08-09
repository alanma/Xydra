package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java String values.
 * 
 * @author Kaidel
 * 
 */
public interface XStringListValue extends XListValue<String> {
	
	/**
	 * Returns the String values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XStringListValue.
	 * 
	 * @return an array containing the list of String values in the order they
	 *         were added to the list
	 */
	String[] contents();
	
}

package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Boolean values.
 * 
 * @author Kaidel
 * 
 */
public interface XBooleanListValue extends XListValue<Boolean> {
	
	/**
	 * Returns the boolean values as an array in the order they were added to
	 * the list.
	 * 
	 * Note: Changes to the returned array will not affect the
	 * XBooleanListValue.
	 * 
	 * @return an array containing the list of boolean values in the order they
	 *         were added to the list
	 */
	boolean[] contents();
	
}

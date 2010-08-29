package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Double values.
 * 
 * @author Kaidel
 * 
 */
public interface XDoubleListValue extends XNumberListValue<Double> {
	
	/**
	 * Returns the Double values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XDoubleListValue.
	 * 
	 * @return an array containing the list of Double values in the order they
	 *         were added to the list
	 */
	double[] contents();
	
	/**
	 * Create a new {@link XDoubleListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XDoubleListValue add(Double entry);
	
	/**
	 * Create a new {@link XDoubleListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XDoubleListValue add(int index, Double entry);
	
	/**
	 * Create a new {@link XDoubleListValue} contains all entries from this
	 * value except the specified entry. If the entry is contained multiple
	 * times, only the first occurrence is removed. This value is not modified.
	 */
	XDoubleListValue remove(Double entry);
	
	/**
	 * Create a new {@link XDoubleListValue} contains all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XDoubleListValue remove(int index);
	
}

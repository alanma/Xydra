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
	
	/**
	 * Create a new {@link XStringListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XStringListValue add(String entry);
	
	/**
	 * Create a new {@link XStringListValue} contains all entries from this
	 * value as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XStringListValue add(int index, String entry);
	
	/**
	 * Create a new {@link XStringListValue} contains all entries from this
	 * value except the specified entry. If the entry is contained multiple
	 * times, only the first occurrence is removed. This value is not modified.
	 */
	XStringListValue remove(String entry);
	
	/**
	 * Create a new {@link XStringListValue} contains all entries from this
	 * value except the entry at the specified index. This value is not
	 * modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XStringListValue remove(int index);
	
}

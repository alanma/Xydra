package org.xydra.core.value;

/**
 * An {@link XValue} for storing a list of Java Byte values.
 * 
 * @author Kaidel
 * 
 */
public interface XByteListValue extends XListValue<Byte> {
	
	/**
	 * Returns the Byte values as an array in the order they were added to the
	 * list.
	 * 
	 * Note: Changes to the returned array will not affect the XByteListValue.
	 * 
	 * @return an array containing the list of Byte values in the order they
	 *         were added to the list
	 */
	byte[] contents();
	
	/**
	 * Create a new {@link XByteListValue} contains all entries from this value
	 * as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added to the end of the list. This value
	 * is not modified.
	 */
	XByteListValue add(Byte entry);
	
	/**
	 * Create a new {@link XByteListValue} contains all entries from this value
	 * as well as the specified entry. The order od existing entries is
	 * preserved and the new entry is added at the specified index This value is
	 * not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	XByteListValue add(int index, Byte entry);
	
	/**
	 * Create a new {@link XByteListValue} contains all entries from this value
	 * except the specified entry. If the entry is contained multiple times,
	 * only the first occurrence is removed. This value is not modified.
	 */
	XByteListValue remove(Byte entry);
	
	/**
	 * Create a new {@link XByteListValue} contains all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 * 
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	XByteListValue remove(int index);
	
}

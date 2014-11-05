package org.xydra.base.value;

/**
 * An {@link XValue} for storing a set of Java String values.
 * 
 * @author kaidel
 * 
 */
public interface XStringSetValue extends XSetValue<String> {
	
	/**
	 * Creates a new {@link XStringSetValue} containing all entries from this
	 * value as well as the specified entry. This value is not modified.
	 * 
	 * @param entry The new entry
	 * @return a new {@link XStringSetValue} containing all entries from this
	 *         value as well as the given entry
	 */
	@Override
    XStringSetValue add(String entry);
	
	/**
	 * Returns the contents of the XStringSetValue as an array.
	 * 
	 * Note: Changes to the returned array will not affect the XStringSetValue
	 * 
	 * @return the contents of the XStringSetValue as an array
	 */
	String[] contents();
	
	/**
	 * Creates a new {@link XStringSetValue} containing all entries from this
	 * value except the specified entry. This value is not modified.
	 * 
	 * @param entry The entry which is to be removed
	 * @return a new {@link XStringSetValue} containing all entries from this
	 *         value expect the given entry
	 */
	@Override
    XStringSetValue remove(String entry);
	
}

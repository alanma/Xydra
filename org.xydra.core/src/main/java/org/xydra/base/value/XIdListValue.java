package org.xydra.base.value;

import org.xydra.base.XId;


/**
 * An {@link XValue} for storing a list of {@link XId XIds}.
 *
 * @author kaidel
 *
 */
public interface XIdListValue extends XListValue<XId> {

	/**
	 * Creates a new {@link XIdListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added at the specified index This value
	 * is not modified.
	 *
	 * @param index The index at which the new entry is to be added
	 * @param entry The new entry
	 * @return a new {@link XIdListValue} containing all entries from this value
	 *         as well as the specified entry at the given index
	 * @throws IndexOutOfBoundsException if index is negative or greater than
	 *             size()
	 */
	@Override
    XIdListValue add(int index, XId entry);

	/**
	 * Creates a new {@link XIdListValue} containing all entries from this value
	 * as well as the specified entry. The order of the already existing entries
	 * is preserved and the new entry is added to the end of the list. This
	 * value is not modified.
	 *
	 * @param entry The new entry.
	 * @return a new {@link XIdListValue} containing all entries from this value
	 *         as well as the specified entry.
	 */
	@Override
    XIdListValue add(XId entry);

	/**
	 * Returns the {@link XId} values as an array in the order they were added
	 * to the list.
	 *
	 * Note: Changes to the returned array will not affect the XIdListValue.
	 *
	 * @return an array containing the list of {@link XId} values in the order
	 *         they were added to the list
	 */
	public XId[] contents();

	/**
	 * Creates a new {@link XIdListValue} containing all entries from this value
	 * except the entry at the specified index. This value is not modified.
	 *
	 * @param index The index of the entry which is to be removed
	 * @return a new {@link XIdListValue} containing all entries from this value
	 *         except the entry at the given index.
	 *
	 * @throws IndexOutOfBoundsException if index is negative or greater than or
	 *             equal to size()
	 */
	@Override
    XIdListValue remove(int index);

	/**
	 * Creates a new {@link XIdListValue} containing all entries from this value
	 * except the specified entry. Only the first occurrence of the entry is
	 * removed, if this list contains it multiple times. This value is not
	 * modified.
	 *
	 * @param entry The entry which is to be removed
	 * @return a new {@link XIdListValue} containing all entries from this value
	 *         except the given entry
	 */
	@Override
    XIdListValue remove(XId entry);

}

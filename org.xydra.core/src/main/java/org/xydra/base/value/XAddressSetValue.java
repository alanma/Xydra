package org.xydra.base.value;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * An {@link XValue} for storing a set of {@link XAddress XAddresss}.
 *
 * @author xamde, dscharrer
 *
 */
public interface XAddressSetValue extends XSetValue<XAddress> {

	/**
	 * Creates a new {@link XAddressSetValue} containing all entries from this
	 * value as well as the specified entry. This value is not modified.
	 *
	 * @param entry The new entry
	 * @return a new {@link XAddressSetValue} containing all entries from this
	 *         value as well as the given entry
	 */
	@Override
    XAddressSetValue add(XAddress entry);

	/**
	 * Returns the contents of this XIdSetValue as an array.
	 *
	 * Note: Changes to the returned array will not affect the XIdSetValue.
	 *
	 * @return an array containing the {@link XId} values of this XIdSetValue.
	 */
	public XAddress[] contents();

	/**
	 * Creates a new {@link XAddressSetValue} containing all entries from this
	 * value except the specified entry. This value is not modified.
	 *
	 * @param entry The entry which is to be removed
	 * @return a new {@link XAddressSetValue} containing all entries from this
	 *         value expect the given entry
	 */
	@Override
    XAddressSetValue remove(XAddress entry);

}

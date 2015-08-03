package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * An adapter for {@link XValueIndexer} providing basic implementations for most
 * methods. Only {@link XValueIndexer#getStringIndexStrings(String)} needs to be
 * implemented.
 *
 * @author kaidel
 *
 */

public abstract class StringValueSimpleIndexerAdapter extends XValueIndexer {
	public StringValueSimpleIndexerAdapter(final ValueIndex index) {
		super(index);
	}

	// ---- Methods returning the index strings ----

	/**
	 * Returns the long value appended to an empty Strings (i.e. ""+value).
	 *
	 * @returns the long value appended to an empty Strings (i.e. ""+value).
	 */
	@Override
	public String getLongIndexString(final Long value) {
		return "" + value;
	}

	/**
	 * Returns the integer value appended to an empty Strings (i.e. ""+value).
	 *
	 * @returns the integer value appended to an empty Strings (i.e. ""+value).
	 */
	@Override
	public String getIntegerIndexString(final Integer value) {
		return "" + value;
	}

	/**
	 * Returns the double value appended to an empty Strings (i.e. ""+value).
	 *
	 * @returns the double value appended to an empty Strings (i.e. ""+value).
	 */
	@Override
	public String getDoubleIndexString(final Double value) {
		return "" + value;
	}

	/**
	 * Returns the byte value appended to an empty Strings (i.e. ""+value).
	 *
	 * @returns the byte value appended to an empty Strings (i.e. ""+value).
	 */
	@Override
	public String getByteIndexString(final Byte value) {
		return "" + value;
	}

	/**
	 * Returns the boolean value appended to an empty Strings (i.e. ""+value).
	 *
	 * @returns the boolean value appended to an empty Strings (i.e. ""+value).
	 */
	@Override
	public String getBooleanIndexString(final Boolean value) {
		return "" + value;
	}

	/**
	 * Returns the {@link XId} appended to an empty Strings (i.e.
	 * ""+value.toString()).
	 *
	 * @returns the {@link XId} appended to an empty Strings (i.e.
	 *          ""+value.toString()).
	 */
	@Override
	public String getIdIndexString(final XId value) {
		return "" + value.toString().toLowerCase();
	}

	/**
	 * Returns the {@link XAddress} appended to an empty Strings (i.e.
	 * ""+value.toString()).
	 *
	 * @returns the {@link XAddress} appended to an empty Strings (i.e.
	 *          ""+value.toString()).
	 */
	@Override
	public String getAddressIndexString(final XAddress value) {
		/*
		 * TODO Maybe index the single IDs too?
		 */
		if(value == null) {
			return "null";
		}
		return "" + value.toString().toLowerCase();
	}

	/**
	 * Simply returns the String "null".
	 */
	@Override
	public String getIndexStringForNull() {
		return "null";
	}
}

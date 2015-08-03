package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java String.
 *
 * @author kaidel
 *
 */
public interface XStringValue extends XSingleValue<String> {

	/**
	 * Returns the stored String value.
	 *
	 * @return The stored String value.
	 */
	String contents();

}

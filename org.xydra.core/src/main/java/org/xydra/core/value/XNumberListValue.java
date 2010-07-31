package org.xydra.core.value;

/**
 * An XValue for storing a list of integer values.
 * 
 * @author Kaidel
 * 
 */
public interface XNumberListValue<T extends Number> extends XListValue<T> {
	
	/**
	 * @return the contents of this {@link XListValue} as an array.
	 */
	Number[] toNumberArray();
	
}

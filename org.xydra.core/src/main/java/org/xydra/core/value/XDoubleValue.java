package org.xydra.core.value;

/**
 * An XValue for storing a double value.
 * 
 * @author Kaidel
 * 
 */
public interface XDoubleValue extends XNumberValue {
	
	/**
	 * Returns the stored double value.
	 * 
	 * @return The stored double value.
	 */
	double contents();
	
}

package org.xydra.core.value;

/**
 * A common base interface for numeric {@link XValue XValues}.
 */
public interface XNumberValue extends XValue {
	
	/**
	 * @return the stored value as a Number object.
	 */
	public Number asNumber();
	
}

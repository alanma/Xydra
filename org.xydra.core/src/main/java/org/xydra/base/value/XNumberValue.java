package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java {@link Number} value. Acts as a
 * common base interface for numeric {@link XValue XValues}.
 */
public interface XNumberValue {
	
	/**
	 * Returns the stored value as a {@link Number} object.
	 * 
	 * @return the stored value as a {@link Number} object.
	 */
	public Number asNumber();
	
}

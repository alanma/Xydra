package org.xydra.base.value;

/**
 * An {@link XValue} for storing a single Java Long value.
 * 
 * @author Kaidel
 * 
 */
public interface XLongValue extends XNumberValue, XSingleValue<Long> {
    
    /**
     * Return the stored Long value.
     * 
     * @return The stored Long value.
     */
    public long contents();
    
}

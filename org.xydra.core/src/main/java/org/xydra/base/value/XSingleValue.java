package org.xydra.base.value;

/**
 * An XValue that contains a single value.
 * 
 * @author dscharrer
 * @param <T> value type
 * 
 */
public interface XSingleValue<T> extends XValue {
    
    /**
     * @return This value in it's primitive type as determined by
     *         {@link ValueType#getJavaClass()}. Implementations must guarantee
     *         that the original {@link XValue} can be reconstructed from the
     *         result of the primitive's toString() method if the type is known
     *         (and the value's content isn't null).
     */
    T getValue();
    
}

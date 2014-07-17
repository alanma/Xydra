package org.xydra.index.query;

public interface ITriple<K, L, E> extends HasEntry<E> {
    
    E getEntry();
    
    K getKey1();
    
    L getKey2();
    
    /**
     * @return @NeverNull
     */
    K s();
    
    /**
     * @return @NeverNull
     */
    L p();
    
    /**
     * @return @NeverNull
     */
    E o();
    
}

package org.xydra.index.query;

public interface ITriple<K, L, E> extends HasEntry<E> {
    
    public E getEntry();
    
    public K getKey1();
    
    public L getKey2();
    
}

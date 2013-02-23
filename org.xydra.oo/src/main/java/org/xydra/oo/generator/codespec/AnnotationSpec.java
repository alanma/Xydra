package org.xydra.oo.generator.codespec;

import org.xydra.annotations.CanBeNull;


public class AnnotationSpec<T> {
    
    public AnnotationSpec(Class<?> annot, T value) {
        this.annot = annot;
        this.value = value;
    }
    
    public Class<?> annot;
    
    @CanBeNull
    public T value;
    
}

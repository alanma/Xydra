package org.xydra.oo.generator.codespec.impl;

import org.xydra.annotations.CanBeNull;


public class AnnotationSpec<T> {
    
    public Class<?> annot;
    
    @CanBeNull
    private T[] values;
    
    /**
     * @param annot which annotation is made
     * @param values of the annotation
     */
    @SafeVarargs
    protected AnnotationSpec(final Class<?> annot, T ... values) {
        this.annot = annot;
        this.values = values;
    }
    
    /**
     * @return the first of the values or null
     */
    public T getValue() {
        return this.values == null ? null : (this.values.length == 0 ? null : this.values[0]);
    }
    
    public T[] getValues() {
        return this.values;
    }
    
}

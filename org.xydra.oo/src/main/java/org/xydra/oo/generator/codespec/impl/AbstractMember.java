package org.xydra.oo.generator.codespec.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;


public abstract class AbstractMember extends NamedElement implements IMember {
    
    public List<AnnotationSpec<?>> annotations = new ArrayList<AnnotationSpec<?>>();
    
    private String comment;
    
    private String generatedFrom;
    
    protected AbstractMember(String name, String generatedFrom) {
        super(name);
        this.generatedFrom = generatedFrom;
    }
    
    public <T> AbstractMember annotateWith(final Class<?> annotationClass,
            @SuppressWarnings("unchecked") final T ... values) {
        this.annotations.add(new AnnotationSpec<T>(annotationClass, values));
        return this;
    }
    
    @Override
    public String getComment() {
        return (this.comment == null ? "" : this.comment)
                + (this.generatedFrom == null ? "" : " [generated from: '" + this.generatedFrom
                        + "']");
    }
    
    @Override
    public String getGeneratedFrom() {
        return this.generatedFrom;
    }
    
    public Set<String> getRequiredImports() {
        HashSet<String> req = new HashSet<String>();
        for(AnnotationSpec<?> ann : this.annotations) {
            req.add(ann.annot.getCanonicalName());
        }
        return req;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
}

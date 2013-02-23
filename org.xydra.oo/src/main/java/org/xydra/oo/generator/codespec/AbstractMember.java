package org.xydra.oo.generator.codespec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class AbstractMember extends NamedElement implements IMember {
    
    public String comment;
    
    public String generatedFrom;
    
    public List<AnnotationSpec<?>> annotations = new ArrayList<>();
    
    public AbstractMember(String name, String generatedFrom) {
        super(name);
        this.generatedFrom = generatedFrom;
    }
    
    @Override
    public String getComment() {
        return this.comment;
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
    
}

package org.xydra.oo.generator.codespec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AbstractConstructorOrMethodSpec extends AbstractMember implements IMember {
    
    public List<FieldSpec> params = new ArrayList<>();
    
    public List<String> sourceLines = new ArrayList<>();
    
    public AbstractConstructorOrMethodSpec(String name, String generatedFrom) {
        super(name, generatedFrom);
    }
    
    public String id() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        for(FieldSpec p : this.params) {
            sb.append(p.id());
        }
        return sb.toString();
    }
    
    public boolean equals(Object other) {
        return other instanceof FieldSpec && ((FieldSpec)other).id().equals(this.id());
    }
    
    public int hashCode() {
        return this.id().hashCode();
    }
    
    @Override
    public int compareTo(IMember o) {
        if(o instanceof AbstractConstructorOrMethodSpec) {
            return this.id().compareTo(((AbstractConstructorOrMethodSpec)o).id());
        } else {
            return getName().compareTo(o.getName());
        }
    }
    
    public void dump() {
        System.out.println(this.toString());
    }
    
    @Override
    public Set<String> getRequiredImports() {
        Set<String> req = new HashSet<>();
        for(FieldSpec p : this.params) {
            req.addAll(p.getRequiredImports());
        }
        req.addAll(super.getRequiredImports());
        return req;
    }
}

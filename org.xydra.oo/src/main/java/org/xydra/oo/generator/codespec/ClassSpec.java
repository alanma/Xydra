package org.xydra.oo.generator.codespec;

import java.util.ArrayList;
import java.util.List;


public class ClassSpec extends NamedElement {
    
    public String kind;
    
    public ClassSpec superClass;
    
    public List<IMember> members = new ArrayList<>();
    
    /** Make sure to add also as required imports */
    public List<String> implementedInterfaces = new ArrayList<>();
    
    public ClassSpec(String kind, String name) {
        super(name);
        this.kind = kind;
    }
    
    public String id() {
        return this.getName();
    }
    
    public int hashCode() {
        return id().hashCode();
    }
    
    public boolean equals(Object other) {
        return other instanceof ClassSpec && ((ClassSpec)other).id().equals(this.id());
    }
    
    public void dump() {
        System.out.println("Class " + getName() + " extends " + this.superClass);
        for(IMember t : this.members) {
            t.dump();
        }
    }
    
}

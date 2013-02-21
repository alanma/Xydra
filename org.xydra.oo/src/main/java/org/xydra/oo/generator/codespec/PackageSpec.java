package org.xydra.oo.generator.codespec;

import java.util.HashSet;
import java.util.Set;


public class PackageSpec {
    
    public String fullPackageName;
    
    public Set<ClassSpec> classes = new HashSet<>();
    
    public Class<?> generatedFrom;
    
    public void dump() {
        System.out.println("PackageSpec " + this.fullPackageName);
        System.out.println("Generated from: " + this.generatedFrom.getCanonicalName());
        for(ClassSpec c : this.classes) {
            c.dump();
        }
    }
    
}

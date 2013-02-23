package org.xydra.oo.generator.codespec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PackageSpec {
    
    private String fullPackageName;
    
    public Set<ClassSpec> classes = new HashSet<>();
    
    public Class<?> generatedFrom;
    
    public List<PackageSpec> subPackages = new ArrayList<>();
    
    private boolean isBuiltIn;
    
    public PackageSpec(String fullPackageName, boolean isBuiltIn) {
        this.fullPackageName = fullPackageName;
        this.isBuiltIn = isBuiltIn;
    }
    
    public String getFQPackageName() {
        return this.fullPackageName;
    }
    
    public void dump() {
        System.out.println("PackageSpec (" + (isBuiltIn() ? "builtIn" : "generated") + ") "
                + this.fullPackageName);
        System.out.println("Generated from: "
                + (this.generatedFrom == null ? "UNKNOWN" : this.generatedFrom.getCanonicalName()));
        for(ClassSpec c : this.classes) {
            c.dump();
        }
    }
    
    public boolean isBuiltIn() {
        return this.isBuiltIn;
    }
    
}

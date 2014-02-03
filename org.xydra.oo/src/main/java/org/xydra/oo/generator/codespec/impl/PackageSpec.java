package org.xydra.oo.generator.codespec.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Packagename + classes, sub-packages
 * 
 * @author xamde
 */
public class PackageSpec {
    
    public Set<ClassSpec> classes = new HashSet<ClassSpec>();
    
    private String fullPackageName;
    
    public Class<?> generatedFrom;
    
    private boolean isBuiltIn;
    
    public List<PackageSpec> subPackages = new ArrayList<PackageSpec>();
    
    public PackageSpec(String fullPackageName, boolean isBuiltIn) {
        this.fullPackageName = fullPackageName;
        this.isBuiltIn = isBuiltIn;
    }
    
    public ClassSpec addAbstractClass(String name) {
        return new ClassSpec(this, "abstract class", name);
    }
    
    public ClassSpec addClass(String name) {
        return new ClassSpec(this, "class", name);
    }
    
    public ClassSpec addInterface(String name) {
        return new ClassSpec(this, "interface", name);
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
    
    public String getFQPackageName() {
        return this.fullPackageName;
    }
    
    public boolean isBuiltIn() {
        return this.isBuiltIn;
    }
    
}

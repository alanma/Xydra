package org.xydra.devtools.javapackages.architecture;

import java.util.HashSet;
import java.util.Set;

import org.xydra.devtools.javapackages.Package;


public class Layer {
    
    private String commonPackagePrefix;
    
    private Set<Layer> lowerLayers = new HashSet<Layer>();
    
    private boolean allowFromAll;
    
    private boolean allowToAll;
    
    public Layer(String commonPackagePrefix, boolean allowFromAll, boolean allowToAll) {
        this.commonPackagePrefix = commonPackagePrefix;
        this.allowFromAll = allowFromAll;
        this.allowToAll = allowToAll;
    }
    
    public int hashCode() {
        return this.commonPackagePrefix.hashCode();
    }
    
    public boolean equals(Object o) {
        return o instanceof Layer
                && ((Layer)o).commonPackagePrefix.equals(this.commonPackagePrefix);
    }
    
    public void mayAccess(Layer ... lower) {
        if(lower != null)
            for(Layer l : lower)
                this.lowerLayers.add(l);
    }
    
    public boolean contains(Package p) {
        return p.getName().contains(this.commonPackagePrefix);
    }
    
    public Set<Layer> getLowerLayers() {
        return this.lowerLayers;
    }
    
    public boolean hasAccessToAllPackages() {
        return this.allowToAll;
    }
    
    public boolean canBeAccessedFromAllPackages() {
        return this.allowFromAll;
    }
    
    public String toString() {
        return this.commonPackagePrefix + "*";
    }
    
}

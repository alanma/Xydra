package org.xydra.devtools.javapackages.architecture;

import java.util.HashSet;
import java.util.Set;

import org.xydra.devtools.javapackages.Package;


/**
 * An API layer is defined by a common package name prefix. Each layer is
 * allowed to access a number of lower layers. Accesses within a layer are
 * ignored.
 * 
 * A layer can allow access to any other package (e.g. tests) or from any other
 * package (e.g. utilities such as logging or annotations).
 * 
 * @author xamde
 */
public class Layer {
    
    private String commonPackagePrefix;
    
    private Set<Layer> lowerLayers = new HashSet<Layer>();
    
    private boolean allowFromAll;
    
    private boolean allowToAll;
    
    /**
     * @param commonPackagePrefix
     * @param allowFromAll
     * @param allowToAll
     */
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
    
    /**
     * Add a number of layer that are allowed to be accessed
     * 
     * @param lower
     */
    public void mayAccess(Layer ... lower) {
        if(lower != null)
            for(Layer l : lower)
                this.lowerLayers.add(l);
    }
    
    /**
     * @param p
     * @return true iff the given package is contained in this layer
     */
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

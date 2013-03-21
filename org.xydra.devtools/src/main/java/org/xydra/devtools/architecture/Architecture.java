package org.xydra.devtools.architecture;

import java.util.HashSet;
import java.util.Set;

import org.xydra.devtools.java.Package;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Defines the constraints and allowed dependencies
 * 
 * @author xamde
 */
public class Architecture {
    
    private static final Logger log = LoggerFactory.getLogger(Architecture.class);
    
    private Set<Layer> layers = new HashSet<Layer>();
    private String[] scopes;
    
    /**
     * @param a
     * @param b
     * @return true if a may access b
     */
    public boolean isPlannedDependency(Package a, Package b) {
        if(isToJavaPackage(a, b)) {
            log.debug("Dependency to java.* is always allowed");
            return true;
        }
        
        for(Layer layer : this.layers) {
            // both packages belong to the same layer
            if(layer.contains(a) && layer.contains(b)) {
                log.debug(a + " and " + b + " are in the same layer " + layer);
                return true;
            }
            for(Layer lower : layer.getLowerLayers()) {
                // planned access from high to low layer
                if(layer.contains(a) && lower.contains(b)) {
                    log.debug(a + " can access " + b + " in a lower layer");
                    return true;
                }
            }
            // a may access anything
            if(layer.contains(a) && layer.hasAccessToAllPackages()) {
                log.debug(a + " can access *");
                return true;
            }
            // b can be accessed by anyone
            if(layer.contains(b) && layer.canBeAccessedFromAllPackages()) {
                log.debug("* can access " + b);
                return true;
            }
        }
        log.debug(a + " should not access " + b);
        return false;
    }
    
    @SuppressWarnings("unused")
    private static boolean hasLevelFive(Package a, Package b) {
        return a.depth() >= 5 || b.depth() >= 5;
    }
    
    private boolean isWithinScope(Package a, Package b) {
        return isWithinScope(a) && isWithinScope(b);
    }
    
    private boolean isWithinScope(Package p) {
        for(String scope : this.scopes) {
            if(p.getName().startsWith(scope))
                return true;
        }
        return false;
    }
    
    @SuppressWarnings("unused")
    private static boolean isToNeighbour(Package a, Package b) {
        return !a.equals(b) && a.getParentPackageName().equals(b.getParentPackageName());
    }
    
    private static boolean isToJavaPackage(Package a, Package b) {
        return b.getName().startsWith("java.");
    }
    
    private static boolean isToParent(Package a, Package b) {
        return a.getName().startsWith(b.getName());
    }
    
    /**
     * @param packageNamePrefixes to deem relevant for the architecture
     */
    public Architecture(String ... packageNamePrefixes) {
        this.scopes = packageNamePrefixes;
    }
    
    public ArchitectureBuilder setLowestLayer(String packageNamePrefix) {
        return new ArchitectureBuilder(packageNamePrefix);
    }
    
    public class ArchitectureBuilder {
        private ArchitectureBuilder(String packageNamePrefix) {
            this.currentLayer = new Layer(packageNamePrefix, false, false);
            Architecture.this.layers.add(this.currentLayer);
        }
        
        Layer currentLayer;
        
        public ArchitectureBuilder addLayerOnTop(String packageNamePrefix) {
            this.currentLayer = new Layer(packageNamePrefix, false, false);
            for(Layer lower : Architecture.this.layers)
                this.currentLayer.mayAccess(lower);
            Architecture.this.layers.add(this.currentLayer);
            return this;
        }
    }
    
    public Architecture allowAcessFromEveryPackage(String packageNamePrefix) {
        Layer currentLayer = new Layer(packageNamePrefix, true, false);
        this.layers.add(currentLayer);
        return this;
    }
    
    public Architecture ignoreForNow(String packageNamePrefix) {
        Layer currentLayer = new Layer(packageNamePrefix, true, true);
        this.layers.add(currentLayer);
        return this;
    }
    
    /**
     * @param a
     * @param b
     * @return true iff dependency from a to b should be in the graph
     */
    public boolean toBeShown(Package a, Package b) {
        
        // no self-loops
        if(a.equals(b)) {
            log.debug("Both in same package " + a);
            return false;
        }
        
        // exclude some libs completely
        if(!isWithinScope(a, b)) {
            log.debug("Not both in scope " + a + " and " + b);
            return false;
        }
        
        // patterns ----------------
        
        // allow parent-child in both directions => hide in graph
        if(isToParent(a, b)) {
            log.debug("Legal access from child " + a + " to parent " + b);
            return false;
        }
        if(isToParent(b, a)) {
            log.debug("Legal access from child " + b + " to parent " + a);
            return false;
        }
        
        // hide known dependencies that adhere to the planned architecture
        if(isPlannedDependency(a, b))
            return false;
        
        return true;
        
        // log.info("Unclassified: " + a.getName() + " -> " + b.getName());
        // return true;
    }
    
    public String[] getScopes() {
        return this.scopes;
    }
    
    /**
     * All sub-packages are considered to be part of the layer and dependencied
     * within the layer are ignored.
     * 
     * @param packageNamePrefix
     * @return the layer
     */
    public Layer defineLayer(String packageNamePrefix) {
        Layer layer = new Layer(packageNamePrefix, false, false);
        Architecture.this.layers.add(layer);
        return layer;
    }
    
}

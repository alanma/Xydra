package org.xydra.devtools.javapackages;

import java.util.Set;


public interface IDependencyFilter {
    
    /**
     * @param a
     * @param b
     * @param causes i.e. fully qualified class names
     * @return true if edge should be in graph
     */
    boolean shouldBeShown(Package a, Package b, Set<String> causes);
    
}

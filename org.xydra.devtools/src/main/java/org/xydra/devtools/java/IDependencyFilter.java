package org.xydra.devtools.java;

public interface IDependencyFilter {
    
    boolean shouldBeShown(Package a, Package b);
    
}

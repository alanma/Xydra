package org.xydra.oo.runtime.shared;

/**
 * A factory for creating empty collections
 * 
 * @author xamde
 */
public interface IXydraCollectionFactory {
    /**
     * @return an empty xydra collection
     */
    Object createEmptyCollection();
    
    /**
     * @return code to be used in GWT to create an empty xydra collection
     */
    String createEmptyCollection_asSourceCode();
    
}

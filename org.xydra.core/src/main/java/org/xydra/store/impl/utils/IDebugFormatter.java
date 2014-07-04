package org.xydra.store.impl.utils;

import java.util.ServiceLoader;


/**
 * This interface is used via {@link ServiceLoader}. Do not rename.
 * 
 * @author xamde
 * 
 */
public interface IDebugFormatter {
    
    /**
     * @param object @NeverNull
     * @return a concise, human-readable string representing the object or null
     *         to denote the type could not be handled @CanBeNull
     */
    String format(Object object);
    
}

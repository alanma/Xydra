package org.xydra.conf;

/**
 * Allows to find {@link IConfigProvider} implementations via reflection as
 * well.
 * 
 * Implementations should be named by convention "ConfParams....".
 * 
 * @author xamde
 */
public interface IConfigProvider {
    
    /**
     * Set a number of configuration settings in the given conf
     * 
     * @param conf
     */
    void configure(IConfig conf);
    
}

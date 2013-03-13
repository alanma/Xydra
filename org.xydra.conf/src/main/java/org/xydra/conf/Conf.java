package org.xydra.conf;

import org.xydra.conf.impl.MemoryConfig;


/**
 * Configuration singleton. Use only in single-thread environments.
 * 
 * @author xamde
 * 
 */
@Deprecated
public class Conf {
    
    private static MemoryConfig CONFIG;
    
    public static synchronized IConfig ig() {
        if(CONFIG == null) {
            CONFIG = new MemoryConfig();
        }
        return CONFIG;
    }
    
}

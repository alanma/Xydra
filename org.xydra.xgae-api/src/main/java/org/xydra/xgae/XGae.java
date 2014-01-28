package org.xydra.xgae;

import org.xydra.sharedutils.ServiceLoaderUtils;
import org.xydra.xgae.memcache.api.IMemCache;


/**
 * TODO provide ability to have
 * XydraRuntime_GwtEmulated#wrapOrReturn(java.util.Map, IMemCache)
 * 
 * @author xamde
 */
public class XGae {
    
    private static boolean platformInitialised;
    
    public static synchronized IXGae get() {
        initialiseRuntimeOnce();
        return xgae;
    }
    
    /**
     * @return a (potentially cached) instance of a IMemCache
     */
    public static synchronized IMemCache getMemcache() {
        return get().memcache();
    }
    
    private static IXGae xgae;
    
    private static synchronized void initialiseRuntimeOnce() {
        if(platformInitialised) {
            return;
        }
        // try to load dynamically
        xgae = ServiceLoaderUtils.getSingleInstance(IXGae.class);
        platformInitialised = true;
    }
    
}

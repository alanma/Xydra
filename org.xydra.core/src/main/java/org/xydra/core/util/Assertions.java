package org.xydra.core.util;

import org.xydra.sharedutils.XyAssert;


/**
 * Small helper to determine if java assertions are enabled.
 * 
 * @author xamde
 * 
 */
public class Assertions {
    
    private static boolean assertionsEnabled = false;
    
    static {
        XyAssert.xyAssert(assertionsAreEnabled());
    }
    
    private static boolean assertionsAreEnabled() {
        // side-effect: remember
        assertionsEnabled = true;
        return true;
    }
    
    public static boolean enabled() {
        return assertionsEnabled;
    }
    
}

package org.xydra.restless.utils;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Simulate typical web-delays to make dev-mode feel more like real usage.
 * 
 * @author xamde
 */
public class Delay {
    
    private static final Logger log = LoggerFactory.getLogger(Delay.class);
    
    private static int ajaxDelayTimeMs = 0;
    
    private static int servePageDelayTimeMs = 0;
    
    public static boolean isSimulateDelay() {
        return ajaxDelayTimeMs > 0 || servePageDelayTimeMs > 0;
    }
    
    /**
     * Simulate a typical delay for an AJAX request
     */
    public static void ajax() {
        delay("ajax", ajaxDelayTimeMs);
    }
    
    public static void delay(String cause, int ms) {
        if(ms == 0)
            return;
        log.info("~~~ Artificial delay of " + ms + " ms while doing '" + cause + "'");
        try {
            Thread.sleep(ms);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public static void setAjaxDelayMs(int ms) {
        assert ms >= 0;
        ajaxDelayTimeMs = ms;
    }
    
    public static void setServePageDelayMs(int ms) {
        assert ms >= 0;
        servePageDelayTimeMs = ms;
    }
    
    public static void servePage() {
        delay("serve page", servePageDelayTimeMs);
    }
    
    public static boolean hasServePageDelay() {
        return servePageDelayTimeMs > 0;
    }
    
}

package org.xydra.xgae.impl;

import org.xydra.xgae.IXGae;


public abstract class AbstractXGaeBaseImpl implements IXGae {
    
    /**
     * @return true if on AppEngine (regardless whether in production or in
     *         development mode)
     */
    public boolean onAppEngine() {
        return inProduction() || inDevelopment();
    }
    
    /**
     * @return true if running without GAE mode; technically: !onAppEngine()
     */
    public boolean notOnAppengine() {
        return !onAppEngine();
    }
    
    /**
     * @return 'inProduction', 'inDevelopment' or 'notOnAppengine'
     */
    public String inModeAsString() {
        if(inProduction()) {
            return "inProduction";
        } else if(inDevelopment()) {
            return "inDevelopment";
        } else {
            return "notOnAppengine";
        }
    }
    
}

package org.xydra.store.sync;

/**
 * TODO document when stable
 * 
 * @author dscharrer
 * 
 */
public interface XSynchronizationCallback {
    
    // TODO kill?
    void onCommandErrror(Throwable t);
    
    // TODO kill?
    void onEventsError(Throwable t);
    
    void onRequestError(Throwable t);
    
    void onSuccess();
    
    // TODO add methods to be called if we detect that synchronization has been
    // lost
    
}

package org.xydra.core.model;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;


/**
 * An {@link XCommand} plus ...
 * 
 * TODO document when stable
 * 
 * @author dscharrer
 * 
 */
public interface XLocalChange {
    
    XId getActor();
    
    XCommand getCommand();
    
    String getPasswordHash();
    
    long getRemoteRevision();
    
    boolean isApplied();
    
    void setRemoteResult(long result);
    
}

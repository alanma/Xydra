package org.xydra.core.model.impl.memory.sync;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


/**
 * An entry in an {@link ISyncLog}/{@link ISyncLogState}
 * 
 * @author xamde
 * @author andreask
 */
public interface ISyncLogEntry {
    
    /**
     * To sync semantics of safe/forced correctly, a local command is kept
     * 
     * @return @CanBeNull
     */
    XCommand getCommand();
    
    /**
     * @return @NeverNull
     */
    XEvent getEvent();
    
}

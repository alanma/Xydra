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
     * @return @CanBeNull
     */
    XCommand getCommand();
    
    /**
     * @return @NeverNull
     */
    XEvent getEvent();
    
}

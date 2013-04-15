package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


public class LocalCommand {
    
    /**
     * To sync semantics of safe/forced correctly, a local command is kept
     */
    private XCommand command;
    
    private XEvent event;
    
}

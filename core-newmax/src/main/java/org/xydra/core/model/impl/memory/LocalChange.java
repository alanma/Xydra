package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


public class LocalChange {
    
    /**
     * To sync semantics of safe/forced correctly, a local command is kept
     */
    private XCommand command;
    
    private XEvent event;
    
    public XEvent getEvent() {
        return this.event;
    }
    
    public XCommand getCommand() {
        return this.command;
    }
    
}

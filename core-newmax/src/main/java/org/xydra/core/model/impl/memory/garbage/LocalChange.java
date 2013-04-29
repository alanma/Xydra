package org.xydra.core.model.impl.memory.garbage;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


public class LocalChange {

	/**
	 * To sync semantics of safe/forced correctly, a local command is kept
	 */
	private XCommand command;
	
    public LocalChange(XCommand command, XEvent event) {
        super();
        this.command = command;
        this.event = event;
    }
    
	private XEvent event;
	
	public XEvent getEvent() {
		return this.event;
	}
	
	public XCommand getCommand() {
		return this.command;
	}
	
}

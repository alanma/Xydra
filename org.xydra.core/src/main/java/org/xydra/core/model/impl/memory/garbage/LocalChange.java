package org.xydra.core.model.impl.memory.garbage;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


public class LocalChange {

	/**
	 * To sync semantics of safe/forced correctly, a local command is kept
	 */
	private final XCommand command;

    public LocalChange(final XCommand command, final XEvent event) {
        super();
        this.command = command;
        this.event = event;
    }

	private final XEvent event;

	public XEvent getEvent() {
		return this.event;
	}

	public XCommand getCommand() {
		return this.command;
	}

}

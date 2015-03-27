package org.xydra.core.model.impl.memory.sync;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;

public class MemorySyncLogEntry implements ISyncLogEntry {

	XCommand command;

	XEvent event;

	public MemorySyncLogEntry(XCommand command, XEvent event) {
		this.command = command;
		this.event = event;
	}

	@Override
	public String toString() {
		return "EVENT=  " + this.event.toString() + "\n" + "COMMAND="
				+ (this.command == null ? "NULL" : this.command.toString());
	}

	@Override
	public XCommand getCommand() {
		return this.command;
	}

	@Override
	public XEvent getEvent() {
		return this.event;
	}

}

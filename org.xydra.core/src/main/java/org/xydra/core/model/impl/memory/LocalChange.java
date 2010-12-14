package org.xydra.core.model.impl.memory;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XSynchronizationCallback;


public class LocalChange {
	
	public final XID actor;
	public final String passwordHash;
	public XCommand command;
	public final XSynchronizationCallback callback;
	
	public LocalChange(XID actor, String passwordHash, XCommand command,
	        XSynchronizationCallback callback) {
		this.actor = actor;
		this.passwordHash = passwordHash;
		this.command = command;
		this.callback = callback;
	}
	
	@Override
	public String toString() {
		return this.command.toString();
	}
	
}

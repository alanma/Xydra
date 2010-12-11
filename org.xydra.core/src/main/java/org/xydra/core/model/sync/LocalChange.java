package org.xydra.core.model.sync;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XID;


public class LocalChange {
	
	public final XID actor;
	public XCommand command;
	public final XCommandCallback callback;
	
	public LocalChange(XID actor, XCommand command, XCommandCallback callback) {
		this.actor = actor;
		this.command = command;
		this.callback = callback;
	}
	
}

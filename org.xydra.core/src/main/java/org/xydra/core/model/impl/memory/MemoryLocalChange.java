package org.xydra.core.model.impl.memory;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;


public class MemoryLocalChange implements XLocalChange {
	
	private final XID actor;
	private final String passwordHash;
	private XCommand command;
	private final XLocalChangeCallback callback;
	private boolean applied = false;
	private long result;
	
	public MemoryLocalChange(XID actor, String passwordHash, XCommand command,
	        XLocalChangeCallback callback) {
		this.actor = actor;
		this.passwordHash = passwordHash;
		this.command = command;
		this.callback = callback;
	}
	
	@Override
	public String toString() {
		return this.command.toString();
	}
	
	@Override
	public XID getActor() {
		return this.actor;
	}
	
	@Override
	public XCommand getCommand() {
		return this.command;
	}
	
	@Override
	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	@Override
	public long getRemoteResult() {
		return this.result;
	}
	
	@Override
	public boolean isApplied() {
		return this.applied;
	}
	
	@Override
	public void setRemoteResult(long result) {
		if(this.applied) {
			throw new IllegalStateException("cannot overwrite remote result " + this.result
			        + " with " + result);
		}
		this.applied = true;
		this.result = result;
		if(result >= 0) {
			this.callback.applied(result);
		}
	}
	
	protected void updateCommand(long syncRev, long nRemote) {
		// Adapt the command if needed.
		if(this.command instanceof XModelCommand) {
			XModelCommand mc = (XModelCommand)this.command;
			if(mc.getChangeType() == ChangeType.REMOVE && mc.getRevisionNumber() > syncRev) {
				this.command = MemoryModelCommand.createRemoveCommand(mc.getTarget(), mc
				        .getRevisionNumber()
				        + nRemote, mc.getObjectID());
			}
		} else if(this.command instanceof XObjectCommand) {
			XObjectCommand oc = (XObjectCommand)this.command;
			if(oc.getChangeType() == ChangeType.REMOVE && oc.getRevisionNumber() > syncRev) {
				this.command = MemoryObjectCommand.createRemoveCommand(oc.getTarget(), oc
				        .getRevisionNumber()
				        + nRemote, oc.getFieldID());
			}
		} else if(this.command instanceof XFieldCommand) {
			XFieldCommand fc = (XFieldCommand)this.command;
			if(fc.getRevisionNumber() > syncRev) {
				switch(this.command.getChangeType()) {
				case ADD:
					this.command = MemoryFieldCommand.createAddCommand(fc.getTarget(), fc
					        .getRevisionNumber()
					        + nRemote, fc.getValue());
					break;
				case REMOVE:
					this.command = MemoryFieldCommand.createRemoveCommand(fc.getTarget(), fc
					        .getRevisionNumber()
					        + nRemote);
					break;
				case CHANGE:
					this.command = MemoryFieldCommand.createChangeCommand(fc.getTarget(), fc
					        .getRevisionNumber()
					        + nRemote, fc.getValue());
					break;
				default:
					assert false : "Invalid command: " + fc;
				}
			}
		}
	}
	
	protected XLocalChangeCallback getCallback() {
		return this.callback;
	}
	
}

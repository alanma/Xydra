package org.xydra.core.model.impl.memory;

import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.sharedutils.XyAssert;


public class MemoryLocalChange implements XLocalChange {
	
	private final XID actor;
	private boolean applied = false;
	private final XLocalChangeCallback callback;
	private XCommand command;
	private final String passwordHash;
	private long result;
	
	public MemoryLocalChange(XID actor, String passwordHash, XCommand command,
	        XLocalChangeCallback callback) {
		this.actor = actor;
		this.passwordHash = passwordHash;
		this.command = command;
		this.callback = callback;
	}
	
	@Override
	public XID getActor() {
		return this.actor;
	}
	
	protected XLocalChangeCallback getCallback() {
		return this.callback;
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
	public long getRemoteRevision() {
		return this.result;
	}
	
	@Override
	public boolean isApplied() {
		return this.applied;
	}
	
	@Override
	public void setRemoteResult(long revision) {
		if(this.applied) {
			throw new IllegalStateException("cannot overwrite remote result " + this.result
			        + " with " + revision);
		}
		if(revision < 0) {
			throw new IllegalArgumentException("invalid revision: " + revision);
		}
		this.applied = true;
		this.result = revision;
		if(this.callback != null) {
			this.callback.onSuccess(revision);
		}
	}
	
	@Override
	public String toString() {
		return this.command.toString();
	}
	
	protected void updateCommand(long oldSyncRev, long newSyncRev) {
		// Adapt the command if needed.
		XyAssert.xyAssert(newSyncRev >= oldSyncRev);
		long nRemote = newSyncRev - oldSyncRev;
		if(this.command instanceof XRepositoryCommand) {
			XRepositoryCommand rc = (XRepositoryCommand)this.command;
			if(rc.getChangeType() == ChangeType.REMOVE && rc.getRevisionNumber() > oldSyncRev) {
				this.command = MemoryRepositoryCommand.createRemoveCommand(rc.getTarget(),
				        rc.getRevisionNumber() + nRemote, rc.getModelId());
			}
		} else if(this.command instanceof XModelCommand) {
			XModelCommand mc = (XModelCommand)this.command;
			if(mc.getChangeType() == ChangeType.REMOVE && mc.getRevisionNumber() > oldSyncRev) {
				this.command = MemoryModelCommand.createRemoveCommand(mc.getTarget(),
				        mc.getRevisionNumber() + nRemote, mc.getObjectId());
			}
		} else if(this.command instanceof XObjectCommand) {
			XObjectCommand oc = (XObjectCommand)this.command;
			if(oc.getChangeType() == ChangeType.REMOVE && oc.getRevisionNumber() > oldSyncRev) {
				this.command = MemoryObjectCommand.createRemoveCommand(oc.getTarget(),
				        oc.getRevisionNumber() + nRemote, oc.getFieldId());
			}
		} else if(this.command instanceof XFieldCommand) {
			XFieldCommand fc = (XFieldCommand)this.command;
			if(fc.getRevisionNumber() > oldSyncRev) {
				switch(this.command.getChangeType()) {
				case ADD:
					this.command = MemoryFieldCommand.createAddCommand(fc.getTarget(),
					        fc.getRevisionNumber() + nRemote, fc.getValue());
					break;
				case REMOVE:
					this.command = MemoryFieldCommand.createRemoveCommand(fc.getTarget(),
					        fc.getRevisionNumber() + nRemote);
					break;
				case CHANGE:
					this.command = MemoryFieldCommand.createChangeCommand(fc.getTarget(),
					        fc.getRevisionNumber() + nRemote, fc.getValue());
					break;
				default:
					assert false : "Invalid command: " + fc;
				}
			}
		}
	}
	
}

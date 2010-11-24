package org.xydra.store.impl.memory;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


public class SimpleEvent implements XEvent {
	
	public XID getActor() {
		return this.actor;
	}
	
	public ChangeType getChangeType() {
		return this.changeType;
	}
	
	public XAddress getChangedEntity() {
		return this.changedEntity;
	}
	
	public long getOldFieldRevision() {
		return this.oldFieldRevision;
	}
	
	public long getOldModelRevision() {
		return this.oldModelRevision;
	}
	
	public long getOldObjectRevision() {
		return this.oldObjectRevision;
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
	public boolean isImplied() {
		return this.implied;
	}
	
	public SimpleEvent(XID actor, ChangeType changeType, XAddress changedEntity,
	        long oldFieldRevision, long oldModelRevision, long oldObjectRevision,
	        long revisionNumber, XAddress target, boolean inTransaction, boolean implied) {
		super();
		this.actor = actor;
		this.changeType = changeType;
		this.changedEntity = changedEntity;
		this.oldFieldRevision = oldFieldRevision;
		this.oldModelRevision = oldModelRevision;
		this.oldObjectRevision = oldObjectRevision;
		this.revisionNumber = revisionNumber;
		this.target = target;
		this.inTransaction = inTransaction;
		this.implied = implied;
	}
	
	protected XID actor;
	protected ChangeType changeType;
	protected XAddress changedEntity;
	protected long oldFieldRevision;
	protected long oldModelRevision;
	protected long oldObjectRevision;
	protected long revisionNumber;
	protected XAddress target;
	protected boolean inTransaction;
	protected boolean implied;
	
}

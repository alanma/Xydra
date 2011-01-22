package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.index.XI;


abstract public class MemoryAtomicEvent implements XEvent {
	
	// The XID of the actor of this event.
	private final XID actor;
	
	// The ChangeType
	private final ChangeType changeType;
	
	// is this remove event implied by another event in the same transaction
	private final boolean implied;
	
	// was this event part of a transaction or not?
	private final boolean inTransaction;
	
	private final XAddress target;
	
	protected MemoryAtomicEvent(XAddress target, ChangeType changeType, XID actor, boolean inTrans,
	        boolean implied) {
		
		if(target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		
		assert !implied
		        || (inTrans && changeType == ChangeType.REMOVE && target.getParent() != null);
		assert changeType != ChangeType.TRANSACTION;
		
		this.target = target;
		this.changeType = changeType;
		this.actor = actor;
		this.inTransaction = inTrans;
		this.implied = implied;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(object == null) {
			return false;
		}
		
		if(!(object instanceof XAtomicEvent)) {
			return false;
		}
		XAtomicEvent event = (XAtomicEvent)object;
		
		if(this.inTransaction != event.inTransaction()) {
			return false;
		}
		
		if(this.implied != event.isImplied()) {
			return false;
		}
		
		return XI.equals(this.actor, event.getActor()) && this.changeType == event.getChangeType()
		        && this.target.equals(event.getTarget());
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public ChangeType getChangeType() {
		return this.changeType;
	}
	
	/**
	 * @return the {@link XID} of the {@link XField} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getFieldId() {
		return this.target.getField();
	}
	
	/**
	 * @return the {@link XID} of the {@link XModel} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getModelId() {
		return this.target.getModel();
	}
	
	/**
	 * @return the {@link XID} of the {@link XObject} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getObjectId() {
		return this.target.getObject();
	}
	
	public long getOldFieldRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getOldModelRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getOldObjectRevision() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	/**
	 * @return the {@link XID} of the {@link XRepository} holding the entity
	 *         this event refers to (may be null)
	 */
	public XID getRepositoryId() {
		return this.target.getRepository();
	}
	
	@Override
	public long getRevisionNumber() {
		
		long rev = getOldModelRevision();
		if(rev >= 0) {
			return rev + 1;
		}
		
		rev = getOldObjectRevision();
		if(rev >= 0) {
			return rev + 1;
		}
		
		rev = getOldFieldRevision();
		if(rev >= 0) {
			return rev + 1;
		}
		
		return 0;
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	@Override
	public int hashCode() {
		
		int result = 0;
		
		// changeType is never null
		result ^= this.changeType.hashCode();
		
		// actor
		result ^= (this.actor != null) ? this.actor.hashCode() : 0;
		
		// target
		result ^= this.target.hashCode();
		
		return result;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
	public boolean isImplied() {
		return this.implied;
	}
	
	String rev2str(long rev) {
		if(rev == XEvent.RevisionOfEntityNotSet) {
			return "-";
		} else if(rev == XEvent.RevisionNotAvailable) {
			return "?";
		} else {
			assert rev >= 0;
			return Long.toString(rev);
		}
	}
	
}

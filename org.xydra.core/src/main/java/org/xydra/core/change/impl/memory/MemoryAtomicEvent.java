package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


abstract public class MemoryAtomicEvent implements XEvent {
	
	private final XAddress target;
	private final ChangeType changeType; // The ChangeType
	private final XID actor; // The XID of the actor of this event.
	
	protected MemoryAtomicEvent(XAddress target, ChangeType changeType, XID actor) {
		
		if(target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		
		this.target = target;
		this.changeType = changeType;
		this.actor = actor;
	}
	
	public ChangeType getChangeType() {
		return this.changeType;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	/**
	 * @return the {@link XID} of the {@link XRepository} holding the entity
	 *         this event refers to (may be null)
	 */
	public XID getRepositoryID() {
		return this.target.getRepository();
	}
	
	/**
	 * @return the {@link XID} of the {@link XModel} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getModelID() {
		return this.target.getModel();
	}
	
	/**
	 * @return the {@link XID} of the {@link XObject} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getObjectID() {
		return this.target.getObject();
	}
	
	/**
	 * @return the {@link XID} of the {@link XField} holding the entity this
	 *         event refers to (may be null)
	 */
	public XID getFieldID() {
		return this.target.getField();
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(object == null)
			return false;
		
		if(!(object instanceof XAtomicEvent))
			return false;
		XAtomicEvent event = (XAtomicEvent)object;
		
		return XX.equals(this.actor, event.getActor()) && this.changeType == event.getChangeType()
		        && this.target.equals(event.getTarget());
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
	
	public long getModelRevisionNumber() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getObjectRevisionNumber() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
	public long getFieldRevisionNumber() {
		return XEvent.RevisionOfEntityNotSet;
	}
	
}

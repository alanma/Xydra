package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * An implementation of {@link XModelEvent}.
 * 
 * @author Kaidel
 */
@RunsInGWT(true)
public class MemoryModelEvent extends MemoryAtomicEvent implements XModelEvent {
	
	private static final long serialVersionUID = -598246000186155639L;
	
	/**
	 * Creates a new {@link XModelEvent} of the add-type (an {@link XObject} was
	 * added to the {@link XModel} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XModel} this event
	 *            refers to - model {@link XID} must not be null
	 * @param objectId The {@link XID} of the added {@link XObject} - must not
	 *            be null
	 * @param modelRevision the revision number of the {@link XModel} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return An {@link XModelEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XModel}, if objectId is null or if the
	 *             given revision number equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 */
	public static XModelEvent createAddEvent(XID actor, XAddress target, XID objectId,
	        long modelRevision, boolean inTransaction) {
		return new MemoryModelEvent(actor, target, objectId, ChangeType.ADD, modelRevision,
		        RevisionOfEntityNotSet, inTransaction, false);
	}
	
	public static XModelEvent createFrom(XModelEvent me) {
		MemoryModelEvent event = new MemoryModelEvent(me.getActor(), me.getTarget(),
		        me.getObjectId(), me.getChangeType(), me.getOldModelRevision(),
		        me.getOldObjectRevision(), me.inTransaction(), me.isImplied());
		return event;
	}
	
	/**
	 * Creates a new {@link XModelEvent} of the remove-type (an {@link XObject}
	 * was removed from the {@link XModel} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XModel} this event
	 *            refers to - model {@link XID} must not be null
	 * @param objectId The {@link XID} of the removed {@link XObject}
	 * @param modelRevision the revision number of the {@link XModel} this event
	 *            refers to
	 * @param objectRevision the revision number of the {@link XObject} which
	 *            was removed
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @param implied sets whether this event describes removing an object whose
	 *            containing model is also removed in the same transaction
	 * @return An XModelEvent of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XModel}, if objectId is null or if one of
	 *             the given revision numbers equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 */
	public static XModelEvent createRemoveEvent(XID actor, XAddress target, XID objectId,
	        long modelRevision, long objectRevision, boolean inTransaction, boolean implied) {
		if(objectRevision < 0 && objectRevision != XEvent.RevisionNotAvailable) {
			throw new IllegalArgumentException(
			        "object revision must be set for model REMOVE events");
		}
		
		return new MemoryModelEvent(actor, target, objectId, ChangeType.REMOVE, modelRevision,
		        objectRevision, inTransaction, implied);
	}
	
	// the revision numbers before the event happened
	private long modelRevision, objectRevision;
	
	// The XID of the object that was created/deleted
	private XID objectId;
	
	// private constructor, use the createEvent for instantiating MemModelEvents
	private MemoryModelEvent(XID actor, XAddress target, XID objectId, ChangeType changeType,
	        long modelRevision, long objectRevision, boolean inTransaction, boolean implied) {
		super(target, changeType, actor, inTransaction, implied);
		
		if(target.getModel() == null || target.getObject() != null || target.getField() != null) {
			throw new IllegalArgumentException("target must refer to a model, was: " + target);
		}
		
		if(objectId == null) {
			throw new IllegalArgumentException("object ID must be set for model events, is null");
		}
		if(modelRevision < 0) {
			throw new IllegalArgumentException("modelRevision must be set for model events");
		}
		
		if(objectRevision < 0 && objectRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid objectRevision: " + objectRevision);
		}
		
		this.objectId = objectId;
		this.objectRevision = objectRevision;
		this.modelRevision = modelRevision;
	}
	
	/**
	 * GWT only
	 */
	protected MemoryModelEvent() {
		
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object)) {
			return false;
		}
		
		if(!(object instanceof XModelEvent)) {
			return false;
		}
		XModelEvent event = (XModelEvent)object;
		
		if(!this.objectId.equals(event.getObjectId())) {
			return false;
		}
		
		if(this.modelRevision != event.getOldModelRevision()) {
			return false;
		}
		
		if(this.objectRevision != event.getOldObjectRevision()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public XAddress getChangedEntity() {
		return XX.resolveObject(getTarget(), getObjectId());
	}
	
	@Override
	public XID getObjectId() {
		return this.objectId;
	}
	
	@Override
	public long getOldModelRevision() {
		return this.modelRevision;
	}
	
	@Override
	public long getOldObjectRevision() {
		return this.objectRevision;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.objectId.hashCode();
		
		// old revisions
		result += this.modelRevision;
		result += this.objectRevision;
		
		return result;
	}
	
	@Override
	public String toString() {
		String str = "ModelEvent  by actor: '" + getActor() + "' " + getChangeType() + " object: '"
		        + this.objectId + "'";
		if(this.objectRevision >= 0)
			str += " r" + rev2str(this.objectRevision);
		str += " @" + getTarget();
		str += " r" + rev2str(this.modelRevision);
		if(isImplied()) {
			str += " [implied]";
		}
		if(inTransaction()) {
			str += " [inTxn]";
		}
		return str;
	}
	
}
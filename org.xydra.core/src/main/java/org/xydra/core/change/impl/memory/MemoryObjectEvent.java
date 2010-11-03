package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;


/**
 * An implementation of {@link XObjectEvent}
 * 
 * @author Kaidel
 * 
 */

public class MemoryObjectEvent extends MemoryAtomicEvent implements XObjectEvent {
	
	// The XID of field that was created/deleted
	private final XID fieldID;
	
	// the revision numbers before the event happened
	private final long fieldRevision, objectRevision, modelRevision;
	
	// was this event part of a transaction or not?
	private final boolean inTransaction;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object)) {
			return false;
		}
		
		if(!(object instanceof XObjectEvent)) {
			return false;
		}
		XObjectEvent event = (XObjectEvent)object;
		
		if(!this.fieldID.equals(event.getFieldID())) {
			return false;
		}
		
		if(this.modelRevision != event.getOldModelRevision()) {
			return false;
		}
		
		long otherObjectRev = event.getOldObjectRevision();
		if(this.objectRevision != otherObjectRev) {
			if((this.objectRevision != XEvent.RevisionNotAvailable && otherObjectRev != XEvent.RevisionNotAvailable)) {
				return false;
			}
		}
		
		if(this.fieldRevision != event.getOldFieldRevision()) {
			return false;
		}
		
		if(this.inTransaction != event.inTransaction()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.fieldID.hashCode();
		
		// old revisions
		result += this.modelRevision;
		if(this.objectRevision != XEvent.RevisionOfEntityNotSet) {
			result += 0x3472089;
		}
		result += this.fieldRevision;
		
		return result;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
	/**
	 * Creates a new {@link XObjectEvent} of the add-type (an {@link XField} was
	 * added to the {@link XObject} this event refers to)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} to which the
	 *            {@link XField} was added - object {@link XID} must not be null
	 * @param fieldID The {@link XID} of the added {@link XField} - must not be
	 *            null
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldID is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	
	public static XObjectEvent createAddEvent(XID actor, XAddress target, XID fieldID,
	        long objectRevision, boolean inTransaction) {
		return createAddEvent(actor, target, fieldID, RevisionOfEntityNotSet, objectRevision,
		        inTransaction);
	}
	
	/**
	 * Creates a new {@link XObjectEvent} of the add-type (an {@link XField} was
	 * added to the {@link XObject} this event refers to)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} to which the
	 *            {@link XField}was added - object {@link XID} must not be null
	 * @param fieldID The {@link XID} of the added {@link XField} - must not be
	 *            null
	 * @param modelRevision the revision number of the {@link XModel} holding
	 *            the {@link XObject} this event refers to
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldID is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	
	public static XObjectEvent createAddEvent(XID actor, XAddress target, XID fieldID,
	        long modelRevision, long objectRevision, boolean inTransaction) {
		
		return new MemoryObjectEvent(actor, target, fieldID, ChangeType.ADD, modelRevision,
		        objectRevision, RevisionOfEntityNotSet, inTransaction);
	}
	
	/**
	 * Creates a new {@link XObjectEvent} of the remove-type (an {@link XField}
	 * was removed from the {@link XObject} this event refers to)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} from which the
	 *            {@link XField} was removed - object {@link XID} must not be
	 *            null
	 * @param fieldID The {@link XID} of the removed {@link XField} - must not
	 *            be null
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param fieldRevision the revision number of the {@link XField} which was
	 *            removed
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldID is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	
	public static XObjectEvent createRemoveEvent(XID actor, XAddress target, XID fieldID,
	        long objectRevision, long fieldRevision, boolean inTransaction) {
		return createRemoveEvent(actor, target, fieldID, RevisionOfEntityNotSet, objectRevision,
		        fieldRevision, inTransaction);
		
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the remove-type (an {@link XField} was
	 * removed from the {@link XObject} this event refers to)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} from which the
	 *            {@link XField} was removed - object {@link XID} must not be
	 *            null
	 * @param fieldID The {@link XID} of the removed {@link XField} - must not
	 *            be null
	 * @param modelRevision the revision number of the {@link XModel} holding
	 *            the {@link XObject} this event refers to
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param fieldRevision the revision number of the {@link XField} which was
	 *            removed
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldID is null or
	 *             if the given objectRevision or fieldRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	
	public static XObjectEvent createRemoveEvent(XID actor, XAddress target, XID fieldID,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
		if(fieldRevision < 0) {
			throw new IllegalArgumentException(
			        "field revision must be set for object REMOVE events");
		}
		
		return new MemoryObjectEvent(actor, target, fieldID, ChangeType.REMOVE, modelRevision,
		        objectRevision, fieldRevision, inTransaction);
	}
	
	// private constructor, use the createEvent methods for instantiating a
	// MemObjectEvent
	private MemoryObjectEvent(XID actor, XAddress target, XID fieldID, ChangeType changeType,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
		super(target, changeType, actor);
		
		if(target.getObject() == null || target.getField() != null) {
			throw new IllegalArgumentException("target must refer to an object, was: " + target);
		}
		
		if(fieldID == null) {
			throw new IllegalArgumentException("field ID must be set for object events");
		}
		
		if(objectRevision < 0 && objectRevision != RevisionNotAvailable) {
			throw new IllegalArgumentException("object revision must be set for object events");
		}
		
		if(fieldRevision < 0 && fieldRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid fieldRevision: " + fieldRevision);
		}
		
		if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
		}
		
		this.fieldID = fieldID;
		this.modelRevision = modelRevision;
		this.objectRevision = objectRevision;
		this.fieldRevision = fieldRevision;
		this.inTransaction = inTransaction;
	}
	
	@Override
	public String toString() {
		String str = "ObjectEvent: " + getChangeType() + " " + this.fieldID;
		if(this.fieldRevision >= 0)
			str += " r" + rev2str(this.fieldRevision);
		str += " @" + getTarget();
		str += " r" + rev2str(this.modelRevision) + "/" + rev2str(this.objectRevision);
		return str;
	}
	
	@Override
	public long getOldFieldRevision() {
		return this.fieldRevision;
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
	public XID getFieldID() {
		return this.fieldID;
	}
	
	public XAddress getChangedEntity() {
		return XX.resolveField(getTarget(), getFieldID());
	}
	
}

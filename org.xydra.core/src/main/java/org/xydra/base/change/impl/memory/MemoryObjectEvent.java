package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * An implementation of {@link XObjectEvent}
 * 
 * @author Kaidel
 * 
 */

public class MemoryObjectEvent extends MemoryAtomicEvent implements XObjectEvent {
	
	private static final long serialVersionUID = 6129548600082005223L;
	
	/**
	 * Creates a new {@link XObjectEvent} of the add-type (an {@link XField} was
	 * added to the {@link XObject} this event refers to)
	 * 
	 * @param actorId The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} to which the
	 *            {@link XField} was added - object {@link XID} must not be null
	 * @param fieldId The {@link XID} of the added {@link XField} - must not be
	 *            null
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldId is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	public static XObjectEvent createAddEvent(XID actorId, XAddress target, XID fieldId,
	        long objectRevision, boolean inTransaction) {
		return createAddEvent(actorId, target, fieldId, RevisionOfEntityNotSet, objectRevision,
		        inTransaction);
	}
	
	/**
	 * Creates a new {@link XObjectEvent} of the add-type (an {@link XField} was
	 * added to the {@link XObject} this event refers to)
	 * 
	 * @param actorId The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} to which the
	 *            {@link XField}was added - object {@link XID} must not be null
	 * @param fieldId The {@link XID} of the added {@link XField} - must not be
	 *            null
	 * @param modelRevision the revision number of the {@link XModel} holding
	 *            the {@link XObject} this event refers to
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XObjectEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldId is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	public static XObjectEvent createAddEvent(XID actorId, XAddress target, XID fieldId,
	        long modelRevision, long objectRevision, boolean inTransaction) {
		
		return new MemoryObjectEvent(actorId, target, fieldId, ChangeType.ADD, modelRevision,
		        objectRevision, RevisionOfEntityNotSet, inTransaction, false);
	}
	
	/**
	 * GWT only
	 */
	protected MemoryObjectEvent() {
		
	}
	
	/**
	 * Creates a new {@link XObjectEvent} of the remove-type (an {@link XField}
	 * was removed from the {@link XObject} this event refers to)
	 * 
	 * @param actorId The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} from which the
	 *            {@link XField} was removed - object {@link XID} must not be
	 *            null
	 * @param fieldId The {@link XID} of the removed {@link XField} - must not
	 *            be null
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param fieldRevision the revision number of the {@link XField} which was
	 *            removed
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @param implied sets whether this event describes removing a field whose
	 *            containing object is also removed in the same transaction
	 * @return an {@link XObjectEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldId is null or
	 *             if the given objectRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	public static XObjectEvent createRemoveEvent(XID actorId, XAddress target, XID fieldId,
	        long objectRevision, long fieldRevision, boolean inTransaction, boolean implied) {
		return createRemoveEvent(actorId, target, fieldId, RevisionOfEntityNotSet, objectRevision,
		        fieldRevision, inTransaction, implied);
		
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the remove-type (an {@link XField} was
	 * removed from the {@link XObject} this event refers to)
	 * 
	 * @param actorId The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XObject} from which the
	 *            {@link XField} was removed - object {@link XID} must not be
	 *            null
	 * @param fieldId The {@link XID} of the removed {@link XField} - must not
	 *            be null
	 * @param modelRevision the revision number of the {@link XModel} holding
	 *            the {@link XObject} this event refers to
	 * @param objectRevision the revision number of the {@link XObject} this
	 *            event refers to
	 * @param fieldRevision the revision number of the {@link XField} which was
	 *            removed
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @param implied sets whether this event describes removing a field whose
	 *            containing object is also removed in the same transaction
	 * @return an {@link XObjectEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XObject}, if the given fieldId is null or
	 *             if the given objectRevision or fieldRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}.
	 */
	public static XObjectEvent createRemoveEvent(XID actorId, XAddress target, XID fieldId,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
	        boolean implied) {
		if(fieldRevision < 0) {
			throw new IllegalArgumentException(
			        "field revision must be set for object REMOVE events");
		}
		
		return new MemoryObjectEvent(actorId, target, fieldId, ChangeType.REMOVE, modelRevision,
		        objectRevision, fieldRevision, inTransaction, implied);
	}
	
	// The XID of field that was created/deleted
	private XID fieldId;
	
	// the revision numbers before the event happened
	private long fieldRevision, objectRevision, modelRevision;
	
	// private constructor, use the createEvent methods for instantiating a
	// MemObjectEvent
	private MemoryObjectEvent(XID actor, XAddress target, XID fieldId, ChangeType changeType,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
	        boolean implied) {
		super(target, changeType, actor, inTransaction, implied);
		
		if(target.getObject() == null || target.getField() != null) {
			throw new IllegalArgumentException("target must refer to an object, was: " + target);
		}
		
		if(fieldId == null) {
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
		
		this.fieldId = fieldId;
		this.modelRevision = modelRevision;
		this.objectRevision = objectRevision;
		this.fieldRevision = fieldRevision;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object)) {
			return false;
		}
		
		if(!(object instanceof XObjectEvent)) {
			return false;
		}
		XObjectEvent event = (XObjectEvent)object;
		
		if(!this.fieldId.equals(event.getFieldId())) {
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
		
		return true;
	}
	
	@Override
	public XAddress getChangedEntity() {
		return XX.resolveField(getTarget(), getFieldId());
	}
	
	@Override
	public XID getFieldId() {
		return this.fieldId;
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
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.fieldId.hashCode();
		
		// old revisions
		result += this.modelRevision;
		if(this.objectRevision != XEvent.RevisionOfEntityNotSet) {
			result += 0x3472089;
		}
		result += this.fieldRevision;
		
		return result;
	}
	
	@Override
	public String toString() {
		String str = "ObjectEvent by " + getActor() + ": " + getChangeType() + " " + this.fieldId;
		if(this.fieldRevision >= 0)
			str += " r" + rev2str(this.fieldRevision);
		str += " @" + getTarget();
		str += " r" + rev2str(this.modelRevision) + "/" + rev2str(this.objectRevision);
		if(isImplied()) {
			str += " [implied]";
		}
		return str;
	}
	
}

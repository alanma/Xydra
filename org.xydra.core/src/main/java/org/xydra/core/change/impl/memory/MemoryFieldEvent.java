package org.xydra.core.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.index.XI;


/**
 * An implementation of {@link XFieldEvent}.
 * 
 * @author voelkel
 * @author kaidel
 */
public class MemoryFieldEvent extends MemoryAtomicEvent implements XFieldEvent {
	
	// the old value, before the event happened (null for "add" events)
	private final XValue oldValue;
	
	// the new value, after the event happened (null for "delete" events)
	private final XValue newValue;
	
	// the revision numbers before the event happened
	private final long modelRevision, objectRevision, fieldRevision;
	
	public XValue getNewValue() {
		return this.newValue;
	}
	
	public XValue getOldValue() {
		return this.oldValue;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object)) {
			return false;
		}
		
		if(!(object instanceof XFieldEvent)) {
			return false;
		}
		XFieldEvent event = (XFieldEvent)object;
		
		if(!XI.equals(this.oldValue, event.getOldValue())) {
			return false;
		}
		
		if(!XI.equals(this.newValue, event.getNewValue())) {
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
	public int hashCode() {
		
		int result = super.hashCode();
		
		// oldValue
		result ^= (this.oldValue == null ? 0 : this.oldValue.hashCode());
		
		// newValue
		result ^= (this.newValue == null ? 0 : this.newValue.hashCode());
		
		// old revisions
		result += this.modelRevision;
		if(this.objectRevision != XEvent.RevisionOfEntityNotSet) {
			result += 0x3472089;
		}
		result += this.fieldRevision;
		
		return result;
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the change-type (the {@link XValue} of
	 * the {@link XField} this event refers to was changed)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} where this event happened - field
	 *            {@link XID} of the given address must not be null
	 * @param oldValue The old {@link XValue} - must not be null
	 * @param newValue The new {@link XValue} - must not be null
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XFieldEvent} of the change-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if either oldValue or newValue is null
	 */
	public static XFieldEvent createChangeEvent(XID actor, XAddress target, XValue oldValue,
	        XValue newValue, long objectRevision, long fieldRevision, boolean inTransaction) {
		return createChangeEvent(actor, target, oldValue, newValue, RevisionOfEntityNotSet,
		        objectRevision, fieldRevision, inTransaction);
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the change-type (the {@link XValue} of
	 * the {@link XField} this event refers to was changed)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} where this event happened - the field
	 *            {@link XID} of the given must not be null
	 * @param oldValue The old {@link XValue} - must not be null
	 * @param newValue The new {@link XValue} - must not be null
	 * @param modelRevision The revision number of the {@link XModel} holding
	 *            the {@link XObject} which holds the {@link XField} this event
	 *            refers to
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return an {@link XFieldEvent} of the change-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if either oldValue or newValue is null
	 */
	public static XFieldEvent createChangeEvent(XID actor, XAddress target, XValue oldValue,
	        XValue newValue, long modelRevision, long objectRevision, long fieldRevision,
	        boolean inTransaction) {
		if(oldValue == null || newValue == null) {
			throw new IllegalArgumentException(
			        "oldValue and newValue must not be null for field CHANGE events");
		}
		
		return new MemoryFieldEvent(actor, target, oldValue, newValue, ChangeType.CHANGE,
		        modelRevision, objectRevision, fieldRevision, inTransaction, false);
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the add-type (an {@link XValue} was
	 * added to the {@link XField} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the target - the field {@link XID}
	 *            of the given address must not be null.
	 * @param newValue The added {@link XValue} - must not be null
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to.
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during a
	 *            {@link XTransaction} or not
	 * @return An {@link XFieldEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if newValue is null
	 */
	public static XFieldEvent createAddEvent(XID actor, XAddress target, XValue newValue,
	        long objectRevision, long fieldRevision, boolean inTransaction) {
		return createAddEvent(actor, target, newValue, RevisionOfEntityNotSet, objectRevision,
		        fieldRevision, inTransaction);
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the add-type (an {@link XValue} was
	 * added to the {@link XField} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the target - the field {@link XID}
	 *            of the given address must not be null.
	 * @param newValue The added {@link XValue} - must not be null
	 * @param modelRevision The revision number of the {@link XModel} holding
	 *            the {@link XObject} holding the {@link XField} this event
	 *            refers to.
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to.
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @return An {@link XFieldEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if newValue is null
	 */
	public static XFieldEvent createAddEvent(XID actor, XAddress target, XValue newValue,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
		if(newValue == null) {
			throw new RuntimeException("newValue must not be null for field ADD events");
		}
		
		return new MemoryFieldEvent(actor, target, null, newValue, ChangeType.ADD, modelRevision,
		        objectRevision, fieldRevision, inTransaction, false);
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
	 * the {@link XFields} this event refers to was removed)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the target - the field {@link XID}
	 *            of the given address must not be null.
	 * @param oldValue The removed {@link XValue} - must not be null
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to.
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @param implied sets whether this event describes removing the value of a
	 *            field whose containing object is also removed in the same
	 *            transaction
	 * @return An {@link XFieldEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if oldValue is null
	 */
	public static XFieldEvent createRemoveEvent(XID actor, XAddress target, XValue oldValue,
	        long objectRevision, long fieldRevision, boolean inTransaction, boolean implied) {
		return createRemoveEvent(actor, target, oldValue, RevisionOfEntityNotSet, objectRevision,
		        fieldRevision, inTransaction, implied);
	}
	
	/**
	 * Creates an {@link XFieldEvent} of the remove-type (the {@link XValue} of
	 * the {@link XFields} this event refers to was removed)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the target - field ID must not be
	 *            null.
	 * @param oldValue The added {@link XValue} - must not be null
	 * @param modelRevision The revision number of the {@link XModel} holding
	 *            the {@link XObject} holding the {@link XField} this event
	 *            refers to.
	 * @param objectRevision The revision number of the {@link XObject} holding
	 *            the {@link XField} this event refers to.
	 * @param fieldRevision The revision number of the {@link XField} this event
	 *            refers to
	 * @param inTransaction sets whether this event occurred during an
	 *            {@link XTransaction} or not
	 * @param implied sets whether this event describes removing the value of a
	 *            field whose containing object is also removed in the same
	 *            transaction
	 * @return An {@link XFieldEvent} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             specify an {@link XField} or if the given revision number
	 *             equals {@link XEvent#RevisionOfEntityNotSet}
	 * @throws IllegalArgumentException if oldValue is null
	 */
	public static XFieldEvent createRemoveEvent(XID actor, XAddress target, XValue oldValue,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction,
	        boolean implied) {
		if(oldValue == null) {
			throw new IllegalArgumentException("oldValue must not be null for field REMOVE events");
		}
		
		return new MemoryFieldEvent(actor, target, oldValue, null, ChangeType.REMOVE,
		        modelRevision, objectRevision, fieldRevision, inTransaction, implied);
	}
	
	// private constructor, use the createEvent-methods for instantiating a
	// MemoryFieldEvent.
	private MemoryFieldEvent(XID actor, XAddress target, XValue oldValue, XValue newValue,
	        ChangeType changeType, long modelRevision, long objectRevision, long fieldRevision,
	        boolean inTransaction, boolean implied) {
		super(target, changeType, actor, inTransaction, implied);
		
		if(target.getField() == null || fieldRevision < 0) {
			throw new IllegalArgumentException("field ID and revision must be set for field events");
		}
		
		if(objectRevision < 0 && objectRevision != RevisionOfEntityNotSet
		        && objectRevision != RevisionNotAvailable) {
			throw new IllegalArgumentException("invalid objectRevision: " + objectRevision);
		}
		
		if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
		}
		
		this.oldValue = oldValue;
		this.newValue = newValue;
		
		this.modelRevision = modelRevision;
		this.objectRevision = objectRevision;
		this.fieldRevision = fieldRevision;
	}
	
	@Override
	public String toString() {
		String prefix = "FieldEvent by " + getActor() + ": ";
		String suffix = " @" + getTarget() + " r" + rev2str(this.modelRevision) + "/"
		        + rev2str(this.objectRevision) + "/" + rev2str(this.fieldRevision);
		switch(getChangeType()) {
		case ADD:
			return prefix + "ADD " + this.newValue + suffix;
		case REMOVE:
			return prefix + "REMOVE " + this.oldValue + suffix + (isImplied() ? " [implied]" : "");
		case CHANGE:
			return prefix + "CHANGE " + this.oldValue + " to " + this.newValue + suffix;
		default:
			throw new RuntimeException("this field event should have never been created");
		}
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
	
	public XAddress getChangedEntity() {
		return getTarget();
	}
	
}

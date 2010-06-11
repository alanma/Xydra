package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * The implementation of XObjectEvent
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
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XObjectEvent))
			return false;
		XObjectEvent event = (XObjectEvent)object;
		
		if(!this.fieldID.equals(event.getFieldID()))
			return false;
		
		if(this.modelRevision != event.getModelRevisionNumber())
			return false;
		
		if(this.objectRevision != event.getObjectRevisionNumber())
			return false;
		
		if(this.fieldRevision != event.getFieldRevisionNumber())
			return false;
		
		if(this.inTransaction != event.inTransaction())
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.fieldID.hashCode();
		
		// old revisions
		result += this.modelRevision;
		result += this.objectRevision;
		result += this.fieldRevision;
		
		return result;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the add-type (field add)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the object to which the field is to
	 *            be added - object ID must not be null
	 * @param fieldID The {@link XID} of the added field - must not be null
	 * @param fieldRevision the revision number of the object this event applies
	 *            to
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return Returns an XObjectEvent of the add-type or null if
	 *         objectID/fieldID was null
	 */
	
	public static XObjectEvent createAddEvent(XID actor, XAddress target, XID fieldID,
	        long objectRevision, boolean inTransaction) {
		return createAddEvent(actor, target, fieldID, XEvent.RevisionOfEntityNotSet,
		        objectRevision, inTransaction);
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the add-type (field add)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the object to which the field is to
	 *            be added - object ID must not be null
	 * @param fieldID The {@link XID} of the added field - must not be null
	 * @param modelRevision the revision number of the model holding the object
	 *            this event applies to
	 * @param fieldRevision the revision number of the object this event applies
	 *            to
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return Returns an XObjectEvent of the add-type or null if
	 *         objectID/fieldID was null
	 */
	
	public static XObjectEvent createAddEvent(XID actor, XAddress target, XID fieldID,
	        long modelRevision, long objectRevision, boolean inTransaction) {
		
		return new MemoryObjectEvent(actor, target, fieldID, ChangeType.ADD, modelRevision,
		        objectRevision, XEvent.RevisionOfEntityNotSet, inTransaction);
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the remove-type (field removed)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the object from which the field is
	 *            to be removed - object ID must not be null
	 * @param fieldID The XID of the removed field - must not be null
	 * @param fieldRevision the revision number of the object this event applies
	 *            to
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return Returns an XObjectEvent of the remove-type
	 */
	
	public static XObjectEvent createRemoveEvent(XID actor, XAddress target, XID fieldID,
	        long objectRevision, long fieldRevision, boolean inTransaction) {
		return createRemoveEvent(actor, target, fieldID, XEvent.RevisionOfEntityNotSet,
		        objectRevision, fieldRevision, inTransaction);
		
	}
	
	/**
	 * Returns an {@link XObjectEvent} of the remove-type (field removed)
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param target The {@link XAddress} of the object from which the field is
	 *            to be removed - object ID must not be null
	 * @param fieldID The XID of the removed field - must not be null
	 * @param modelRevision the revision number of the model holding the object
	 *            this event applies to
	 * @param fieldRevision the revision number of the object this event applies
	 *            to
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return Returns an XObjectEvent of the remove-type
	 */
	
	public static XObjectEvent createRemoveEvent(XID actor, XAddress target, XID fieldID,
	        long modelRevision, long objectRevision, long fieldRevision, boolean inTransaction) {
		if(fieldRevision == XEvent.RevisionOfEntityNotSet) {
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
		
		if(fieldID == null || objectRevision == XEvent.RevisionOfEntityNotSet) {
			throw new IllegalArgumentException(
			        "field ID and object revision must be set for object events");
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
		if(this.fieldRevision != XEvent.RevisionOfEntityNotSet)
			str += " r" + this.fieldRevision;
		str += " @" + getTarget();
		str += " r"
		        + (this.modelRevision == XEvent.RevisionOfEntityNotSet ? "-" : this.modelRevision)
		        + "/" + this.objectRevision;
		return str;
	}
	
	@Override
	public long getFieldRevisionNumber() {
		return this.fieldRevision;
	}
	
	@Override
	public long getModelRevisionNumber() {
		return this.modelRevision;
	}
	
	@Override
	public long getObjectRevisionNumber() {
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

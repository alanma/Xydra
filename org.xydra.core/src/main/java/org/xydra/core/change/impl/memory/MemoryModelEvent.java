package org.xydra.core.change.impl.memory;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;


/**
 * The implementation of XModelEvent.
 * 
 * @author Kaidel
 */

public class MemoryModelEvent extends MemoryAtomicEvent implements XModelEvent {
	
	// The XID of the object that was created/deleted
	private final XID objectID;
	
	// the revision numbers before the event happened
	private final long modelRevision, objectRevision;
	
	// was this event part of a transaction or not?
	private final boolean inTransaction;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XModelEvent))
			return false;
		XModelEvent event = (XModelEvent)object;
		
		if(!this.objectID.equals(event.getObjectID()))
			return false;
		
		if(this.modelRevision != event.getModelRevisionNumber())
			return false;
		
		if(this.objectRevision != event.getObjectRevisionNumber())
			return false;
		
		if(this.inTransaction != event.inTransaction())
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.objectID.hashCode();
		
		// old revisions
		result += this.modelRevision;
		result += this.objectRevision;
		
		return result;
	}
	
	public boolean inTransaction() {
		return this.inTransaction;
	}
	
	/**
	 * Returns an XModelEvent of the add-type (object added)
	 * 
	 * @param actor The XID of the actor
	 * @param repositoryID The XID of the XRepository containing the XModel
	 *            which the object was added to
	 * @param modelID The XModel which the object was added to - must not be
	 *            null
	 * @param objectID The XID of the added object - must not be null
	 * @param objectRevision the revision number of the model this event applies
	 *            to
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return An XModelEvent of the add-type or null if modelID/objectID was
	 *         null
	 */
	
	public static XModelEvent createAddEvent(XID actor, XAddress target, XID objectID,
	        long modelRevision, boolean inTransaction) {
		return new MemoryModelEvent(actor, target, objectID, ChangeType.ADD, modelRevision,
		        XEvent.RevisionOfEntityNotSet, inTransaction);
	}
	
	/**
	 * Returns an XModelEvent of the remove-type (object removed)
	 * 
	 * @param actor The XID of the actor
	 * @param repositoryID The XID of the XRepository containing the XModel
	 *            which the object was removed from
	 * @param modelID The XModel which the object was removed from
	 * @param objectID The XID of the removed object
	 * @param inTransaction sets whether this event occurred during a
	 *            transaction or not
	 * @return An XModelEvent of the remove-type
	 */
	
	public static XModelEvent createRemoveEvent(XID actor, XAddress target, XID objectID,
	        long modelRevision, long objectRevision, boolean inTransaction) {
		if(objectRevision == XEvent.RevisionOfEntityNotSet) {
			throw new IllegalArgumentException(
			        "object revision must be set for model REMOVE events");
		}
		
		return new MemoryModelEvent(actor, target, objectID, ChangeType.REMOVE, modelRevision,
		        objectRevision, inTransaction);
	}
	
	// private constructor, use the createEvent for instantiating MemModelEvents
	private MemoryModelEvent(XID actor, XAddress target, XID objectID, ChangeType changeType,
	        long modelRevision, long objectRevision, boolean inTransaction) {
		super(target, changeType, actor);
		
		if(target.getModel() == null || target.getObject() != null) {
			throw new IllegalArgumentException("target must refer to a model, was: " + target);
		}
		
		if(objectID == null) {
			throw new IllegalArgumentException("object ID must be set for model events, is null");
		}
		if(modelRevision == XEvent.RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("revision must be set for model events");
		}
		
		this.objectID = objectID;
		this.objectRevision = objectRevision;
		this.modelRevision = modelRevision;
		this.inTransaction = inTransaction;
	}
	
	@Override
	public String toString() {
		String str = "ModelEvent: " + getChangeType() + " " + this.objectID;
		if(this.objectRevision != XEvent.RevisionOfEntityNotSet)
			str += " r" + this.objectRevision;
		str += " @" + getTarget();
		str += " r" + this.modelRevision;
		return str;
	}
	
	@Override
	public long getObjectRevisionNumber() {
		return this.objectRevision;
	}
	
	@Override
	public long getModelRevisionNumber() {
		return this.modelRevision;
	}
	
	@Override
	public XID getObjectID() {
		return this.objectID;
	}
	
}

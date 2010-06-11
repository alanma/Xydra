package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;


/**
 * The implementation of XRepositoryEvent
 * 
 * @author Kaidel
 * 
 */

public class MemoryRepositoryEvent extends MemoryAtomicEvent implements XRepositoryEvent {
	
	// The XID of the model that was added/deleted
	private final XID modelID;
	
	// the model revision before this event happened
	private final long modelRevision;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XRepositoryEvent))
			return false;
		XRepositoryEvent event = (XRepositoryEvent)object;
		
		if(!this.modelID.equals(event.getModelID()))
			return false;
		
		if(this.modelRevision != event.getModelRevisionNumber())
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.modelID.hashCode();
		
		// old revisions
		result += this.modelRevision;
		
		return result;
	}
	
	public boolean inTransaction() {
		// repository events never occur in transactions
		return false;
	}
	
	/**
	 * Returns an {@link XRepositoryEvent} of the add-type (model added)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            model is to be added to - repository ID must not be null
	 * @param modelID The {@link XID} of the model which is to be added - must
	 *            not be null
	 * @return An {@link XRepositoryEvent} of the add-type or null if
	 *         repositoryID/modelID was null
	 */
	
	public static XRepositoryEvent createAddEvent(XID actor, XAddress target, XID modelID) {
		return new MemoryRepositoryEvent(actor, target, modelID, ChangeType.ADD,
		        XEvent.RevisionOfEntityNotSet);
	}
	
	/**
	 * Returns an {@link XRepositoryEvent} of the remove-type (model remove)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            model is to be added to - repository ID must not be null
	 * @param modelID The {@link XID} of the model which is to be added - must
	 *            not be null
	 * @return An {@link XRepositoryEvent} of the add-type or null if
	 *         repositoryID/modelID was null
	 */
	
	public static XRepositoryEvent createRemoveEvent(XID actor, XAddress target, XID modelID,
	        long modelRevison) {
		if(modelRevison == XEvent.RevisionOfEntityNotSet) {
			throw new IllegalArgumentException(
			        "model revision must be set for repository REMOVE events");
		}
		
		return new MemoryRepositoryEvent(actor, target, modelID, ChangeType.REMOVE, modelRevison);
	}
	
	// private constructor, use the createEvent methods for instantiating a
	// MemRepositoryEvent
	private MemoryRepositoryEvent(XID actor, XAddress target, XID modelID, ChangeType changeType,
	        long modelRevision) {
		super(target, changeType, actor);
		
		if(target.getRepository() == null || target.getModel() != null) {
			throw new IllegalArgumentException("target must refer to a repository, was: " + target);
		}
		
		if(modelID == null) {
			throw new IllegalArgumentException("model ID must be set for repository events");
		}
		
		this.modelID = modelID;
		this.modelRevision = modelRevision;
	}
	
	@Override
	public String toString() {
		String str = "RepositoryEvent: " + getChangeType() + " " + this.modelID;
		if(this.modelRevision != XEvent.RevisionOfEntityNotSet)
			str += " r" + this.modelRevision;
		str += " @" + getTarget();
		return str;
	}
	
	@Override
	public long getModelRevisionNumber() {
		return this.modelRevision;
	}
	
	@Override
	public XID getModelID() {
		return this.modelID;
	}
	
	public XAddress getChangedEntity() {
		return XX.resolveModel(getTarget(), getModelID());
	}
	
}

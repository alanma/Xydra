package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An implementation of {@link XRepositoryEvent}
 * 
 * @author Kaidel
 * 
 */

public class MemoryRepositoryEvent extends MemoryAtomicEvent implements XRepositoryEvent {
	
	// The XID of the model that was added/deleted
	private final XID modelID;
	
	// the model revision before this event happened
	private final long modelRevision;
	
	private final boolean inTransaction;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XRepositoryEvent))
			return false;
		XRepositoryEvent event = (XRepositoryEvent)object;
		
		if(!this.modelID.equals(event.getModelID()))
			return false;
		
		if(this.modelRevision != event.getOldModelRevision())
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
		return this.inTransaction;
	}
	
	/**
	 * Creates a new {@link XRepositoryEvent} of the add-type (an {@link XModel}
	 * was added to the {@link XRepository} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            {@link XModel} was added to - repository {@link XID} must not
	 *            be null
	 * @param modelID The {@link XID} of the added {@link XModel} - must not be
	 *            null
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository} or if the given modelID is
	 *             null
	 */
	public static XRepositoryEvent createAddEvent(XID actor, XAddress target, XID modelID) {
		return new MemoryRepositoryEvent(actor, target, modelID, ChangeType.ADD,
		        RevisionOfEntityNotSet, false);
	}
	
	/**
	 * Creates a new {@link XRepositoryEvent} of the add-type (an {@link XModel}
	 * was added to the {@link XRepository} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            {@link XModel} was added to - repository {@link XID} must not
	 *            be null
	 * @param modelID The {@link XID} of the added {@link XModel} - must not be
	 *            null
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository} or if the given modelID is
	 *             null
	 */
	public static XRepositoryEvent createAddEvent(XID actor, XAddress target, XID modelID,
	        long modelRev, boolean inTrans) {
		return new MemoryRepositoryEvent(actor, target, modelID, ChangeType.ADD, modelRev, inTrans);
	}
	
	/**
	 * Creates a new {@link XRepositoryEvent} of the remove-type (an
	 * {@link XModel} was removed from the {@link XRepository} this event refers
	 * to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            {@link XModel} was removed from - repository {@link XID} must
	 *            not be null
	 * @param modelID The {@link XID} of the removed {@link XModel} - must not
	 *            be null
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository}, if the given modelID is null
	 *             or if the given modelRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 */
	public static XRepositoryEvent createRemoveEvent(XID actor, XAddress target, XID modelID,
	        long modelRevison, boolean inTrans) {
		if(modelRevison < 0) {
			throw new IllegalArgumentException(
			        "model revision must be set for repository REMOVE events");
		}
		
		return new MemoryRepositoryEvent(actor, target, modelID, ChangeType.REMOVE, modelRevison,
		        inTrans);
	}
	
	// private constructor, use the createEvent methods for instantiating a
	// MemRepositoryEvent
	private MemoryRepositoryEvent(XID actor, XAddress target, XID modelID, ChangeType changeType,
	        long modelRevision, boolean inTrans) {
		super(target, changeType, actor);
		
		if(target.getRepository() == null || target.getModel() != null) {
			throw new IllegalArgumentException("target must refer to a repository, was: " + target);
		}
		
		if(modelID == null) {
			throw new IllegalArgumentException("model ID must be set for repository events");
		}
		
		if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
		}
		
		this.modelID = modelID;
		this.modelRevision = modelRevision;
		this.inTransaction = inTrans;
	}
	
	@Override
	public String toString() {
		String str = "RepositoryEvent: " + getChangeType() + " " + this.modelID;
		if(this.modelRevision >= 0)
			str += " r" + rev2str(this.modelRevision);
		str += " @" + getTarget();
		return str;
	}
	
	@Override
	public long getOldModelRevision() {
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

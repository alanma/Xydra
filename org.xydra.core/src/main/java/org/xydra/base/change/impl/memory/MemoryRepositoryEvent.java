package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An implementation of {@link XRepositoryEvent}
 * 
 * @author Kaidel
 * 
 */
@RunsInGWT(true)
public class MemoryRepositoryEvent extends MemoryAtomicEvent implements XRepositoryEvent {
	
	private static final long serialVersionUID = 4709068915672914712L;
	
	/**
	 * Creates a new {@link XRepositoryEvent} of the add-type (an {@link XModel}
	 * was added to the {@link XRepository} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            {@link XModel} was added to - repository {@link XID} must not
	 *            be null
	 * @param modelId The {@link XID} of the added {@link XModel} - must not be
	 *            null
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository} or if the given modelId is
	 *             null
	 */
	public static XRepositoryEvent createAddEvent(XID actor, XAddress target, XID modelId) {
		return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD,
		        RevisionOfEntityNotSet, false, false);
	}
	
	public static XRepositoryEvent createFrom(XRepositoryEvent re) {
		MemoryRepositoryEvent event = new MemoryRepositoryEvent(re.getActor(), re.getTarget(),
		        re.getModelId(), re.getChangeType(), re.getOldModelRevision(), re.inTransaction(),
		        re.isImplied());
		return event;
	}
	
	/**
	 * Creates a new {@link XRepositoryEvent} of the add-type (an {@link XModel}
	 * was added to the {@link XRepository} this event refers to)
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param target The {@link XAddress} of the {@link XRepository} which the
	 *            {@link XModel} was added to - repository {@link XID} must not
	 *            be null
	 * @param modelId The {@link XID} of the added {@link XModel} - must not be
	 *            null
	 * @param modelRev
	 * @param inTrans
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository} or if the given modelId is
	 *             null
	 */
	public static XRepositoryEvent createAddEvent(XID actor, XAddress target, XID modelId,
	        long modelRev, boolean inTrans) {
		return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD, modelRev, inTrans,
		        false);
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
	 * @param modelId The {@link XID} of the removed {@link XModel} - must not
	 *            be null
	 * @param modelRevison of the remove event
	 * @param inTrans if in transaction
	 * @return An {@link XRepositoryEvent} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XRepository}, if the given modelId is null
	 *             or if the given modelRevision equals
	 *             {@link XEvent#RevisionOfEntityNotSet}
	 */
	public static XRepositoryEvent createRemoveEvent(XID actor, XAddress target, XID modelId,
	        long modelRevison, boolean inTrans) {
		if(modelRevison < 0) {
			throw new IllegalArgumentException(
			        "model revision must be set for repository REMOVE events");
		}
		
		return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.REMOVE, modelRevison,
		        inTrans, false);
	}
	
	// The XID of the model that was added/deleted
	private XID modelId;
	
	// the model revision before this event happened
	private long modelRevision;
	
	// private constructor, use the createEvent methods for instantiating a
	// MemoryRepositoryEvent
	private MemoryRepositoryEvent(XID actor, XAddress target, XID modelId, ChangeType changeType,
	        long modelRevision, boolean inTrans, boolean implied) {
		super(target, changeType, actor, inTrans, implied);
		
		if(target.getRepository() == null || target.getModel() != null) {
			throw new IllegalArgumentException("target must refer to a repository, was: " + target);
		}
		
		if(modelId == null) {
			throw new IllegalArgumentException("model ID must be set for repository events");
		}
		
		if(modelRevision < 0 && modelRevision != RevisionOfEntityNotSet) {
			throw new IllegalArgumentException("invalid modelRevision: " + modelRevision);
		}
		
		this.modelId = modelId;
		this.modelRevision = modelRevision;
	}
	
	/**
	 * GWT only
	 */
	protected MemoryRepositoryEvent() {
		
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object)) {
			return false;
		}
		
		if(!(object instanceof XRepositoryEvent)) {
			return false;
		}
		XRepositoryEvent event = (XRepositoryEvent)object;
		
		if(!this.modelId.equals(event.getModelId())) {
			return false;
		}
		
		if(this.modelRevision != event.getOldModelRevision()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public XAddress getChangedEntity() {
		return XX.resolveModel(getTarget(), getModelId());
	}
	
	@Override
	public XID getModelId() {
		return this.modelId;
	}
	
	@Override
	public long getOldModelRevision() {
		return this.modelRevision;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.modelId.hashCode();
		
		// old revisions
		result += this.modelRevision;
		
		return result;
	}
	
	@Override
	public String toString() {
		String str = "RepositoryEvent by actor:" + getActor() + " " + getChangeType() + " modelId:"
		        + this.modelId;
		if(this.modelRevision >= 0) {
			str += " r" + rev2str(this.modelRevision);
		}
		str += " @" + getTarget();
		return str;
	}
	
}

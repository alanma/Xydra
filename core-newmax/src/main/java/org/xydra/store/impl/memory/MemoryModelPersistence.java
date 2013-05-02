package org.xydra.store.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;


/**
 * A helper class used by {@link MemoryPersistence} to manage individual models.
 * 
 * @author dscharrer
 * 
 */

public class MemoryModelPersistence {
	
	static private Logger log = LoggerFactory.getLogger(MemoryModelPersistence.class);
	
	private List<XEvent> events = new ArrayList<XEvent>();
	
	/**
	 * The current state of the model, or null if the model doesn't currently
	 * exist.
	 */
	private XRevWritableModel model = null;
	XAddress modelAddr;
	
	public MemoryModelPersistence(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	synchronized public long executeCommand(XId actorId, XCommand command) {
		
		long newRev = getRevisionNumber() + 1;
		
		// Parse the command and calculate the changes needed to apply it. This
		// does not actually change the model.
		Pair<ChangedModel,DeltaUtils.ModelChange> change = DeltaUtils.executeCommand(this.model,
		        command);
		if(change == null) {
			// There was something wrong with the command.
			return XCommand.FAILED;
		}
		
		// Create events. Do this before we destroy any necessary information by
		// changing the model.
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, change, actorId,
		        newRev, command.getChangeType() == ChangeType.TRANSACTION);
		XyAssert.xyAssert(events != null);
		assert events != null;
		
		if(events.isEmpty()) {
			/*
			 * Note how this differs from the GAE implementation where failed
			 * commands produce a NoChange event
			 */
			return XCommand.NOCHANGE;
		}
		
		XEvent event;
		if(events.size() > 1 || command.getChangeType() == ChangeType.TRANSACTION) {
			// Create a transaction event.
			
			// check whether it needs to be a model or an object transaction
			if(command.getTarget().getAddressedType() == XType.XMODEL
			        || command.getTarget().getAddressedType() == XType.XREPOSITORY) {
				/*
				 * if the target is a repository and the list of events contains
				 * more than one event, a model with child-object was removed,
				 * which is why we need to construct a transaction event in this
				 * case.
				 */
				
				event = MemoryTransactionEvent.createTransactionEvent(actorId, this.modelAddr,
				        events, getRevisionNumber(), XEvent.REVISIONOFENTITYNOTSET);
			} else {
				assert command.getTarget().getAddressedType() == XType.XOBJECT;
				
				event = MemoryTransactionEvent.createTransactionEvent(actorId, command.getTarget(),
				        events, XEvent.REVISIONOFENTITYNOTSET, getRevisionNumber());
			}
			
		} else {
			event = events.get(0);
		}
		this.events.add(event);
		XyAssert.xyAssert(this.events.get((int)newRev) == event);
		
		// Actually apply the changes.
		this.model = DeltaUtils.applyChanges(this.modelAddr, this.model, change, newRev);
		
		XyAssert.xyAssert(getRevisionNumber() == newRev);
		XyAssert.xyAssert(this.model == null || this.model.getRevisionNumber() == newRev);
		
		return newRev;
	}
	
	public boolean exists() {
		return this.model != null;
	}
	
	synchronized public List<XEvent> getEvents(XAddress address, long beginRevision,
	        long endRevision) {
		
		if(this.events.isEmpty()) {
			return null;
		}
		
		long currentRev = getRevisionNumber();
		long start = beginRevision < 0 ? 0 : beginRevision;
		long end = endRevision > currentRev ? currentRev : endRevision;
		
		log.info("getEvents: br" + beginRevision + " er" + endRevision + " cr" + currentRev
		        + " size" + this.events.size() + " mr"
		        + (this.model == null ? -2 : this.model.getRevisionNumber()) + " s" + start + " e"
		        + end);
		
		if(start > end) {
			// happens if start >= currentRev, which is allowed
			return new ArrayList<XEvent>();
		}
		
		List<XEvent> result = new ArrayList<XEvent>();
		
		/*
		 * filter a (sub-) list
		 * 
		 * Note: Can handle max. Integer.MAX events = 2^31 which is a lot of
		 * events and the standard java containers cannot contain more anyway,
		 * at least the array-based ArrayList.
		 */
		for(XEvent xe : this.events.subList((int)start, (int)end + 1)) {
			// TODO how to filter transaction events? ~Daniel
			// TODO should this filtering be done in the calling
			// DelegateToPersistenceAndArm since it needs to filter for access
			// rights anyway?
			if(address.equalsOrContains(xe.getChangedEntity())) {
				result.add(xe);
			}
		}
		
		return result;
	}
	
	/**
	 * @return the snapshot or null if not found
	 */
	synchronized public XRevWritableModel getModelSnapshot() {
		return XCopyUtils.createSnapshot(this.model);
	}
	
	synchronized public XRevWritableObject getObjectSnapshot(XId objectId) {
		/*
		 * if this model has not been created yet, there cannot be an object
		 * snapshot
		 */
		if(this.model == null) {
			return null;
		}
		
		return XCopyUtils.createSnapshot(this.model.getObject(objectId));
	}
	
	synchronized public long getRevisionNumber() {
		return this.events.size() - 1;
	}
	
	public ModelRevision getModelRevision() {
		return new ModelRevision(getRevisionNumber(), exists());
	}
	
}

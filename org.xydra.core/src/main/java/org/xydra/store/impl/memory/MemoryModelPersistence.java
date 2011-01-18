package org.xydra.store.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.store.MAXDone;
import org.xydra.store.base.SimpleModel;
import org.xydra.store.base.SimpleObject;


/**
 * A helper class used by {@link MemoryPersistence} to manage individual models.
 * 
 * @author dscharrer
 * 
 */
@MAXDone
public class MemoryModelPersistence {
	
	XAddress modelAddr;
	
	/**
	 * The current state of the model, or null if the model doesn't currently
	 * exist.
	 */
	private SimpleModel model = null;
	private List<XEvent> events = new ArrayList<XEvent>();
	
	public MemoryModelPersistence(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	synchronized public long executeCommand(XID actorId, XCommand command) {
		
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
		List<XAtomicEvent> events = DeltaUtils
		        .createEvents(this.modelAddr, change, actorId, newRev);
		assert events != null;
		
		if(events.isEmpty()) {
			// TODO take up a revision anyway with a null event to better test
			// users of the API? (to mimic behaviour of the GAE implementation)
			return XCommand.NOCHANGE;
		}
		
		XEvent event;
		if(events.size() > 1) {
			// Create a transaction event.
			event = MemoryTransactionEvent.createTransactionEvent(actorId, this.modelAddr, events,
			        getRevisionNumber(), XEvent.RevisionOfEntityNotSet);
		} else {
			event = events.get(0);
		}
		this.events.add(event);
		assert this.events.get((int)newRev) == event;
		
		// Actually apply the changes.
		this.model = DeltaUtils.applyChanges(this.modelAddr, this.model, change, newRev);
		
		assert getRevisionNumber() == newRev;
		assert this.model == null || this.model.getRevisionNumber() == newRev;
		
		return newRev;
	}
	
	synchronized public List<XEvent> getEvents(XAddress address, long beginRevision,
	        long endRevision) {
		
		long currentRev = getRevisionNumber();
		long start = beginRevision < 0 ? 0 : beginRevision;
		long end = endRevision > currentRev ? currentRev : endRevision;
		
		if(start > end) {
			// happens if start >= currentRev, which is allowed
			return new ArrayList<XEvent>();
		}
		
		if(start == 0 && end == endRevision) {
			// we still need to copy the list because the caller might expect to
			// have a instance it can modify when doing filtering.
			List<XEvent> result = new ArrayList<XEvent>();
			result.addAll(this.events);
			return result;
		}
		
		List<XEvent> result = new ArrayList<XEvent>();
		
		// filter a sub-list
		// TODO IMROVE can handle max. Integer.MAX events. -- So what? 2^31 is a
		// lot of events and the standard java containers cannot contain more
		// anyway, at least the array-based ArrayList. ~Daniel
		
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
	
	synchronized public long getRevisionNumber() {
		return this.events.size() - 1;
	}
	
	/**
	 * @return the snapshot or null if not found
	 */
	synchronized public SimpleModel getModelSnapshot() {
		return XCopyUtils.createSnapshot(this.model);
	}
	
	synchronized public SimpleObject getObjectSnapshot(XID objectId) {
		
		if(this.model == null) {
			// TODO is this the correct behaviour?
			return null;
		}
		
		return XCopyUtils.createSnapshot(this.model.getObject(objectId));
	}
	
	public boolean exists() {
		return this.model != null;
	}
	
}

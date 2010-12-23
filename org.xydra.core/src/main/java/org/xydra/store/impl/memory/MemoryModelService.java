package org.xydra.store.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.store.base.SimpleModel;


/**
 * A helper class used by {@link MemoryNoAccessRightsNoBatchNoAsyncStore} to
 * manage individual models.
 * 
 * @author dscharrer
 * 
 */
public class MemoryModelService {
	
	XAddress modelAddr;
	
	/*
	 * FIXME model is never initialized in this class, which means that all
	 * methods like executeCommand always fail from what I can see at the
	 * moment. ~bjoern
	 */
	SimpleModel model = null;
	private List<XEvent> events = new ArrayList<XEvent>();
	
	public MemoryModelService(XAddress modelAddr) {
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
		
		return newRev;
	}
	
	synchronized public XEvent[] getEvents(XAddress address, long beginRevision, long endRevision) {
		
		long rev = this.events.size();
		long start = beginRevision < 0 ? 0 : beginRevision;
		long end = endRevision > rev ? rev : endRevision;
		
		if(start == 0 && end == rev) {
			return this.events.toArray(new XEvent[this.events.size()]);
		}
		
		List<XEvent> result = new ArrayList<XEvent>();
		
		// filter a sub-list
		// TODO IMROVE can handle max. Integer.MAX events
		for(XEvent xe : this.events.subList((int)start, (int)end)) {
			// TODO how to filter transaction events? ~Daniel
			if(address.equalsOrContains(xe.getChangedEntity())) {
				result.add(xe);
			}
		}
		
		return result.toArray(new XEvent[result.size()]);
	}
	
	synchronized public long getRevisionNumber() {
		return this.events.size() - 1;
	}
	
	synchronized public XBaseModel getModelSnapshot() {
		return XCopyUtils.createSnapshot(this.model);
	}
	
	synchronized public XBaseObject getObjectSnapshot(XID objectId) {
		
		if(this.model == null) {
			// TODO is this the correct behaviour?
			return null;
		}
		
		return XCopyUtils.createSnapshot(this.model.getObject(objectId));
	}
	
}

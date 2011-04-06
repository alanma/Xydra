package org.xydra.base.rmof.impl.delegate;

import java.util.Iterator;
import java.util.List;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Delegates all reads and writes to a {@link XydraPersistence}
 * 
 * Additionally you can retrieve all events that happened since the revision of
 * this entity since the last call of {@link #ignoreAllEventsUntilNow()}.
 * 
 * @author xamde
 */
public class WritableModelOnPersistence extends AbstractWritableOnPersistence implements
        XWritableModel {
	
	private long lastRev = 0;
	
	private XID modelId;
	
	public WritableModelOnPersistence(XydraPersistence persistence, XID executingActorId,
	        XID modelId) {
		super(persistence, executingActorId);
		this.modelId = modelId;
	}
	
	public XWritableObject createObject(XID objectId) {
		assert this.persistence.hasModel(this.modelId);
		XWritableObject object = this.getObject(objectId);
		if(object != null) {
			return object;
		}
		// create in persistence
		XCommand command = X.getCommandFactory().createAddObjectCommand(
		        this.persistence.getRepositoryId(), this.modelId, objectId, false);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0 : "Command " + command + " returned " + commandResult;
		return getObject(objectId);
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, null, null);
		}
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.modelId;
	}
	
	/**
	 * @return an array of XEvent or null if there are no events. The array
	 *         contains all events that happened in the underlying
	 *         {@link XydraPersistence} since the last call of this method.
	 */
	public List<XEvent> getNewEvents() {
		// get highestEvent
		long currentRev = this.persistence.getModelRevision(this.getAddress());
		List<XEvent> events = null;
		if(currentRev > this.lastRev) {
			// get event between lastProcessed and highest
			events = this.persistence.getEvents(getAddress(), this.lastRev, currentRev);
		}
		this.lastRev = currentRev;
		return events;
	}
	
	public XWritableObject getObject(XID objectId) {
		if(hasObject(objectId)) {
			// make sure changes to object are reflected in persistence
			return new WritableObjectOnPersistence(this.persistence, this.executingActorId,
			        this.modelId, objectId);
		} else {
			return null;
		}
	}
	
	public long getRevisionNumber() {
		return this.persistence.getModelSnapshot(getAddress()).getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		XWritableModel modelSnapshot = this.persistence.getModelSnapshot(getAddress());
		if(modelSnapshot == null) {
			return false;
		}
		return modelSnapshot.hasObject(objectId);
	}
	
	/**
	 * Allows load() methods to init this model correctly.
	 */
	public void ignoreAllEventsUntilNow() {
		this.lastRev = this.persistence.getModelRevision(this.getAddress());
	}
	
	public boolean isEmpty() {
		return this.persistence.getModelSnapshot(getAddress()).isEmpty();
	}
	
	public Iterator<XID> iterator() {
		XWritableModel modelSnapshot = this.persistence.getModelSnapshot(getAddress());
		if(modelSnapshot == null || modelSnapshot.isEmpty()) {
			return new NoneIterator<XID>();
		}
		return modelSnapshot.iterator();
	}
	
	public boolean removeObject(XID objectId) {
		boolean result = hasObject(objectId);
		XCommand command = X.getCommandFactory().createRemoveObjectCommand(
		        this.persistence.getRepositoryId(), this.modelId, objectId, XCommand.FORCED, false);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result && commandResult >= 0;
	}
	
}

package org.xydra.store.impl.gae;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl2;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotService2;


/**
 * Xydra allows transaction only within one model. The GAE implementation
 * maintains one change log per model. This class keeps all access to a model
 * within the datastore and memcache in one place.
 * 
 * Goal: If the datastore or memcache is called to read or write a certain
 * model, that access is triggered only from here.
 * 
 * @author xamde
 * 
 */
public class GaeModelPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(GaeModelPersistence.class);
	
	private XAddress modelAddress;
	private IGaeChangesService changesService;
	private GaeSnapshotService2 snapshotService;
	
	public GaeModelPersistence(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.changesService = new GaeChangesServiceImpl2(this.modelAddress);
		this.snapshotService = new GaeSnapshotService2(this.modelAddress, this.changesService);
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		return this.changesService.executeCommand(command, actorId);
	}
	
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
		/*
		 * TODO(Complete Impl) filter events (objectevents, fieldevents) if
		 * address is not a model address?
		 */
		return this.changesService.getEventsBetween(beginRevision, endRevision);
	}
	
	public long getCurrentRevisionNumber() {
		return this.changesService.getCurrentRevisionNumber();
	}
	
	synchronized public XWritableModel getSnapshot() {
		/* get fresh current revNr */
		long currentRevNr = this.changesService.getCurrentRevisionNumber();
		if(currentRevNr == -1) {
			return null;
		}
		if(currentRevNr == 0) {
			// model is empty
			return new SimpleModel(this.modelAddress);
		}
		XWritableModel snapshot = this.snapshotService.getSnapshot(currentRevNr);
		log.trace("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
		return snapshot;
	}
	
	public XWritableObject getObjectSnapshot(XID objectId) {
		/*
		 * IMPROVE(performance, defer) generate the object snapshot directly.
		 * While reading the change events, one could skip reading large,
		 * unaffected XVALUes.
		 * 
		 * Idea 2: Put object snapshots individually in a cache -- maybe only
		 * large ones?
		 * 
		 * Ideas 3: Easy to implement: If localVMcache has it, copy directly
		 * just the object from it.
		 */
		XWritableModel modelSnapshot = getSnapshot();
		if(modelSnapshot == null) {
			return null;
		}
		return modelSnapshot.getObject(objectId);
	}
	
}

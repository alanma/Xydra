package org.xydra.store.impl.gae;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl2;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.execute.GaeExecutionServiceImpl3;
import org.xydra.store.impl.gae.execute.IGaeExecutionService;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotServiceImpl2;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;


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
	private IGaeSnapshotService snapshotService;
	private IGaeExecutionService executionService;
	
	public GaeModelPersistence(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.changesService = new GaeChangesServiceImpl2(this.modelAddress);
		this.snapshotService = new GaeSnapshotServiceImpl2(this.changesService);
		this.executionService = new GaeExecutionServiceImpl3(this.changesService,
		        this.snapshotService);
		/*
		 * this.executionService = new
		 * GaeExecutionServiceImpl3(this.changesService, this.snapshotService);
		 */
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		return this.executionService.executeCommand(command, actorId);
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
		boolean modelExists = this.changesService.exists();
		if(!modelExists) {
			return null;
		}
		long currentRevNr = this.changesService.getCurrentRevisionNumber();
		XWritableModel snapshot = this.snapshotService.getModelSnapshot(currentRevNr, false);
		log.debug("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
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
	
	public RevisionState getModelRevision() {
		return new RevisionState(getCurrentRevisionNumber(), this.changesService.exists());
	}
	
	@Override
	public int hashCode() {
		return this.modelAddress.hashCode();
		
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof GaeModelPersistence
		        && ((GaeModelPersistence)other).modelAddress.equals(this.modelAddress);
		
	}
	
}

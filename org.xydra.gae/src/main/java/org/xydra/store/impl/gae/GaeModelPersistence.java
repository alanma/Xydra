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
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl3;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.execute.GaeExecutionServiceImpl3;
import org.xydra.store.impl.gae.execute.IGaeExecutionService;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotServiceImpl3;
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
	
	private final XAddress modelAddress;
	private final IGaeChangesService changesService;
	private final IGaeSnapshotService snapshotService;
	private final IGaeExecutionService executionService;
	private final RevisionManager revisionManager;
	
	public GaeModelPersistence(XAddress modelAddress) {
		this.revisionManager = new RevisionManager(modelAddress);
		this.modelAddress = modelAddress;
		this.changesService = new GaeChangesServiceImpl3(this.modelAddress, this.revisionManager);
		this.snapshotService = new GaeSnapshotServiceImpl3(this.changesService);
		this.executionService = new GaeExecutionServiceImpl3(this.revisionManager,
		        this.changesService, this.snapshotService);
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		assertInstanceRevisionNumberHasBeenInitialized();
		return this.executionService.executeCommand(command, actorId);
	}
	
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
		assertThreadLocalRevisionNumberIsUpToDate();
		return this.changesService.getEventsBetween(address, beginRevision, endRevision);
	}
	
	private void assertThreadLocalRevisionNumberIsUpToDate() {
		if(this.revisionManager.isThreadLocallyDefined()) {
			// fine
		} else {
			this.changesService.calculateCurrentModelRevision();
		}
	}
	
	private void assertInstanceRevisionNumberHasBeenInitialized() {
		if(this.revisionManager.getInstanceRevisionState() == null) {
			this.changesService.calculateCurrentModelRevision();
		}
	}
	
	synchronized public XWritableModel getSnapshot() {
		assertThreadLocalRevisionNumberIsUpToDate();
		ModelRevision currentRevision = this.revisionManager.getThreadLocalRevision();
		if(!currentRevision.modelExists()) {
			return null;
		}
		long currentRevNr = currentRevision.revision();
		XWritableModel snapshot = this.snapshotService.getModelSnapshot(currentRevNr, false);
		log.debug("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
		return snapshot;
	}
	
	public XWritableObject getObjectSnapshot(XID objectId) {
		assertThreadLocalRevisionNumberIsUpToDate();
		ModelRevision currentRevision = this.revisionManager.getThreadLocalRevision();
		boolean modelExists = currentRevision.modelExists();
		if(!modelExists) {
			return null;
		}
		long currentRevNr = currentRevision.revision();
		return this.snapshotService.getObjectSnapshot(currentRevNr, true, objectId);
	}
	
	/**
	 * use thread-local caches to return the same revision number to the same
	 * asking thread, until that thread terminates or does a change. Or
	 * {@link InstanceContext#clearThreadContext()} is called.
	 * 
	 * @return the current {@link ModelRevision}
	 */
	public ModelRevision getModelRevision() {
		assertThreadLocalRevisionNumberIsUpToDate();
		return this.revisionManager.getThreadLocalRevision();
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
	
	public boolean modelHasBeenManaged(XID modelId) {
		return this.changesService.modelHasBeenManaged();
	}
	
}

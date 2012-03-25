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
import org.xydra.store.impl.gae.changes.GaeModelRevision;
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
	private final InstanceRevisionManager instanceRevisionManager;
	
	public GaeModelPersistence(XAddress modelAddress) {
		this.instanceRevisionManager = new InstanceRevisionManager(modelAddress);
		this.modelAddress = modelAddress;
		this.changesService = new GaeChangesServiceImpl3(this.modelAddress,
		        this.instanceRevisionManager);
		this.snapshotService = new GaeSnapshotServiceImpl3(this.changesService);
		this.executionService = new GaeExecutionServiceImpl3(this.instanceRevisionManager,
		        this.changesService, this.snapshotService);
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		// absolutely required
		calculateModelRevAndCacheInInstance(false);
		return this.executionService.executeCommand(command, actorId);
	}
	
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
		calculateModelRevAndCacheInInstance(true);
		return this.changesService.getEventsBetween(address, beginRevision, endRevision);
	}
	
	private void calculateModelRevAndCacheInInstance(boolean includeTentative) {
		GaeModelRevision gaeModelRev = this.changesService
		        .calculateCurrentModelRevision(includeTentative);
		if(gaeModelRev.getModelRevision() == null) {
			gaeModelRev = new GaeModelRevision(gaeModelRev.getLastSilentCommitted(),
			        ModelRevision.MODEL_DOES_NOT_EXIST_YET);
		}
		this.instanceRevisionManager.getInstanceRevisionInfo()
		        .setCurrentGaeModelRevIfRevisionIsHigher(gaeModelRev);
	}
	
	synchronized public XWritableModel getSnapshot(boolean includeTentative) {
		calculateModelRevAndCacheInInstance(false);
		ModelRevision currentRevision = this.instanceRevisionManager.getInstanceRevisionInfo()
		        .getGaeModelRevision().getModelRevision();
		if(!currentRevision.modelExists()) {
			return null;
		}
		long currentRevNr = currentRevision.revision();
		XWritableModel snapshot = this.snapshotService.getModelSnapshot(currentRevNr, false);
		log.debug("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
		return snapshot;
	}
	
	public XWritableObject getObjectSnapshot(XID objectId, boolean includeTentative) {
		calculateModelRevAndCacheInInstance(false);
		ModelRevision currentRevision = this.instanceRevisionManager.getInstanceRevisionInfo()
		        .getGaeModelRevision().getModelRevision();
		boolean modelExists = currentRevision.modelExists();
		if(!modelExists) {
			return null;
		}
		long currentRevNr = currentRevision.revision();
		return this.snapshotService.getObjectSnapshot(currentRevNr, true, objectId);
	}
	
	/**
	 * @param includeTentative if true, then in addition to the stable model
	 *            revision number also the unstable tentative revision number is
	 *            calculated.
	 * @return the current {@link ModelRevision} or null
	 */
	public ModelRevision getModelRevision(boolean includeTentative) {
		calculateModelRevAndCacheInInstance(includeTentative);
		return this.instanceRevisionManager.getInstanceRevisionInfo().getGaeModelRevision()
		        .getModelRevision();
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

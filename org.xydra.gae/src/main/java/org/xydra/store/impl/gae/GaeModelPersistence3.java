package org.xydra.store.impl.gae;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl3;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.execute.GaeExecutionServiceImpl3;
import org.xydra.store.impl.gae.execute.IGaeExecutionService;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotServiceImpl3;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

// not referenced anywhere
@Deprecated
public class GaeModelPersistence3 implements IGaeModelPersistence {

	private static final Logger log = LoggerFactory.getLogger(GaeModelPersistence3.class);

	private final XAddress modelAddress;
	private final IGaeChangesService changesService;
	private final IGaeSnapshotService snapshotService;
	private final IGaeExecutionService executionService;
	private final InstanceRevisionManager instanceRevisionManager;

	public GaeModelPersistence3(final XAddress modelAddress) {
		this.instanceRevisionManager = new InstanceRevisionManager(modelAddress);
		this.modelAddress = modelAddress;
		this.changesService = new GaeChangesServiceImpl3(this.modelAddress,
				this.instanceRevisionManager);
		this.snapshotService = new GaeSnapshotServiceImpl3(this.changesService);
		this.executionService = new GaeExecutionServiceImpl3(this.instanceRevisionManager,
				this.changesService, this.snapshotService);
	}


	@Override
	public long executeCommand(final XCommand command, final XId actorId) {
		// absolutely required
		calculateModelRevAndCacheInInstance(false);
		return this.executionService.executeCommand(command, actorId);
	}


	@Override
	public List<XEvent> getEventsBetween(final XAddress address, final long beginRevision, final long endRevision) {
		calculateModelRevAndCacheInInstance(true);
		return this.changesService.getEventsBetween(address, beginRevision, endRevision);
	}

	private void calculateModelRevAndCacheInInstance(final boolean includeTentative) {
		GaeModelRevision gaeModelRev = this.changesService
				.calculateCurrentModelRevision(includeTentative);
		if (gaeModelRev.getModelRevision() == null) {
			gaeModelRev = new GaeModelRevision(gaeModelRev.getLastSilentCommitted(),
					ModelRevision.MODEL_DOES_NOT_EXIST_YET);
		}
		this.instanceRevisionManager.getInstanceRevisionInfo()
				.setCurrentGaeModelRevIfRevisionIsHigher(gaeModelRev);
	}


	@Override
	synchronized public XWritableModel getSnapshot(final boolean includeTentative) {
		calculateModelRevAndCacheInInstance(false);
		final ModelRevision currentRevision = this.instanceRevisionManager.getInstanceRevisionInfo()
				.getGaeModelRevision().getModelRevision();
		if (!currentRevision.modelExists()) {
			return null;
		}
		final long currentRevNr = currentRevision.revision();
		final XWritableModel snapshot = this.snapshotService.getModelSnapshot(currentRevNr, false);
		log.debug("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
		return snapshot;
	}


	@Override
	public XWritableObject getObjectSnapshot(final XId objectId, final boolean includeTentative) {
		calculateModelRevAndCacheInInstance(false);
		final ModelRevision currentRevision = this.instanceRevisionManager.getInstanceRevisionInfo()
				.getGaeModelRevision().getModelRevision();
		final boolean modelExists = currentRevision.modelExists();
		if (!modelExists) {
			return null;
		}
		final long currentRevNr = currentRevision.revision();
		return this.snapshotService.getObjectSnapshot(currentRevNr, true, objectId);
	}


	@Override
	public ModelRevision getModelRevision(final boolean includeTentative) {
		calculateModelRevAndCacheInInstance(includeTentative);
		return this.instanceRevisionManager.getInstanceRevisionInfo().getGaeModelRevision()
				.getModelRevision();
	}

	@Override
	public int hashCode() {
		return this.modelAddress.hashCode();

	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof GaeModelPersistence3
				&& ((GaeModelPersistence3) other).modelAddress.equals(this.modelAddress);

	}


	@Override
	public boolean modelHasBeenManaged() {
		return this.changesService.modelHasBeenManaged();
	}

}

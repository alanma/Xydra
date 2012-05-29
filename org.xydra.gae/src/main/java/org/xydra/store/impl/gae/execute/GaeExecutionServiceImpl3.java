package org.xydra.store.impl.gae.execute;

import java.util.List;
import java.util.concurrent.Future;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.change.EventUtils;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.NanoClock;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.FutureUtils;
import org.xydra.store.impl.gae.InstanceRevisionManager;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.VoluntaryTimeoutException;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

import com.google.appengine.api.datastore.Key;


/**
 * A class responsible for executing changes for one model, in the datastore.
 * 
 * This variant checks preconditions by retrieving the latest snapshots and
 * updating those parts that the executing change has locked. There is no
 * separate executing step that can be rolled forward.
 * 
 * There is no additional state stored in the GAE datastore besides that used by
 * the {@link IGaeChangesService} and {@link IGaeSnapshotService}
 * implementations.
 * 
 * @author dscharrer
 * 
 */
public class GaeExecutionServiceImpl3 implements IGaeExecutionService {
	
	/**
	 * Initial time to wait before re-checking the status of an event who'se
	 * locks we need.
	 */
	private static final long WAIT_INITIAL = 10;
	
	/**
	 * Maximum time to wait before re-checking the status of an event who's
	 * locks we need.
	 */
	// IMPROVE set WAIT_MAX cleverly
	private static final long WAIT_MAX = 1000;
	
	private static final Logger log = LoggerFactory.getLogger(GaeExecutionServiceImpl3.class);
	
	private final InstanceRevisionManager revisionManager;
	private final IGaeChangesService changesservice;
	private final IGaeSnapshotService snapshots;
	private final XAddress modelAddr;
	
	/**
	 * @param revisionManager TODO docu
	 * @param changes The change log used for the model to execute changes on.
	 * @param snapshots A snapshot service for the model service
	 */
	public GaeExecutionServiceImpl3(InstanceRevisionManager revisionManager,
	        IGaeChangesService changes, IGaeSnapshotService snapshots) {
		this.revisionManager = revisionManager;
		this.changesservice = changes;
		this.modelAddr = changes.getModelAddress();
		
		XyAssert.xyAssert(snapshots.getModelAddress() == this.modelAddr);
		this.snapshots = snapshots;
	}
	
	@Override
	/*
	 * Requires a fresh revision number to find the right base snapshot from
	 * which to work on
	 */
	public long executeCommand(XCommand command, XID actorId) {
		/**
		 * InstanceRevisionManager should have been updated before. This is
		 * checked in GaeModelPersistence
		 */
		log.debug("Execute " + DebugFormatter.format(command));
		NanoClock c = new NanoClock().start();
		XyAssert.xyAssert(this.modelAddr.equalsOrContains(command.getChangedEntity()),
		        "cannot handle command " + command + " - it does not address a model");
		
		GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		
		log.debug("Phase 1: grabRevisionAndRegister " + locks.size() + " locks = " + locks);
		GaeChange change = this.changesservice.grabRevisionAndRegisterLocks(this.revisionManager
		        .getInstanceRevisionInfo().getLastTaken(), locks, actorId);
		XyAssert.xyAssert(change.rev >= 0);
		c.stopAndStart("grabRevisionAndRegisterLocks");
		
		GaeModelRevision gaeModelRev = this.revisionManager.getInstanceRevisionInfo()
		        .getGaeModelRevision();
		long snapshotRev = gaeModelRev.getModelRevision().revision();
		log.info("[r" + change.rev + "] Phase 2: getPartialSnapshot at {rev=" + snapshotRev
		        + "/lastCommited="
		        + this.revisionManager.getInstanceRevisionInfo().getLastCommitted() + "}");
		
		XRevWritableModel partialSnapshot = null;
		if(gaeModelRev.getModelRevision().modelExists()) {
			partialSnapshot = this.snapshots.getPartialSnapshot(snapshotRev, change.getLocks());
			c.stopAndStart("getPartialSnapshot");
		}
		
		/*
		 * IMPROVE we can ignore all changes in [currentRev + 1,
		 * lastCommittedRev) as they either failed or didn't change anything,
		 * but we need to make sure that there isn't a better currentRev <=
		 * lastCommittedRev
		 */
		log.info("[r" + change.rev + "] Phase 3: updateSnapshot to " + (change.rev - 1)
		        + " and wait for locks");
		XRevWritableModel workingModel = updatePartialSnapshot(partialSnapshot, snapshotRev, change);
		c.stopAndStart("updateSnapshot");
		
		log.debug("[r" + change.rev + "] Phase 4: checkPreconditionsAndSaveEvents change = "
		        + change + ", command = " + command);
		long ret = checkPreconditionsAndSaveEvents(change, command, actorId, workingModel);
		log.trace("result " + ret);
		c.stopAndStart("checkPreconditionsAndSaveEvents");
		
		XyAssert.xyAssert(change.getStatus().isCommitted(),
		        "If we reach this line, change must be commited is %s", change.getStatus());
		
		if(log.isInfoEnabled() || ret == XCommand.FAILED && log.isWarnEnabled()) {
			String msg = "[r"
			        + change.rev
			        + "] -> "
			        + (ret == XCommand.FAILED ? "failed" : ret == XCommand.NOCHANGE ? "nochange"
			                : "success") + " {" + gaeModelRev + "}. Stats: " + c.getStats();
			if(ret == XCommand.FAILED) {
				log.warn(msg);
			} else {
				log.info(msg);
			}
			
		}
		return ret;
	}
	
	/**
	 * Update all locked parts of the given snapshot. If some conflicting
	 * changes are still executing, wait for them to finish.
	 * 
	 * @param snapshot Can be null. Can be partial. Must be RevWritable so that
	 *            parts of it can be copied into another RevWritableModel. See
	 *            also
	 *            {@link IGaeSnapshotService#getPartialSnapshot(long, Iterable)}
	 * @param snapshotRev the revision of the given snapshot.
	 * @param change never null
	 * @return the resulting snapshot of applying the event
	 */
	private XRevWritableModel updatePartialSnapshot(@CanBeNull XRevWritableModel snapshot,
	        long snapshotRev, @NeverNull GaeChange change) {
		@CanBeNull
		XRevWritableModel workingModel = snapshot;
		
		// IMPROVE use the last committed rev to skip failed / empty changes
		
		// TODO 2012-02 use silentLastCommited + 1
		
		/* scan events between last stable snapshot and my change rev */
		for(long checkRev = snapshotRev + 1; checkRev < change.rev; checkRev++) {
			GaeChange checkChange = this.changesservice.getChange(checkRev);
			if(checkChange == null) {
				throw new IllegalStateException("Our change.rev=" + change.rev
				        + " waits for locks. Check for " + checkRev + " got null from backend");
			}
			
			if(checkChange.getStatus().canChange()) {
				// Check if the change needs conflicting locks.
				if(!change.isConflicting(checkChange)) {
					// Mark any object revisions that we don't know as
					// XEvent.RevisionNotAvailable
					workingModel = invalidateObjectRevisions(snapshot, workingModel,
					        checkChange.getLocks());
					// not conflicting, so ignore
					continue;
				}
				
				/*
				 * The checkChange is uncommitted and holds conflicting locks,
				 * so we need to wait. Waiting is done by sleeping increasing
				 * intervals and then checking the change entity again.
				 * 
				 * The locks that we already "acquired" cannot be released
				 * before entering the waiting mode, as releasing them before
				 * completely executing our own change would allow other changes
				 * with conflicting locks and a revision greater than ours to
				 * execute before our own change.
				 */
				long waitTime = WAIT_INITIAL;
				boolean timedOut;
				while(!(timedOut = checkChange.isTimedOut())) {
					// IMPROVE save own command if waitTime is too long (so that
					// we can be rolled forward in case of timeout)
					try {
						Thread.sleep(waitTime);
					} catch(InterruptedException e) {
						// ignore interrupt
					}
					// IMPROVE update own lastActivity?
					
					checkChange.reload();
					
					if(!checkChange.getStatus().canChange()) {
						this.changesservice.cacheCommittedChange(checkChange);
						// now finished, so should have no locks anymore
						XyAssert.xyAssert(!checkChange.hasLocks());
						break;
					}
					
					// IMPROVE allow to update the locks and re-check them here?
					
					waitTime = increaseExponentiallyWithFactorAndMaximum(waitTime, 2, WAIT_MAX);
				}
				
				if(timedOut) {
					this.changesservice.commit(checkChange, Status.FailedTimeout);
				}
			}
			
			XyAssert.xyAssert(checkChange.getStatus().isCommitted());
			if(checkChange.getStatus().hasEvents()) {
				log.trace("checkRev=" + checkRev + " Applying " + checkChange.getEvent());
				workingModel = EventUtils.applyEventNonDestructive(snapshot, workingModel,
				        checkChange.getEvent(), true);
			}
		}
		
		// gather operations stats
		if(log.isInfoEnabled()) {
			long start = snapshotRev;
			long end = change.rev;
			long workingWindowSize = end - start;
			if(workingWindowSize > 1) {
				log.info("[r" + change.rev + "] Current working window size = " + workingWindowSize
				        + " [" + start + "," + end
				        + "] GA?category=performance&action=workingwindow&label=size&value="
				        + workingWindowSize);
			}
		}
		
		if(workingModel != null && workingModel.getRevisionNumber() != change.rev - 1) {
			if(workingModel == snapshot) {
				workingModel = SimpleModel.shallowCopy(snapshot);
			}
			workingModel.setRevisionNumber(change.rev - 1);
		}
		
		return workingModel;
	}
	
	private static long increaseExponentiallyWithFactorAndMaximum(long l, int f, long max) {
		long result = l * 2;
		if(result > max) {
			result = max;
		}
		return result;
	}
	
	/**
	 * Mark all object revisions that could be updated by a change owning the
	 * given locks as unknown. Any entities that are also in the reference model
	 * are copied before being modified.
	 * 
	 * @param referenceModel @CanBeNull can be partial
	 * @param model @CanBeNull can be partial
	 * @param locks
	 * @return
	 */
	private static XRevWritableModel invalidateObjectRevisions(
	        @CanBeNull XReadableModel referenceModel, @CanBeNull XRevWritableModel model,
	        GaeLocks locks) {
		if(model == null) {
			return null;
		}
		
		XRevWritableModel result = model;
		for(XAddress lock : locks) {
			
			XID objectId = lock.getObject();
			if(objectId == null) {
				continue;
			}
			
			XRevWritableObject object = result.getObject(lock.getObject());
			if(object == null) {
				log.warn("null-object '" + lock.getObject() + "' in snapshot "
				        + result.getAddress());
				continue;
			}
			
			if(referenceModel != null && object == referenceModel.getObject(lock.getObject())) {
				if(result == referenceModel) {
					result = SimpleModel.shallowCopy(result);
				}
				object = SimpleObject.shallowCopy(object);
				result.addObject(object);
			}
			
			object.setRevisionNumber(XEvent.RevisionNotAvailable);
		}
		
		return result;
	}
	
	/**
	 * Check the preconditions required to execute the given command and write
	 * the events that describe the transformation of the model into the new
	 * state.
	 * 
	 * Assumes that we have all the required locks.
	 * 
	 * @param change The change that the command belongs to.
	 * @param command
	 * @param actorId The actor to log in the created events.
	 * @param snapshot on which the command is executed, can be null
	 * @return a copy of the created events or null if the command cannot be
	 *         applied.
	 */
	private long checkPreconditionsAndSaveEvents(GaeChange change, XCommand command, XID actorId,
	        XReadableModel snapshot) {
		Pair<ChangedModel,DeltaUtils.ModelChange> c = DeltaUtils.executeCommand(snapshot, command);
		if(c == null) {
			change.giveUpIfTimeoutCritical();
			this.changesservice.commit(change, Status.FailedPreconditions);
			log.info("Failed preconditions");
			return XCommand.FAILED;
		}
		
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, c, actorId, change.rev);
		log.debug("[r" + change.rev + "] DeltaUtils generated " + events.size() + " events");
		if(events.size() > 1000) {
			log.warn("Created over 1000 events (" + events.size()
			        + ") GA?category=xydra&action=saveManyEvents&label=events&value="
			        + events.size());
			try {
				throw new RuntimeException("Over 1000 events");
			} catch(Exception e) {
				log.warn("Over 1000 events", e);
			}
		}
		XyAssert.xyAssert(events != null);
		try {
			if(events.isEmpty()) {
				change.giveUpIfTimeoutCritical();
				this.changesservice.commit(change, Status.SuccessNochange);
				log.debug("No change");
				return XCommand.NOCHANGE;
			}
			Pair<int[],List<Future<Key>>> res = change.setEvents(events);
			
			// Wait on all changes.
			for(Future<Key> future : res.getSecond()) {
				FutureUtils.waitFor(future);
			}
			
			change.giveUpIfTimeoutCritical();
		} catch(VoluntaryTimeoutException vte) {
			/*
			 * Since we have not changed the status to EXEUTING, no thread will
			 * be able to roll this change forward and we might as well clean it
			 * up to prevent unnecessary waits.
			 */
			this.changesservice.commit(change, Status.FailedTimeout);
			throw vte;
		}
		
		this.changesservice.commit(change, Status.SuccessExecuted);
		
		return change.rev;
	}
}

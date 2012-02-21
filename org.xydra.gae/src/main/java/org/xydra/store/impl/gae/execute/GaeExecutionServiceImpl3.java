package org.xydra.store.impl.gae.execute;

import java.util.List;
import java.util.concurrent.Future;

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
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.FutureUtils;
import org.xydra.store.impl.gae.RevisionManager;
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
	
	private final RevisionManager revisionManager;
	private final IGaeChangesService changes;
	private final IGaeSnapshotService snapshots;
	private final XAddress modelAddr;
	
	/**
	 * @param revisionManager TODO
	 * @param changes The change log used for the model to execute changes on.
	 * @param snapshots A snapshot service for the model service
	 */
	public GaeExecutionServiceImpl3(RevisionManager revisionManager, IGaeChangesService changes,
	        IGaeSnapshotService snapshots) {
		this.revisionManager = revisionManager;
		this.changes = changes;
		this.modelAddr = changes.getModelAddress();
		assert snapshots.getModelAddress() == this.modelAddr;
		this.snapshots = snapshots;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see IGaeExecutionService#executeCommand(XCommand, XID)
	 */
	@Override
	public long executeCommand(XCommand command, XID actorId) {
		assert this.revisionManager.isInstanceModelRevisionInitialised();
		
		log.debug("Execute " + DebugFormatter.format(command));
		NanoClock c = new NanoClock().start();
		assert this.modelAddr.equalsOrContains(command.getChangedEntity()) : "cannot handle command "
		        + command + " - it does not address a model";
		c.stopAndStart("assert");
		
		GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		
		log.debug("Phase 1: grabRevisionAndRegister " + locks.size() + " locks");
		GaeChange change = this.changes.grabRevisionAndRegisterLocks(this.revisionManager
		        .getInstanceRevisionInfo().getLastTaken(), locks, actorId);
		assert change.rev >= 0;
		c.stopAndStart("grabRevisionAndRegisterLocks");
		
		GaeModelRevision gaeModelRev = this.revisionManager.getInstanceRevisionInfo()
		        .getGaeModelRevision();
		long snapshotRev = gaeModelRev.getModelRevision().revision();
		log.debug("[r" + change.rev + "] Phase 2: getPartialSnapshot at {" + snapshotRev + "/"
		        + this.revisionManager.getInstanceRevisionInfo().getLastCommitted() + "}");
		
		XRevWritableModel snapshot = null;
		if(gaeModelRev.getModelRevision().modelExists()) {
			snapshot = this.snapshots.getPartialSnapshot(snapshotRev, change.getLocks());
			c.stopAndStart("getPartialSnapshot");
		}
		
		// long snapshotRev = this.changes.getCurrentRevisionNumber();
		// boolean exists = this.changes.exists(); // TODO exists depends on the
		// // revision number
		//
		// log.debug("[r" + change.rev + "] Phase 2: getPartialSnapshot at {" +
		// snapshotRev + "/"
		// + this.changes.getLastCommited() + "}");
		// XRevWritableModel snapshot = null;
		// if(exists) {
		// snapshot = this.snapshots.getPartialSnapshot(snapshotRev,
		// change.getLocks());
		// c.stopAndStart("getPartialSnapshot");
		//
		// // TODO this should not be needed ~Daniel
		// snapshot = XCopyUtils.createSnapshot(snapshot);
		// c.stopAndStart("copy");
		// }
		
		/*
		 * IMPROVE we can ignore all changes in [currentRev + 1,
		 * lastCommittedRev) as they either failed or didn't change anything,
		 * but we need to make sure that there isn't a better currentRev <=
		 * lastCommittedRev
		 */
		log.debug("[r" + change.rev + "] Phase 3: updateSnapshot to " + (change.rev - 1)
		        + " and wait for locks");
		XRevWritableModel workingModel = updateSnapshot(snapshot, snapshotRev, change);
		c.stopAndStart("updateSnapshot");
		
		log.debug("[r" + change.rev + "] Phase 4: checkPreconditionsAndSaveEvents");
		long ret = checkPreconditionsAndSaveEvents(change, command, actorId, workingModel);
		log.trace("result " + ret);
		c.stopAndStart("checkPreconditionsAndSaveEvents");
		
		assert change.getStatus().isCommitted() : "If we reach this line, change must be committed";
		
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
	 * @param snapshot Can be null. Must be RevWritable so that parts of it can
	 *            be copied into another RevWritableModel. See also
	 *            {@link IGaeSnapshotService#getPartialSnapshot(long, Iterable)}
	 * @param snapshotRev the revision of the given snapshot.
	 * @param change never null
	 * @return the resulting snapshot of applying the event TODO ....
	 */
	private XRevWritableModel updateSnapshot(XRevWritableModel snapshot, long snapshotRev,
	        GaeChange change) {
		XRevWritableModel workingModel = snapshot;
		
		// IMPROVE use the last committed rev to skip failed / empty changes
		
		// TODO 2012-02 use silentLastCommited + 1
		
		for(long otherRev = snapshotRev + 1; otherRev < change.rev; otherRev++) {
			
			GaeChange otherChange = this.changes.getChange(otherRev);
			
			if(otherChange == null) {
				throw new IllegalStateException("Our change.rev=" + change.rev
				        + " waits for locks. Check for " + otherRev + " got null from backend");
			}
			
			if(!otherChange.getStatus().isCommitted()) {
				
				// Check if the change needs conflicting locks.
				if(!change.isConflicting(otherChange)) {
					
					// Mark any object revisions that we don't know as
					// XEvent.RevisionNotAvailable
					workingModel = invalidateObjectRevisions(snapshot, workingModel,
					        otherChange.getLocks());
					
					// not conflicting, so ignore
					continue;
				}
				
				/*
				 * The otherChange is uncommitted and holds conflicting locks,
				 * so we need to wait.
				 * 
				 * Waiting is done by sleeping increasing intervals and then
				 * checking the change entity again.
				 * 
				 * The locks that we already "acquired" cannot be released
				 * before entering the waiting mode, as releasing them before
				 * completely executing our own change would allow other changes
				 * with conflicting locks and a revision greater than ours to
				 * execute before our own change.
				 */
				long waitTime = WAIT_INITIAL;
				boolean timedOut;
				while(!(timedOut = otherChange.isTimedOut())) {
					
					// IMPROVE save own command if waitTime is too long (so that
					// we
					// can be rolled forward in case of timeout)
					try {
						Thread.sleep(waitTime);
					} catch(InterruptedException e) {
						// ignore interrupt
					}
					// IMPROVE update own lastActivity?
					
					otherChange.reload();
					
					if(otherChange.getStatus().isCommitted()) {
						this.changes.cacheCommittedChange(otherChange);
						// now finished, so should have no locks anymore
						assert !otherChange.hasLocks();
						break;
					}
					
					// IMPROVE allow to update the locks and re-check them here?
					
					// increase wait time exponentially
					waitTime *= 2;
					if(waitTime > WAIT_MAX) {
						waitTime = WAIT_MAX;
					}
				}
				
				if(timedOut) {
					this.changes.commit(otherChange, Status.FailedTimeout);
				}
				
			}
			
			assert otherChange.getStatus().isCommitted();
			
			if(!otherChange.getStatus().hasEvents()) {
				// nothing of interest in this change
				continue;
			}
			
			workingModel = EventUtils.applyEventNonDestructive(snapshot, workingModel,
			        otherChange.getEvent());
		}
		
		// gather operations stats
		if(log.isInfoEnabled()) {
			long start = snapshotRev;
			long end = change.rev;
			long workingWindowSize = end - start;
			if(workingWindowSize > 1) {
				log.info("[r" + change.rev + "] Current working window size = " + workingWindowSize
				        + " [" + start + "," + end + "]");
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
	
	/**
	 * Mark all object revisions that could be updated by a change owning the
	 * given locks as unknown. Any entities that are also in the reference model
	 * are copied before being modified.
	 * 
	 * @param referenceModel
	 * @param model
	 * @param locks
	 * @return
	 */
	private static XRevWritableModel invalidateObjectRevisions(XReadableModel referenceModel,
	        XRevWritableModel model, GaeLocks locks) {
		
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
			this.changes.commit(change, Status.FailedPreconditions);
			log.info("Failed preconditions");
			return XCommand.FAILED;
		}
		
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, c, actorId, change.rev);
		log.debug("[r" + change.rev + "] DeltaUtils generated " + events.size() + " events");
		if(events.size() > 1000) {
			log.warn("Created over 1000 events (" + events.size()
			        + ") GA?category=xydra&action=saveManyEvents&label=events&value="
			        + events.size());
		}
		assert events != null;
		try {
			if(events.isEmpty()) {
				change.giveUpIfTimeoutCritical();
				this.changes.commit(change, Status.SuccessNochange);
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
			 * /up to prevent unnecessary waits.
			 */
			this.changes.commit(change, Status.FailedTimeout);
			throw vte;
		}
		
		this.changes.commit(change, Status.SuccessExecuted);
		
		return change.rev;
	}
}

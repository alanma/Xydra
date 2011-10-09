package org.xydra.store.impl.gae.execute;

import java.util.List;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.Clock;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.FutureUtils;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.VoluntaryTimeoutException;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

import com.google.appengine.api.datastore.Key;


/**
 * A class responsible for executing changes for one model, in the datastore.
 * 
 * This variant checks preconditions by retrieving the latest snapshots and
 * updating those parts that the executing change has locked. There is no
 * separate executing step that can be rolled forward.
 * 
 * WARNING: Because this variant no longer maintains the internal MOF tree used
 * by older version, any model modified with {@link GaeExecutionServiceImpl3}
 * can no longer be correctly modified with {@link GaeExecutionServiceImpl2} or
 * {@link GaeExecutionServiceImpl1}.
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
	
	private final IGaeChangesService changes;
	private final IGaeSnapshotService snapshots;
	private final XAddress modelAddr;
	
	/**
	 * @param changes The change log used for the model to execute changes on.
	 * @param snapshots A snapshot service for the model service
	 */
	public GaeExecutionServiceImpl3(IGaeChangesService changes, IGaeSnapshotService snapshots) {
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
		
		log.debug("Execute " + DebugFormatter.format(command));
		Clock c = new Clock().start();
		assert this.modelAddr.equalsOrContains(command.getChangedEntity()) : "cannot handle command "
		        + command + " - it does not address a model";
		c.stopAndStart("assert");
		GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		
		log.debug("Phase 1: grabRevisionAndRegister " + locks.size() + " locks");
		GaeChange change = this.changes.grabRevisionAndRegisterLocks(locks, actorId);
		assert change.rev >= 0;
		c.stopAndStart("grabRevisionAndRegisterLocks");
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		long snapshotRev = this.changes.getCurrentRevisionNumber();
		boolean exists = this.changes.exists(); // TODO exists depends on the
		// revision number
		
		log.debug("Phase 2: getPartialSnapshot at " + snapshotRev);
		XRevWritableModel snapshot = null;
		if(exists) {
			snapshot = this.snapshots.getPartialSnapshot(snapshotRev, change.getLocks());
		}
		c.stopAndStart("getPartialSnapshot");
		
		/*
		 * IMPROVE we can ignore all changes in [currentRev + 1,
		 * lastCommittedRev) as they either failed or didn't change anything,
		 * but we need to make sure that there isn't a better currentRev <=
		 * lastCommittedRev
		 */
		log.debug("Phase 3: updateSnapshot to " + (change.rev - 1));
		snapshot = updateSnapshot(snapshot, snapshotRev, change);
		c.stopAndStart("updateSnapshot");
		
		log.debug("Phase 4: checkPreconditionsAndSaveEvents");
		long ret = checkPreconditionsAndSaveEvents(change, command, actorId, snapshot);
		c.stopAndStart("checkPreconditionsAndSaveEvents");
		
		assert change.getStatus().isCommitted() : "If we reach this line, change must be committed";
		
		// FIXME REENABLE revCache.writeToMemcache();
		
		log.info("Success. Stats: " + c.getStats());
		
		return ret;
	}
	
	/**
	 * Update all locked parts of the given snapshot. If some conflicting
	 * changes are still executing, wait for them to finish.
	 */
	private XRevWritableModel updateSnapshot(XRevWritableModel snapshot, long snapshotRev,
	        GaeChange change) {
		
		XRevWritableModel model = snapshot;
		boolean copied = false;
		
		// IMPROVE use the last committed rev to skip failed / empty changes
		
		for(long otherRev = snapshotRev + 1; otherRev < change.rev; otherRev++) {
			
			GaeChange otherChange = this.changes.getChange(otherRev);
			
			if(otherChange == null) {
				throw new IllegalStateException("Our change.rev=" + change.rev
				        + " waits for locks. Check for " + otherRev + " got null from backend");
			}
			
			if(!otherChange.getStatus().isCommitted()) {
				
				// Check if the change needs conflicting locks.
				if(!change.isConflicting(otherChange)) {
					if(!copied) {
						// IMPROVE use shallow copy.
						model = XCopyUtils.createSnapshot(snapshot);
						copied = true;
					}
					invalidateObjectRevisions(model, otherChange);
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
					if(otherChange.getStatus().canRollForward()) {
						assert false;
					} else {
						this.changes.commit(otherChange, Status.FailedTimeout);
					}
				}
				
			}
			
			assert otherChange.getStatus().isCommitted();
			
			if(!otherChange.getStatus().hasEvents()) {
				// nothing of interest in this change
				continue;
			}
			
			if(!copied) {
				// IMPROVE use shallow copy, apply event nondestructively.
				model = XCopyUtils.createSnapshot(snapshot);
				copied = true;
			}
			model = appplyEvent(model, otherChange.getEvent(), change.getLocks());
		}
		
		// gather operations stats
		if(log.isInfoEnabled()) {
			long start = snapshotRev;
			long end = change.rev;
			long workingWindowSize = end - start;
			if(workingWindowSize > 1) {
				log.info("Current working window size = " + workingWindowSize + " [" + start + ","
				        + end + "]");
			}
		}
		
		if(model != null && model.getRevisionNumber() != change.rev - 1) {
			if(!copied) {
				model = SimpleModel.shallowCopy(snapshot);
			}
			model.setRevisionNumber(change.rev - 1);
		}
		
		return model;
	}
	
	private XRevWritableModel applySingleEvent(XRevWritableModel snapshot, XAtomicEvent event,
	        GaeLocks locks) {
		
		XRevWritableModel model = snapshot;
		
		XAddress changed = event.getChangedEntity();
		if(!locks.canRead(changed)) {
			XRevWritableObject object = model.getObject(changed.getObject());
			if(object != null) {
				object.setRevisionNumber(XEvent.RevisionNotAvailable);
			}
			return model;
		}
		
		if(event instanceof XRepositoryEvent) {
			if(event.getChangeType() == ChangeType.ADD) {
				assert model == null;
				return new SimpleModel(this.modelAddr);
			} else {
				assert model != null;
				return null;
			}
		}
		
		assert model != null;
		XID objectId = changed.getObject();
		assert objectId != null;
		
		if(event instanceof XModelEvent) {
			if(event.getChangeType() == ChangeType.ADD) {
				assert !model.hasObject(objectId);
				XRevWritableObject object = model.createObject(objectId);
				object.setRevisionNumber(event.getRevisionNumber());
			} else {
				assert model.hasObject(objectId);
				model.removeObject(objectId);
			}
			return model;
		}
		
		XRevWritableObject object = model.getObject(objectId);
		assert object != null;
		XID fieldId = changed.getField();
		assert fieldId != null;
		
		if(event instanceof XObjectEvent) {
			if(event.getChangeType() == ChangeType.ADD) {
				assert !object.hasField(fieldId);
				XRevWritableField field = object.createField(fieldId);
				field.setRevisionNumber(event.getRevisionNumber());
				object.setRevisionNumber(event.getRevisionNumber());
			} else {
				assert object.hasField(fieldId);
				object.removeField(fieldId);
			}
			return model;
		}
		
		XRevWritableField field = object.getField(fieldId);
		assert field != null;
		assert event instanceof XFieldEvent;
		
		field.setValue(((XFieldEvent)event).getNewValue());
		field.setRevisionNumber(event.getRevisionNumber());
		object.setRevisionNumber(event.getRevisionNumber());
		
		return model;
	}
	
	private XRevWritableModel appplyEvent(XRevWritableModel model, XEvent event, GaeLocks locks) {
		
		if(event instanceof XAtomicEvent) {
			return applySingleEvent(model, (XAtomicEvent)event, locks);
		}
		
		assert event instanceof XTransactionEvent;
		XTransactionEvent trans = (XTransactionEvent)event;
		
		XRevWritableModel temp = model;
		
		for(XAtomicEvent ae : trans) {
			temp = applySingleEvent(temp, ae, locks);
		}
		
		return temp;
	}
	
	private void invalidateObjectRevisions(XRevWritableModel model, GaeChange otherChange) {
		
		if(model == null) {
			return;
		}
		
		for(XAddress lock : otherChange.getLocks()) {
			
			if(lock.getObject() == null) {
				continue;
			}
			
			XRevWritableObject object = model.getObject(lock.getObject());
			if(object != null) {
				object.setRevisionNumber(XEvent.RevisionNotAvailable);
			}
			
		}
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
	 * @param snapshot
	 * @return a copy of the created events or null if the command cannot be
	 *         applied.
	 */
	private long checkPreconditionsAndSaveEvents(GaeChange change, XCommand command, XID actorId,
	        XReadableModel snapshot) {
		
		Pair<ChangedModel,DeltaUtils.ModelChange> c = DeltaUtils.executeCommand(snapshot, command);
		
		if(c == null) {
			change.giveUpIfTimeoutCritical();
			this.changes.commit(change, Status.FailedPreconditions);
			return XCommand.FAILED;
		}
		
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, c, actorId, change.rev);
		log.debug("DeltaUtils generated " + events.size() + " events");
		
		assert events != null;
		
		try {
			
			if(events.isEmpty()) {
				change.giveUpIfTimeoutCritical();
				this.changes.commit(change, Status.SuccessNochange);
				return XCommand.NOCHANGE;
			}
			
			Pair<int[],List<Future<Key>>> res = change.setEvents(events);
			
			// Wait on all changes.
			for(Future<Key> future : res.getSecond()) {
				FutureUtils.waitFor(future);
			}
			
			// FIXME GaePersistence uses the this entity for getModelIds() and
			// hasModel()
			XAtomicEvent first = events.get(0);
			XAtomicEvent last = events.get(events.size() - 1);
			if(last instanceof XRepositoryEvent && last.getChangeType() == ChangeType.REMOVE) {
				FutureUtils.waitFor(InternalGaeXEntity.remove(this.modelAddr, change.getLocks()));
			} else if(first instanceof XRepositoryEvent && first.getChangeType() == ChangeType.ADD) {
				FutureUtils
				        .waitFor(InternalGaeModel.createModel(this.modelAddr, change.getLocks()));
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

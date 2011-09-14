package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.Clock;
import org.xydra.store.RevisionState;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.FutureUtils;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


/**
 * A class responsible for executing and logging changes to a specific
 * {@link XModel} in the GAE datastore.
 * 
 * This class is the core of the GAE {@link XydraStore} implementation.
 * 
 * Keys for XMODEL, XOBJECT and XFIELD entities are encoded according to
 * {@link KeyStructure#createEntityKey(XAddress)}.
 * 
 * There are five different kinds of GAE Entities that are used by this class:
 * <dl>
 * <dt>Entity type XMODEL</dt>
 * <dd>These are used to represent the internal state of a model and are managed
 * by {@link InternalGaeModel}. The model entities only store the repository
 * address (for queries). Individual objects are stored separately and the model
 * revision is not stored at all. In fact, the contained object might not all
 * correspond to the same object revision at the same time.</dd>
 * 
 * <dt>Entity type XOBJECT</dt>
 * <dd>Like XMODEL. Used to represent objects and managed by
 * {@link InternalGaeObject}. XOBJECT Entities store a revision number, but it
 * is not guaranteed to be up to date. An objects actual revision number can
 * only be calculated by locking the whole object and then calculating the
 * maximum of the stored revision and the revision numbers of all contained
 * fields.</dd>
 * 
 * <dt>Entity type XFIELD</dt>
 * <dd>They represent fields and are managed by {@link InternalGaeField}. The
 * value is not stored in the field entity. Instead, additionally to the field
 * revision, an index into the transaction (or zero) is stored that can be used
 * with {@link GaeEvents#getValue(XAddress, long, int)} to load the
 * {@link XValue}.</dd>
 * 
 * 
 * <dt>Entity type XCHANGE</dt>
 * <dd>These represent a change to the model resulting from a single
 * {@link XCommand} (which may be a {@link XTransaction}). These entities
 * represent both an entry into the {@link XChangeLog} as well as a change that
 * is currently in progress. Keys are encoded according to
 * {@link KeyStructure#createChangeKey(XAddress, long)}
 * 
 * The XCHANGE entities are managed by {@link GaeChange}. They store the status
 * of the change, the required locks, the actor that initiated the change, and
 * the time the (last) process started working on the change.
 * 
 * Events and small XValues are also saved in the XCHANGE entities. These
 * properties are managed by {@link GaeEvents}. No events are guaranteed to be
 * set before the change has reached {@link Status#Executing}.
 * 
 * 
 * <dt>Entity type XVALUE</dt>
 * <dd>Stores an {@link XValue} set by an {@link XFieldEvent} that was too large
 * to be stored directly in the corresponding XCHANGE entity. These are managed
 * by {@link GaeEvents}.
 * 
 * </dd>
 * 
 * As commands need to be executed in a well defined order each change needs to
 * grab a revision number before executing. This is done using a GAE
 * transaction.
 * 
 * To synchronize access to the internal MOF tree, the first thing each change
 * does is declare it's required locks in the change entity when grabbing the
 * revision. Before executing, a change checks any uncommitted changes with
 * lower revision numbers for conflicting locks. If a conflict is found, the
 * change will have to wait or abort / roll forward the conflicting change after
 * a timeout. Other pending changes that don't conflict can be safely ignored as
 * they will never touch the same part of the MOF tree. After a change is done,
 * it removes all locks from the change entity.
 * 
 * Locks are managed by {@link GaeLocks}.
 * 
 * @author dscharrer
 * 
 */
public class GaeChangesServiceImpl2 implements IGaeChangesService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesServiceImpl2.class);
	
	private static final long serialVersionUID = -2080744796962188941L;
	
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
	
	// Implementation.
	
	private final XAddress modelAddr;
	private final RevisionCache2 revCache;
	
	@GaeOperation()
	public GaeChangesServiceImpl2(XAddress modelAddr) {
		this.modelAddr = modelAddr;
		this.revCache = new RevisionCache2(modelAddr);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#executeCommand(org
	 * .xydra.base.change.XCommand, org.xydra.base.XID)
	 */
	@Override
	public RevisionState executeCommand(XCommand command, XID actorId) {
		log.debug("Execute " + DebugFormatter.format(command));
		Clock c = new Clock().start();
		assert this.modelAddr.equalsOrContains(command.getChangedEntity()) : "cannot handle command "
		        + command + " - it does not address a model";
		c.stopAndStart("assert");
		GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		
		log.debug("Phase 1: grabRevisionAndRegister " + locks.size() + " locks");
		GaeChange change = grabRevisionAndRegisterLocks(locks, actorId);
		assert change.rev >= 0;
		c.stopAndStart("grabRevisionAndRegisterLocks");
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		
		waitForLocks(change);
		c.stopAndStart("waitForLocks");
		
		Pair<List<XAtomicEvent>,int[]> events = checkPreconditionsAndSaveEvents(change, command,
		        actorId);
		c.stopAndStart("checkPreconditionsAndSaveEvents");
		if(events == null) {
			log.info("Failed. Stats: " + c.getStats());
			return new RevisionState(XCommand.FAILED, modelExists());
		} else if(events.getFirst().isEmpty()) {
			log.info("NOCHANGE. Stats: " + c.getStats());
			return new RevisionState(XCommand.NOCHANGE, modelExists());
		}
		
		executeAndUnlock(change, events);
		c.stopAndStart("executeAndUnlock");
		assert change.getStatus().isCommitted() : "If we reach this line, change must be committed";
		
		// FIXME REENABLE revCache.writeToMemcache();
		
		log.info("Success. Stats: " + c.getStats());
		
		/*
		 * Compute if current model is existing. If last succesful event was not
		 * a Model.REMOVE, it must exist.
		 */
		XEvent event = change.getEvent();
		if(event instanceof XTransactionEvent) {
			event = ((XTransactionEvent)event).getEvent(((XTransactionEvent)event).size() - 1);
		}
		boolean modelJustGotDeleted = event.getTarget().getAddressedType() == XType.XREPOSITORY
		        && event.getChangeType() == ChangeType.REMOVE;
		
		return new RevisionState(change.rev, !modelJustGotDeleted);
	}
	
	private boolean modelExists() {
		// FIXME CALC
		return true;
	}
	
	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 * 
	 *         Note: Reads revCache.lastTaken
	 */
	@GaeOperation(memcacheRead = true ,datastoreRead = true ,datastoreWrite = true ,memcacheWrite = true)
	private GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId) {
		long lastTaken = this.revCache.getLastTaken();
		assert lastTaken >= -1;
		long start = lastTaken + 1;
		for(long rev = start;; rev++) {
			
			GaeChange cachedChange = getCachedChange(rev);
			if(cachedChange != null) {
				// Revision already taken.
				continue;
			}
			
			// Try to grab this revision.
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
			/* use txn to do: avoid overwriting existing change entities */
			Transaction trans = SyncDatastore.beginTransaction();
			
			Entity changeEntity = SyncDatastore.getEntity(key, trans);
			
			if(changeEntity == null) {
				
				GaeChange newChange = new GaeChange(this.modelAddr, rev, locks, actorId);
				newChange.save(trans);
				
				try {
					SyncDatastore.endTransaction(trans);
				} catch(ConcurrentModificationException cme) {
					/*
					 * One cause: 'too much contention on these datastore
					 * entities. please try again.'
					 */
					log.warn("ConcurrentModificationException");
					log.info("failed to take revision: " + key, cme);
					
					// transaction failed as another process wrote to this
					// entity
					
					// IMPROVE if we can assume that at least one thread was
					// successful, we go ahead to the next revision.
					
					// Check this revision again
					rev--;
					continue;
				} catch(DatastoreTimeoutException dte) {
					log.warn("DatastoreTimeout");
					log.info("failed to take revision: " + key, dte);
					
					// try this revision again
					rev--;
					continue;
				}
				
				this.revCache.setLastTaken(rev);
				
				// transaction succeeded, we have a revision
				return newChange;
				
			} else {
				// Revision already taken.
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
				
				// Since we read the entity anyway, might as well use that
				// information.
				Status status = change.getStatus();
				if(status.isCommitted()) {
					cacheCommittedChange(change);
					this.revCache.setCurrentModelRev(change.rev);
				} else if(!status.canRollForward() && change.isTimedOut()) {
					commit(change, Status.FailedTimeout);
				}
				
			}
			
		}
		
		// unreachable
	}
	
	/**
	 * Wait for all locks needed to execute the given change.
	 * 
	 * @param change which lists a Set of required locks
	 */
	private void waitForLocks(GaeChange change) {
		log.debug("waitForLocks: " + DebugFormatter.format(change));
		
		long commitedRev = this.revCache.getLastCommited();
		
		// Track if we find a greater last commitedRev.
		long newCommitedRev = -1;
		
		for(long otherRev = change.rev - 1; otherRev > commitedRev; otherRev--) {
			
			GaeChange otherChange = getCachedChange(otherRev);
			if(otherChange != null) {
				// Change already committed, so it won't conflict.
				continue;
			}
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, otherRev);
			Entity entityFromGae = SyncDatastore.getEntity(key);
			if(entityFromGae == null) {
				throw new IllegalStateException("Our change.rev=" + change.rev
				        + " waits for locks. Check for " + otherRev + " got null from backend");
			}
			otherChange = new GaeChange(this.modelAddr, otherRev, entityFromGae);
			
			// Check if the change is committed.
			if(otherChange.getStatus().isCommitted()) {
				cacheCommittedChange(otherChange);
				if(newCommitedRev < 0) {
					newCommitedRev = otherRev;
				}
				// finished, so should have no locks
				continue;
			}
			
			// Check if the change needs conflicting locks.
			if(!change.isConflicting(otherChange)) {
				newCommitedRev = -1;
				// not conflicting, so ignore
				continue;
			}
			
			/*
			 * The otherChange is uncommitted and holds conflicting locks, so we
			 * need to wait.
			 * 
			 * Waiting is done by sleeping increasing intervals and then
			 * checking the change entity again.
			 * 
			 * The locks that we already "acquired" cannot be released before
			 * entering the waiting mode, as releasing them before completely
			 * executing our own change would allow other changes with
			 * conflicting locks and a revision greater than ours to execute
			 * before our own change.
			 */
			long waitTime = WAIT_INITIAL;
			boolean timedOut;
			while(!(timedOut = otherChange.isTimedOut())) {
				
				// IMPROVE save own command if waitTime is too long (so that we
				// can be rolled forward in case of timeout)
				try {
					Thread.sleep(waitTime);
				} catch(InterruptedException e) {
					// ignore interrupt
				}
				// IMPROVE update own lastActivity?
				
				otherChange.reload();
				
				if(otherChange.getStatus().isCommitted()) {
					cacheCommittedChange(otherChange);
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
					// IMPROVE save own command so that we can be rolled
					// forward in case of timeout
					
					// Don't catch any VoluntaryTimeoutException thrown while
					// rolling forward, as rolling forward will have a start
					// time equal to or greater than that of our own change. So
					// if the roll forward is close to timeout, our own change
					// is even more so.
					
					if(!rollForward(otherChange)) {
						// Someone else grabbed the revision, check again if it
						// is rolled forward.
						otherRev++;
						continue;
					}
				} else {
					commit(otherChange, Status.FailedTimeout);
				}
			}
			
			// other change is now committed
			if(newCommitedRev < 0) {
				newCommitedRev = otherRev;
			}
			
			// IMPROVE: maybe re-read commitedRev?
		}
		
		// gather operations stats
		if(log.isInfoEnabled()) {
			long start = (newCommitedRev >= 0 ? newCommitedRev : commitedRev);
			long end = change.rev;
			long workingWindowSize = end - start;
			if(workingWindowSize > 1) {
				log.info("Current working window size = " + workingWindowSize + " [" + start + ","
				        + end + "]");
			}
		}
		
		if(newCommitedRev >= 0) {
			this.revCache.setLastCommited(newCommitedRev);
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
	 * @return a copy of the created events or null if the command cannot be
	 *         applied.
	 */
	private Pair<List<XAtomicEvent>,int[]> checkPreconditionsAndSaveEvents(GaeChange change,
	        XCommand command, XID actorId) {
		
		XReadableModel currentModel = InternalGaeModel.get(this, change.rev - 1, change.getLocks());
		
		Pair<ChangedModel,DeltaUtils.ModelChange> c = DeltaUtils.executeCommand(currentModel,
		        command);
		if(c == null) {
			change.giveUpIfTimeoutCritical();
			commit(change, Status.FailedPreconditions);
			return null;
		}
		
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, c, actorId, change.rev);
		log.debug("DeltaUtils generated " + events.size() + " events");
		
		int[] valueIds = null;
		
		assert events != null;
		
		try {
			
			if(events.isEmpty()) {
				change.giveUpIfTimeoutCritical();
				commit(change, Status.SuccessNochange);
				return new Pair<List<XAtomicEvent>,int[]>(events, null);
			}
			
			Pair<int[],List<Future<Key>>> res = change.setEvents(events);
			
			valueIds = res.getFirst();
			
			// Wait on all changes.
			for(Future<Key> future : res.getSecond()) {
				FutureUtils.waitFor(future);
			}
			
			change.setStatus(Status.Executing);
			change.save();
			
			change.giveUpIfTimeoutCritical();
			
		} catch(VoluntaryTimeoutException vte) {
			// Since we have not changed the status to EXEUTING, no thread will
			// be able to roll this change forward and we might as well clean it
			// up to prevent unnecessary waits.
			commit(change, Status.FailedTimeout);
		}
		
		return new Pair<List<XAtomicEvent>,int[]>(events, valueIds);
	}
	
	/**
	 * Apply the changes described by the given locks and free any locks held by
	 * this change.
	 * 
	 * @param change
	 * @param events
	 */
	private void executeAndUnlock(GaeChange change, Pair<List<XAtomicEvent>,int[]> events) {
		
		for(XAtomicEvent event : events.getFirst()) {
			log.debug("executeAndUnlock event " + event.toString());
		}
		
		/**
		 * Track which object's revision numbers we have already saved and which
		 * ones we still need to save.
		 * 
		 * This assumes that the events are minimal: A set of events are
		 * "minimal", if:
		 * <ol>
		 * <li>a) if there is an event of type REMOVE, the set contains no other
		 * events for the same XEvent#getChangedEntity();</li>
		 * <li>b) no models, object or fields are added more than once. and</li>
		 * <li>c) the value of no field is added/changed more than once.</li>
		 * </ul>
		 * 
		 * Events generated from a ChangedModel (as used here and in the XModel
		 * transaction code) are always minimal.
		 */
		Set<XID> objectsWithSavedRev = new HashSet<XID>();
		Set<XID> objectsWithPossiblyUnsavedRev = new HashSet<XID>();
		
		List<Future<?>> futures = new ArrayList<Future<?>>(events.getFirst().size());
		
		Boolean modelExists = null;
		for(int i = 0; i < events.getFirst().size(); i++) {
			XAtomicEvent event = events.getFirst().get(i);
			
			assert this.modelAddr.equalsOrContains(event.getChangedEntity());
			assert event.getRevisionNumber() == change.rev;
			
			if(event instanceof XFieldEvent) {
				modelExists = true;
				assert Arrays.asList(ChangeType.REMOVE, ChangeType.ADD, ChangeType.CHANGE)
				        .contains(event.getChangeType());
				
				if(event.isImplied()) {
					assert event.getChangeType() == ChangeType.REMOVE;
					// removed by the XObjectEvent
					continue;
				}
				
				// Set the field as empty and containing the XValue stored
				// at the specified transaction index.
				futures.add(InternalGaeField.set(event.getTarget(), change.rev,
				        events.getSecond()[i], change.getLocks()));
				
				assert !event.isImplied();
				assert event.getTarget().getObject() != null;
				// revision saved in changed field.
				// this assumes (correctly) that the field is not also removed
				// in the same transaction
				objectsWithSavedRev.add(event.getTarget().getObject());
				
			} else if(event instanceof XObjectEvent) {
				modelExists = true;
				if(event.getChangeType() == ChangeType.REMOVE) {
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(),
					        change.getLocks()));
					// cannot save revision in the removed field
					objectsWithPossiblyUnsavedRev.add(event.getTarget().getObject());
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeField.set(event.getChangedEntity(), change.rev,
					        change.getLocks()));
					// revision saved in created field
					objectsWithSavedRev.add(event.getTarget().getObject());
				}
				assert event.getTarget().getObject() != null;
				
			} else if(event instanceof XModelEvent) {
				modelExists = true;
				XID objectId = ((XModelEvent)event).getObjectId();
				if(event.getChangeType() == ChangeType.REMOVE) {
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(),
					        change.getLocks()));
					// object removed, so revision is of no interest
					objectsWithPossiblyUnsavedRev.remove(objectId);
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeObject.createObject(event.getChangedEntity(),
					        change.getLocks(), change.rev));
					// revision saved in new object
					objectsWithSavedRev.add(objectId);
				}
				
			} else {
				assert event instanceof XRepositoryEvent;
				if(event.getChangeType() == ChangeType.REMOVE) {
					modelExists = false;
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(),
					        change.getLocks()));
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					modelExists = true;
					futures.add(InternalGaeModel.createModel(event.getChangedEntity(),
					        change.getLocks()));
				}
			}
			
		}
		
		for(XID objectId : objectsWithPossiblyUnsavedRev) {
			if(!objectsWithSavedRev.contains(objectId)) {
				XAddress objectAddr = XX.resolveObject(this.modelAddr, objectId);
				
				InternalGaeObject.updateObjectRev(objectAddr, change.getLocks(), change.rev);
			}
		}
		
		for(Future<?> future : futures) {
			FutureUtils.waitFor(future);
		}
		
		commit(change, Status.SuccessExecuted);
		
		// update revCache
		if(modelExists != null) {
			this.revCache.setModelExists(modelExists);
		}
		this.revCache.setCurrentModelRev(change.rev);
		
		// // TODO do we really need to ask the memcache here?
		// if(this.revCache.getLastCommited(true) >= change.rev) {
		// updateCurrentRev(Math.max(change.rev,
		// // TODO do we really need to ask the memcache here?
		// this.revCache.getCurrentModelRev(true)));
		// }
	}
	
	/**
	 * Try to roll forward the change with the given revision number. All saved
	 * events will be executed on the {@link InternalGaeModel}, after which the
	 * locks held by the change are freed.
	 * 
	 * As multiple processes might try to roll forward the event at the same
	 * time, the change is grabbed safely before rolling forward. If the change
	 * was grabbed by another process first, false is returned.
	 * 
	 * It is the responsibility of the caller to make sure that the change has
	 * enough information to be rolled forward (all events are saved). See
	 * {@link Status#canRollForward(int)}.
	 * 
	 * @param rev The revision number of the change to roll forward.
	 * @param key The key of the corresponding change entity.
	 * 
	 * @return True if the change was rolled forward or false if the change was
	 *         grabbed by another process.
	 */
	private boolean rollForward(GaeChange change) {
		log.debug("roll forward: " + change);
		assert change.isTimedOut() && change.getStatus().canRollForward();
		
		// Try to "grab" the change entity to prevent multiple processes from
		// rolling forward the same entity.
		Transaction trans = SyncDatastore.beginTransaction();
		
		// We need to re-load the change in the transaction so we will notice
		// when someone else modifies it.
		change.reload(trans);
		
		if(!change.isTimedOut()) {
			// Cannot roll forward, change was grabbed by another process.
			
			// Cleanup the transaction.
			SyncDatastore.endTransaction(trans);
			return false;
		}
		
		// Grab the change.
		change.registerActivity();
		
		change.save(trans);
		// Synchronized by endTransaction()
		try {
			SyncDatastore.endTransaction(trans);
		} catch(ConcurrentModificationException cme) {
			// Cannot roll forward, change was grabbed by another process.
			return false;
		}
		
		assert change.getStatus().canRollForward();
		assert change.getStatus() == Status.Executing;
		
		Pair<List<XAtomicEvent>,int[]> events = change.getAtomicEvents();
		
		executeAndUnlock(change, events);
		
		return true;
	}
	
	/**
	 * Mark the given change as committed.
	 * 
	 * @param status The new (and final) status.
	 */
	private void commit(GaeChange change, Status status) {
		assert status.isCommitted();
		assert !change.getStatus().isCommitted();
		if(status == Status.FailedTimeout) {
			log.warn("Comitting timed out change " + change);
		}
		change.commit(status);
		// TODO do we really need to ask the memcache here?
		if(this.revCache.getLastCommited() == change.rev - 1) {
			this.revCache.setLastCommited(change.rev);
		}
		assert change.getStatus().isCommitted();
		cacheCommittedChange(change);
	}
	
	private static final boolean USE_COMMITTED_CHANGE_CACHE = true;
	
	private static final String VM_COMMITED_CHANGES_CACHENAME = "[.c2]";
	
	private static final long NOT_FOUND = -3;
	
	// IMPROVE experiment with MAX_BATCH_FETCH_SIZE
	private static final int MAX_BATCH_FETCH_SIZE = 100;
	
	private static final long MAX_REVISION_NR = 8 * 1024;
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached
	 */
	private void cacheCommittedChange(GaeChange change) {
		if(USE_COMMITTED_CHANGE_CACHE) {
			assert change != null;
			assert change.getStatus() != null;
			assert change.getStatus().isCommitted();
			log.debug(DebugFormatter.dataPut(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
			        + change.rev, change, Timing.Now));
			Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
			synchronized(committedChangeCache) {
				committedChangeCache.put(change.rev, change);
			}
		}
	}
	
	private GaeChange getCachedChange(long rev) {
		if(!USE_COMMITTED_CHANGE_CACHE) {
			return null;
		}
		GaeChange change;
		Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
		synchronized(committedChangeCache) {
			change = committedChangeCache.get(rev);
		}
		if(change != null) {
			assert change.getStatus().isCommitted();
		}
		log.debug(DebugFormatter.dataGet(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, "" + rev,
		        change, Timing.Now));
		return change;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getModelAddress()
	 */
	@Override
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getCurrentRevisionNumber
	 * ()
	 */
	@Override
	public long getCurrentRevisionNumber() {
		long currentRev = this.revCache.getExactCurrentRev();
		if(currentRev == IRevisionInfo.NOT_SET) {
			log.debug("exact version not locally known");
			currentRev = this.revCache.getCurrentRev();
			log.debug("revCache = " + currentRev);
			currentRev = updateCurrentRev(currentRev);
			this.revCache.setCurrentModelRev(currentRev);
		} else {
			log.debug("currentRev is locally exact defined");
		}
		log.debug("getCurrentRevisionNumber = " + currentRev);
		return currentRev;
	}
	
	private long updateCurrentRev(long lastCurrentRev) {
		log.debug("Updating rev from lastCurrentRev=" + lastCurrentRev + " ...");
		int windowSize = 1;
		long rev = NOT_FOUND;
		long start = lastCurrentRev + 1;
		long end;
		while(rev == NOT_FOUND) {
			log.debug("windowsize = " + windowSize);
			end = start + windowSize - 1;
			rev = updateCurrentRev_Step(start, end);
			// adjust probe window
			windowSize = windowSize * 2;
			// avoid too big windows
			if(windowSize > MAX_BATCH_FETCH_SIZE) {
				windowSize = MAX_BATCH_FETCH_SIZE;
			}
			// move window
			start = end + 1;
		}
		assert rev != NOT_FOUND : "found no rev nr";
		this.revCache.setCurrentModelRev(rev);
		log.debug("Updated rev from [" + lastCurrentRev + " ==> " + rev);
		return rev;
	}
	
	private long updateCurrentRev_Step(long beginRevInclusive, long endRevInclusive) {
		log.debug("Update rev step [" + beginRevInclusive + "," + endRevInclusive + "]");
		if(endRevInclusive >= MAX_REVISION_NR) {
			log.warn("Checking for very high revision number: " + endRevInclusive);
		}
		
		/*
		 * Try to fetch 'initialBatchFetchSize' changes past the last known
		 * "current" revision and put them in the local vm cache.
		 */
		/* === Phase 1: Determine revisions not yet locally cached === */
		Set<Long> locallyMissingRevs = computeLocallyMissingRevs(beginRevInclusive, endRevInclusive);
		log.debug("missingRevs: " + locallyMissingRevs.size());
		
		/* === Phase 2+3: Ask Memcache + Datastore === */
		fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
		log.debug("number of missingRevs after asking DS&MC: " + locallyMissingRevs.size());
		
		/* === Phase 4: Compute result from local cache === */
		/* compute model exists from event before asking range */
		boolean foundEnd = false;
		long currentRev = beginRevInclusive - 1;
		for(long i = beginRevInclusive; i <= endRevInclusive; i++) {
			GaeChange change = getCachedChange(i);
			log.debug("cached change " + i + ": " + change);
			
			// TODO careful: too much caching?
			if(change == null) {
				foundEnd = true;
				break;
			} else {
				if(change.getStatus() == Status.SuccessExecuted) {
					currentRev = i;
					XEvent event = change.getEvent();
					assert event != null;
					if(event instanceof XTransactionEvent) {
						// check only last event
						XTransactionEvent txn = (XTransactionEvent)event;
						assert txn.size() >= 1;
						event = txn.getEvent(txn.size() - 1);
						assert event != null;
					}
					if(event.getTarget().getAddressedType() == XType.XREPOSITORY
					        && event.getChangeType() == ChangeType.REMOVE) {
						this.revCache.setModelExists(false);
					} else {
						this.revCache.setModelExists(true);
					}
				}
			}
		}
		
		if(foundEnd) {
			log.debug("Step: return currentRev = " + currentRev);
			return currentRev;
		}
		// else
		assert locallyMissingRevs.size() == 0;
		/* === Phase 5: go on */
		/* We know these revisions are all committed */
		this.revCache.setLastCommited(endRevInclusive);
		/*
		 * All revisions we looked at have been processed. Need to repeat the
		 * process by looking at more revisions.
		 */
		return NOT_FOUND;
	}
	
	/**
	 * Fetch all given revisions from memcache and those not found there from
	 * datastore. New revisions are added to local cache.
	 * 
	 * 
	 * 
	 * @param locallyMissingRevs Caller is responsible not to ask for revisions
	 *            already known locally. Removes all revisions that have been
	 *            found from this set.
	 */
	private void fetchMissingRevisionsFromMemcacheAndDatastore(Set<Long> locallyMissingRevs) {
		/* === Phase 2: Ask memcache === */
		List<String> memcacheBatchRequest = new ArrayList<String>(locallyMissingRevs.size());
		if(locallyMissingRevs.size() > 0) {
			// prepare batch request
			for(long askRev : locallyMissingRevs) {
				Key key = KeyStructure.createChangeKey(getModelAddress(), askRev);
				memcacheBatchRequest.add(KeyStructure.toString(key));
			}
			// batch request
			Map<String,Object> memcacheResult = Memcache.getEntities(memcacheBatchRequest);
			long newLastCommitted = -1;
			for(Entry<String,Object> entry : memcacheResult.entrySet()) {
				Key key = KeyStructure.toKey(entry.getKey());
				Object v = entry.getValue();
				GaeAssert.gaeAssert(v != null, "v!=null");
				assert v != null;
				assert v instanceof Entity : v.getClass();
				Entity entity = (Entity)v;
				assert !entity.equals(Memcache.NULL_ENTITY) : "" + key;
				long rev = KeyStructure.getRevisionFromChangeKey(key);
				GaeChange change = new GaeChange(getModelAddress(), rev, entity);
				assert change.getStatus() != null;
				assert change.getStatus().isCommitted() : change.rev + " " + change.getStatus();
				cacheCommittedChange(change);
				if(change.rev > newLastCommitted) {
					newLastCommitted = change.rev;
				}
				locallyMissingRevs.remove(change.rev);
				log.debug("Found in memcache " + change.rev);
			}
			if(newLastCommitted >= 0) {
				this.revCache.setLastCommited(newLastCommitted);
			}
			// re-use strings in memcacheBatchRequest: retain only *still*
			// missing keys (neither locally found, nor in
			// memcache)
			memcacheBatchRequest.removeAll(memcacheResult.keySet());
		} else {
			// log.debug("All found in localVmCache");
		}
		
		/* === Phase 3: Ask datastore === */
		if(memcacheBatchRequest.size() > 0) {
			// prepare batch request
			List<Key> datastoreBatchRequest = new ArrayList<Key>(memcacheBatchRequest.size());
			for(String keyStr : memcacheBatchRequest) {
				Key key = KeyStructure.toKey(keyStr);
				datastoreBatchRequest.add(key);
			}
			// execute batch request
			Map<Key,Entity> datastoreResult = SyncDatastore.getEntities(datastoreBatchRequest);
			Map<String,Entity> memcacheBatchPut = new HashMap<String,Entity>();
			long newLastTaken = -1;
			long newLastCommitted = -1;
			for(Entry<Key,Entity> entry : datastoreResult.entrySet()) {
				Key key = entry.getKey();
				Entity entity = entry.getValue();
				assert entity != null;
				assert entity != Memcache.NULL_ENTITY;
				long revFromKey = KeyStructure.getRevisionFromChangeKey(key);
				
				// process status of change
				GaeChange change = new GaeChange(getModelAddress(), revFromKey, entity);
				Status status = change.getStatus();
				if(status.isCommitted()) {
					// use it
					log.debug("Found in datastore, comitted " + change.rev);
					memcacheBatchPut.put(KeyStructure.toString(key), entity);
					cacheCommittedChange(change);
					locallyMissingRevs.remove(revFromKey);
					if(revFromKey > newLastCommitted) {
						newLastCommitted = revFromKey;
					}
				} else {
					// FIXME .......
					log.warn("Change is " + change.getStatus() + " timeout?" + change.isTimedOut()
					        + ". Dump: " + change + " ||| Now = " + System.currentTimeMillis());
					if(change.isTimedOut()) {
						log.debug("handle timed-out change " + change.rev);
						boolean success = handleTimeout(change);
						// TODO @Daniel: why reload?
						if(!success) {
							change.reload();
						}
						// change might be complete now (we or another process
						// might have done it)
						if(change.getStatus().isCommitted()) {
							// use it
							memcacheBatchPut.put(KeyStructure.toString(key), entity);
							cacheCommittedChange(change);
							locallyMissingRevs.remove(revFromKey);
						} else {
							log.warn("made no progress on time-out change " + change.rev);
						}
					} else {
						assert status == Status.Creating || status == Status.Executing;
						log.warn("Change " + change.rev + " is still " + change.getStatus()
						        + ". Not cached.");
						// don't cache
					}
					if(change.rev > newLastTaken) {
						newLastTaken = change.rev;
					}
				}
			}
			if(newLastTaken >= 0) {
				this.revCache.setLastTaken(newLastTaken);
			}
			if(newLastCommitted >= 0) {
				this.revCache.setLastCommited(newLastCommitted);
			}
			
			// update memcache IMPROVE do this async
			XydraRuntime.getMemcache().putAll(memcacheBatchPut);
		}
	}
	
	/**
	 * @param startRevInclusive
	 * @param endRevInclusive
	 * @return
	 */
	private Set<Long> computeLocallyMissingRevs(long startRevInclusive, long endRevInclusive) {
		log.debug("computeLocallyMissingRevs [" + startRevInclusive + "," + endRevInclusive + "]");
		Set<Long> locallyMissingRevs = new HashSet<Long>();
		for(long i = startRevInclusive; i <= endRevInclusive; i++) {
			// add key only if result not known locally yet
			GaeChange change = this.getCachedChange(i);
			if(change == null) {
				locallyMissingRevs.add(i);
			} else {
				assert change.rev == i;
				assert change.getStatus().isCommitted();
				// log.debug("Already locally cached: " +
				// DebugFormatter.format(change));
			}
		}
		return locallyMissingRevs;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getEventsBetween(
	 * long, long)
	 */
	@Override
	public List<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		log.debug("getEventsBetween [" + beginRevision + "," + endRevision + "] @"
		        + getModelAddress());
		/* sanity checks */
		if(beginRevision < 0) {
			throw new IndexOutOfBoundsException(
			        "beginRevision is not a valid revision number, was " + beginRevision);
		}
		if(endRevision < 0) {
			throw new IndexOutOfBoundsException("endRevision is not a valid revision number, was "
			        + endRevision);
		}
		if(beginRevision > endRevision) {
			throw new IllegalArgumentException("beginRevision may not be greater than endRevision");
		}
		if(endRevision <= 0) {
			return new ArrayList<XEvent>(0);
		}
		
		/* adjust range */
		long endRev = endRevision;
		/*
		 * ask one revision below requested to see the last
		 * repocommand.removeModel if there was one
		 */
		long begin = beginRevision < 0 ? 0 : beginRevision;
		long currentRev = getCurrentRevisionNumber();
		if(currentRev == -1) {
			return null;
		}
		// Don't try to get more events than there actually are.
		if(beginRevision > currentRev) {
			return new ArrayList<XEvent>(0);
		} else if(endRev > currentRev) {
			endRev = currentRev;
		}
		
		log.debug("Adjusted range [" + begin + "," + endRev + "]");
		
		List<XEvent> events = new ArrayList<XEvent>();
		
		Set<Long> locallyMissingRevs = computeLocallyMissingRevs(begin, endRev);
		/* Ask Memcache + Datastore */
		fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
		// construct result
		long newRev = -1;
		for(long i = begin; i <= endRev; i++) {
			log.debug("Trying to find & apply event " + i);
			GaeChange change = getCachedChange(i);
			// use only positive information
			if(change != null) {
				if(change.getStatus() == Status.SuccessExecuted) {
					log.debug("Change " + i + " rev=" + change.rev + " is successful");
					XEvent event = change.getEvent();
					assert event != null : change;
					events.add(event);
					newRev = i;
				} else {
					assert change.getStatus() != Status.Creating;
					assert change.getStatus() != Status.Executing;
					log.debug("Change " + i + " is " + change.getStatus().name());
				}
			} else {
				log.warn("==== Change " + i + " is null, was asking [" + begin + "," + endRev
				        + "]. Retry.");
				// FIXME RECHECK
				Set<Long> set = new HashSet<Long>();
				set.add(i);
				fetchMissingRevisionsFromMemcacheAndDatastore(set);
				Thread.yield();
				i--;
				continue;
				
				//
				// throw new IllegalStateException("Change " + i +
				// " null was asking [" + begin + ","
				// + endRev + "]");
			}
		}
		if(newRev >= 0) {
			this.revCache.setCurrentModelRev(newRev);
		}
		
		GaeAssert.gaeAssert(eventsAreWithinRange(events, begin, endRev));
		
		return events;
	}
	
	private boolean eventsAreWithinRange(List<XEvent> events, long begin, long endRev) {
		for(XEvent e : events) {
			GaeAssert.gaeAssert(e.getRevisionNumber() >= begin);
			GaeAssert.gaeAssert(e.getRevisionNumber() <= endRev);
		}
		return true;
	}
	
	/**
	 * Roll forward a timed out entity if possible, otherwise just mark it as
	 * timed out.
	 * 
	 * @return false if the entity could not be rolled forward.
	 */
	private boolean handleTimeout(GaeChange change) {
		log.debug("handleTimeout: " + change);
		if(change.getStatus().canRollForward()) {
			// FIXME Why return true if we just expect another thread to likely
			// roll forward in the future?
			return rollForward(change);
		} else {
			commit(change, Status.FailedTimeout);
			return false;
		}
	}
	
	@Override
	public AsyncValue getValue(long rev, int transindex) {
		
		GaeChange change = getCachedChange(rev);
		if(change != null) {
			int realindex = GaeEvents.getEventIndex(transindex);
			if(realindex >= 0) {
				XEvent event = change.getEvent();
				if(event instanceof XTransactionEvent) {
					assert ((XTransactionEvent)event).size() > realindex;
					event = ((XTransactionEvent)event).getEvent(realindex);
				} else {
					assert realindex == 0;
				}
				assert event instanceof XFieldEvent;
				return new AsyncValue(((XFieldEvent)event).getNewValue());
			}
		}
		
		return GaeEvents.getValue(this.modelAddr, rev, transindex);
	}
	
	/**
	 * @return the instance-level cache of committed change objects
	 */
	@SuppressWarnings("unchecked")
	private Map<Long,GaeChange> getCommittedChangeCache() {
		String key = "changes:" + this.modelAddr;
		Map<String,Object> instanceCache = InstanceContext.getInstanceCache();
		Map<Long,GaeChange> committedChangeCache;
		synchronized(instanceCache) {
			committedChangeCache = (Map<Long,GaeChange>)instanceCache.get(key);
			if(committedChangeCache == null) {
				log.debug(DebugFormatter.init(VM_COMMITED_CHANGES_CACHENAME));
				committedChangeCache = new HashMap<Long,GaeChange>();
				InstanceContext.getInstanceCache().put(key, committedChangeCache);
			}
		}
		return committedChangeCache;
	}
	
	public void clear() {
		log.info("Cleared. Make to sure to also clear memcache.");
		this.getCommittedChangeCache().clear();
		this.revCache.clear();
	}
	
	@Override
	public boolean exists() {
		return this.revCache.modelExists() != null && this.revCache.modelExists();
	}
	
}

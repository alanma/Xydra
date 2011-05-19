package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


/**
 * A class responsible for executing and logging changes to a specific XModel in
 * the GAE datastore.
 * 
 * This class is the core of the GAE {@link IXydraServer} implementation.
 * 
 * Keys for XMODEL, XOBJEC and XFIELD entities are encoded according to
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
 * <dd>Represent fields and managed by {@link InternalGaeField}. The value is
 * not stored in the field entity. Instead, additionally to the field revision,
 * an index into the transaction (or zero) is stored that can be used with
 * {@link GaeEvents#getValue(XAddress, long, int)} to load the {@link XValue}.</dd>
 * 
 * 
 * <dt>Entity type XCHANGE</dt>
 * <dd>These represent a change to the model resulting from a single
 * {@link XCommand} (which may be a {@link XTransaction}). These entities
 * represent both an entry into the {@link XChangeLog} as well as a change that
 * is currently in progress. Keys are encoded according to
 * {@link KeyStructure#createChangeKey(XAddress, long)}
 * 
 * The XCHANGE entities are managed by {@link GaeChange} stores the status of
 * the change, the required locks, the actor that initiated the change, the time
 * the (last) process started working on the change.
 * 
 * Events and small XValues are also saved in the XCHANGE entities. These
 * properties are managed by {@link GaeEvents}. No events are guaranteed to be
 * set before the change has reached {@link Status#Executing}.
 * 
 * 
 * <dt>Entity type XEVENT</dt>
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
public class GaeChangesService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesService.class);
	
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
	private static final long WAIT_MAX = 1000; // TODO set
	
	// Implementation.
	
	private final XAddress modelAddr;
	private final RevisionCache revCache;
	
	public GaeChangesService(XAddress modelAddr) {
		this.modelAddr = modelAddr;
		this.revCache = new RevisionCache(modelAddr);
	}
	
	/**
	 * Execute the given {@link XCommand} as a transaction.
	 * 
	 * @param command The command to execute. (can be a {@link XTransaction})
	 * @param actorId The actor to log in the resulting event.
	 * @return If the command executed successfully, the revision of the
	 *         resulting {@link XEvent} or {@link XCommand#NOCHANGE} if the
	 *         command din't change anything; {@link XCommand#FAILED} otherwise.
	 * 
	 * @throws VoluntaryTimeoutException if we came too close to the timeout
	 *             while executing the command. A caller may catch this
	 *             exception and try again, but doing so may just result in a
	 *             timeout from GAE if TIME_CRITICAL is set to more than half
	 *             the GAE timeout.
	 * 
	 * @see XydraStore#executeCommands(XID, String, XCommand[],
	 *      org.xydra.store.Callback)
	 */
	public long executeCommand(XCommand command, XID actorId) {
		
		// IMPROVE maybe let the caller provide an XID that can be used to check
		// the status in case there is a GAE timeout?
		
		assert this.modelAddr.equalsOrContains(command.getChangedEntity()) : "cannot handle command "
		        + command;
		
		GaeLocks locks = new GaeLocks(command);
		
		GaeChange change = grabRevisionAndRegisterLocks(locks, actorId);
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		
		waitForLocks(change);
		
		Pair<List<XAtomicEvent>,int[]> events = checkPreconditionsAndSaveEvents(change, command,
		        actorId);
		if(events == null) {
			return XCommand.FAILED;
		} else if(events.getFirst().isEmpty()) {
			// TODO maybe return revision?
			return XCommand.NOCHANGE;
		}
		
		executeAndUnlock(change, events);
		
		return change.rev;
	}
	
	/**
	 * Grabs the first available revision number and registers a change for that
	 * revision number with the provided locks.
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 */
	private GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId) {
		
		for(long rev = this.revCache.getLastTaken() + 1;; rev++) {
			
			if(getCachedChange(rev) != null) {
				// Revision already taken.
				continue;
			}
			
			// Try to grab this revision.
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity changeEntity = GaeUtils.getEntityExists(key, trans);
			
			if(changeEntity == null) {
				
				GaeChange newChange = new GaeChange(this.modelAddr, rev, locks, actorId);
				newChange.save(trans);
				
				try {
					GaeUtils.endTransaction(trans);
				} catch(ConcurrentModificationException cme) {
					
					log.info("failed to take revision: " + key);
					
					// transaction failed as another process wrote to this
					// entity
					
					// IMPROVE if we can assume that at least one thread was
					// successful, we go ahead to the next revision.
					
					// Check this revision again
					rev--;
					continue;
				}
				
				this.revCache.setLastTaken(rev);
				
				// transaction succeeded, we have a revision
				return newChange;
				
			} else {
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				
				// Revision already taken.
				
				GaeUtils.endTransaction(trans);
				
				// Since we read the entity anyway, might as well use that
				// information.
				Status status = change.getStatus();
				if(status.isCommitted()) {
					cacheCommittedChange(change);
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
			otherChange = new GaeChange(this.modelAddr, otherRev, GaeUtils.getEntity(key));
			
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
		
		if(newCommitedRev > 0) {
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
				GaeUtils.waitFor(future);
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
		
		/*
		 * Track which object's revision numbers we have already saved and which
		 * ones we still need to save.
		 * 
		 * This assumes that the events are minimal: A set of events are
		 * "minimal", if: a) if there is an event of type RMOVE, the set
		 * contains no other events for the same XEvent#getChangedEntity();, b)
		 * no models, object or fields are added more than once. and c) the
		 * value of no field is added/changed more than once. </ul>
		 * 
		 * Events generated from a ChangedModel (as used here and in the XModel
		 * transaction code) are always minimal.
		 */
		Set<XID> objectsWithSavedRev = new HashSet<XID>();
		Set<XID> objectsWithPossiblyUnsavedRev = new HashSet<XID>();
		
		List<Future<?>> futures = new ArrayList<Future<?>>(events.getFirst().size());
		
		for(int i = 0; i < events.getFirst().size(); i++) {
			XAtomicEvent event = events.getFirst().get(i);
			
			assert this.modelAddr.equalsOrContains(event.getChangedEntity());
			assert event.getRevisionNumber() == change.rev;
			
			if(event instanceof XFieldEvent) {
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
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(),
					        change.getLocks()));
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeModel.createModel(event.getChangedEntity(),
					        change.getLocks()));
				}
			}
			
		}
		
		// IMPROVE can this be made asynchronous?
		for(XID objectId : objectsWithPossiblyUnsavedRev) {
			if(!objectsWithSavedRev.contains(objectId)) {
				XAddress objectAddr = XX.resolveObject(this.modelAddr, objectId);
				
				InternalGaeObject.updateObjectRev(objectAddr, change.getLocks(), change.rev);
			}
		}
		
		for(Future<?> future : futures) {
			GaeUtils.waitFor(future);
		}
		
		commit(change, Status.SuccessExecuted);
		
		if(this.revCache.getLastCommited() >= change.rev) {
			updateCurrentRev(Math.max(change.rev, this.revCache.getCurrent()));
		}
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
		
		assert change.isTimedOut() && change.getStatus().canRollForward();
		
		// Try to "grab" the change entity to prevent multiple processes from
		// rolling forward the same entity.
		Transaction trans = GaeUtils.beginTransaction();
		
		// We need to re-load the change in the transaction so we will notice
		// when someone else modifies it.
		change.reload(trans);
		
		if(!change.isTimedOut()) {
			// Cannot roll forward, change was grabbed by another process.
			
			// Cleanup the transaction.
			GaeUtils.endTransaction(trans);
			return false;
		}
		
		// Grab the change.
		change.registerActivity();
		
		change.save(trans);
		// Synchronized by endTransaction()
		try {
			GaeUtils.endTransaction(trans);
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
		change.commit(status);
		if(this.revCache.getLastCommited() == change.rev - 1) {
			this.revCache.setLastCommited(change.rev);
		}
		cacheCommittedChange(change);
	}
	
	private static final boolean useLocalVMCache = true;
	private Map<Long,GaeChange> committedChangeCache = new HashMap<Long,GaeChange>();
	
	protected void cacheCommittedChange(GaeChange change) {
		assert change.getStatus().isCommitted();
		if(useLocalVMCache) {
			log.trace("cache PUT " + this.modelAddr + " rev=" + change.rev + " := " + change);
			synchronized(this.committedChangeCache) {
				this.committedChangeCache.put(change.rev, change);
			}
		}
	}
	
	protected GaeChange getCachedChange(long rev) {
		if(!useLocalVMCache) {
			return null;
		}
		GaeChange change;
		synchronized(this.committedChangeCache) {
			change = this.committedChangeCache.get(rev);
			
		}
		if(change != null) {
			assert change.getStatus().isCommitted();
		}
		log.trace("cache GET " + this.modelAddr + " rev=" + rev + " => " + change);
		return change;
	}
	
	/**
	 * @return the {@link XAddress} of the model managed by this
	 *         {@link GaeChangesService} instance.
	 */
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	/**
	 * Get the model's current revision number.
	 * 
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[],
	 *      org.xydra.store.Callback)
	 */
	public long getCurrentRevisionNumber() {
		
		long currentRev = this.revCache.getCurrentIfSet();
		if(currentRev != RevisionCache.NOT_SET) {
			return currentRev;
		} else {
			return updateCurrentRev(-1L);
		}
	}
	
	private long updateCurrentRev(long lastCurrentRev) {
		
		long currentRev = lastCurrentRev;
		long rev = currentRev;
		
		// Try to fetch one change past the last known "current" revision.
		List<AsyncChange> batch = new ArrayList<AsyncChange>(1);
		batch.add(getChangeAt(rev + 1));
		
		int pos = 0;
		
		long end = this.revCache.getLastCommitedIfSet();
		if(end == RevisionCache.NOT_SET) {
			end = Long.MAX_VALUE;
		}
		
		for(; rev <= end; rev++) {
			
			GaeChange change = batch.get(pos).get();
			if(change == null) {
				break;
			}
			
			Status status = change.getStatus();
			if(!status.isCommitted()) {
				if(change.isTimedOut()) {
					if(handleTimeout(change)) {
						change.reload();
						rev--;
						continue;
					}
				} else {
					// Found the lastCommitedRev
					break;
				}
			}
			
			// Only update the current revision if the command actually changed
			// something.
			if(status == Status.SuccessExecuted) {
				currentRev = rev + 1;
			}
			
			// Asynchronously fetch new change entities.
			batch.set(pos, getChangeAt(rev + batch.size() + 1));
			pos++;
			if(pos == batch.size()) {
				batch.add(getChangeAt(rev + batch.size() + 2));
				pos = 0;
			}
			
		}
		
		this.revCache.setLastCommited(rev);
		this.revCache.setCurrent(currentRev);
		
		return currentRev;
	}
	
	/**
	 * Get the change at the specified revision number.
	 */
	public AsyncChange getChangeAt(long rev) {
		
		GaeChange change = getCachedChange(rev);
		if(change != null) {
			return new AsyncChange(change);
		}
		
		return new AsyncChange(this, rev);
	}
	
	/**
	 * Fetch a range of events from the datastore.
	 * 
	 * See {@link GetEventsRequest} for parameters.
	 * 
	 * @return a list of events or null if this model was never created.
	 * 
	 * @see XydraStore#getEvents(XID, String, GetEventsRequest[],
	 *      org.xydra.store.Callback)
	 */
	public List<XEvent> getEventsBetween(long beginRevision, long _endRevision) {
		
		long endRevision = _endRevision;
		
		log.info("getEventsBetwen " + beginRevision + " " + endRevision + " @" + getModelAddress());
		
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
		
		long begin = beginRevision < 0 ? 0 : beginRevision;
		
		long currentRev = this.revCache.getCurrentIfSet();
		
		// Don't try to get more events than there actually are.
		if(currentRev == RevisionCache.NOT_SET) {
			currentRev = -1L;
		} else if(beginRevision > currentRev) {
			return new ArrayList<XEvent>(0);
		} else if(endRevision > currentRev) {
			endRevision = currentRev;
		}
		
		List<XEvent> events = new ArrayList<XEvent>();
		
		// Asynchronously fetch an initial batch of change entities
		int initialBuffer = 1;
		if(endRevision <= currentRev) {
			initialBuffer = (int)(endRevision - begin + 1);
		} else {
			// IMPROVE maybe use an initial buffer size of currentRev - begin +
			// 1?
		}
		List<AsyncChange> batch = new ArrayList<AsyncChange>(initialBuffer);
		for(int i = 0; i < initialBuffer; i++) {
			batch.add(getChangeAt(begin + i));
		}
		
		int pos = 0;
		
		// Only update the currentRev cache value if we aren't skipping any
		// events.
		boolean trackCurrentRev = (begin <= currentRev);
		
		long rev = begin;
		for(; rev <= endRevision; rev++) {
			
			// Wait for the first change entities
			GaeChange change = batch.get(pos).get();
			if(change == null) {
				// Found end of the change log
				break;
			}
			
			Status status = change.getStatus();
			if(!status.isCommitted()) {
				if(change.isTimedOut()) {
					if(handleTimeout(change)) {
						change.reload();
						rev--;
						continue;
					}
				} else {
					// Found the lastCommitedRev
					break;
				}
			}
			
			XEvent event = change.getEvent();
			if(event != null) {
				// Something actually changed
				if(trackCurrentRev) {
					currentRev = rev;
				}
				events.add(event);
			}
			
			// Asynchronously fetch new change entities.
			if(rev + batch.size() <= endRevision) {
				batch.set(pos, getChangeAt(rev + batch.size()));
			}
			pos++;
			if(pos == batch.size()) {
				if(rev + batch.size() + 1 <= endRevision) {
					batch.add(getChangeAt(rev + batch.size() + 1));
				}
				pos = 0;
			}
			
		}
		
		if(currentRev == -1) {
			assert events.isEmpty();
			return null;
		}
		
		if(trackCurrentRev) {
			this.revCache.setLastCommited(rev - 1);
			this.revCache.setCurrent(currentRev);
		}
		
		return events;
	}
	
	/**
	 * Roll forward a timed out entity if possible, otherwise just mark it as
	 * timed out.
	 * 
	 * @return false if the entity could not be rolled forward.
	 */
	private boolean handleTimeout(GaeChange change) {
		if(change.getStatus().canRollForward()) {
			rollForward(change);
			return true;
		} else {
			commit(change, Status.FailedTimeout);
			return false;
		}
	}
	
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
}

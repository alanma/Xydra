package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
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
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.GaeUtils.AsyncEntity;
import org.xydra.store.impl.gae.changes.GaeChange.Status;

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
 * an index into the transaction (or zero) is stored that identifies the
 * {@link XAtomicEvent} containing the corresponding value.</dd>
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
 * See {@link GaeEventService} for how events associated with this change are
 * stored. No events are guaranteed to be set before the change has reached
 * {@link Status#Executing}.
 * 
 * Possible {@link Status} progression for XCHANGE Entities:
 * 
 * <pre>
 * 
 *  Creating ------> FailedTimeout
 *     |
 *     |----> Executing ----> SucessExecuted
 *     |
 *     |----> SuccessNochange
 *     |
 *     \----> FailedPreconditions
 * 
 * </pre>
 * 
 * </dd>
 * 
 * TODO document XValue storing, locking
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
	 * Maximum time to wait before re-checking the status of an event who'se
	 * locks we need.
	 */
	private static final long WAIT_MAX = 1000; // TODO set
	
	// Implementation.
	
	private final XAddress modelAddr;
	
	public GaeChangesService(XAddress modelAddr) {
		this.modelAddr = modelAddr;
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
		
		Set<XAddress> locks = GaeLocks.calculateRequiredLocks(command);
		
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
	private GaeChange grabRevisionAndRegisterLocks(Set<XAddress> locks, XID actorId) {
		
		// Prepare locks to be saved in GAE entity.
		List<String> lockStrs = GaeChange.prepareLocks(locks);
		
		for(long rev = getCachedLastTakenRevision() + 1;; rev++) {
			
			// Try to grab this revision.
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity changeEntity = GaeUtils.getEntityExists(key, trans);
			
			if(changeEntity == null) {
				
				Entity newChange = new Entity(key);
				GaeChange.setLocks(newChange, lockStrs);
				GaeChange.setStatus(newChange, Status.Creating);
				long startTime = GaeChange.registerActivity(newChange);
				GaeChange.setActor(newChange, actorId);
				
				GaeUtils.putEntityAsync(newChange, trans);
				// Synchronized by endTransaction()
				
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
				
				setCachedLastTakenRevision(rev);
				
				// transaction succeeded, we have a revision
				return new GaeChange(rev, startTime, locks, newChange);
				
			} else {
				
				// Revision already taken.
				
				GaeUtils.endTransaction(trans);
				
				// Since we read the entity anyway, might as well use that
				// information.
				int status = GaeChange.getStatus(changeEntity);
				if(!Status.isCommitted(status) && !Status.canRollForward(status)
				        && GaeChange.isTimedOut(changeEntity)) {
					GaeChange.cleanup(changeEntity, Status.FailedTimeout);
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
		
		long commitedRev = getCachedLastCommitedRevision();
		
		// Track if we find a greater last commitedRev.
		long newCommitedRev = -1;
		
		for(long otherRev = change.rev - 1; otherRev > commitedRev; otherRev--) {
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, otherRev);
			Entity otherChange = GaeUtils.getEntity(key);
			assert otherChange != null;
			
			// Check if the change is committed.
			int status = GaeChange.getStatus(otherChange);
			if(Status.isCommitted(status)) {
				if(newCommitedRev < 0) {
					newCommitedRev = otherRev;
				}
				// finished, so should have no locks
				continue;
			}
			
			// Check if the change needs conflicting locks.
			Set<XAddress> otherLocks = GaeChange.getLocks(otherChange);
			assert otherLocks != null : "locks should not be removed before change is commited";
			if(!GaeLocks.isConflicting(change.locks, otherLocks)) {
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
			while(!(timedOut = GaeChange.isTimedOut(otherChange))) {
				
				// IMPROVE save own command if waitTime is too long (so that we
				// can be rolled forward in case of timeout)
				try {
					Thread.sleep(waitTime);
				} catch(InterruptedException e) {
					// ignore interrupt
				}
				// IMPROVE update own lastActivity?
				
				otherChange = GaeUtils.getEntity(key);
				assert otherChange != null : "change entities should not vanish";
				
				status = GaeChange.getStatus(otherChange);
				if(Status.isCommitted(status)) {
					// now finished, so should have no locks anymore
					assert !GaeChange.hasLocks(otherChange);
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
				if(Status.canRollForward(status)) {
					// IMPROVE save own command so that we can be rolled
					// forward in case of timeout
					
					// Don't catch any VoluntaryTimeoutException thrown while
					// rolling forward, as rolling forward will have a start
					// time equal to or greater than that of our own change. So
					// if the roll forward is close to timeout, our own change
					// is vent more so.
					
					if(!rollForward(otherRev, otherChange.getKey())) {
						// Someone else grabbed the revision, check again if it
						// is rolled forward.
						otherRev++;
						continue;
					}
				} else {
					GaeChange.cleanup(otherChange, Status.FailedTimeout);
				}
			}
			
			// other change is now committed
			if(newCommitedRev < 0) {
				newCommitedRev = otherRev;
			}
			
			// IMPROVE: maybe re-read commitedRev?
		}
		
		if(newCommitedRev > 0) {
			setCachedLastCommitedRevision(newCommitedRev);
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
		
		XReadableModel currentModel = InternalGaeModel.get(this, change.rev - 1, change.locks);
		
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
			
			Pair<int[],List<Future<Key>>> res = GaeEventService.saveEvents(this.modelAddr,
			        change.entity, events);
			valueIds = res.getFirst();
			
			// Wait on all changes.
			for(Future<Key> future : res.getSecond()) {
				GaeUtils.waitFor(future);
			}
			
			GaeChange.setStatus(change.entity, Status.Executing);
			GaeUtils.putEntity(change.entity);
			
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
				        events.getSecond()[i], change.locks));
				
				assert !event.isImplied();
				assert event.getTarget().getObject() != null;
				// revision saved in changed field.
				// this assumes (correctly) that the field is not also removed
				// in the same transaction
				objectsWithSavedRev.add(event.getTarget().getObject());
				
			} else if(event instanceof XObjectEvent) {
				if(event.getChangeType() == ChangeType.REMOVE) {
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(), change.locks));
					// cannot save revision in the removed field
					objectsWithPossiblyUnsavedRev.add(event.getTarget().getObject());
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeField.set(event.getChangedEntity(), change.rev,
					        change.locks));
					// revision saved in created field
					objectsWithSavedRev.add(event.getTarget().getObject());
				}
				assert event.getTarget().getObject() != null;
				
			} else if(event instanceof XModelEvent) {
				XID objectId = ((XModelEvent)event).getObjectId();
				if(event.getChangeType() == ChangeType.REMOVE) {
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(), change.locks));
					// object removed, so revision is of no interest
					objectsWithPossiblyUnsavedRev.remove(objectId);
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeObject.createObject(event.getChangedEntity(),
					        change.locks, change.rev));
					// revision saved in new object
					objectsWithSavedRev.add(objectId);
				}
				
			} else {
				assert event instanceof XRepositoryEvent;
				if(event.getChangeType() == ChangeType.REMOVE) {
					futures.add(InternalGaeXEntity.remove(event.getChangedEntity(), change.locks));
				} else {
					assert event.getChangeType() == ChangeType.ADD;
					futures.add(InternalGaeModel
					        .createModel(event.getChangedEntity(), change.locks));
				}
			}
			
		}
		
		// IMPROVE can this be made asynchronous?
		for(XID objectId : objectsWithPossiblyUnsavedRev) {
			if(!objectsWithSavedRev.contains(objectId)) {
				XAddress objectAddr = XX.resolveObject(this.modelAddr, objectId);
				
				InternalGaeObject.updateObjectRev(objectAddr, change.locks, change.rev);
			}
		}
		
		for(Future<?> future : futures) {
			GaeUtils.waitFor(future);
		}
		
		commit(change, Status.SuccessExecuted);
		
		if(getCachedCurrentRevision() == change.rev - 1) {
			setCachedCurrentRevision(change.rev);
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
	private boolean rollForward(long rev, Key key) {
		
		assert KeyStructure.assertRevisionInKey(key, rev);
		
		// Try to "grab" the change entity to prevent multiple processes from
		// rolling forward the same entity.
		Transaction trans = GaeUtils.beginTransaction();
		Entity changeEntity = GaeUtils.getEntity(key, trans);
		assert changeEntity != null;
		
		if(!GaeChange.isTimedOut(changeEntity)) {
			// Cannot roll forward, change was grabbed by another process.
			
			// Cleanup the transaction.
			GaeUtils.endTransaction(trans);
			
			return false;
		}
		/*
		 * IMPROVE use the PROP_LAST_ACTIVITY of our own change instead? Both
		 * now() and the last activity of our own change are "correct" as
		 * whatever we set will be used for
		 * giveUpIfTimeoutCritical(change.startTime); However if TIME_CRITICAL
		 * is set close to the GAE timeout (as it should be to reduce
		 * unnecessary voluntary timeouts), setting this higher than our own
		 * start time might cause other changes to wait longer for this one
		 * while not actually reducing the chance of the roll forward timing out
		 * (only changing it from a voluntary timeout to a GAE enforced timeout)
		 */
		long now = GaeChange.registerActivity(changeEntity);
		GaeUtils.putEntityAsync(changeEntity, trans);
		// Synchronized by endTransaction()
		try {
			GaeUtils.endTransaction(trans);
		} catch(ConcurrentModificationException cme) {
			// Cannot roll forward, change was grabbed by another process.
			return false;
		}
		
		assert Status.canRollForward(GaeChange.getStatus(changeEntity));
		assert GaeChange.getStatus(changeEntity) == Status.Executing.value;
		
		Set<XAddress> locks = GaeChange.getLocks(changeEntity);
		
		GaeChange change = new GaeChange(rev, now, locks, changeEntity);
		
		Pair<List<XAtomicEvent>,int[]> events = loadEvents(change);
		
		executeAndUnlock(change, events);
		
		return true;
	}
	
	/**
	 * Load the individual events associated with the given change.
	 * 
	 * @param change The change whose events should be loaded.
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	private Pair<List<XAtomicEvent>,int[]> loadEvents(GaeChange change) {
		
		assert KeyStructure.assertRevisionInKey(change.entity.getKey(), change.rev);
		assert Status.hasEvents(GaeChange.getStatus(change.entity));
		
		Pair<XAtomicEvent[],int[]> res = GaeEventService.loadAtomicEvents(this.modelAddr,
		        change.rev, null, change.entity, false);
		
		return new Pair<List<XAtomicEvent>,int[]>(Arrays.asList(res.getFirst()), res.getSecond());
	}
	
	/**
	 * Mark the given change as committed.
	 * 
	 * @param status The new (and final) status.
	 */
	private void commit(GaeChange change, Status status) {
		assert Status.isCommitted(status.value);
		GaeChange.cleanup(change.entity, status);
		if(getCachedLastCommitedRevision() == change.rev - 1) {
			setCachedLastCommitedRevision(change.rev);
		}
	}
	
	/**
	 * @return the {@link XAddress} of the model managed by this
	 *         {@link GaeChangesService} instance.
	 */
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	private long getCachedLastCommitedRevision() {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		Long entry = (Long)cache.get(getCommitedRevCacheName());
		long rev = (entry == null) ? -1L : entry;
		
		long current = getCachedCurrentRevision();
		
		return (current > rev ? current : rev);
	}
	
	/**
	 * Set a new value to be returned by
	 * {@link #getCachedLastCommitedRevision()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	private void setCachedLastCommitedRevision(long l) {
		increaseCachedValue(getCommitedRevCacheName(), l);
	}
	
	private String getCommitedRevCacheName() {
		return getModelAddress() + "-commitedRev";
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	private long getCachedLastTakenRevision() {
		return getCachedLastCommitedRevision(); // TODO implement
	}
	
	/**
	 * Set a new value to be returned by {@link #getCachedLastTakenRevision()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	private void setCachedLastTakenRevision(long rev) {
		// TODO implement
	}
	
	/**
	 * Retrieve a cached value of the current revision number as defined by
	 * {@link #getCurrentRevisionNumber()}.
	 * 
	 * The returned value may be less that the actual "current" revision number,
	 * but is guaranteed to never be greater.
	 */
	private long getCachedCurrentRevision() {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		Long value = (Long)cache.get(getCurrentRevCacheName());
		return (value == null) ? -1L : value;
	}
	
	/**
	 * Set a new value to be returned by {@link #getCachedCurrentRevision()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	private void setCachedCurrentRevision(long l) {
		increaseCachedValue(getCurrentRevCacheName(), l);
	}
	
	/**
	 * @return the name of the cached value used by
	 *         {@link #getCachedCurrentRevision()} and
	 *         {@link #setCachedCurrentRevision(long)}
	 */
	private String getCurrentRevCacheName() {
		return getModelAddress() + "-currentRev";
	}
	
	/**
	 * Increase a cached {@link Long} value.
	 * 
	 * @param cachname The value to increase.
	 * @param l The new value to set. Ignored if it is less than the current
	 *            value.
	 */
	private void increaseCachedValue(String cachname, long l) {
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		Long value = l;
		while(true) {
			Long old = (Long)cache.put(cachname, value);
			if(old == null || old <= value) {
				break;
			}
			value = old;
		}
	}
	
	/**
	 * Get the model's current revision number.
	 * 
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[],
	 *      org.xydra.store.Callback)
	 */
	public long getCurrentRevisionNumber() {
		
		long currentRev = getCachedCurrentRevision();
		
		long rev = currentRev;
		
		List<AsyncEntity> batch = new ArrayList<AsyncEntity>(1);
		batch.add(GaeUtils.getEntityAsync(KeyStructure.createChangeKey(this.modelAddr, rev + 1)));
		
		int pos = 0;
		
		for(;; rev++) {
			
			Entity changeEntity = batch.get(pos).get();
			if(changeEntity == null) {
				break;
			}
			
			int status = GaeChange.getStatus(changeEntity);
			if(!Status.isCommitted(status)) {
				break;
			}
			
			// Only update the current revision if the command actually changed
			// something.
			if(status == Status.SuccessExecuted.value) {
				currentRev = rev + 1;
			}
			
			// Asynchronously fetch new change entities.
			Key nextKey = KeyStructure.createChangeKey(this.modelAddr, rev + batch.size() + 1);
			batch.set(pos, GaeUtils.getEntityAsync(nextKey));
			pos++;
			if(pos == batch.size()) {
				Key newKey = KeyStructure.createChangeKey(this.modelAddr, rev + batch.size() + 2);
				batch.add(GaeUtils.getEntityAsync(newKey));
				pos = 0;
			}
			
		}
		
		setCachedLastCommitedRevision(rev);
		setCachedCurrentRevision(currentRev);
		
		return currentRev;
	}
	
	/**
	 * Get the event at the specified revision number.
	 * 
	 * @see XydraStore#getEvents(XID, String, GetEventsRequest[],
	 *      org.xydra.store.Callback)
	 */
	public AsyncEvent getEventAt(long rev) {
		return new AsyncEvent(this.modelAddr, rev);
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
	public List<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		
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
		
		long currentRev = getCachedCurrentRevision();
		
		List<XEvent> events = new ArrayList<XEvent>();
		
		// Asynchronously fetch an initial batch of change entities
		int initialBuffer = 1;
		if(endRevision <= currentRev) {
			initialBuffer = (int)(endRevision - begin + 1);
		} else {
			// IMPROVE maybe use an initial buffer size of currentRev - begin +
			// 1?
		}
		List<AsyncEvent> batch = new ArrayList<AsyncEvent>(initialBuffer);
		for(int i = 0; i < initialBuffer; i++) {
			batch.add(getEventAt(begin + i));
		}
		
		int pos = 0;
		
		// Only update the currentRev cache value if we aren't skipping any
		// events.
		boolean trackCurrentRev = (begin <= currentRev);
		
		long rev = begin;
		for(; rev <= endRevision; rev++) {
			
			// Wait for the first change entities
			Entity changeEntity = batch.get(pos).getEntity();
			if(changeEntity == null) {
				// Found end of the change log
				break;
			}
			
			assert KeyStructure.assertRevisionInKey(changeEntity.getKey(), rev);
			
			int status = GaeChange.getStatus(changeEntity);
			if(!Status.isCommitted(status)) {
				// Found the lastCommitedRev
				break;
			}
			
			XEvent event = batch.get(pos).get();
			if(event != null) {
				// Something actually changed
				if(trackCurrentRev) {
					currentRev = rev;
				}
				events.add(event);
			}
			
			// Asynchronously fetch new change entities.
			if(rev + batch.size() <= endRevision) {
				batch.set(pos, getEventAt(rev + batch.size()));
			}
			pos++;
			if(pos == batch.size()) {
				if(rev + batch.size() + 1 <= endRevision) {
					batch.add(getEventAt(rev + batch.size() + 1));
				}
				pos = 0;
			}
			
		}
		
		if(currentRev == -1) {
			assert events.isEmpty();
			return null;
		}
		
		if(trackCurrentRev) {
			setCachedLastCommitedRevision(rev - 1);
			setCachedCurrentRevision(currentRev);
		}
		
		return events;
	}
	
}

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
import org.xydra.base.change.XAtomicCommand;
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
import org.xydra.core.model.impl.memory.AbstractChangeLog;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.XI;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
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
 * The XCHANGE entities are managed directly by this class stores the status of
 * the change ( {@link #PROP_STATUS}), the required locks ({@link #PROP_ACTOR},
 * the time the (last) process started working on the change (
 * {@link #PROP_LAST_ACTIVITY}).
 * 
 * If there are events associated with this change, {@link #PROP_EVENTCOUNT}
 * will specify how many. This property will not be set before the change has
 * reached {@link #STATUS_EXECUTING}
 * 
 * Possible status progression for XCHANGE Entities:
 * 
 * <pre>
 * 
 *  STATUS_CREATING ------> STATUS_FAILED_TIMEOUT
 *        |
 *        |----> STATUS_EXECUTING ----> STATUS_SUCCESS_EXECUTED
 *        |
 *        |----> STATUS_SUCCESS_NOCHANGE
 *        |
 *        \----> STATUS_FAILED_PRECONDITIONS
 * 
 * </pre>
 * 
 * </dd>
 * 
 * <dt>Entity type XEVENT</dt>
 * <dd>Stores a single {@link XAtomicEvent} associated with a XCHANGE entity.
 * Keys are encoded according to {@link KeyStructure#createEventKey(Key, int)}.
 * Currently events are simply dumped as a XML-Encoded {@link String} using
 * {@link XmlEvent#toXml(XEvent, org.xydra.core.xml.XmlOut, XAddress)}.</dd>
 * </dl>
 * * Locking: TODO
 * 
 * @author dscharrer
 * 
 */
public class GaeChangesService extends AbstractChangeLog {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesService.class);
	
	private static final long serialVersionUID = -2080744796962188941L;
	
	// GAE Entity (type=XCHANGE) property keys.
	
	/**
	 * GAE Property key for the timestamp (in milliseconds) when the last thread
	 * started working on this entity.
	 */
	private static final String PROP_LAST_ACTIVITY = "lastActivity";
	
	/**
	 * GAE Property key for the locks held by a change. The locks are stored as
	 * a {@link List} of the {@link String} representations of the locked
	 * {@link XAddress XAddresses}.
	 * 
	 * Locks are set when entering {@link #STATUS_CREATING}, removed when
	 * entering {@link #STATUS_SUCCESS_EXECUTED},
	 * {@link #STATUS_SUCCESS_NOCHANGE}, {@link #STATUS_FAILED_TIMEOUT} or
	 * {@link #STATUS_FAILED_PRECONDITIONS}.
	 */
	private static final String PROP_LOCKS = "locks";
	
	/**
	 * GAE Property key for the status of a change. See the STATUS_* constants
	 * for possible status values.
	 */
	private static final String PROP_STATUS = "status";
	
	/**
	 * GAE Property key for the actor that is responsible for the change. The
	 * actor's {@link XID} is stored as a {@link String}.
	 * 
	 * Set when entering {@link #STATUS_CREATING}, never removed.
	 */
	private static final String PROP_ACTOR = "actor";
	
	/**
	 * GAE Property key for the number of events that are associated with this
	 * change.
	 * 
	 * Set when entering {@link #STATUS_EXECUTING}, never removed.
	 */
	private static final String PROP_EVENTCOUNT = "eventCount";
	
	// GAE Entity (type=XEVENT) property keys.
	
	/**
	 * GAE Property key for the content of an individual event.
	 */
	private static final String PROP_EVENTCONTENT = "eventContent";
	
	// status IDs
	
	/**
	 * assigned revision
	 * 
	 * => waiting for locks / checking preconditions / writing events
	 */
	private static final int STATUS_CREATING = 0;
	
	/**
	 * got locks, preconditions checked, events written
	 * 
	 * => applying changes
	 * 
	 * a.k.a. readyToExecute
	 */
	private static final int STATUS_EXECUTING = 2;
	
	/** changes made, locks freed */
	private static final int STATUS_SUCCESS_EXECUTED = 3;
	
	/** there was nothing to change, locks freed */
	private static final int STATUS_SUCCESS_NOCHANGE = 4;
	
	/** could not execute command because of preconditions, locks freed */
	private static final int STATUS_FAILED_PRECONDITIONS = 100;
	
	/** timed out before saving events (status was STATUS_CREATING), locks freed */
	private static final int STATUS_FAILED_TIMEOUT = 101;
	
	// Parameters for waiting for other changes.
	
	/**
	 * timeout for changes in milliseconds
	 * 
	 * If this is set too low, longer commands may not be executed successfully.
	 * A too long timeout however might cause the model to "starve" as processes
	 * are be aborted by GAE while waiting for other changes.
	 * */
	private static final long TIMEOUT = 30000;
	
	/**
	 * critical time (in milliseconds) after which a process will voluntarily
	 * give up it's change to prevent another process from rolling it forward
	 * while the change is still active
	 * 
	 * To prevent unnecessary timeouts, this should be set close enough to the
	 * GAE timeout.
	 * 
	 * However, setting this too close to TIMEOUT might result in two processes
	 * executing the same change.
	 */
	private static final long TIME_CRITICAL = 27000;
	
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
	
	{
		assert TIME_CRITICAL < TIMEOUT;
	}
	
	// Implementation.
	
	private final XAddress modelAddr;
	
	public GaeChangesService(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	/**
	 * Internal helper class to track information of a change that is being
	 * executed.
	 * 
	 * Manages revision, startTime, the GAE entity of the change, and a Set of
	 * {@link XAddress} (the locks)
	 * 
	 * @author dscharrer
	 * 
	 */
	private static class ChangeInProgress {
		
		final long rev;
		final long startTime;
		final Set<XAddress> locks;
		
		final Entity entity;
		
		private ChangeInProgress(long rev, long startTime, Set<XAddress> locks, Entity entity) {
			this.rev = rev;
			this.startTime = startTime;
			this.locks = locks;
			this.entity = entity;
		}
		
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
		
		Set<XAddress> locks = calculateRequiredLocks(command);
		
		ChangeInProgress change = grabRevisionAndRegisterLocks(locks, actorId);
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		
		waitForLocks(change);
		
		List<XAtomicEvent> events = checkPreconditionsAndSaveEvents(change, command, actorId);
		if(events == null) {
			return XCommand.FAILED;
		} else if(events.isEmpty()) {
			// TODO maybe return revision?
			return XCommand.NOCHANGE;
		}
		
		executeAndUnlock(change, events);
		
		return change.rev;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	private long getCachedLastCommitedRevision() {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		String cachname = getCommitedRevCacheName();
		
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			Long entry = (Long)cache.get(cachname);
			if(entry == null) {
				return -1L;
			}
			return entry;
		}
	}
	
	private void setCachedLastCommitedRevision(long l) {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		String cachname = getCommitedRevCacheName();
		
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			Long entry = (Long)cache.get(cachname);
			if(entry == null || entry < l) {
				cache.put(cachname, l);
			}
		}
	}
	
	private String getCommitedRevCacheName() {
		return getBaseAddress() + "-commitedRev";
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	private long getCachedLastTakenRevision() {
		return getCachedLastCommitedRevision(); // TODO implement
	}
	
	private void setCachedLastTakenRevision(long rev) {
		// TODO implement
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
	private ChangeInProgress grabRevisionAndRegisterLocks(Set<XAddress> locks, XID actorId) {
		
		// Prepare locks to be saved in GAE entity.
		List<String> lockStrs = new ArrayList<String>(locks.size());
		for(XAddress a : locks) {
			lockStrs.add(a.toURI());
		}
		
		for(long rev = getCachedLastTakenRevision() + 1;; rev++) {
			
			// Try to grab this revision.
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity changeEntity = GaeUtils.getEntityExists(key, trans);
			
			if(changeEntity == null) {
				
				Entity newChange = new Entity(key);
				newChange.setUnindexedProperty(PROP_LOCKS, lockStrs);
				newChange.setUnindexedProperty(PROP_STATUS, STATUS_CREATING);
				long startTime = now();
				newChange.setUnindexedProperty(PROP_LAST_ACTIVITY, startTime);
				if(actorId != null) {
					newChange.setUnindexedProperty(PROP_ACTOR, actorId.toString());
				}
				
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
				return new ChangeInProgress(rev, startTime, locks, newChange);
				
			} else {
				
				// Revision already taken.
				
				GaeUtils.endTransaction(trans);
				
				// Since we read the entity anyway, might as well use that
				// information.
				int status = getStatus(changeEntity);
				if(!isCommitted(status) && !canRollForward(status) && isTimedOut(changeEntity)) {
					cleanupChangeEntity(changeEntity, STATUS_FAILED_TIMEOUT);
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
	private void waitForLocks(ChangeInProgress change) {
		
		long commitedRev = getCachedLastCommitedRevision();
		
		// Track if we find a greater last commitedRev.
		long newCommitedRev = -1;
		
		for(long otherRev = change.rev - 1; otherRev > commitedRev; otherRev--) {
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, otherRev);
			Entity otherChange = GaeUtils.getEntity(key);
			assert otherChange != null;
			
			// Check if the change is committed.
			int status = getStatus(otherChange);
			if(isCommitted(status)) {
				if(newCommitedRev < 0) {
					newCommitedRev = otherRev;
				}
				// finished, so should have no locks
				continue;
			}
			
			// Check if the change needs conflicting locks.
			Set<XAddress> otherLocks = getLocks(otherChange);
			assert otherLocks != null : "locks should not be removed before change is commited";
			if(!isConflicting(change.locks, otherLocks)) {
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
			while(!(timedOut = isTimedOut(otherChange))) {
				
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
				
				status = getStatus(otherChange);
				if(isCommitted(status)) {
					// now finished, so should have no locks anymore
					assert otherChange.getProperty(PROP_LOCKS) == null;
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
				if(canRollForward(status)) {
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
					cleanupChangeEntity(otherChange, STATUS_FAILED_TIMEOUT);
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
	private List<XAtomicEvent> checkPreconditionsAndSaveEvents(ChangeInProgress change,
	        XCommand command, XID actorId) {
		
		XReadableModel currentModel = InternalGaeModel.get(this, change.rev - 1, change.locks);
		
		Pair<ChangedModel,DeltaUtils.ModelChange> c = DeltaUtils.executeCommand(currentModel,
		        command);
		if(c == null) {
			giveUpIfTimeoutCritical(change.startTime);
			cleanupChangeEntity(change.entity, STATUS_FAILED_PRECONDITIONS);
			return null;
		}
		
		List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr, c, actorId, change.rev);
		
		assert events != null;
		
		try {
			
			if(events.isEmpty()) {
				giveUpIfTimeoutCritical(change.startTime);
				cleanupChangeEntity(change.entity, STATUS_SUCCESS_NOCHANGE);
				return events;
			}
			
			@SuppressWarnings("unchecked")
			Future<Key>[] futures = (Future<Key>[])new Future<?>[events.size()];
			
			Key baseKey = change.entity.getKey();
			for(int i = 0; i < events.size(); i++) {
				XAtomicEvent ae = events.get(i);
				assert (events.size() == 1) ^ ae.inTransaction();
				Entity eventEntity = new Entity(KeyStructure.createEventKey(baseKey, i));
				
				// IMPROVE save event in a GAE-specific format:
				// - don't save the "oldValue" again
				// - don't save the actor again, as it's already in the change
				// entity
				// - don't save the model rev, as it is already in the key
				XmlOutStringBuffer out = new XmlOutStringBuffer();
				XmlEvent.toXml(ae, out, this.modelAddr);
				Text text = new Text(out.getXml());
				eventEntity.setUnindexedProperty(PROP_EVENTCONTENT, text);
				
				/*
				 * Ignore if we are in danger of timing out (by not calling
				 * giveUpIfTimeoutCritical()), as the events won't be read by
				 * anyone until the change's status is set to STATUS_EXECUTING
				 * (or STATUS_SUCCESS_EXECUTED)
				 */
				futures[i] = GaeUtils.putEntityAsync(eventEntity);
				
			}
			
			for(Future<Key> future : futures) {
				GaeUtils.waitFor(future);
			}
			
			Integer eventCount = events.size();
			change.entity.setUnindexedProperty(PROP_EVENTCOUNT, eventCount);
			
			change.entity.setUnindexedProperty(PROP_STATUS, STATUS_EXECUTING);
			GaeUtils.putEntity(change.entity);
			
			giveUpIfTimeoutCritical(change.startTime);
			
		} catch(VoluntaryTimeoutException vte) {
			// Since we have not changed the status to EXEUTING, no thread will
			// be able to roll this change forward and we might as well clean it
			// up to prevent unnecessary waits.
			cleanupChangeEntity(change.entity, STATUS_FAILED_TIMEOUT);
		}
		
		return events;
	}
	
	/**
	 * Apply the changes described by the given locks and free any locks held by
	 * this change.
	 * 
	 * @param change
	 * @param events
	 */
	private void executeAndUnlock(ChangeInProgress change, List<XAtomicEvent> events) {
		
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
		
		List<Future<?>> futures = new ArrayList<Future<?>>(events.size());
		
		for(int i = 0; i < events.size(); i++) {
			XAtomicEvent event = events.get(i);
			
			assert this.modelAddr.equalsOrContains(event.getChangedEntity());
			assert event.getRevisionNumber() == change.rev;
			
			if(event instanceof XFieldEvent) {
				assert Arrays.asList(ChangeType.REMOVE, ChangeType.ADD, ChangeType.CHANGE)
				        .contains(event.getChangeType());
				
				if(((XFieldEvent)event).getNewValue() == null) {
					if(event.isImplied()) {
						assert event.getChangeType() == ChangeType.REMOVE;
						// removed by the XObjectEvent
						continue;
					}
					// Set the field as existing but empty.
					futures.add(InternalGaeField.set(event.getTarget(), change.rev, change.locks));
				} else {
					// Set the field as empty and containing the XValue stored
					// at the specified transaction index.
					futures.add(InternalGaeField
					        .set(event.getTarget(), change.rev, i, change.locks));
				}
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
		
		cleanupChangeEntity(change.entity, STATUS_SUCCESS_EXECUTED);
	}
	
	/**
	 * Check that the revision encoded in the given key matches the given
	 * revision.
	 * 
	 * @param key A key for a change entity as returned by
	 *            {@link KeyStructure#createChangeKey(XAddress, long)}.
	 * @param key The key to check
	 */
	private boolean assertRevisionInKey(Key key, long rev) {
		assert KeyStructure.isChangeKey(key);
		String keyStr = key.getName();
		int p = keyStr.lastIndexOf("/");
		assert p > 0;
		String revStr = keyStr.substring(p + 1);
		return (Long.parseLong(revStr) == rev);
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
	 * {@link #canRollForward(int)}.
	 * 
	 * @param rev The revision number of the change to roll forward.
	 * @param key The key of the corresponding change entity.
	 * 
	 * @return True if the change was rolled forward or false if the change was
	 *         grabbed by another process.
	 */
	private boolean rollForward(long rev, Key key) {
		
		assert assertRevisionInKey(key, rev);
		
		// Try to "grab" the change entity to prevent multiple processes from
		// rolling forward the same entity.
		Transaction trans = GaeUtils.beginTransaction();
		Entity changeEntity = GaeUtils.getEntity(key, trans);
		assert changeEntity != null;
		
		if(!isTimedOut(changeEntity)) {
			// Cannot roll forward, change was grabbed by another process.
			
			// Cleanup the transaction.
			GaeUtils.endTransaction(trans);
			
			return false;
		}
		
		long now = now();
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
		changeEntity.setProperty(PROP_LAST_ACTIVITY, now);
		GaeUtils.putEntityAsync(changeEntity, trans);
		// Synchronized by endTransaction()
		try {
			GaeUtils.endTransaction(trans);
		} catch(ConcurrentModificationException cme) {
			// Cannot roll forward, change was grabbed by another process.
			return false;
		}
		
		assert canRollForward(getStatus(changeEntity));
		assert getStatus(changeEntity) == STATUS_EXECUTING;
		
		Set<XAddress> locks = getLocks(changeEntity);
		
		ChangeInProgress change = new ChangeInProgress(rev, now, locks, changeEntity);
		
		List<XAtomicEvent> events = loadEvents(change);
		
		executeAndUnlock(change, events);
		
		return true;
	}
	
	/**
	 * Throw an exception if this change has been worked on for more than
	 * {@link #TIME_CRITICAL} milliseconds. This is done before writing to
	 * prevent another process rolling forward our change while we are still
	 * working on it.
	 * 
	 * @param startTime The time when we started working on the change.
	 * @throws VoluntaryTimeoutException to abort the current change
	 */
	private void giveUpIfTimeoutCritical(long startTime) throws VoluntaryTimeoutException {
		long now = now();
		if(now - startTime > TIME_CRITICAL) {
			// TODO use a better exception type?
			throw new VoluntaryTimeoutException("voluntarily timing out to prevent"
			        + " multiple processes working in the same thread; " + " start time was "
			        + startTime + "; now is " + now);
		}
	}
	
	/**
	 * @return the status code associated with the given change {@link Entity}.
	 */
	private int getStatus(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_STATUS);
		assert n != null : "All change entities should have a status";
		return n.intValue();
	}
	
	/**
	 * @return the locks associated with the given change {@link Entity}.
	 */
	@SuppressWarnings("unchecked")
	private Set<XAddress> getLocks(Entity changeEntity) {
		List<String> lockStrs = (List<String>)changeEntity.getProperty(PROP_LOCKS);
		if(lockStrs == null) {
			return null;
		}
		Set<XAddress> otherLocks = new HashSet<XAddress>((int)(lockStrs.size() / 0.75));
		for(String s : lockStrs) {
			otherLocks.add(XX.toAddress(s));
		}
		return otherLocks;
	}
	
	/**
	 * @return the actor associated with the given change {@link Entity}.
	 */
	private XID getActor(Entity changeEntity) {
		String actorStr = (String)changeEntity.getProperty(PROP_ACTOR);
		if(actorStr == null) {
			return null;
		}
		return XX.toId(actorStr);
	}
	
	/**
	 * Load the individual events associated with the given change.
	 * 
	 * @param change The change whose events should be loaded.
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	private List<XAtomicEvent> loadEvents(ChangeInProgress change) {
		
		assert assertRevisionInKey(change.entity.getKey(), change.rev);
		assert Arrays.asList(STATUS_EXECUTING, STATUS_SUCCESS_EXECUTED).contains(
		        change.entity.getProperty(PROP_STATUS));
		assert change.entity.getProperty(PROP_EVENTCOUNT) != null;
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		int eventCount = getEventCount(change.entity);
		
		for(int i = 0; i < eventCount; i++) {
			events.add(getAtomicEvent(change.rev, i));
		}
		
		return events;
	}
	
	private void cleanupChangeEntity(Entity changeEntity, int status) {
		changeEntity.removeProperty(PROP_LOCKS);
		changeEntity.setUnindexedProperty(PROP_STATUS, status);
		// IMPROVE remove saved events?
		GaeUtils.putEntity(changeEntity);
	}
	
	/**
	 * @return the current time in milliseconds.
	 */
	private static long now() {
		return System.currentTimeMillis();
	}
	
	/**
	 * @return true, if a change with the given status can be rolled forward,
	 *         false otherwise.
	 */
	private boolean canRollForward(int status) {
		return status == STATUS_EXECUTING;
	}
	
	/**
	 * @return true, if the status is either "success" or "failed".
	 */
	private boolean isCommitted(int status) {
		return (isSuccess(status) || isFailure(status));
	}
	
	private boolean isSuccess(int status) {
		return (status == STATUS_SUCCESS_EXECUTED || status == STATUS_SUCCESS_NOCHANGE);
	}
	
	private boolean isFailure(int status) {
		return (status == STATUS_FAILED_PRECONDITIONS || status == STATUS_FAILED_TIMEOUT);
	}
	
	/**
	 * @param changeEntity
	 * @return true if more than {@link #TIMEOUT} milliseconds elapsed since a
	 *         thread started working with the given changeEntity
	 */
	private boolean isTimedOut(Entity changeEntity) {
		long timer = (Long)changeEntity.getProperty(PROP_LAST_ACTIVITY);
		return now() - timer > TIMEOUT;
	}
	
	/**
	 * Handles wild-cards in locks.
	 * 
	 * @return true if the given set contains any locks that imply the given
	 *         lock (but are not the same).
	 */
	private static boolean hasMoreGeneralLock(Set<XAddress> locks, XAddress lock) {
		XAddress l = lock.getParent();
		while(l != null) {
			if(l.contains(l)) {
				return true;
			}
			l = l.getParent();
		}
		return false;
	}
	
	/**
	 * An address in the locks means that exclusive access is required to the
	 * entity referred to by that address, as well as all descendant entities.
	 * Also, a read lock on the ancestors of that entity is implied.
	 * 
	 * @param command The command to calculate the locks for.
	 * @return the calculated locks required to execute the given command.
	 */
	private static Set<XAddress> calculateRequiredLocks(XCommand command) {
		
		Set<XAddress> locks = new HashSet<XAddress>();
		if(command instanceof XTransaction) {
			
			XTransaction trans = (XTransaction)command;
			Set<XAddress> tempLocks = new HashSet<XAddress>();
			for(XAtomicCommand ac : trans) {
				XAddress lock = ac.getChangedEntity();
				// IMPROVE ADD events don't need to lock the whole added entity
				// (they don't care if children change)
				assert lock != null;
				tempLocks.add(lock);
			}
			for(XAddress lock : tempLocks) {
				if(!hasMoreGeneralLock(tempLocks, lock)) {
					locks.add(lock);
				}
			}
			
		} else {
			XAddress lock = command.getChangedEntity();
			assert lock != null;
			locks.add(lock);
		}
		
		return locks;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return if the two sets of locks conflict, i.e. one of them requires a
	 *         lock that is implied by the other set (wild-card locks are
	 *         respected).
	 */
	private static boolean isConflicting(Set<XAddress> a, Set<XAddress> b) {
		for(XAddress lock : a) {
			if(b.contains(lock) || hasMoreGeneralLock(b, lock)) {
				return true;
			}
		}
		for(XAddress lock : b) {
			if(hasMoreGeneralLock(a, lock)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param addr
	 * @param locks
	 * @return true if the specified locks are sufficient to read from the
	 *         entity at the given address.
	 */
	protected static boolean canRead(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(addr.equalsOrContains(lock) || lock.contains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param addr
	 * @param locks
	 * @return true if the specified locks are sufficient to write to the entity
	 *         at the given address.
	 */
	protected static boolean canWrite(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(lock.equalsOrContains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param revisionNumber The revision number of the change the event is part
	 *            of.
	 * @param transindex The index of the event in the change.
	 * @return the {@link XAtomicEvent} with the given index in the change with
	 *         the given revisionNumber.
	 */
	public XAtomicEvent getAtomicEvent(long revisionNumber, int transindex) {
		
		Key changeKey = KeyStructure.createChangeKey(this.modelAddr, revisionNumber);
		Key eventKey = KeyStructure.createEventKey(changeKey, transindex);
		
		// IMPROVE cache events
		Entity eventEntity = GaeUtils.getEntity(eventKey);
		if(eventEntity == null) {
			return null;
		}
		Text eventData = (Text)eventEntity.getProperty(PROP_EVENTCONTENT);
		
		MiniElement eventElement = new MiniXMLParserImpl().parseXml(eventData.getValue());
		
		XAtomicEvent ae = XmlEvent.toAtomicEvent(eventElement, this.modelAddr);
		
		assert ae.getRevisionNumber() == revisionNumber;
		
		return ae;
	}
	
	/**
	 * @return the {@link XAddress} of the model managed by this
	 *         {@link GaeChangesService} instance.
	 */
	public XAddress getBaseAddress() {
		return this.modelAddr;
	}
	
	private long getCachedCurrentRevision() {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		String cachname = getCurrentRevCacheName();
		
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			Long entry = (Long)cache.get(cachname);
			if(entry == null) {
				return -1L;
			}
			return entry;
		}
	}
	
	private void setCachedCurrentRevision(long l) {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		String cachname = getCurrentRevCacheName();
		
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			Long entry = (Long)cache.get(cachname);
			if(entry == null || entry < l) {
				cache.put(cachname, l);
			}
		}
	}
	
	private String getCurrentRevCacheName() {
		return getBaseAddress() + "-currentRev";
	}
	
	/**
	 * Get the model's current revision number.
	 * 
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[],
	 *      org.xydra.store.Callback)
	 */
	public long getCurrentRevisionNumber() {
		
		// IMPROVE cache the current revision
		long currentRev = getCachedCurrentRevision();
		
		long rev = currentRev;
		
		// Check if the revision is up to date.
		for(rev = currentRev;; rev++) {
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev + 1);
			Entity changeEntity = GaeUtils.getEntity(key);
			if(changeEntity == null) {
				break;
			}
			
			int status = getStatus(changeEntity);
			if(!isCommitted(status)) {
				break;
			}
			
			// Only update the current revision if the command actually changed
			// something.
			if(status == STATUS_SUCCESS_EXECUTED) {
				currentRev = rev + 1;
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
	public XEvent getEventAt(long rev) {
		
		Entity changeEntity = GaeUtils.getEntity(KeyStructure.createChangeKey(this.modelAddr, rev));
		
		if(changeEntity == null) {
			return null;
		}
		
		int status = getStatus(changeEntity);
		
		if(status != STATUS_EXECUTING && status != STATUS_SUCCESS_EXECUTED) {
			// no events available (yet) for this revision.
			return null;
		}
		
		int eventCount = getEventCount(changeEntity);
		assert eventCount > 0 : "executed changes should have at least one event";
		
		XID actor = getActor(changeEntity);
		
		if(eventCount > 1) {
			
			// IMPROVE cache transaction event
			return new GaeTransactionEvent(this, eventCount, actor, rev);
			
		} else {
			
			XAtomicEvent ae = getAtomicEvent(rev, 0);
			assert ae != null;
			assert XI.equals(actor, ae.getActor());
			assert this.modelAddr.equalsOrContains(ae.getChangedEntity());
			assert ae.getChangeType() != ChangeType.TRANSACTION;
			assert !ae.inTransaction();
			return ae;
			
		}
		
	}
	
	/**
	 * @return the number of {@link XAtomicEvent}s associated with the given
	 *         change {@link Entity}.
	 */
	private int getEventCount(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_EVENTCOUNT);
		if(n == null) {
			return 0;
		}
		return n.intValue();
	}
	
	public long getFirstRevisionNumber() {
		// We always start our revision numbers at 0.
		return 0;
	}
	
	/**
	 * @return true if there have been any changes to this model in the past.
	 */
	public boolean hasLog() {
		
		if(getCachedLastTakenRevision() >= 0) {
			return true;
		}
		
		Key key = KeyStructure.createChangeKey(this.modelAddr, 0L);
		return (GaeUtils.getEntity(key) != null);
	}
	
}

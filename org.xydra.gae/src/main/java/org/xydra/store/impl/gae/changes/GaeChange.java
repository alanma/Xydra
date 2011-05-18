package org.xydra.store.impl.gae.changes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.gae.AboutAppEngine;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


/**
 * Internal helper class to track information of a change that is being
 * executed.
 * 
 * This class manages GAE entities of kind XCHANGE
 * 
 * These represent a change to the model resulting from a single
 * {@link XCommand} (which may be a {@link XTransaction}). These entities
 * represent both an entry into the {@link XChangeLog} as well as a change that
 * is currently in progress.
 * 
 * Keys are encoded according to
 * {@link KeyStructure#createChangeKey(XAddress, long)}
 * 
 * Manages revision, startTime, the GAE entity of the change, and a Set of
 * {@link XAddress} (the locks)
 * 
 * @author dscharrer
 * 
 */
public class GaeChange {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChange.class);
	
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
	 * Locks are set when entering {@link STATUS_CREATING}, removed when
	 * entering {@link STATUS_SUCCESS_EXECUTED}, {@link STATUS_SUCCESS_NOCHANGE}
	 * , {@link STATUS_FAILED_TIMEOUT} or {@link STATUS_FAILED_PRECONDITIONS}.
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
	 * Set when entering {@link STATUS_CREATING}, never removed.
	 */
	private static final String PROP_ACTOR = "actor";
	
	/**
	 * The status of a change entity.
	 * 
	 * These are stored as integers in the {@link GaeChange#PROP_STATUS}
	 * property.
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
	 * @author dscharrer
	 * 
	 */
	enum Status {
		
		/**
		 * assigned revision
		 * 
		 * => waiting for locks / checking preconditions / writing events
		 */
		Creating(0),

		/**
		 * got locks, preconditions checked, events written
		 * 
		 * => applying changes
		 * 
		 * a.k.a. readyToExecute
		 */
		Executing(2),

		/** changes made, locks freed */
		SuccessExecuted(3),

		/** there was nothing to change, locks freed */
		SuccessNochange(4),

		/** could not execute command because of preconditions, locks freed */
		FailedPreconditions(100),

		/**
		 * timed out before saving events (status was STATUS_CREATING), locks
		 * freed
		 */
		FailedTimeout(101);
		
		private final int value;
		
		Status(int value) {
			this.value = value;
		}
		
		/**
		 * @return true if the given status indicates that the change has failed
		 *         to execute.
		 */
		protected boolean isFailure() {
			return (this == FailedPreconditions || this == FailedTimeout);
		}
		
		/**
		 * @return true if the given status indicates that the change has been
		 *         successfully executed.
		 */
		protected boolean isSuccess() {
			return (this == SuccessExecuted || this == SuccessNochange);
		}
		
		/**
		 * @return true, if the status is either "success" or "failed".
		 */
		protected boolean isCommitted() {
			return (isSuccess() || isFailure());
		}
		
		/**
		 * @return true, if a change with the given status can be rolled
		 *         forward, false otherwise.
		 */
		protected boolean canRollForward() {
			return (this == Executing);
		}
		
		/**
		 * @return true if the given status indicates that events are stored in
		 *         the change entity.
		 */
		protected boolean hasEvents() {
			return (this == Executing || this == SuccessExecuted);
		}
		
		public static Status get(int value) {
			Status status = null;
			switch(value) {
			case 0:
				status = Creating;
				break;
			case 2:
				status = Executing;
				break;
			case 3:
				status = SuccessExecuted;
				break;
			case 4:
				status = SuccessNochange;
				break;
			case 100:
				status = FailedPreconditions;
				break;
			case 101:
				status = FailedTimeout;
				break;
			}
			assert status != null && status.value == value;
			return status;
		}
		
	}
	
	// timeouts
	
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
	 * timeout for changes in milliseconds
	 * 
	 * If this is set too low, longer commands may not be executed successfully.
	 * A too long timeout however might cause the model to "starve" as processes
	 * are be aborted by GAE while waiting for other changes.
	 * */
	private static final long TIMEOUT = 30000;
	
	{
		assert TIME_CRITICAL < TIMEOUT;
	}
	
	// non-static members
	
	protected final long rev;
	private long lastActivity;
	private GaeLocks locks;
	private final XAddress modelAddr;
	private Entity entity;
	private Status status;
	private XID actor;
	private Pair<List<XAtomicEvent>,int[]> events;
	private XEvent event;
	
	/**
	 * Construct a new change entity with the given properties. The entity is
	 * created but not put into the datastore.
	 */
	public GaeChange(XAddress modelAddr, long rev, GaeLocks locks, XID actorId) {
		
		this.rev = rev;
		this.locks = locks;
		this.modelAddr = modelAddr;
		this.entity = new Entity(KeyStructure.createChangeKey(modelAddr, rev));
		
		this.status = Status.Creating;
		this.entity.setUnindexedProperty(PROP_STATUS, this.status.value);
		
		this.actor = actorId;
		if(actorId != null) {
			this.entity.setUnindexedProperty(PROP_ACTOR, actorId.toString());
		}
		this.entity.setUnindexedProperty(PROP_LOCKS, locks.encode());
		
		registerActivity();
		
		// Synchronized by endTransaction()
	}
	
	private void clearCache() {
		this.locks = null;
		this.lastActivity = -1;
		this.actor = null;
		this.status = null;
		this.events = null;
		this.event = null;
	}
	
	/**
	 * Take over the given change entity. The entity is updated but the updated
	 * version is not put back into the datastore.
	 */
	public GaeChange(XAddress modelAddr, long rev, Entity entity) {
		assert entity != null;
		this.entity = entity;
		this.rev = rev;
		this.modelAddr = modelAddr;
		assert KeyStructure.assertRevisionInKey(entity.getKey(), rev);
		clearCache();
	}
	
	public void reload(Transaction trans) {
		assert !getStatus().isCommitted();
		this.entity = GaeUtils.getEntity(this.entity.getKey(), trans);
		assert this.entity != null : "change entities should not vanish";
		clearCache();
	}
	
	public void reload() {
		reload(null);
	}
	
	/**
	 * @return the actor associated with the given change {@link Entity}.
	 */
	protected XID getActor() {
		if(this.actor == null) {
			synchronized(this) {
				String actorStr = (String)this.entity.getProperty(PROP_ACTOR);
				if(actorStr == null) {
					return null;
				}
				this.actor = XX.toId(actorStr);
			}
		}
		return this.actor;
	}
	
	/**
	 * @return true if more than {@link #TIMEOUT} milliseconds elapsed since a
	 *         thread started working with the given change entity
	 */
	protected boolean isTimedOut() {
		assert !getStatus().isCommitted();
		if(this.lastActivity < 0) {
			this.lastActivity = (Long)this.entity.getProperty(PROP_LAST_ACTIVITY);
		}
		return now() - this.lastActivity > TIMEOUT;
	}
	
	/**
	 * Remove locks owned by the given change entity.
	 * 
	 * @param status The new status of the entity.
	 */
	protected void commit(Status status) {
		assert !getStatus().isCommitted();
		this.locks = null;
		this.entity.removeProperty(PROP_LOCKS);
		setStatus(status);
		GaeUtils.putEntity(this.entity);
	}
	
	/**
	 * Update the status of this change.
	 */
	protected void setStatus(Status status) {
		assert !getStatus().isCommitted();
		this.status = status;
		this.entity.setUnindexedProperty(PROP_STATUS, status.value);
	}
	
	/**
	 * @return the locks associated with this change.
	 */
	@SuppressWarnings("unchecked")
	synchronized public GaeLocks getLocks() {
		
		assert !getStatus().isCommitted();
		if(this.locks == null) {
			List<String> lockStrs = (List<String>)this.entity.getProperty(PROP_LOCKS);
			if(lockStrs == null) {
				return null;
			}
			this.locks = new GaeLocks(lockStrs);
		}
		
		return this.locks;
	}
	
	/**
	 * @return the status code associated with the given change {@link Entity}.
	 */
	synchronized public Status getStatus() {
		
		if(this.status == null) {
			Number n = (Number)this.entity.getProperty(PROP_STATUS);
			if(n == null) {
				try {
					throw new RuntimeException("Tracing caller of getStatus()");
				} catch(RuntimeException e) {
					log.error("change entity without status", e);
				}
			}
			assert n != null : "All change entities should have a status";
			int index = n.intValue();
			this.status = Status.get(index);
		}
		
		return this.status;
	}
	
	/**
	 * @return true if the given change entity has any locks set.
	 */
	protected boolean hasLocks() {
		return (this.locks != null || this.entity.getProperty(PROP_LOCKS) != null);
	}
	
	protected void registerActivity() {
		assert !getStatus().isCommitted();
		this.lastActivity = now();
		this.entity.setUnindexedProperty(PROP_LAST_ACTIVITY, this.lastActivity);
	}
	
	/**
	 * @return the current time in milliseconds.
	 */
	protected static long now() {
		return System.currentTimeMillis();
	}
	
	/**
	 * Throw an exception if this change has been worked on for more than
	 * {@link #TIME_CRITICAL} milliseconds. This is done before writing to
	 * prevent another process rolling forward our change while we are still
	 * working on it.
	 * 
	 * @param lastActivity The time when we started working on the change.
	 * @throws VoluntaryTimeoutException to abort the current change
	 */
	protected void giveUpIfTimeoutCritical() throws VoluntaryTimeoutException {
		/* Don't give up in development mode to let the debugger step through */
		if(!AboutAppEngine.inProduction()) {
			return;
		}
		assert !getStatus().isCommitted();
		long now = now();
		if(now - this.lastActivity > TIME_CRITICAL) {
			// TODO use a better exception type?
			throw new VoluntaryTimeoutException("voluntarily timing out to prevent"
			        + " multiple processes working in the same thread; " + " start time was "
			        + this.lastActivity + "; now is " + now);
		}
	}
	
	protected Pair<int[],List<Future<Key>>> setEvents(List<XAtomicEvent> events) {
		assert !getStatus().isCommitted();
		Pair<int[],List<Future<Key>>> res = GaeEvents.saveEvents(this.modelAddr, this.entity,
		        events);
		this.events = new Pair<List<XAtomicEvent>,int[]>(events, res.getFirst());
		return res;
	}
	
	/**
	 * Asynchronously put this change entity into datastore within the given
	 * transaction.
	 */
	protected void save(Transaction trans) {
		assert !getStatus().isCommitted();
		// Synchronized by endTransaction()
		GaeUtils.putEntityAsync(this.entity, trans);
	}
	
	/**
	 * Put this change entity in the datastore.
	 */
	protected void save() {
		assert !getStatus().isCommitted();
		GaeUtils.putEntity(this.entity);
	}
	
	/**
	 * Load the individual events associated with this change.
	 * 
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	synchronized protected Pair<List<XAtomicEvent>,int[]> getAtomicEvents() {
		
		assert getStatus().hasEvents();
		
		if(this.events == null) {
			Pair<XAtomicEvent[],int[]> res = GaeEvents.loadAtomicEvents(this.modelAddr, this.rev,
			        getActor(), this.entity);
			
			this.events = new Pair<List<XAtomicEvent>,int[]>(Arrays.asList(res.getFirst()), res
			        .getSecond());
		}
		return this.events;
	}
	
	public boolean isConflicting(GaeChange otherChange) {
		GaeLocks ourLocks = getLocks();
		GaeLocks otherLocks = otherChange.getLocks();
		assert ourLocks != null : "our locks should not be removed before change is commited";
		assert otherLocks != null : "locks should not be removed before change is commited";
		return ourLocks.isConflicting(otherLocks);
	}
	
	/**
	 * This method should only be called if the change entity actually contains
	 * events.
	 * 
	 * @return the XEvent represented by this change.
	 */
	synchronized public XEvent getEvent() {
		
		if(this.event == null) {
			if(!getStatus().hasEvents()) {
				// no events available (or not yet) for this revision.
				return null;
			}
			
			List<XAtomicEvent> events = getAtomicEvents().getFirst();
			assert events.size() > 0;
			
			if(events.size() == 1) {
				this.event = events.get(0);
			} else {
				this.event = MemoryTransactionEvent.createTransactionEvent(getActor(),
				        this.modelAddr, events, this.rev - 1, XEvent.RevisionOfEntityNotSet);
			}
			
			// Not needed anymore.
			this.events = null;
		}
		
		return this.event;
	}
	
}

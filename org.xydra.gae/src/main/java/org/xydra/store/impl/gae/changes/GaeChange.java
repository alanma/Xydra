package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.index.query.Pair;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


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
class GaeChange {
	
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
		
		protected final int value;
		
		Status(int value) {
			this.value = value;
		}
		
		protected static boolean isFailure(int status) {
			return (status == FailedPreconditions.value || status == FailedTimeout.value);
		}
		
		protected static boolean isSuccess(int status) {
			return (status == SuccessExecuted.value || status == SuccessNochange.value);
		}
		
		/**
		 * @return true, if the status is either "success" or "failed".
		 */
		protected static boolean isCommitted(int status) {
			return (isSuccess(status) || isFailure(status));
		}
		
		/**
		 * @return true, if a change with the given status can be rolled
		 *         forward, false otherwise.
		 */
		protected static boolean canRollForward(int status) {
			return status == Executing.value;
		}
		
		protected static boolean hasEvents(int status) {
			return (status == Executing.value || status == SuccessExecuted.value);
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
	private final long startTime;
	protected final Set<XAddress> locks;
	private final XAddress modelAddr;
	private final Entity entity;
	
	public GaeChange(XAddress modelAddr, long rev, Set<XAddress> locks, XID actorId) {
		
		this.rev = rev;
		this.locks = locks;
		this.modelAddr = modelAddr;
		this.entity = new Entity(KeyStructure.createChangeKey(modelAddr, rev));
		
		if(actorId != null) {
			this.entity.setUnindexedProperty(PROP_ACTOR, actorId.toString());
		}
		setLocks(this.entity, locks);
		
		this.startTime = registerActivity(this.entity);
		
		// Synchronized by endTransaction()
		
		// TODO Auto-generated constructor stub
	}
	
	public GaeChange(XAddress modelAddr, long rev, Entity entity) {
		this.entity = entity;
		this.rev = rev;
		this.modelAddr = modelAddr;
		assert KeyStructure.assertRevisionInKey(entity.getKey(), rev);
		this.locks = getLocks(this.entity);
		this.startTime = registerActivity(this.entity);
	}
	
	/**
	 * @return the actor associated with the given change {@link Entity}.
	 */
	protected static XID getActor(Entity changeEntity) {
		String actorStr = (String)changeEntity.getProperty(PROP_ACTOR);
		if(actorStr == null) {
			return null;
		}
		return XX.toId(actorStr);
	}
	
	/**
	 * @param changeEntity
	 * @return true if more than {@link #TIMEOUT} milliseconds elapsed since a
	 *         thread started working with the given changeEntity
	 */
	protected static boolean isTimedOut(Entity changeEntity) {
		long timer = (Long)changeEntity.getProperty(PROP_LAST_ACTIVITY);
		return now() - timer > TIMEOUT;
	}
	
	/**
	 * Remove locks owned by the given change entity.
	 * 
	 * @param status The new status of the entity.
	 */
	protected static void cleanup(Entity changeEntity, Status status) {
		changeEntity.removeProperty(PROP_LOCKS);
		setStatus(changeEntity, status);
		GaeUtils.putEntity(changeEntity);
	}
	
	protected static void setStatus(Entity changeEntity, Status status) {
		changeEntity.setUnindexedProperty(PROP_STATUS, status.value);
	}
	
	protected void setStatus(Status status) {
		setStatus(this.entity, status);
	}
	
	/**
	 * @return the locks associated with the given change {@link Entity}.
	 */
	@SuppressWarnings("unchecked")
	protected static Set<XAddress> getLocks(Entity changeEntity) {
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
	 * @return the status code associated with the given change {@link Entity}.
	 */
	protected static int getStatus(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_STATUS);
		assert n != null : "All change entities should have a status";
		return n.intValue();
	}
	
	protected static boolean hasLocks(Entity otherChange) {
		return otherChange.getProperty(PROP_LOCKS) != null;
	}
	
	private static void setLocks(Entity changeEntity, Set<XAddress> locks) {
		
		List<String> lockStrs = new ArrayList<String>(locks.size());
		for(XAddress a : locks) {
			lockStrs.add(a.toURI());
		}
		
		changeEntity.setUnindexedProperty(PROP_LOCKS, lockStrs);
	}
	
	private static long registerActivity(Entity newChange) {
		long startTime = now();
		newChange.setUnindexedProperty(PROP_LAST_ACTIVITY, startTime);
		return startTime;
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
	 * @param startTime The time when we started working on the change.
	 * @throws VoluntaryTimeoutException to abort the current change
	 */
	protected void giveUpIfTimeoutCritical() throws VoluntaryTimeoutException {
		long now = now();
		if(now - this.startTime > TIME_CRITICAL) {
			// TODO use a better exception type?
			throw new VoluntaryTimeoutException("voluntarily timing out to prevent"
			        + " multiple processes working in the same thread; " + " start time was "
			        + this.startTime + "; now is " + now);
		}
	}
	
	protected Pair<int[],List<Future<Key>>> setEvents(List<XAtomicEvent> events) {
		return GaeEventService.saveEvents(this.modelAddr, this.entity, events);
	}
	
	protected void save(Transaction trans) {
		// Synchronized by endTransaction()
		GaeUtils.putEntityAsync(this.entity, trans);
	}
	
	protected void save() {
		GaeUtils.putEntity(this.entity);
	}
	
	/**
	 * Load the individual events associated with this change.
	 * 
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	public Pair<List<XAtomicEvent>,int[]> loadEvents() {
		
		assert Status.hasEvents(GaeChange.getStatus(this.entity));
		
		Pair<XAtomicEvent[],int[]> res = GaeEventService.loadAtomicEvents(this.modelAddr, this.rev,
		        null, this.entity, false);
		
		return new Pair<List<XAtomicEvent>,int[]>(Arrays.asList(res.getFirst()), res.getSecond());
	}
	
	protected void cleanup(Status status) {
		cleanup(this.entity, status);
	}
	
}

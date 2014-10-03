package org.xydra.store.impl.gae.changes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.XGae;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.STransaction;

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

	/* GAE Entity (type=XCHANGE) property keys. */

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
	 * actor's {@link XId} is stored as a {@link String}.
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
	 *  Creating 
	 *     |
	 *     |----> SucessExecuted --->  SucessExecutedApplied
	 *     |
	 *     |----> SuccessNochange
	 *     |
	 *     |----> FailedTimeout (might also be set by another process)
	 *     |
	 *     \----> FailedPreconditions
	 * 
	 * </pre>
	 * 
	 * @author dscharrer
	 * 
	 */
	static public enum Status {

		/**
		 * assigned revision
		 * 
		 * => waiting for locks / checking preconditions / writing events
		 * 
		 * Other thread should check age of the status and either wait for
		 * anything OR if too old: Mark as FailedTimeout
		 */
		Creating(0),

		/** changes made, locks freed. Current revision is now bigger. */
		SuccessExecuted(3),

		/**
		 * Temporary object states updated
		 * 
		 * @since 2012-05
		 */
		SuccessExecutedApplied(5),

		/** there was nothing to change, locks freed */
		SuccessNochange(4),

		/** could not execute command because of preconditions, locks freed */
		FailedPreconditions(100),

		/**
		 * timed out before saving events (status was STATUS_CREATING), locks
		 * freed.
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
		public boolean isFailure() {
			return (this == FailedPreconditions || this == FailedTimeout);
		}

		/**
		 * @return true if the given status indicates that the change has been
		 *         successfully executed - even if that changed nothing.
		 */
		public boolean isSuccess() {
			return (this == SuccessExecuted || this == SuccessNochange || this == SuccessExecutedApplied);
		}

		/**
		 * @return true, if the status is either "success" or "failed". A
		 *         terminal state.
		 */
		public boolean isCommitted() {
			return (isSuccess() || isFailure());
		}

		/**
		 * @return true if the given status indicates that events are stored in
		 *         the change entity.
		 * 
		 *         Events are saved after preconditions have been checked.
		 * 
		 *         Thus, events are guaranteed to exist in
		 *         {@link #SuccessExecuted} stages.
		 */
		public boolean hasEvents() {
			return this == SuccessExecuted || this == SuccessExecutedApplied;
		}

		public static Status get(int value) {
			Status status = null;
			switch (value) {
			case 0:
				status = Creating;
				break;
			case 3:
				status = SuccessExecuted;
				break;
			case 4:
				status = SuccessNochange;
				break;
			case 5:
				status = SuccessExecutedApplied;
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

		public boolean canChange() {
			return this == Creating || this == SuccessExecuted;
		}

		public boolean changedSomething() {
			return this == SuccessExecuted || this == SuccessExecutedApplied;
		}

	}

	// timeouts

	// take up to 20 seconds for "other stuff" into account
	public static final long APPLICATION_RESERVED_TIME = 20 * 1000;

	/**
	 * timeout for changes in milliseconds
	 * 
	 * If this is set too low, longer commands may not be executed successfully.
	 * A too long timeout however might cause the model to "starve" as processes
	 * are be aborted by GAE while waiting for other changes.
	 * */
	private static final long TIMEOUT = XGae.get().getRuntimeLimitInMillis()
			- APPLICATION_RESERVED_TIME;

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
	private static final long TIME_CRITICAL = TIMEOUT - (3 * 1000);

	{
		assert TIME_CRITICAL < TIMEOUT;
	}

	// non-static members

	public final long rev;
	private long lastActivity;
	private GaeLocks locks;
	private final XAddress modelAddr;
	private SEntity entity;
	private Status status;
	private XId actor;
	private Pair<List<XAtomicEvent>, int[]> events;
	private transient XEvent event;

	/**
	 * Construct a new change entity with the given properties. The entity is
	 * created but not put into the datastore.
	 * 
	 * @param modelAddr
	 * @param rev
	 * @param locks
	 * @param actorId
	 */
	public GaeChange(XAddress modelAddr, long rev, GaeLocks locks, XId actorId) {

		this.rev = rev;
		this.locks = locks;
		this.modelAddr = modelAddr;
		this.entity = XGae.get().datastore()
				.createEntity(KeyStructure.createChangeKey(modelAddr, rev));

		this.status = Status.Creating;
		this.entity.setAttribute(PROP_STATUS, this.status.value);

		this.actor = actorId;
		if (actorId != null) {
			this.entity.setAttribute(PROP_ACTOR, actorId.toString());
		}
		this.entity.setAttribute(PROP_LOCKS, locks.encode());

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
	 * 
	 * @param modelAddr
	 * @param rev
	 * @param entity
	 */
	public GaeChange(XAddress modelAddr, long rev, SEntity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("entity is null");
		}
		this.entity = entity;
		this.rev = rev;
		this.modelAddr = modelAddr;
		assert entity == Memcache.NULL_ENTITY
				|| KeyStructure.assertRevisionInKey(entity.getKey(), rev);
		clearCache();
	}

	public void reload(STransaction trans) {
		XyAssert.xyAssert(getStatus().canChange());
		this.entity = XGae.get().datastore().sync().getEntity(this.entity.getKey(), trans);
		assert this.entity != null : "change entities should not vanish";
		clearCache();
	}

	public void reload() {
		reload(null);
	}

	/**
	 * @return the actor associated with the given change {@link Entity}.
	 */
	private XId getActor() {
		if (this.actor == null) {
			synchronized (this) {
				String actorStr = (String) this.entity.getAttribute(PROP_ACTOR);
				if (actorStr == null) {
					return null;
				}
				this.actor = XX.toId(actorStr);
			}
		}
		return this.actor;
	}

	/**
	 * @return true status is Creating and if more than {@link #TIMEOUT}
	 *         milliseconds elapsed since a thread started working with the
	 *         given change entity
	 */
	public boolean isTimedOut() {
		if (!getStatus().canChange())
			return false;
		if (this.lastActivity < 0) {
			this.lastActivity = (Long) this.entity.getAttribute(PROP_LAST_ACTIVITY);
		}
		return now() - this.lastActivity > TIMEOUT;
	}

	/**
	 * Remove locks owned by the given change entity.
	 * 
	 * @param status
	 *            The new status of the entity.
	 */
	public void commitAndClearLocks(Status status) {
		assert getStatus().canChange();
		this.locks = null;
		this.entity.removeAttribute(PROP_LOCKS);
		setStatus(status);
		XGae.get().datastore().sync().putEntity(this.entity);
	}

	/**
	 * Update the status of this change.
	 * 
	 * @param status
	 */
	public void setStatus(Status status) {
		XyAssert.xyAssert(getStatus().canChange(), "A commited change cannot change its status");
		this.status = status;
		this.entity.setAttribute(PROP_STATUS, status.value);
	}

	/**
	 * @return the locks associated with this change.
	 */
	synchronized public GaeLocks getLocks() {
		XyAssert.xyAssert(getStatus().canChange());
		if (this.locks == null) {
			@SuppressWarnings("unchecked")
			List<String> lockStrs = (List<String>) this.entity.getAttribute(PROP_LOCKS);
			if (lockStrs == null) {
				return null;
			}
			this.locks = new GaeLocks(lockStrs);
		}

		return this.locks;
	}

	/**
	 * @return the status code associated with the given change {@link SEntity}.
	 */
	synchronized public Status getStatus() {
		if (this.status == null) {
			Object o = this.entity.getAttribute(PROP_STATUS);
			// trying to find the NPE bug here...
			if (o == null) {
				try {
					throw new RuntimeException("Tracing caller of getStatus()");
				} catch (RuntimeException e) {
					log.error("Accessing a change entity without status", e);
				}
			}
			Number n = (Number) o;
			assert n != null : "All change entities should have a status";
			int index = n.intValue();
			this.status = Status.get(index);
		}
		return this.status;
	}

	/**
	 * @return true if the given change entity has any locks set.
	 */
	public boolean hasLocks() {
		return (this.locks != null || this.entity.getAttribute(PROP_LOCKS) != null);
	}

	private void registerActivity() {
		XyAssert.xyAssert(getStatus().canChange());
		this.lastActivity = now();
		this.entity.setAttribute(PROP_LAST_ACTIVITY, this.lastActivity);
	}

	/**
	 * @return the current time in milliseconds.
	 */
	private static long now() {
		return System.currentTimeMillis();
	}

	private transient long timeoutCheckCount = 0;

	/**
	 * Throw an exception if this change has been worked on for more than
	 * {@link #TIME_CRITICAL} milliseconds. This is done before writing to
	 * prevent another process rolling forward our change while we are still
	 * working on it.
	 * 
	 * @throws VoluntaryTimeoutException
	 *             to abort the current change
	 */
	public void giveUpIfTimeoutCritical() throws VoluntaryTimeoutException {

		if (XGae.get().getRuntimeLimitInMillis() == -1) {
			// never time out on a backend
			return;
		}

		/* Don't give up in development mode to let the debugger step through */
		if (!XGae.get().inProduction()) {
			this.timeoutCheckCount++;
			if (this.timeoutCheckCount > 10000) {
				throw new RuntimeException("Waiting to long");
			}
			return;
		}
		XyAssert.xyAssert(getStatus().canChange());
		long now = now();
		// IMPROVE Use new API since AppEngine 1.6.5 to get time left
		if (now - this.lastActivity > TIME_CRITICAL) {
			// IMPROVE use a better exception type?
			throw new VoluntaryTimeoutException("voluntarily timing out to prevent"
					+ " multiple processes working in the same thread; " + " start time was "
					+ this.lastActivity + "; now is " + now);
		}
	}

	public Pair<int[], List<Future<SKey>>> setEvents(List<XAtomicEvent> events) {
		XyAssert.xyAssert(getStatus().canChange());
		XyAssert.xyAssert(events.size() >= 1);
		Pair<int[], List<Future<SKey>>> res = GaeEvents.saveEvents(this.modelAddr, this.entity,
				events);
		this.events = new Pair<List<XAtomicEvent>, int[]>(events, res.getFirst());
		return res;
	}

	/**
	 * Asynchronously put this change entity into datastore within the given
	 * transaction.
	 * 
	 * @param trans
	 */
	@XGaeOperation(datastoreWrite = true, memcacheWrite = true)
	public void save(STransaction trans) {
		XyAssert.xyAssert(getStatus().canChange());

		registerActivity();

		// Synchronized by endTransaction()
		XGae.get().datastore().async().putEntity(this.entity, trans);
	}

	/**
	 * Put this change entity in the datastore.
	 * 
	 * @return a future that returns the putted key on success
	 */
	public Future<SKey> save() {
		XyAssert.xyAssert(getStatus().canChange());

		registerActivity();

		XyAssert.xyAssert(getStatus().canChange(), "getStatus().canChange()");
		XyAssert.xyAssert(this.entity.getAttribute("eventTypes") != null,
				"Trying to save changeEntity with PROP_EVENT_TYPES==null");
		return XGae.get().datastore().async().putEntity(this.entity);
	}

	/**
	 * Load the individual events associated with this change.
	 * 
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	synchronized public Pair<List<XAtomicEvent>, int[]> getAtomicEvents() {

		XyAssert.xyAssert(getStatus().hasEvents());

		if (this.events == null) {
			Pair<XAtomicEvent[], int[]> res = GaeEvents.loadAtomicEvents(this.modelAddr, this.rev,
					getActor(), this.entity);

			this.events = new Pair<List<XAtomicEvent>, int[]>(Arrays.asList(res.getFirst()),
					res.getSecond());
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

		if (this.event == null) {
			if (!getStatus().hasEvents()) {
				// no events available (or not yet) for this revision.
				return null;
			}

			List<XAtomicEvent> events = getAtomicEvents().getFirst();
			XyAssert.xyAssert(events.size() > 0);

			if (events.size() == 1) {
				this.event = events.get(0);
			} else {
				// FIXME could also be an object Txn
				this.event = MemoryTransactionEvent.createTransactionEvent(getActor(),
						this.modelAddr, events, this.rev - 1, XEvent.REVISION_OF_ENTITY_NOT_SET);
			}

			// Not needed anymore.
			this.events = null;
		}

		return this.event;
	}

	@Override
	public String toString() {
		return "rev:" + this.rev + " lastAct:" + this.lastActivity + " status:" + this.status + " "
				+ DebugFormatter.format(this.entity);
	}

	public XId getActorId() {
		return this.actor;
	}

}

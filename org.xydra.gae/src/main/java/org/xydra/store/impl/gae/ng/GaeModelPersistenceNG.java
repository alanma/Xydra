package org.xydra.store.impl.gae.ng;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.xydra.annotations.Setting;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.common.NanoClock;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.Iterators;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.IGaeModelPersistence;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.VoluntaryTimeoutException;
import org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.util.FutureUtils;

/**
 * Xydra allows transaction only within one model. The GAE implementation
 * maintains one change log per model. This class keeps all access to a model
 * within the datastore and memcache in one place.
 *
 * If the datastore or memcache is called to read or write a certain model, that
 * access is triggered only from here.
 *
 *
 * Design decisions:
 *
 * Chosen: 1A) Within 60 seconds (or another short timeout) the caller gets a
 * notice if her command got executed successfully, failed or got a timeout.
 * This gives always a quick response.
 *
 * Alternative: 1B) Commands get enqueued, caller gets an event number and can
 * later check what has happened with the command. The command might wait for
 * some locks and take minutes for execution. This produces less dead change
 * events in the log.
 *
 * @author xamde
 */
public class GaeModelPersistenceNG implements IGaeModelPersistence {

	static final Logger log = LoggerFactory.getLogger(GaeModelPersistenceNG.class);

	/* How many changes to fetch in one batch fetch at most */
	@Setting(value = "")
	private static final long MAX_CHANGES_FETCH_SIZE = RevisionManager.WRITE_REV_EVERY + 3;

	/**
	 * Initial time to wait before re-checking the status of an event who'se
	 * locks we need.
	 */
	@Setting(value = "")
	private static final long WAIT_INITIAL = 10;

	/**
	 * Maximum time to wait before re-checking the status of an event who's
	 * locks we need.
	 */
	@Setting(value = "")
	private static final long WAIT_MAX = 1000;

	private final ChangeLogManager changelogManager;

	private final XAddress modelAddress;

	private final RevisionManager revisionManager;

	private final IGaeSnapshotService snapshotService;

	private final ContextBeforeCommand executionContext;

	public GaeModelPersistenceNG(final XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.revisionManager = new RevisionManager(this.modelAddress);
		final GaeModelRevInfo info = this.revisionManager.getInfo();

		this.changelogManager = new ChangeLogManager(this.modelAddress);
		this.snapshotService = new GaeSnapshotServiceImpl5(this.changelogManager);
		this.executionContext = new ContextBeforeCommand(modelAddress, info, this.snapshotService);
		XyAssert.xyAssert(this.executionContext.getAddress() != null);
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof GaeModelPersistenceNG
				&& ((GaeModelPersistenceNG) other).modelAddress.equals(this.modelAddress);

	}

	/**
	 * Write the events that describe the transformation of the model into the
	 * new state.
	 *
	 * Assumes that we have all the required locks.
	 *
	 * @param change
	 *            The change that the command belongs to.
	 * @param partialSnapshot
	 *            on which the command is executed, can be null
	 */
	private void execute_saveEventsReleaseLocks(final ExecutionResult executionResult, final GaeChange change)
			throws VoluntaryTimeoutException {
		if (executionResult.getStatus().isFailure()) {
			change.giveUpIfTimeoutCritical();
			this.changelogManager.commitAndClearLocks(change, Status.FailedPreconditions);
			return;
		}
		XyAssert.xyAssert(executionResult.getStatus().isSuccess());

		final List<XAtomicEvent> events = executionResult.getEvents();

		final long atomicEventCount = events.size();
		log.debug("[r" + change.rev + "] generated " + events.size() + " events");
		if (events.isEmpty()) {
			change.giveUpIfTimeoutCritical();
			this.changelogManager.commitAndClearLocks(change, Status.SuccessNochange);
			log.debug("No change");
			return;
		}
		XyAssert.xyAssert(!events.isEmpty());

		if (atomicEventCount > 1000) {
			log.warn("Created over 1000 events (" + atomicEventCount
					+ ") GA?category=xydra&action=saveManyEvents&label=events&value="
					+ atomicEventCount);
			try {
				throw new RuntimeException("Over 1000 events for result=" + executionResult
						+ " change=" + change);
			} catch (final Exception e) {
				log.warn("Over 1000 events", e);
			}
		}

		final Pair<int[], List<Future<SKey>>> res = change.setEvents(events);
		// Wait on all changes.
		for (final Future<SKey> future : res.getSecond()) {
			FutureUtils.waitFor(future);
		}

		change.giveUpIfTimeoutCritical();
		this.changelogManager.commitAndClearLocks(change, Status.SuccessExecutedApplied);
	}

	/**
	 * Phase 2: Check locks from our change number on downwards: We check all
	 * changes until:
	 *
	 * a) a change is timed out. We try to progress the change.
	 *
	 * b) the change is 'Creating' and has a lock that we need: We give up.
	 *
	 * c) we reached the last known lastSilentCommited number: We know we have
	 * all required locks and execute.
	 *
	 * We check by batch-fetching. We have a maximal window size (might need to
	 * be smaller when large values are there). 1 MB fetch size, max size of
	 * change ca. 10 objects each with 4k => 40k. So we fetch 25 changes.
	 *
	 * @param ourChange
	 *            waiting to be executed
	 * @param info
	 * @throws VoluntaryTimeoutException
	 */
	private void execute_waitForLocks(final GaeChange ourChange, final GaeModelRevInfo info,
			final RevisionManager revisionManager) throws VoluntaryTimeoutException {
		final long lastCommited = info.getLastStableCommitted();
		XyAssert.xyAssert(lastCommited == -1 || ourChange.rev > lastCommited,
				"If a lastSilentCommitted is not undefiend (i.e. == -1), "
						+ "it must be before this change");

		final Interval pendingChangesSearchRange = new Interval(lastCommited + 1, ourChange.rev - 1);
		if (pendingChangesSearchRange.isEmpty()) {
			/* working window is zero, there are no pending changes */
			return;
		}
		XyAssert.xyAssert(pendingChangesSearchRange.size() < 1000, "?", pendingChangesSearchRange,
				pendingChangesSearchRange.size());

		final Interval fetchRange = pendingChangesSearchRange.copy();
		fetchRange.adjustStartToFitSizeIfNecessary(MAX_CHANGES_FETCH_SIZE);

		final Map<Long, GaeChange> changes = this.changelogManager.getChanges(fetchRange);
		for (long rev = fetchRange.start; rev <= fetchRange.end; rev++) {
			final GaeChange otherChange = changes.get(rev);
			XyAssert.xyAssert(otherChange != null);
			assert otherChange != null;

			/* Always check if a change is timed-out, and if so, progress it */
			if (this.changelogManager.progressChangeIfTimedOut(otherChange, revisionManager)) {
				/* now it cannot have any locks */
				continue;
			}
			if (!otherChange.getStatus().canChange()) {
				this.revisionManager.foundNewHigherCommitedChange(otherChange);
				continue;
			} else {
				/* its pending, somebody else is just working on it */
				final GaeLocks nextLocks = otherChange.getLocks();
				if (nextLocks.isConflicting(ourChange.getLocks())) {
					execute_waitForOtherThreadToCommit(ourChange, otherChange);
				}
			}
		}
	}

	/**
	 * The nextChange is uncommitted and holds conflicting locks, so we need to
	 * wait. Waiting is done by sleeping increasing intervals and then checking
	 * the change entity again.
	 *
	 * The locks that we already "acquired" cannot be released before entering
	 * the waiting mode, as releasing them before completely executing our own
	 * change would allow other changes with conflicting locks and a revision
	 * greater than ours to execute before our own change.
	 *
	 * @param ourChange
	 * @param nextChange
	 *            is in status 'Creating' and we wait for its commit
	 * @throws VoluntaryTimeoutException
	 */
	private void execute_waitForOtherThreadToCommit(final GaeChange ourChange, final GaeChange nextChange)
			throws VoluntaryTimeoutException {
		long waitTime = WAIT_INITIAL;
		while (!nextChange.isTimedOut()) {

			ourChange.giveUpIfTimeoutCritical();

			/*
			 * IMPROVE save own command if waitTime is too long (so that we can
			 * be rolled forward in case of timeout)
			 */
			try {
				Thread.sleep(waitTime);
			} catch (final InterruptedException e) {
				// ignore interrupt
			}
			// IMPROVE update own lastActivity?

			nextChange.reload();

			if (!nextChange.getStatus().canChange()) {
				XyAssert.xyAssert(!nextChange.hasLocks(),
						"now finished, so cannot have no locks anymore");
				this.revisionManager.foundNewHigherCommitedChange(nextChange);
				return;
			}

			waitTime = Algorithms.increaseExponentiallyWithFactorAndMaximum(waitTime, 2, WAIT_MAX);
		}

		XyAssert.xyAssert(nextChange.isTimedOut(), "nextChange timed out");
		this.changelogManager.commitAndClearLocks(nextChange, Status.FailedTimeout);
	}

	/**
	 * Responsible for executing changes for one model, in the datastore.
	 *
	 * This variant checks preconditions by retrieving the latest snapshots and
	 * updating those parts that the executing change has locked. There is no
	 * separate executing step that can be rolled forward.
	 *
	 * There is no additional state stored in the GAE datastore besides that
	 * used by the {@link IGaeChangesService} and {@link IGaeSnapshotService}
	 * implementations.
	 *
	 * @param command
	 *            to be executed
	 * @param actorId
	 * @return the resulting revision number
	 */
	@Override
	public long executeCommand(final XCommand command, final XId actorId) throws VoluntaryTimeoutException {
		log.debug("----------------------------------------- Execute "
				+ DebugFormatter.format(command));
		final NanoClock c = new NanoClock().start();
		XyAssert.xyAssert(this.modelAddress.equalsOrContains(command.getChangedEntity()),
				"cannot handle command " + command + " - it does not address a model");

		final GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		XyAssert.xyAssert(locks.size() > 0, "no locks created for command %s", command);

		/*
		 * Phase 1: Starting from lastTaken try to execute a GAE-transaction
		 * that writes the locks for this command into the change log. Our
		 * status: Creating
		 */
		log.debug("[???] Phase 1: grabRevisionAndRegister " + locks.size() + " locks = " + locks);
		final GaeModelRevInfo info = this.revisionManager.getInfo();
		final GaeChange change = grabRevisionAndRegisterLocks(info, locks, actorId);
		XyAssert.xyAssert(change.rev >= 0);
		c.stopAndStart("grabRevisionAndRegisterLocks");

		/* Phase 2: Entering synchronised code ... */
		log.debug("[r" + change.rev + "] Phase 2: waitForLocks");
		execute_waitForLocks(change, info, this.revisionManager);
		c.stopAndStart("waitForLocks");

		/* --- Code synchronised by Xydra locks in GAE datastore --- */

		// FIXME GAE some serious do-all-or-nothing problems

		/* Phase 3 */
		log.debug("[r" + change.rev + "] Phase 3: check constraints, compute events = " + change
				+ ", command = " + command);
		CheckResult checkResult;
		try {
			checkResult = Executor.checkPreconditions(this.executionContext, command, change);
		} catch (final Throwable t) {
			log.error("Error in phase 3", t);
			throw new RuntimeException(t);
		}

		/*
		 * implicitly this method also changes tentativeObjectStates from
		 * implied events caused by remove-commands
		 */
		log.debug("[r" + change.rev + "] Phase 3b: computeEvents '"
				+ checkResult.getStatus().name() + "' change = " + change + ", command = "
				+ command);
		final ExecutionResult executionResult = ExecutionResult.createEventsFrom(checkResult,
				this.executionContext);

		// updateTentativeState
		log.debug("[r" + change.rev + "] Phase 3c: updateTos '"
				+ executionResult.getStatus().name() + "' change = " + change + ", command = "
				+ command + " --> " + executionResult.getEvents().size() + " events");
		if (checkResult.getStatus() == Status.SuccessExecuted) {
			updateTentativeObjectStates(checkResult.getExecutionContextInTxn(),
					this.executionContext, change.rev);
		}

		/* --- End of code synchronised by Xydra locks in GAE datastore --- */

		/* Phase 4: Write result in change-log, releasing the locks ... */
		log.debug("[r" + change.rev + "] Phase 4: saveEvents '"
				+ executionResult.getStatus().name() + "' change = " + change + ", command = "
				+ command);
		execute_saveEventsReleaseLocks(executionResult, change);
		c.stopAndStart("saveEvents");
		XyAssert.xyAssert(!change.getStatus().canChange(),
				"If we reach this line, change must be committed");

		this.revisionManager.foundNewHigherCommitedChange(change);

		if (log.isInfoEnabled() || executionResult.getStatus().isFailure() && log.isWarnEnabled()) {
			final String msg = "[r"
					+ change.rev
					+ "] -> "
					+ executionResult.getStatus()
					+ "."
					+

					(executionResult.getDebugHint() != null ? " Reason: "
							+ executionResult.getDebugHint() : "")

					+ " Stats: " + c.getStats();
			if (executionResult.getStatus().isFailure()) {
				log.warn("!!! " + msg);
			} else {
				log.debug("+++ " + msg);
			}
		}

		log.debug("Resulting revInfo: " + this.revisionManager.getInfo() + " for "
				+ this.modelAddress);

		switch (executionResult.getStatus()) {
		case FailedPreconditions:
			return XCommand.FAILED;
		case FailedTimeout:
			return XCommand.FAILED;
		case SuccessExecuted:
		case SuccessExecutedApplied:
			return change.rev;
		case SuccessNochange:
			return XCommand.NOCHANGE;
		default:
		case Creating:
			throw new AssertionError("Cannot happen");
		}
	}

	private static void updateTentativeObjectStates(final ContextInTxn sourceContext,
			final ContextBeforeCommand targetContext, final long changeRev) {

		for (final XReadableObject addedObject : sourceContext.getAdded()) {
			final TentativeObjectState tos = new TentativeObjectState(addedObject, true, changeRev);
			for (final XId fieldId : tos) {
				final XRevWritableField field = tos.getField(fieldId);
				field.setRevisionNumber(changeRev);
			}
			tos.setRevisionNumber(changeRev);
			targetContext.saveTentativeObjectState(tos);
		}
		for (final XId removedObject : sourceContext.getRemoved()) {
			final TentativeObjectState tos = targetContext.getTentativeObjectState(removedObject);
			XyAssert.xyAssert(tos != null);
			assert tos != null;
			tos.setObjectExists(false);
			tos.setModelRev(changeRev);
			tos.setRevisionNumber(changeRev);
			targetContext.saveTentativeObjectState(tos);
		}
		for (final ChangedObject changedObject : sourceContext.getChanged()) {
			if (changedObject.hasChanges()) {
				final XRevWritableObject object = XCopyUtils.createSnapshot(changedObject);
				for (final XReadableField addedField : changedObject.getAdded()) {
					object.getField(addedField.getId()).setRevisionNumber(changeRev);
				}
				for (final ChangedField changedField : changedObject.getChangedFields()) {
					if (changedField.isChanged()) {
						object.getField(changedField.getId()).setRevisionNumber(changeRev);
					}
				}

				final TentativeObjectState tos = new TentativeObjectState(object, true, changeRev);
				tos.setRevisionNumber(changeRev);
				targetContext.saveTentativeObjectState(tos);
			}
		}
	}

	@Override
	public List<XEvent> getEventsBetween(final XAddress address, final long beginRevision,
			final long endRevision) {
		final Interval interval = new Interval(beginRevision, endRevision);
		final List<XEvent> events = this.changelogManager.getEventsInInterval(interval);
		if (address.getAddressedType() == XType.XMODEL) {
			XyAssert.xyAssert(address.equals(this.modelAddress));
			/**
			 * Fulfil the XydraStore specification and return null if the model
			 * has not been managed
			 */
			if (events.size() == 0) {
				return null;
			}
			return events;
		} else {
			XyAssert.xyAssert(Base.resolveModel(address).equals(this.modelAddress), "", address,
					this.modelAddress);
			final AbstractFilteringIterator<XEvent> it = new AbstractFilteringIterator<XEvent>(
					events.iterator()) {

				@Override
				protected boolean matchesFilter(final XEvent entry) {
					if (entry instanceof XTransactionEvent) {
						final XTransactionEvent txnEvent = (XTransactionEvent) entry;
						for (final XAtomicEvent a : txnEvent) {
							if (addressContainsOther(address, a.getChangedEntity())) {
								return true;
							}
						}
						return false;
					} else {
						return addressContainsOther(address, entry.getChangedEntity());
					}
				}

			};
			return Iterators.addAll(it, new LinkedList<XEvent>());
		}
	}

	/**
	 * @param a
	 *            parent
	 * @param b
	 *            child
	 * @return true iff a equals b or if b is an address within the entity
	 *         addressed by a.
	 *
	 *         // TODO GAE: move to core
	 */
	public static boolean addressContainsOther(final XAddress a, final XAddress b) {
		switch (a.getAddressedType()) {
		case XREPOSITORY:
			return b.getRepository().equals(a.getRepository());
		case XMODEL:
			return b.getRepository().equals(a.getRepository()) && b.getModel() != null
					&& b.getModel().equals(a.getModel());
		case XOBJECT:
			return b.getRepository().equals(a.getRepository()) && b.getModel() != null
					&& b.getModel().equals(a.getModel()) && b.getObject() != null
					&& b.getObject().equals(a.getObject());
		case XFIELD:
			return a.equals(b);
		}
		throw new AssertionError();
	}

	/**
	 * @param includeTentative
	 *            if true, then in addition to the stable model revision number
	 *            also the unstable tentative revision number is calculated.
	 * @return the current {@link ModelRevision} or null
	 */
	@Override
	public ModelRevision getModelRevision(final boolean includeTentative) {
		GaeModelRevInfo info;

		// FIXME GAE should suffice to compute only if imprecise
		// info = this.revisionManager.getInfo();
		// if(info.getPrecision() != Precision.Precise) {
		computeMorePreciseCurrentRev();
		info = this.revisionManager.getInfo();
		// }

		final long revision = info.getLastStableSuccessChange();
		final boolean modelExists = info.isModelExists();
		ModelRevision modelRev;
		if (includeTentative) {
			final long tentativeRevision = info.getLastSuccessChange();
			modelRev = new ModelRevision(revision, modelExists, tentativeRevision);
		} else {
			modelRev = new ModelRevision(revision, modelExists);
		}

		return modelRev;
	}

	private void computeMorePreciseCurrentRev() {
		final GaeModelRevInfo info = this.revisionManager.getInfo();
		final long lastCommited = info.getLastStableCommitted();
		final long searchStart = Math.max(lastCommited, 0);
		final Interval currentSearchRange = new Interval(searchStart, searchStart
				+ MAX_CHANGES_FETCH_SIZE);

		log.debug("@" + this.modelAddress + " compute rev from " + info + " in "
				+ currentSearchRange);

		computeMorePreciseCurrentRevInInterval(info, currentSearchRange);
		log.debug("@" + this.modelAddress + " new rev is " + info + "; searched "
				+ currentSearchRange);
	}

	/**
	 * @param info
	 * @param searchRange
	 *            just the initial search range, gets extended to infinity
	 */
	private void computeMorePreciseCurrentRevInInterval(final GaeModelRevInfo info, final Interval searchRange) {
		XyAssert.xyAssert(searchRange.start >= 0);
		Interval window = searchRange.copy();
		while (true) {
			// log.debug("Scanning changes in " + window + " within " +
			// searchRange);
			final Map<Long, GaeChange> changes = this.changelogManager.getChanges(window);
			if (changes.isEmpty()) {
				log.debug("Found no changes in " + window);
				info.setPrecision(Precision.Precise);
				info.setDebugHint("Found no changes in " + window + " so rev "
						+ info.getLastStableSuccessChange() + " is now precise for "
						+ this.modelAddress);
				return;
			} else {

				for (long rev = window.start; rev <= window.end; rev++) {
					final GaeChange change = changes.get(rev);
					if (change == null) {
						log.debug("Found first empty spot in changelog at rev=" + rev);
						info.setDebugHint("Found first empty spot in changelog at rev=" + rev);
						info.setPrecision(Precision.Precise);
						return;
					} else {
						/*
						 * Always check if a change is timed-out, and if so,
						 * progress it
						 */
						final boolean progressed = this.changelogManager.progressChangeIfTimedOut(change,
								this.revisionManager);
						if (progressed) {
							// revision was incremented via
							// foundNewHigherCommitedChange
						} else if (!change.getStatus().canChange()) {
							this.revisionManager.foundNewHigherCommitedChange(change);
						} else {
							/* its pending, somebody else is just working on it */
							log.debug("Found first pending spot in changelog at rev=" + rev
									+ " status=" + change.getStatus());
							info.setPrecision(Precision.Precise);
							info.setDebugHint("Found first pending spot in changelog at rev=" + rev
									+ " status=" + change.getStatus());
							return;
						}
					}
				}
				window = window.moveRight();
			}
		}
	}

	@Override
	public XWritableObject getObjectSnapshot(final XId objectId, final boolean includeTentative) {
		if (includeTentative) {
			// short-cut
			final TentativeObjectState tos = this.executionContext.getObject(objectId);
			if (tos == null) {
				return null;
			}
			if (!tos.exists()) {
				return null;
			}
			return tos;
		}

		// slow
		final XWritableModel snapshot = getSnapshot(includeTentative);
		if (snapshot == null) {
			return null;
		}
		return snapshot.getObject(objectId);
	}

	@Override
	synchronized public XWritableModel getSnapshot(final boolean includeTentative) {
		computeMorePreciseCurrentRev();
		final GaeModelRevInfo info = this.revisionManager.getInfo();

		// // FIXME GAE returns sometimes a way too low rev number as Precise
		// GaeModelRevInfo info = this.revisionManager.getInfo();
		// if(info.getPrecision() != Precision.Precise) {
		// computeMorePreciseCurrentRev();
		// info = this.revisionManager.getInfo();
		// }
		//
		// // FIXME GAE !!!!!!!!!!!!!!!
		// else {
		// GaeModelRevInfo before = info.copy();
		// computeMorePreciseCurrentRev();
		// XyAssert.xyAssert(before.equals(info), " before %s but after %s",
		// before, info);
		// }

		XyAssert.xyAssert(info.getPrecision() == Precision.Precise);

		if (!info.isModelExists()) {
			return null;
		}

		final long currentRevNr = info.getLastStableSuccessChange();
		if (includeTentative) {
			// take performance short-cut for large models
			final XWritableModel snapshot = this.snapshotService.getTentativeModelSnapshot(currentRevNr);
			log.info("return tentative snapshot rev " + currentRevNr + " for model "
					+ this.modelAddress);
			return snapshot;
		} else {
			// do slower standard way
			final XWritableModel snapshot = this.snapshotService.getModelSnapshot(currentRevNr,
					!includeTentative);
			log.debug("return snapshot rev " + currentRevNr + " for model " + this.modelAddress);
			return snapshot;
		}

	}

	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 *
	 * @param info
	 *
	 * @param lastTaken
	 *
	 * @param locks
	 *            which locks to get
	 * @param actorId
	 *            The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 *
	 *         Note: Reads revCache.lastTaken
	 */
	@XGaeOperation(memcacheRead = true, datastoreRead = true, datastoreWrite = true, memcacheWrite = true)
	private GaeChange grabRevisionAndRegisterLocks(final GaeModelRevInfo info, final GaeLocks locks, final XId actorId) {
		final long lastTaken = info.getLastTaken();
		XyAssert.xyAssert(lastTaken >= -1);
		final long start = lastTaken + 1;

		final GaeChange change = this.changelogManager.grabRevisionAndRegisterLocks(locks, actorId,
				start, this.revisionManager);
		return change;
	}

	@Override
	public int hashCode() {
		return this.modelAddress.hashCode();
	}

	@Override
	public boolean modelHasBeenManaged() {
		final GaeChange change = this.changelogManager.getChange(0);
		return change != null;
	}

	/**
	 * Saves the updated change.
	 *
	 * @param modelAddress
	 *
	 * @param change
	 * @param info
	 * @param changelogManager
	 */
	public static void rollForward_updateTentativeObjectStates(final XAddress modelAddress,
			final GaeChange change, final GaeModelRevInfo info, final ChangeLogManager changelogManager) {
		XyAssert.xyAssert(change.getStatus().changedSomething());
		log.debug("roll forward " + change);

		final GaeSnapshotServiceImpl5 snapshotService = new GaeSnapshotServiceImpl5(changelogManager);
		final ContextBeforeCommand ctxBeforeCmd = new ContextBeforeCommand(modelAddress, info,
				snapshotService);
		final ContextInTxn ctxInTxn = ctxBeforeCmd.forkTxn();

		final XEvent event = change.getEvent();
		if (event instanceof XTransactionEvent) {
			final XTransactionEvent txnEvent = (XTransactionEvent) event;
			for (int i = 0; i < txnEvent.size(); i++) {
				final XAtomicEvent e = txnEvent.getEvent(i);
				updateTentativeObjectStates(modelAddress, e, ctxInTxn);
			}
		} else {
			final XAtomicEvent e = (XAtomicEvent) event;
			updateTentativeObjectStates(modelAddress, e, ctxInTxn);
		}

		// move changes from txnContext to tos
		updateTentativeObjectStates(ctxInTxn, ctxBeforeCmd, change.rev);

		change.setStatus(Status.SuccessExecutedApplied);
		change.save();
	}

	private static void updateTentativeObjectStates(final XAddress modelAddress, final XAtomicEvent e,
			final ContextInTxn ctxInTxn) {
		switch (e.getTarget().getAddressedType()) {
		case XREPOSITORY: {
			/* model add/remove */
			switch (e.getChangeType()) {
			case ADD:
				ctxInTxn.setExists(true);
				break;
			case REMOVE:
				ctxInTxn.setExists(false);
				break;
			default:
				throw new AssertionError();
			}
			break;
		}
		case XMODEL: {
			/* object add/remove */
			switch (e.getChangeType()) {
			case ADD:
				ctxInTxn.createObject(e.getChangedEntity().getObject());
				break;
			case REMOVE:
				ctxInTxn.removeObject(e.getChangedEntity().getObject());
				break;
			default:
				throw new AssertionError();
			}
			break;
		}
		case XOBJECT: {
			/* field add/remove */
			final XStateWritableObject obj = ctxInTxn.getObject(e.getChangedEntity().getObject());
			switch (e.getChangeType()) {
			case ADD:
				obj.createField(e.getChangedEntity().getField());
				break;
			case REMOVE:
				obj.removeField(e.getChangedEntity().getField());
				break;
			default:
				throw new AssertionError();
			}
			break;
		}
		case XFIELD: {
			/* value add/remove/change */
			final XStateWritableObject obj = ctxInTxn.getObject(e.getChangedEntity().getObject());
			final XStateWritableField field = obj.getField(e.getChangedEntity().getField());
			final XFieldEvent fieldEvent = (XFieldEvent) e;
			switch (e.getChangeType()) {
			case ADD:
				field.setValue(fieldEvent.getNewValue());
				break;
			case REMOVE:
				field.setValue(null);
				break;
			case CHANGE:
				field.setValue(fieldEvent.getNewValue());
				break;
			default:
				throw new AssertionError();
			}

			break;
		}
		default:
			break;
		}

	}

}

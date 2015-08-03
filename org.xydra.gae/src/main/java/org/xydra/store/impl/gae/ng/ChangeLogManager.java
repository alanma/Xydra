package org.xydra.store.impl.gae.ng;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.CommittedButStillApplyingException;
import org.xydra.xgae.datastore.api.DatastoreFailureException;
import org.xydra.xgae.datastore.api.DatastoreTimeoutException;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.STransaction;

/**
 * Knows how to access the change log in the datastore. Maintain its integrity.
 *
 * @author xamde
 * @since 2012-05
 */
public class ChangeLogManager {

	private static final Logger log = LoggerFactory.getLogger(ChangeLogManager.class);

	@Setting("")
	static final int MAXIMAL_CHANGES_FETCH_SIZE = 256;

	private final XAddress modelAddress;

	/**
	 * @param modelAddress
	 *            required to compute keys in datastore
	 */
	public ChangeLogManager(@NeverNull final XAddress modelAddress) {
		XyAssert.xyAssert(modelAddress != null);
		this.modelAddress = modelAddress;
	}

	/**
	 * @param change
	 *            must not yet be a terminal state
	 * @param status
	 *            must be a terminal state, i.e. not 'Creating'
	 */
	public void commitAndClearLocks(final GaeChange change, final Status status) {
		XyAssert.xyAssert(!status.canChange());
		XyAssert.xyAssert(change.getStatus().canChange());

		change.commitAndClearLocks(status);

		XyAssert.xyAssert(change.getStatus() == status);
	}

	public @CanBeNull GaeChange getChange(final long rev) {
		final SKey key = KeyStructure.createChangeKey(this.modelAddress, rev);
		final SEntity entityFromGae = XGae.get().datastore().sync().getEntity(key);
		if (entityFromGae == null) {
			return null;
		}
		final GaeChange change = new GaeChange(this.modelAddress, rev, entityFromGae);
		return change;
	}

	/**
	 * Going down to the data store and fetch the actual content of a models
	 * change log
	 *
	 * @param maxSingleBatchFetchRange
	 *            for which to fetch changes
	 * @return a map of revision number -> GaeChange
	 */
	private @NeverNull Map<Long, GaeChange> getChangesInBatch(final Interval maxSingleBatchFetchRange) {
		/* prepare keys for batch request */
		final List<SKey> keys = new ArrayList<SKey>();
		for (long rev = maxSingleBatchFetchRange.start; rev <= maxSingleBatchFetchRange.end; rev++) {
			final SKey key = KeyStructure.createChangeKey(this.modelAddress, rev);
			keys.add(key);
		}
		/* execute batch request */
		final Map<SKey, SEntity> entities = XGae.get().datastore().sync().getEntities(keys);

		/* process result */
		final Map<Long, GaeChange> changes = new HashMap<Long, GaeChange>();
		for (final Entry<SKey, SEntity> entry : entities.entrySet()) {
			final SKey key = entry.getKey();
			final long rev = KeyStructure.getRevisionFromChangeKey(key);
			final SEntity entity = entry.getValue();
			if (entity != null) {
				final GaeChange change = new GaeChange(this.modelAddress, rev, entity);
				changes.put(rev, change);
			}
		}

		log.debug("BatchGet changes in " + maxSingleBatchFetchRange + " => " + changes.size()
				+ " changes");

		return changes;
	}

	/**
	 * @param fetchRange
	 * @return a map of revision number -> GaeChange
	 */
	public @NeverNull Map<Long, GaeChange> getChanges(final Interval fetchRange) {
		final Map<Long, GaeChange> changes = new HashMap<Long, GaeChange>();

		if (!fetchRange.isEmpty()) {
			/**
			 * get requested events in batches of MAXIMAL_CHANGES_FETCH_SIZE and
			 * use smaller ranges when exceptions occur
			 */
			Interval singleBatchFetchRange = fetchRange.getSubInterval(MAXIMAL_CHANGES_FETCH_SIZE);
			boolean hadNullChanges = false;
			do {
				try {
					final Map<Long, GaeChange> newChanges = getChangesInBatch(singleBatchFetchRange);
					changes.putAll(newChanges);
					if (newChanges.size() < singleBatchFetchRange.size()) {
						hadNullChanges = true;
					}
				} catch (final Throwable t) {
					log.warn("Could not read a change interval " + singleBatchFetchRange, t);
					singleBatchFetchRange = singleBatchFetchRange.firstHalf();
				}
				singleBatchFetchRange = singleBatchFetchRange
						.moveRightAndShrinkToKeepEndMaxAt(fetchRange.end);
			} while (singleBatchFetchRange.end < fetchRange.end && !hadNullChanges);
		}
		return changes;
	}

	public @NeverNull List<XEvent> getEventsInInterval(final Interval interval) {
		log.debug("Getting events from changes in " + interval + " for " + this.modelAddress);
		final LinkedList<XEvent> events = new LinkedList<XEvent>();
		final Map<Long, GaeChange> changes = getChanges(interval);
		for (long rev = interval.start; rev <= interval.end; rev++) {
			final GaeChange change = changes.get(rev);
			if (change == null) {
				break;
			}

			if (change.getStatus().changedSomething()) {
				events.add(change.getEvent());
			}
		}
		log.debug("Got " + events.size() + " events from " + changes.size() + " changes in "
				+ interval + " for " + this.modelAddress);
		return events;
	}

	public XAddress getModelAddress() {
		return this.modelAddress;
	}

	public GaeChange grabRevisionAndRegisterLocks(final GaeLocks locks, final XId actorId, final long start,
			@NeverNull final RevisionManager revisionManager) {
		for (long rev = start;; rev++) {

			// Try to grab this revision.
			final SKey key = KeyStructure.createChangeKey(this.modelAddress, rev);
			/* use txn to do: avoid overwriting existing change entities */
			final STransaction trans = XGae.get().datastore().sync().beginTransaction();

			final SEntity changeEntity = XGae.get().datastore().sync().getEntity(key, trans);

			if (changeEntity == null) {

				final GaeChange newChange = new GaeChange(this.modelAddress, rev, locks, actorId);
				newChange.save(trans);

				try {
					XGae.get().datastore().sync().endTransaction(trans);
				} catch (final ConcurrentModificationException cme) {
					/*
					 * One possible cause: 'too much contention on these
					 * datastore entities. please try again.'
					 */
					log.info("ConcurrentModificationException, failed to take revision: " + key,
							cme);

					// transaction failed as another process wrote to this
					// entity

					// TODO ! if we can assume that at least one thread was
					// successful, we go ahead to the next revision.

					// Check this revision again
					rev--;
					continue;
				} catch (final DatastoreTimeoutException dte) {
					log.info("failed to take revision: " + key
							+ " GA?category=error&action=DatastoreTimeout", dte);

					// try this revision again
					rev--;
					continue;
				} catch (final DatastoreFailureException dfe) {
					/*
					 * Some forums report this happens for read-only entities
					 * that got stuck in a wrong state after scheduled
					 * maintenance
					 */
					log.info("failed to take revision: " + key
							+ " GA?category=error&action=DatastoreFailureException", dfe);

					// try this revision again TODO !!! might be an endless
					// loop!
					rev--;
					continue;
				} catch (final CommittedButStillApplyingException csa) {
					log.warn("CommittedButStillApplyingException on " + key
							+ " GA?category=error&action=CommittedButStillApplyingException");
					/* We believe the commit worked */
					continue;
				}

				revisionManager.foundNewLastTaken(rev);

				// transaction succeeded, we have a revision
				return newChange;

			} else {
				// Revision already taken.

				final GaeChange change = new GaeChange(this.modelAddress, rev, changeEntity);
				XGae.get().datastore().sync().endTransaction(trans);
				revisionManager.foundNewLastTaken(rev);

				/* Since we read the entity anyway, we use the information */
				progressChangeIfTimedOut(change, revisionManager);
			}
		}

		// unreachable
	}

	/**
	 * Check if change is timed-out and then move to status
	 *
	 * @param change
	 *            @NeverNull
	 * @param revisionManager
	 *            @NeverNull
	 * @return true if change was timed-out and hence progressed
	 */
	public boolean progressChangeIfTimedOut(@NeverNull final GaeChange change,
			@NeverNull final RevisionManager revisionManager) {
		XyAssert.xyAssert(change != null);
		assert change != null;

		final Status status = change.getStatus();
		if (status.canChange()) {
			log.debug("Trying to progress change " + change);
			if (change.isTimedOut()) {
				log.debug("handleTimeout: " + change);
				if (status == Status.Creating) {
					commitAndClearLocks(change, Status.FailedTimeout);
					revisionManager.foundNewHigherCommitedChange(change);
					return true;
				} else if (status == Status.SuccessExecuted) {
					// record changes to signal other threads we work on it
					final Future<SKey> f = change.save();
					SKey key;
					try {
						key = f.get();
						if (key != null) {
							GaeModelPersistenceNG.rollForward_updateTentativeObjectStates(
									this.modelAddress, change, revisionManager.getInfo(), this);
						}
					} catch (final InterruptedException e) {
					} catch (final ExecutionException e) {
					}
				}
			}
		}
		return false;
	}

}

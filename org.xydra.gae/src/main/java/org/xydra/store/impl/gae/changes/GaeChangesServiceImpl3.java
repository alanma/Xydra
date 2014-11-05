package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.InstanceRevisionManager;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;
import org.xydra.xgae.XGae;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.CapabilityDisabledException;
import org.xydra.xgae.datastore.api.CommittedButStillApplyingException;
import org.xydra.xgae.datastore.api.DatastoreFailureException;
import org.xydra.xgae.datastore.api.DatastoreTimeoutException;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.STransaction;

/**
 * A class responsible for managing a running log of executed and in-progress
 * changes to a specific {@link XModel} in the GAE datastore.
 * 
 * This class is the core of the GAE {@link XydraStore} implementation.
 * 
 * There are two different kinds of GAE Entities that are used by this class:
 * 
 * <dt>Entity type XCHANGE</dt> <dd>These represent a change to the model
 * resulting from a single {@link XCommand} (which may be a {@link XTransaction}
 * ). These entities represent both an entry into the {@link XChangeLog} as well
 * as a change that is currently in progress. Keys are encoded according to
 * {@link KeyStructure#createChangeKey(XAddress, long)}
 * 
 * The XCHANGE entities are managed by {@link GaeChange}. They store the status
 * of the change, the required locks, the actor that initiated the change, and
 * the time the (last) process started working on the change.
 * 
 * Events and small XValues are also saved in the XCHANGE entities. These
 * properties are managed by {@link GaeEvents}. No events are guaranteed to be
 * set before the change has reached a terminal state.
 * 
 * 
 * <dt>Entity type XVALUE</dt> <dd>Stores an {@link XValue} set by an
 * {@link XFieldEvent} that was too large to be stored directly in the
 * corresponding XCHANGE entity. These are managed by {@link GaeEvents}.
 * 
 * </dd>
 * 
 * As commands need to be executed in a well defined order each change needs to
 * grab a revision number before executing. This is done using a GAE
 * transaction.
 * 
 * The first thing each change does is declare it's required locks in the change
 * entity when grabbing the revision. Before executing, a change checks any
 * uncommitted changes with lower revision numbers for conflicting locks. If a
 * conflict is found, the change will have to wait or abort the conflicting
 * change after a timeout. Other pending changes that don't conflict can be
 * safely ignored. After a change is done, it removes all locks from the change
 * entity.
 * 
 * Locks are managed by {@link GaeLocks}.
 * 
 * @author dscharrer
 * @author xamde
 */
public class GaeChangesServiceImpl3 implements IGaeChangesService {

	private static final Logger log = LoggerFactory.getLogger(GaeChangesServiceImpl3.class);

	/** part of cache key names for {@link RevisionInfo} */
	private static final String KEY_CACHE_REVINFO = "revInfo";

	/** re-usable instance for conversion helper */
	private static final UniCacheRevisionInfoEntryHandler uniCacheRevisionInfoEntryHandler = new UniCacheRevisionInfoEntryHandler();

	// IMPROVE use this and measure the performance impact
	private static final boolean USE_MEMCACHE_FOR_CHANGES = false;

	// IMPROVE use this and measure the performance impact
	private static final boolean USE_MEMCACHE_FOR_REVISIONS = false;

	private static final int OFFSET = (int) (Math.random() * 16);

	/**
	 * Interval sizes for fetches to back-end when looking for current rev. Last
	 * value is used for all following rounds as well.
	 */
	// IMPROVE verify that 128 is not too large
	// IMPROVE find better values
	private static final int[] WINDOW_SIZES = { 1, 8, 32, 128 };

	@Override
	public XAddress getModelAddress() {
		return this.modelAddr;
	}

	/**
	 * @param e
	 *            atomic or transaction event @NeverNull
	 * @return true if model must exist after this event
	 */
	private static boolean eventIndicatesModelExists(@NeverNull final XEvent e) {
		XyAssert.xyAssert(e != null);
		assert e != null;
		XEvent event = e;
		if (event.getChangeType() == ChangeType.TRANSACTION) {
			// check only last event
			XTransactionEvent txnEvent = (XTransactionEvent) event;
			XyAssert.xyAssert(txnEvent.size() >= 1);
			event = txnEvent.getEvent(txnEvent.size() - 1);
			XyAssert.xyAssert(event != null);
			assert event != null;
		}
		XyAssert.xyAssert(event.getChangeType() != ChangeType.TRANSACTION);
		if (event.getTarget().getAddressedType() == XType.XREPOSITORY) {
			if (event.getChangeType() == ChangeType.REMOVE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Only for assertions.
	 * 
	 * @param events
	 * @param begin
	 * @param endRev
	 * @return always true
	 * @throws AssertionError
	 */
	private static boolean eventsAreWithinRange(List<XEvent> events, long begin, long endRev) {
		for (XEvent e : events) {
			XyAssert.xyAssert(e.getRevisionNumber() >= begin);
			XyAssert.xyAssert(e.getRevisionNumber() <= endRev);
		}
		return true;
	}

	/**
	 * @param round
	 * @return the defined window size
	 */
	private static int windowsSizeForRound(int round) {
		XyAssert.xyAssert(round >= 0);
		XyAssert.xyAssert(WINDOW_SIZES.length > 0);
		if (round < WINDOW_SIZES.length) {
			return WINDOW_SIZES[round];
		} else {
			return WINDOW_SIZES[WINDOW_SIZES.length - 1];
		}
	}

	/**
	 * Used to coordinate calculateCurrentModelRevision and
	 * computeCurrenRevisionFromLocalCache
	 */
	private RevisionInfo revInfoFromMemcacheAndDatastore = null;

	/** The address of the model being represented */
	private final XAddress modelAddr;

	/**
	 * Instance-wide shared {@link RevisionInfo}. Defer reading until the point
	 * when we need it to ensure maximum freshness.
	 */
	private final InstanceRevisionManager instanceRevInfoManager;

	/** UniCache for {@link RevisionInfo}s */
	private UniCache<RevisionInfo> revInfoMemcacheAndDatastoreCache;

	private AllChanges cachedChanges;

	private String revisionCacheName;

	@XGaeOperation()
	public GaeChangesServiceImpl3(XAddress modelAddr, InstanceRevisionManager revisionManager) {
		this.modelAddr = modelAddr;
		this.instanceRevInfoManager = revisionManager;
		this.cachedChanges = new AllChanges(modelAddr);
		this.revisionCacheName = KEY_CACHE_REVINFO + modelAddr;
		this.revInfoMemcacheAndDatastoreCache = new UniCache<RevisionInfo>(
				uniCacheRevisionInfoEntryHandler);
	}

	@Override
	/*
	 * Called from: public.
	 * 
	 * While calculating, fetch all events not yet known on this instance.
	 */
	// FIXME calculateCurrentModelRevision
	public GaeModelRevision calculateCurrentModelRevision(boolean includeTentative) {
		/* === Using data from our instance === */
		GaeModelRevision lastCurrentRev = this.instanceRevInfoManager.getInstanceRevisionInfo()
				.getGaeModelRevision();
		XyAssert.xyAssert(lastCurrentRev.getModelRevision() != null);
		log.info(this.getModelAddress() + " >> Update currentRev from lastCurrentRev="
				+ lastCurrentRev);
		/*
		 * After this method, it might turn out, that 'current' is in fact the
		 * current revision.
		 */
		CandidateRev candidate = new CandidateRev(lastCurrentRev);
		XyAssert.xyAssert(candidate.isFinalModelRev() == false, "we cannot know this yet");

		/*
		 * Find a good starting point for our search. Initially use local
		 * information, but after a while fetch a cached RevInfo from a backend
		 * service to have a hopefully better starting point.
		 */
		int round = 0;
		long eventsLoadedBeforeRevInfoLookupInBackend = 0;
		boolean askedMemcacheOrDatastore = false;
		long lastCheckedRev = lastCurrentRev.getLastSilentCommitted();
		log.debug("Start searching at " + lastCheckedRev + " with last rev being "
				+ candidate.gaeModelRev.getModelRevision());
		while (true) {
			if (!askedMemcacheOrDatastore && eventsLoadedBeforeRevInfoLookupInBackend > 0) {
				log.info("Asking cache after " + eventsLoadedBeforeRevInfoLookupInBackend
						+ " attempts");

				RevisionInfo revInfoFromMemcacheOrDatastore = this.revInfoMemcacheAndDatastoreCache
						.get(this.revisionCacheName, StorageOptions.create(
						/** We looked there already via instanceRevInfoManager */
						0, USE_MEMCACHE_FOR_REVISIONS, true, false));
				if (revInfoFromMemcacheOrDatastore != null) {
					this.revInfoFromMemcacheAndDatastore = revInfoFromMemcacheOrDatastore;
					ModelRevision cachedModelRev = revInfoFromMemcacheOrDatastore
							.getGaeModelRevision().getModelRevision();
					if (cachedModelRev != null
							&& cachedModelRev.revision() > candidate.gaeModelRev.getModelRevision()
									.revision()) {
						candidate.gaeModelRev = revInfoFromMemcacheOrDatastore
								.getGaeModelRevision();
						XyAssert.xyAssert(revInfoFromMemcacheOrDatastore.getLastCommitted() > lastCheckedRev);
						lastCheckedRev = revInfoFromMemcacheOrDatastore.getLastCommitted();
						XyAssert.xyAssert(lastCheckedRev >= candidate.gaeModelRev
								.getModelRevision().revision());
						round = 0;
						log.debug("Cached value is a better start than what we had. Now using "
								+ candidate.gaeModelRev);
					}
				}
				askedMemcacheOrDatastore = true;
				XyAssert.xyAssert(candidate.isFinalModelRev() == false);
			}

			int windowSize = windowsSizeForRound(round);
			log.debug("Windowsize = " + windowSize);
			long beginRevInclusive = lastCheckedRev + 1;
			/*
			 * the highest revision number (inclusive) to check for in this
			 * revision update step
			 */
			long endRevInclusive = beginRevInclusive + windowSize - 1;
			log.debug(this.modelAddr + ":: Update rev step [" + beginRevInclusive + ","
					+ endRevInclusive + "]");

			/**
			 * Try to fetch 'windowSize' changes in batch from memcache and
			 * datastore past the last known "current" revision and put them in
			 * the CommitedChanges
			 */
			log.debug("=== Phase 1: Determine revisions not yet locally cached; windowsize = "
					+ windowSize);
			Set<Long> locallyMissingRevs = computeLocallyMissingRevs(beginRevInclusive,
					endRevInclusive);
			int missingRevs = locallyMissingRevs.size();
			log.trace("locallyMissingRevs: " + missingRevs + " in this window of "
					+ (endRevInclusive - beginRevInclusive + 1) + " revs in total");

			log.debug("=== Phase 2+3: Ask Memcache + Datastore ===");
			long queryTime = fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
			int fetchedRevs = missingRevs - locallyMissingRevs.size();
			log.trace("Number of missingRevs after asking DS&MC: " + locallyMissingRevs.size());

			log.debug("=== Phase 4: Compute result from local cache ===");
			candidate = computeCurrenRevisionFromLocalChanges(beginRevInclusive, endRevInclusive,
					candidate, includeTentative);
			if (candidate.finalModelRev) {
				long rev = candidate.gaeModelRev.getModelRevision().revision();
				log.info(this.modelAddr
						+ ">> Computed rev = "
						+ rev
						+ " DATA?changesMethod=calculateCurrentModelRevision"
						+ // .
						"&i_type=rev"// .
						+ "&i_addr="
						+ this.modelAddr
						+ // .
						"&rev="
						+ candidate.gaeModelRev.getModelRevision().revision()
						+ // .
						"&tentative="
						+ candidate.gaeModelRev.getModelRevision().tentativeRevision()
						+ // .
						"&window=" + windowSize + "&instance=" + XGae.get().getInstanceId()
						+ "&queryAge=" + (System.nanoTime() - queryTime)

				);
				return candidate.gaeModelRev;
			} else {
				lastCheckedRev = endRevInclusive;
				eventsLoadedBeforeRevInfoLookupInBackend += fetchedRevs;
				round++;
			}
		}
	}

	public void clear() {
		log.info("Cleared. Make to sure to also clear memcache.");
		this.instanceRevInfoManager.getInstanceRevisionInfo().clear();
	}

	// FIXME commit
	@Override
	public void commit(GaeChange change, Status status) {

		XyAssert.xyAssert(status.isCommitted());
		XyAssert.xyAssert(!change.getStatus().isCommitted());

		change.commitAndClearLocks(status);
		XyAssert.xyAssert(change.getStatus() == status);

		// if(status == Status.SuccessExecuted) {
		// boolean modelExists = eventIndicatesModelExists(change.getEvent());
		// // FIXME concept: new revision can be lower, there might be
		// // uncommitted
		// // intermediary versions
		// GaeModelRevision gaeModelRev = new GaeModelRevision(change.rev, new
		// ModelRevision(
		// change.rev, modelExists));
		// this.instanceRevInfoManager.getInstanceRevisionInfo()
		// .setCurrentGaeModelRevIfRevisionIsHigher(gaeModelRev);
		// }

		cacheCommittedChange(change);
		log.trace(" DATA?changesMethod=commit" + // .
				"&i_type=rev"// .
				+ "&i_addr=" + this.modelAddr + // .
				"&rev=" + change.rev + // .
				"&status=" + change.getStatus() + // .
				"&instance=" + XGae.get().getInstanceId());
	}

	/**
	 * Called from: {@link #calculateCurrentModelRevision(boolean)} to determine
	 * if a candidate is a finalRev or not;
	 * {@link #grabRevisionAndRegisterLocks(long, GaeLocks, XId)}
	 * 
	 * @param beginRevInclusive
	 * @param endRevInclusive
	 * @param candidate
	 *            not final model rev @NeverNull
	 * @param includeTentative
	 *            TODO docu tentative
	 * @return a {@link CandidateRev} that is not necessarily a final model rev.
	 */
	private CandidateRev computeCurrenRevisionFromLocalChanges(long beginRevInclusive,
			long endRevInclusive, @NeverNull CandidateRev candidate, boolean includeTentative) {
		XyAssert.xyAssert(candidate.isFinalModelRev() == false);
		XyAssert.xyAssert(candidate.gaeModelRev.getModelRevision() != null);
		XyAssert.xyAssert(endRevInclusive - beginRevInclusive >= 0, "begin:" + beginRevInclusive
				+ ",end:" + endRevInclusive);
		log.debug(this.modelAddr + ":: computeFromCache candidate=" + candidate + " in range ["
				+ beginRevInclusive + "," + endRevInclusive + "]");

		for (long i = beginRevInclusive; i <= endRevInclusive; i++) {
			GaeChange change = this.cachedChanges.getCachedChange(i);

			/**
			 * If we look for the current revision (not tentative), then the
			 * first change that is either null or not committed validates our
			 * candiateRev.
			 * 
			 * If we look for the tentative revision, then the first null change
			 * validates the candidateRev.
			 * 
			 * In both cases, we progress the candidateRev for each
			 * SuccessExecuted change.
			 */
			if (change == null) {
				// we just validated a candidate revision
				log.debug("Found end at " + i + " return workingRev=" + candidate);
				candidate.markAsFinalRev();
			} else {
				if (change.getStatus().isCommitted()) {
					// move candidate revision
					candidate.gaeModelRev.setLastSilentCommittedIfHigher(i);
					/*
					 * Impl note: Status.SuccessNoChange is not a candiate rev
					 * because there is no event for that change
					 */
					if (change.getStatus().changedSomething()) {
						// Candidate for new revision is changed
						XEvent event = change.getEvent();
						boolean modelExist = eventIndicatesModelExists(event);

						if (candidate.inTentativeRange) {
							/* do not advance silentCommited */
							candidate.setModelRev(new GaeModelRevision(candidate.gaeModelRev
									.getLastSilentCommitted(), new ModelRevision(
									candidate.gaeModelRev.getModelRevision().revision(),
									modelExist, i)));

						} else {
							candidate.setModelRev(new GaeModelRevision(event.getRevisionNumber(),
									new ModelRevision(i, modelExist)));
						}
						log.debug(this.modelAddr + ":: New currentRev candidate " + candidate);
					}
				} else {
					XyAssert.xyAssert(!change.getStatus().isCommitted());
					if (!includeTentative) {
						// we just validated a stable candidate revision
						log.debug("Found end at " + i + " return workingRev=" + candidate);
						candidate.markAsFinalRev();
					}
				}
			}

			/* Coordinate with other GAE instances */
			boolean putInMemcache = i % 16 == 4 && USE_MEMCACHE_FOR_REVISIONS;
			/*
			 * Use OFFSET here instead of fixed 16 to make collisions (multiple
			 * instances trying to update the cache at the same time) less
			 * likely
			 */
			boolean putInDatastore = i % 64 == OFFSET;
			if (putInMemcache || putInDatastore) {
				// share findings via memcache & datastore with other instances
				StorageOptions storeOpts = UniCache.StorageOptions.create(0, putInMemcache,
						putInDatastore, false);
				// read cache first to prevent destroying good knowledge
				if (this.revInfoFromMemcacheAndDatastore == null) {
					this.revInfoFromMemcacheAndDatastore = this.revInfoMemcacheAndDatastoreCache
							.get(this.revisionCacheName, storeOpts);
				}
				RevisionInfo toBeCached = new RevisionInfo("toBeCached",
						// FIXME is this too low?
						candidate.gaeModelRev,
						// FIXME is this too high?
						this.instanceRevInfoManager.getInstanceRevisionInfo().getLastCommitted(),
						this.instanceRevInfoManager.getInstanceRevisionInfo().getLastTaken());
				if (toBeCached.isBetterThan(this.revInfoFromMemcacheAndDatastore)) {
					log.debug("this revInfo " + toBeCached + " is better than "
							+ this.revInfoFromMemcacheAndDatastore + " and thus will be cached");
					try {
						this.revInfoMemcacheAndDatastoreCache.put(this.revisionCacheName,
								toBeCached, storeOpts);
					} catch (CapabilityDisabledException err) {
						log.warn("Could not write", err);
					}
				}
			}

			// are we done?
			if (candidate.finalModelRev) {
				if (includeTentative) {
					GaeModelRevision g = candidate.gaeModelRev;
					ModelRevision mr = g.getModelRevision();
					candidate.gaeModelRev = new GaeModelRevision(g.getLastSilentCommitted(),
							new ModelRevision(mr.revision(), mr.modelExists(), mr.revision()));
				}
				this.instanceRevInfoManager.getInstanceRevisionInfo()
						.setCurrentGaeModelRevIfRevisionIsHigher(candidate.gaeModelRev);
				log.debug("Updated rev to " + candidate.gaeModelRev);
				return candidate;
			}
		}
		// default:
		XyAssert.xyAssert(candidate.finalModelRev == false);
		return candidate;
	}

	/**
	 * @param startRevInclusive
	 * @param endRevInclusive
	 * @return a set of all revisions in the given interval that are not in the
	 *         local committed-events-cache
	 */
	private Set<Long> computeLocallyMissingRevs(long startRevInclusive, long endRevInclusive) {
		log.debug("computeLocallyMissingRevs [" + startRevInclusive + "," + endRevInclusive + "]");
		Set<Long> locallyMissingRevs = new HashSet<Long>();
		for (long i = startRevInclusive; i <= endRevInclusive; i++) {
			// add key only if result not known locally yet
			GaeChange change = this.cachedChanges.getCachedChange(i);
			if (change == null) {
				locallyMissingRevs.add(i);
			} else {
				XyAssert.xyAssert(change.rev == i);
				// re-request all pending changes
				if (!change.getStatus().isCommitted()) {
					locallyMissingRevs.add(i);
				}
			}
		}
		return locallyMissingRevs;
	}

	/**
	 * Called via fetchMissingRevisionsFromMemcacheAndDatastore from:
	 * calculateCurrentModelRevision, getEventsBetween
	 * 
	 * Datastore also returns uncommitted changes which we also cache in order
	 * to be able to compute tentative versions
	 * 
	 * @param locallyMissingRevs
	 *            never empty @NeverNull
	 * @return timepoint in nanoseconds when query to datastore was issued
	 */
	private long fetchMissingRevisionsFromDatastore(@NeverNull Set<Long> locallyMissingRevs) {
		XyAssert.xyAssert(locallyMissingRevs != null);
		assert locallyMissingRevs != null;
		XyAssert.xyAssert(!locallyMissingRevs.isEmpty());

		// prepare batch request
		List<SKey> datastoreBatchRequest = new ArrayList<SKey>(locallyMissingRevs.size());
		for (Long l : locallyMissingRevs) {
			SKey key = KeyStructure.createChangeKey(getModelAddress(), l);
			datastoreBatchRequest.add(key);
		}
		// execute batch request
		long queryTime = System.nanoTime();
		Map<SKey, SEntity> datastoreResult = XGae.get().datastore().sync()
				.getEntities(datastoreBatchRequest);
		// only used if USE_MEMCACHE_FOR_CHANGES
		Map<String, SEntity> memcacheBatchPut = new HashMap<String, SEntity>();
		long newLastTaken = -1;
		for (Entry<SKey, SEntity> entry : datastoreResult.entrySet()) {
			SKey key = entry.getKey();
			SEntity entity = entry.getValue();
			XyAssert.xyAssert(entity != null);
			XyAssert.xyAssert(entity != Memcache.NULL_ENTITY);
			long revFromKey = KeyStructure.getRevisionFromChangeKey(key);

			// process status of change
			GaeChange change = new GaeChange(getModelAddress(), revFromKey, entity);
			Status status = change.getStatus();

			if (status.canChange()) {
				try {
					progressChange(change);
				} catch (CapabilityDisabledException err) {
					log.warn("Could not progress change", err);
				}
				if (change.getStatus() != Status.FailedTimeout) {
					log.debug("Change " + change.rev
							+ " is being worked on by another 'thread', left untouched");
				}
				if (change.rev > newLastTaken) {
					newLastTaken = change.rev;
				}
			}

			/*
			 * process changes (which might just have been committed by
			 * progressChange
			 */
			if (status.isCommitted()) {
				log.debug("Found in datastore, comitted " + change.rev);
				memcacheBatchPut.put(KeyStructure.toString(key), entity);
				locallyMissingRevs.remove(revFromKey);
			}
			cacheChange(change);
			log.trace("Got change from DS " + change.getStatus() + " timeout?"
					+ change.isTimedOut() + ". Dump: " + change + " ||| Now = "
					+ System.currentTimeMillis()
					+ " DATA:changesMethod=fetchMissingRevisionsFromDatastore" + // .
					"&i_addr=" + this.getModelAddress() + // .
					"&rev=" + change.rev + // .
					"&instance=" + XGae.get().getInstanceId() + // .
					"&status=" + change.getStatus());

		}
		if (newLastTaken >= 0) {
			this.instanceRevInfoManager.getInstanceRevisionInfo()
					.setLastTakenIfHigher(newLastTaken);
		}

		// update memcache IMPROVE do this async via newer GAE API
		if (USE_MEMCACHE_FOR_CHANGES) {
			XGae.get().memcache().putAll(memcacheBatchPut);
		}

		return queryTime;
	}

	/**
	 * Called via fetchMissingRevisionsFromMemcacheAndDatastore from:
	 * calculateCurrentModelRevision, getEventsBetween
	 * 
	 * As our local cache of {@link CommitedChanges} cannot contain the info we
	 * see here and the info here must be more recent, it advances potentially
	 * our knowledge about the highest revision.
	 * 
	 * @param locallyMissingRevs
	 *            never empty @NeverNull
	 */
	private void fetchMissingRevisionsFromMemcache(@NeverNull Set<Long> locallyMissingRevs) {
		XyAssert.xyAssert(locallyMissingRevs != null);
		assert locallyMissingRevs != null;
		XyAssert.xyAssert(!locallyMissingRevs.isEmpty());

		// prepare batch request: Which keys to look-up?
		List<String> memcacheBatchRequest = new ArrayList<String>(locallyMissingRevs.size());
		for (long askRev : locallyMissingRevs) {
			SKey key = KeyStructure.createChangeKey(getModelAddress(), askRev);
			memcacheBatchRequest.add(KeyStructure.toString(key));
		}
		// run batch request
		Map<String, Object> memcacheResult = Memcache.getEntities(memcacheBatchRequest);
		for (Entry<String, Object> entry : memcacheResult.entrySet()) {
			SKey key = KeyStructure.toKey(entry.getKey());
			Object v = entry.getValue();
			XyAssert.xyAssert(v != null, "v!=null");
			assert v != null;
			assert v instanceof SEntity : v.getClass();
			SEntity entity = (SEntity) v;
			XyAssert.xyAssert(!entity.equals(Memcache.NULL_ENTITY), "" + key);
			long rev = KeyStructure.getRevisionFromChangeKey(key);
			GaeChange change = new GaeChange(getModelAddress(), rev, entity);
			XyAssert.xyAssert(change.getStatus() != null);
			XyAssert.xyAssert(change.getStatus().isCommitted(),
					change.rev + " " + change.getStatus());
			// we cache only committed changes in memcache
			cacheCommittedChange(change);
			locallyMissingRevs.remove(change.rev);
			log.trace("Found in memcache " + change.rev);
		}
	}

	/**
	 * Called from: calculateCurrentModelRevision, getEventsBetween
	 * 
	 * Fetch all given revisions from memcache and those not found there from
	 * datastore. New revisions are added to {@link CommitedChanges}.
	 * 
	 * @param locallyMissingRevs
	 *            @NeverNull Caller is responsible not to ask for revisions
	 *            already known locally.
	 *            <em>Removes all revisions that have been
	 *            found from this set.</em>
	 * @return timepoint of datastore fetch in nanoseconds
	 */
	private long fetchMissingRevisionsFromMemcacheAndDatastore(
			@NeverNull Set<Long> locallyMissingRevs) {
		XyAssert.xyAssert(locallyMissingRevs != null);
		assert locallyMissingRevs != null;
		if (locallyMissingRevs.isEmpty()) {
			log.debug("No revisions are missing, nothing to fetch from memcache/datastore");
			return -1;
		}
		if (USE_MEMCACHE_FOR_CHANGES) {
			fetchMissingRevisionsFromMemcache(locallyMissingRevs);
		}
		return fetchMissingRevisionsFromDatastore(locallyMissingRevs);
	}

	// FIXME getChange
	@Override
	public GaeChange getChange(long rev) {

		GaeChange change = this.cachedChanges.getCachedChange(rev);
		if (change != null) {
			return change;
		}

		SKey key = KeyStructure.createChangeKey(this.modelAddr, rev);
		SEntity entityFromGae = XGae.get().datastore().sync().getEntity(key);
		if (entityFromGae == null) {
			return null;
		}
		change = new GaeChange(this.modelAddr, rev, entityFromGae);
		cacheChange(change);
		return change;
	}

	// FIXME getEventsBetween
	@Override
	/* TODO think if incrementing the currentRev is possible and saves anything */
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
		log.debug("getEventsBetween [" + beginRevision + "," + endRevision + "] @"
				+ getModelAddress());
		/* sanity checks */
		if (beginRevision < 0) {
			throw new IndexOutOfBoundsException(
					"beginRevision is not a valid revision number, was " + beginRevision);
		}
		if (endRevision < 0) {
			throw new IndexOutOfBoundsException("endRevision is not a valid revision number, was "
					+ endRevision);
		}
		if (beginRevision > endRevision) {
			throw new IllegalArgumentException("beginRevision may not be greater than endRevision");
		}

		/* adjust range */
		long endRev = endRevision;
		/*
		 * ask one revision below requested to see the last
		 * repocommand.removeModel if there was one
		 */
		long begin = beginRevision < 0 ? 0 : beginRevision;
		// FIXME concept: better use real current number here?
		long currentRev = this.instanceRevInfoManager.getInstanceRevisionInfo()
				.getGaeModelRevision().getModelRevision().revision();
		if (currentRev == -1) {
			log.info("Current rev==-1, return null from " + currentRev);
			return null;
		}
		// Don't try to get more events than there actually are.
		if (beginRevision > currentRev) {
			return new ArrayList<XEvent>(0);
		} else if (endRev > currentRev) {
			endRev = currentRev;
		}

		log.debug("Adjusted range [" + begin + "," + endRev + "]");

		List<XEvent> events = new ArrayList<XEvent>();

		Set<Long> locallyMissingRevs = computeLocallyMissingRevs(begin, endRev);
		/* Ask Memcache + Datastore */
		fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
		// construct result
		for (long i = begin; i <= endRev; i++) {
			log.debug("Trying to find & apply event " + i);
			GaeChange change = this.cachedChanges.getCachedChange(i);
			// use only positive information
			if (change != null) {
				XyAssert.xyAssert(!change.getStatus().canChange(), change.getStatus());
				if (change.getStatus().changedSomething()) {
					log.debug("Change " + i + " rev=" + change.rev + " is successful");
					XEvent event = change.getEvent();
					XyAssert.xyAssert(event != null, change);
					events.add(event);
				} else {
					XyAssert.xyAssert(change.getStatus().canChange());
					log.debug("Change " + i + " is " + change.getStatus().name());
				}
			} else {
				log.warn("==== Change "
						+ i
						+ " is null, was asking ["
						+ begin
						+ ","
						+ endRev
						+ "]. Retry. Current rev = "
						+ this.instanceRevInfoManager.getInstanceRevisionInfo()
								.getGaeModelRevision().getModelRevision());
				throw new RuntimeException("Encountered null-change at " + i);

				// // FIXME RECHECK
				// Set<Long> set = new HashSet<Long>();
				// set.add(i);
				// fetchMissingRevisionsFromMemcacheAndDatastore(set);
				// Thread.yield();
				// i--;
				// continue;
				//
				// //
				// // throw new IllegalStateException("Change " + i +
				// // " null was asking [" + begin + ","
				// // + endRev + "]");
			}
		}
		XyAssert.xyAssert(eventsAreWithinRange(events, begin, endRev));

		/*
		 * TODO(Complete Impl) filter events (objectevents, fieldevents) if
		 * address is not a model address?
		 */

		return events;
	}

	// FIXME getValue
	@Override
	public AsyncValue getValue(long rev, int transindex) {

		GaeChange change = this.cachedChanges.getCachedChange(rev);
		if (change != null) {
			int realindex = GaeEvents.getEventIndex(transindex);
			if (realindex >= 0) {
				XEvent event = change.getEvent();
				if (event instanceof XTransactionEvent) {
					XyAssert.xyAssert(((XTransactionEvent) event).size() > realindex);
					event = ((XTransactionEvent) event).getEvent(realindex);
				} else {
					XyAssert.xyAssert(realindex == 0);
				}
				XyAssert.xyAssert(event instanceof XFieldEvent);
				return new AsyncValue(((XFieldEvent) event).getNewValue());
			}
		}

		return GaeEvents.getValue(this.modelAddr, rev, transindex);
	}

	// FIXME grabRevisionAndRegisterLocks
	@Override
	@XGaeOperation(memcacheRead = true, datastoreRead = true, datastoreWrite = true, memcacheWrite = true)
	public GaeChange grabRevisionAndRegisterLocks(long lastTaken, GaeLocks locks, XId actorId) {
		XyAssert.xyAssert(lastTaken >= -1);
		long start = lastTaken + 1;
		for (long rev = start;; rev++) {

			GaeChange cachedChange = this.cachedChanges.getCachedChange(rev);
			if (cachedChange != null) {
				// Revision already taken for sure
				continue;
			}

			// Try to grab this revision.
			SKey key = KeyStructure.createChangeKey(this.modelAddr, rev);
			/* use txn to do: avoid overwriting existing change entities */
			STransaction trans = XGae.get().datastore().sync().beginTransaction();

			SEntity changeEntity = XGae.get().datastore().sync().getEntity(key, trans);

			if (changeEntity == null) {

				GaeChange newChange = new GaeChange(this.modelAddr, rev, locks, actorId);
				newChange.save(trans);

				try {
					XGae.get().datastore().sync().endTransaction(trans);
				} catch (ConcurrentModificationException cme) {
					/*
					 * One cause: 'too much contention on these datastore
					 * entities. please try again.'
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
				} catch (DatastoreTimeoutException dte) {
					log.warn("DatastoreTimeout");
					log.info("failed to take revision: " + key, dte);

					// try this revision again
					rev--;
					continue;
				} catch (DatastoreFailureException dfe) {
					/*
					 * Some forums report this happens for read-only entities
					 * that got stuck in a wrong state after scheduled
					 * maintenance
					 */
					log.warn("DatastoreFailureException on " + key);
					log.info("failed to take revision: " + key, dfe);

					// try this revision again FIXME might be an endless loop!
					rev--;
					continue;
				} catch (CommittedButStillApplyingException csa) {
					log.warn("CommittedButStillApplyingException on " + key);
					/* We believe the commit worked */
					continue;
				}

				this.instanceRevInfoManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);

				// transaction succeeded, we have a revision

				// TODO @Daniel: Why do we do this?
				// progress current version
				computeCurrenRevisionFromLocalChanges(start, rev,
						new CandidateRev(
								new GaeModelRevision(rev, this.instanceRevInfoManager
										.getInstanceRevisionInfo().getGaeModelRevision()
										.getModelRevision())), false);
				return newChange;

			} else {
				// Revision already taken.

				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				XGae.get().datastore().sync().endTransaction(trans);
				this.instanceRevInfoManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);

				// Since we read the entity anyway, might as well use that
				// information.
				Status status = change.getStatus();
				if (!status.isCommitted()) {
					progressChange(change);
				}
				cacheChange(change);
			}
		}

		// unreachable
	}

	@Override
	public boolean modelHasBeenManaged() {
		GaeChange change = getChange(0);
		return change != null;
	}

	/**
	 * Check is change is timed-out and then move to status to FailedTimeout
	 * 
	 * @param change
	 */
	private void progressChange(GaeChange change) {
		log.debug("Progressing change " + change);
		if (change.isTimedOut()) {
			log.debug("handleTimeout: " + change);
			commit(change, Status.FailedTimeout);
		}
	}

	/* Called also from GaeExecutionService */
	@Override
	public void cacheCommittedChange(GaeChange change) {
		XyAssert.xyAssert(change.getStatus().isCommitted());
		this.cachedChanges.cacheCommittedChange(change);
		this.instanceRevInfoManager.getInstanceRevisionInfo().setLastCommittedIfHigher(change.rev);
	}

	private void cacheChange(GaeChange change) {
		if (change.getStatus().isCommitted()) {
			cacheCommittedChange(change);
		} else {
			this.cachedChanges.cacheCommittedChange(change);
		}
	}
}

// /**
// * A new last committed change has been found - update revision caches.
// */
// private void newCurrentRev(GaeChange change) {
//
// log.debug("(r" + change.rev + ") {" +
// this.revManager.getRevisionState().revision() + "/"
// + this.revManager.getLastCommited() + "} new current rev");
//
// assert change.getStatus().hasEvents();
// assert change.getStatus().isSuccess();
//
// XEvent event = change.getEvent();
// if(event instanceof XTransactionEvent) {
// XTransactionEvent trans = (XTransactionEvent)event;
// event = trans.getEvent(trans.size() - 1);
// }
// assert !event.isImplied();
//
// boolean modelExists = true;
// if(event instanceof XRepositoryEvent) {
// modelExists = (event.getChangeType() != ChangeType.REMOVE);
// }
//
// synchronized(this.revManager) {
// this.revManager.setBothCurrentModelRev(new ModelRevision(change.rev,
// modelExists));
// }
//
// }

// /**
// * @param change
// */
// @SuppressWarnings("unused")
// private void updateCachedRevisions(GaeChange change) {
//
// assert change.getStatus().isCommitted();
//
// ModelRevision state = this.revManager.getInstanceRevisionState();
//
// if(change.rev == this.revManager.getLastCommited() + 1) {
// long newLastCommittedRev = change.rev;
// Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
// GaeChange newCurrentChange = change.getStatus().hasEvents() ? change :
// null;
// synchronized(committedChangeCache) {
// GaeChange otherChange;
// while((otherChange = committedChangeCache.get(newLastCommittedRev + 1))
// != null) {
// newLastCommittedRev++;
// assert otherChange.rev == newLastCommittedRev;
// if(otherChange.getStatus().hasEvents()) {
// newCurrentChange = otherChange;
// }
// }
// }
//
// log.debug("(r" + change.rev + ") {"
// + this.revManager.getInstanceRevisionState().revision() + "/"
// + this.revManager.getLastCommited() + "} new last committed rev "
// + newLastCommittedRev);
// this.revManager.setLastCommited(newLastCommittedRev);
// if(state != null && newCurrentChange != null && newCurrentChange.rev >
// state.revision()) {
// newCurrentRev(newCurrentChange);
// }
//
// } else if(state != null && change.getStatus().hasEvents()
// && change.rev <= this.revManager.getLastCommited() && change.rev >
// state.revision()) {
// newCurrentRev(change);
// }
//
// }

package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceRevisionManager;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


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
 * @author voelkel
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
	
	/**
	 * Interval sizes for fetches to back-end when looking for current rev. Last
	 * value is used for all following rounds as well.
	 */
	// IMPROVE verify that 128 is not too large
	private static final int[] WINDOW_SIZES = { 1, 8, 32, 128 };
	
	@Override
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	/**
	 * @param e atomic or transaction event, never null
	 * @return true if model must exist after this event
	 */
	private static boolean eventIndicatesModelExists(final XEvent e) {
		assert e != null;
		XEvent event = e;
		if(event.getChangeType() == ChangeType.TRANSACTION) {
			// check only last event
			XTransactionEvent txnEvent = (XTransactionEvent)event;
			assert txnEvent.size() >= 1;
			event = txnEvent.getEvent(txnEvent.size() - 1);
			assert event != null;
		}
		GaeAssert.gaeAssert(event.getChangeType() != ChangeType.TRANSACTION);
		if(event.getTarget().getAddressedType() == XType.XREPOSITORY) {
			if(event.getChangeType() == ChangeType.REMOVE) {
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
		for(XEvent e : events) {
			GaeAssert.gaeAssert(e.getRevisionNumber() >= begin);
			GaeAssert.gaeAssert(e.getRevisionNumber() <= endRev);
		}
		return true;
	}
	
	/**
	 * @param round
	 * @return the defined window size
	 */
	private static int windowsSizeForRound(int round) {
		assert round >= 0;
		assert WINDOW_SIZES.length > 0;
		if(round < WINDOW_SIZES.length) {
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
	
	private CommitedChanges commitedChanges;
	
	private String revisionCacheName;
	
	@GaeOperation()
	public GaeChangesServiceImpl3(XAddress modelAddr, InstanceRevisionManager revisionManager) {
		this.modelAddr = modelAddr;
		this.instanceRevInfoManager = revisionManager;
		this.commitedChanges = new CommitedChanges(modelAddr);
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
	public GaeModelRevision calculateCurrentModelRevision() {
		/* === Using data from our instance === */
		GaeModelRevision lastCurrentRev = this.instanceRevInfoManager.getInstanceRevisionInfo()
		        .getGaeModelRevision();
		assert lastCurrentRev.getModelRevision() != null;
		log.info("Update currentRev from lastCurrentRev=" + lastCurrentRev);
		/*
		 * After this method it might turn out, that 'current' is in fact the
		 * current revision.
		 */
		CandidateRev candidate = new CandidateRev(lastCurrentRev);
		assert candidate.isFinalModelRev() == false : "we cannot know this yet";
		
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
		while(true) {
			if(!askedMemcacheOrDatastore && eventsLoadedBeforeRevInfoLookupInBackend > 0) {
				log.info("Asking cache after " + eventsLoadedBeforeRevInfoLookupInBackend
				        + " attempts");
				
				RevisionInfo revInfoFromMemcacheOrDatastore = this.revInfoMemcacheAndDatastoreCache
				        .get(this.revisionCacheName, StorageOptions.create(
				        /** We looked there already via instanceRevInfoManager */
				        false, USE_MEMCACHE_FOR_REVISIONS, true));
				if(revInfoFromMemcacheOrDatastore != null) {
					this.revInfoFromMemcacheAndDatastore = revInfoFromMemcacheOrDatastore;
					ModelRevision cachedModelRev = revInfoFromMemcacheOrDatastore
					        .getGaeModelRevision().getModelRevision();
					if(cachedModelRev != null
					        && cachedModelRev.revision() > candidate.gaeModelRev.getModelRevision()
					                .revision()) {
						candidate.gaeModelRev = revInfoFromMemcacheOrDatastore
						        .getGaeModelRevision();
						assert revInfoFromMemcacheOrDatastore.getLastCommitted() > lastCheckedRev;
						lastCheckedRev = revInfoFromMemcacheOrDatastore.getLastCommitted();
						assert lastCheckedRev >= candidate.gaeModelRev.getModelRevision()
						        .revision();
						round = 0;
						log.debug("Cached value is a better start than what we had. Now using "
						        + candidate.gaeModelRev);
					}
				}
				askedMemcacheOrDatastore = true;
			}
			
			int windowSize = windowsSizeForRound(round);
			log.debug("Windowsize = " + windowSize);
			long beginRevInclusive = lastCheckedRev + 1;
			/*
			 * the highest revision number (inclusive) to check for in this
			 * revision update step
			 */
			long endRevInclusive = beginRevInclusive + windowSize - 1;
			log.info(this.modelAddr + ":: Update rev step [" + beginRevInclusive + ","
			        + endRevInclusive + "]");
			
			/**
			 * Try to fetch 'windowSize' changes in batch from memcache and
			 * datastore past the last known "current" revision and put them in
			 * the CommitedChanges
			 */
			log.info("=== Phase 1: Determine revisions not yet locally cached; windowsize = "
			        + windowSize);
			Set<Long> locallyMissingRevs = computeLocallyMissingRevs(beginRevInclusive,
			        endRevInclusive);
			int missingRevs = locallyMissingRevs.size();
			log.trace("locallyMissingRevs: " + missingRevs + " in this window of "
			        + (endRevInclusive - beginRevInclusive + 1) + " revs in total");
			
			log.info("=== Phase 2+3: Ask Memcache + Datastore ===");
			fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
			int fetchedRevs = missingRevs - locallyMissingRevs.size();
			log.trace("Number of missingRevs after asking DS&MC: " + locallyMissingRevs.size());
			
			log.info("=== Phase 4: Compute result from local cache ===");
			candidate = computeCurrenRevisionFromLocalCommittedChanges(beginRevInclusive,
			        endRevInclusive, candidate);
			if(candidate.finalModelRev) {
				long rev = candidate.gaeModelRev.getModelRevision().revision();
				log.info("Computed rev of " + this.modelAddr + " = " + rev
				        + " DATA?i_type=rev&i_addr=" + this.modelAddr + "&rev=" + rev
				        + "&instance=" + AboutAppEngine.getInstanceId());
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
		
		assert status.isCommitted();
		assert !change.getStatus().isCommitted();
		
		change.commit(status);
		assert change.getStatus() == status;
		
		if(status == Status.SuccessExecuted) {
			boolean modelExists = eventIndicatesModelExists(change.getEvent());
			// FIXME concept: new revision can be lower, there might be
			// uncommitted
			// intermediary versions
			GaeModelRevision gaeModelRev = new GaeModelRevision(change.rev, new ModelRevision(
			        change.rev, modelExists));
			this.instanceRevInfoManager.getInstanceRevisionInfo()
			        .setCurrentGaeModelRevIfRevisionIsHigher(gaeModelRev);
		}
		cacheCommittedChange(change);
	}
	
	/**
	 * Called from: {@link #calculateCurrentModelRevision()} to determine if a
	 * candidate is a finalRev or not;
	 * {@link #grabRevisionAndRegisterLocks(long, GaeLocks, XID)}
	 * 
	 * @param beginRevInclusive
	 * @param endRevInclusive
	 * @param candidate never null, not final model rev
	 * @return a {@link CandidateRev} that is not necessarily a final model rev.
	 */
	private CandidateRev computeCurrenRevisionFromLocalCommittedChanges(long beginRevInclusive,
	        long endRevInclusive, CandidateRev candidate) {
		assert candidate.isFinalModelRev() == false;
		assert candidate.gaeModelRev.getModelRevision() != null;
		assert endRevInclusive - beginRevInclusive >= 0 : "begin:" + beginRevInclusive + ",end:"
		        + endRevInclusive;
		log.info(this.modelAddr + ":: computeFromCache candidate=" + candidate + " in range ["
		        + beginRevInclusive + "," + endRevInclusive + "]");
		
		for(long i = beginRevInclusive; i <= endRevInclusive; i++) {
			GaeChange change = this.commitedChanges.getCachedChange(i);
			if(change == null) {
				// we just validated a candidate revision
				log.debug("Found end at " + i + " return workingRev=" + candidate);
				candidate.markAsFinalRev();
			} else {
				assert change.getStatus().isCommitted();
				// move candidate revision
				candidate.gaeModelRev.setLastSilentCommittedIfHigher(i);
				/*
				 * Impl note: Statsu.SuccessNoChange is no a candiate rev as
				 * there is no event for that change
				 */
				if(change.getStatus() == Status.SuccessExecuted) {
					// Candidate for new revision is changed
					XEvent event = change.getEvent();
					boolean modelExist = eventIndicatesModelExists(event);
					candidate.setModelRev(new GaeModelRevision(event.getRevisionNumber(),
					        new ModelRevision(i, modelExist)));
					log.debug(this.modelAddr + ":: New currentRev candidate " + candidate);
				}
			}
			
			/* Coordinate with other GAE instances */
			boolean putInMemcache = i % 16 == 4 && USE_MEMCACHE_FOR_REVISIONS;
			boolean putInDatastore = i % 64 == 16;
			if(putInMemcache || putInDatastore) {
				// share findings via memcache & datastore with other instances
				StorageOptions storeOpts = UniCache.StorageOptions.create(false, putInMemcache,
				        putInDatastore);
				// read cache first to prevent destroying good knowledge
				if(this.revInfoFromMemcacheAndDatastore == null) {
					this.revInfoFromMemcacheAndDatastore = this.revInfoMemcacheAndDatastoreCache
					        .get(this.revisionCacheName, storeOpts);
				}
				RevisionInfo toBeCached = new RevisionInfo("toBeCached", candidate.gaeModelRev,
				        this.instanceRevInfoManager.getInstanceRevisionInfo().getLastCommitted(),
				        this.instanceRevInfoManager.getInstanceRevisionInfo().getLastTaken());
				if(toBeCached.isBetterThan(this.revInfoFromMemcacheAndDatastore)) {
					log.debug("this revInfo " + toBeCached + " is better than "
					        + this.revInfoFromMemcacheAndDatastore + " and thus will be cached");
					this.revInfoMemcacheAndDatastoreCache.put(this.revisionCacheName, toBeCached,
					        storeOpts);
				}
			}
			
			// are we done?
			if(candidate.finalModelRev) {
				this.instanceRevInfoManager.getInstanceRevisionInfo()
				        .setCurrentGaeModelRevIfRevisionIsHigher(candidate.gaeModelRev);
				log.debug("Updated rev to " + candidate.gaeModelRev);
				return candidate;
			}
		}
		// default:
		assert candidate.finalModelRev == false;
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
		for(long i = startRevInclusive; i <= endRevInclusive; i++) {
			// add key only if result not known locally yet
			if(!this.commitedChanges.hasCachedChange(i)) {
				locallyMissingRevs.add(i);
			} else {
				assert this.commitedChanges.getCachedChange(i) != null
				        && this.commitedChanges.getCachedChange(i).rev == i;
			}
		}
		return locallyMissingRevs;
	}
	
	/**
	 * Called via fetchMissingRevisionsFromMemcacheAndDatastore from:
	 * calculateCurrentModelRevision, getEventsBetween
	 * 
	 * @param locallyMissingRevs never null; never empty
	 */
	private void fetchMissingRevisionsFromDatastore(Set<Long> locallyMissingRevs) {
		assert locallyMissingRevs != null;
		assert !locallyMissingRevs.isEmpty();
		
		// prepare batch request
		List<Key> datastoreBatchRequest = new ArrayList<Key>(locallyMissingRevs.size());
		for(Long l : locallyMissingRevs) {
			Key key = KeyStructure.createChangeKey(getModelAddress(), l);
			datastoreBatchRequest.add(key);
		}
		// execute batch request
		Map<Key,Entity> datastoreResult = SyncDatastore.getEntities(datastoreBatchRequest);
		// only used if USE_MEMCACHE_FOR_CHANGES
		Map<String,Entity> memcacheBatchPut = new HashMap<String,Entity>();
		long newLastTaken = -1;
		for(Entry<Key,Entity> entry : datastoreResult.entrySet()) {
			Key key = entry.getKey();
			Entity entity = entry.getValue();
			assert entity != null;
			assert entity != Memcache.NULL_ENTITY;
			long revFromKey = KeyStructure.getRevisionFromChangeKey(key);
			
			// process status of change
			GaeChange change = new GaeChange(getModelAddress(), revFromKey, entity);
			Status status = change.getStatus();
			
			if(!status.isCommitted()) {
				// TODO 2012-05 remove warning if it appears not so often
				log.warn("Change is " + change.getStatus() + " timeout?" + change.isTimedOut()
				        + ". Dump: " + change + " ||| Now = " + System.currentTimeMillis()
				        + " DATA?changesMethod=fetchMissingRevisionsFromDatastore&model="
				        + this.getModelAddress() + "&rev=" + change.rev);
				assert status == Status.Creating;
				progressChange(change);
				if(change.getStatus() != Status.FailedTimeout) {
					log.warn("Change " + change.rev
					        + " is being worked on by another 'thread', left untouched");
				}
				//
				if(change.rev > newLastTaken) {
					newLastTaken = change.rev;
				}
			}
			
			// save & cache committed changes (which might just have been
			// committed by progressChange
			if(status.isCommitted()) {
				log.debug("Found in datastore, comitted " + change.rev);
				memcacheBatchPut.put(KeyStructure.toString(key), entity);
				cacheCommittedChange(change);
				locallyMissingRevs.remove(revFromKey);
			}
			
		}
		if(newLastTaken >= 0) {
			this.instanceRevInfoManager.getInstanceRevisionInfo()
			        .setLastTakenIfHigher(newLastTaken);
		}
		
		// update memcache IMPROVE do this async via newer GAE API
		if(USE_MEMCACHE_FOR_CHANGES) {
			XydraRuntime.getMemcache().putAll(memcacheBatchPut);
		}
	}
	
	/**
	 * Called via fetchMissingRevisionsFromMemcacheAndDatastore from:
	 * calculateCurrentModelRevision, getEventsBetween
	 * 
	 * As our local cache of {@link CommitedChanges} cannot contain the info we
	 * see here and the info here must be more recent, it advances potentially
	 * our knowledge about the highest revision.
	 * 
	 * @param locallyMissingRevs never null, never empty
	 */
	private void fetchMissingRevisionsFromMemcache(Set<Long> locallyMissingRevs) {
		assert locallyMissingRevs != null;
		assert !locallyMissingRevs.isEmpty();
		
		// prepare batch request: Which keys to look-up?
		List<String> memcacheBatchRequest = new ArrayList<String>(locallyMissingRevs.size());
		for(long askRev : locallyMissingRevs) {
			Key key = KeyStructure.createChangeKey(getModelAddress(), askRev);
			memcacheBatchRequest.add(KeyStructure.toString(key));
		}
		// run batch request
		Map<String,Object> memcacheResult = Memcache.getEntities(memcacheBatchRequest);
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
	 * @param locallyMissingRevs Never null. Caller is responsible not to ask
	 *            for revisions already known locally.
	 *            <em>Removes all revisions that have been
	 *            found from this set.</em>
	 */
	private void fetchMissingRevisionsFromMemcacheAndDatastore(Set<Long> locallyMissingRevs) {
		assert locallyMissingRevs != null;
		if(locallyMissingRevs.isEmpty()) {
			log.debug("No revisions are missing, nothing to fetch from memcache/datastore");
			return;
		}
		if(USE_MEMCACHE_FOR_CHANGES) {
			fetchMissingRevisionsFromMemcache(locallyMissingRevs);
		}
		fetchMissingRevisionsFromDatastore(locallyMissingRevs);
	}
	
	// FIXME getChange
	@Override
	public GaeChange getChange(long rev) {
		
		GaeChange change = this.commitedChanges.getCachedChange(rev);
		if(change != null) {
			return change;
		}
		
		Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
		Entity entityFromGae = SyncDatastore.getEntity(key);
		if(entityFromGae == null) {
			return null;
		}
		change = new GaeChange(this.modelAddr, rev, entityFromGae);
		
		// Cache the change if it is committed.
		if(change.getStatus().isCommitted()) {
			cacheCommittedChange(change);
		}
		
		return change;
	}
	
	// FIXME getEventsBetween
	@Override
	/* TODO think if incrementing the currentRev is possible and saves anything */
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
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
		if(currentRev == -1) {
			log.info("Current rev==-1, return null from " + currentRev);
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
		for(long i = begin; i <= endRev; i++) {
			log.debug("Trying to find & apply event " + i);
			GaeChange change = this.commitedChanges.getCachedChange(i);
			// use only positive information
			if(change != null) {
				if(change.getStatus() == Status.SuccessExecuted) {
					log.debug("Change " + i + " rev=" + change.rev + " is successful");
					XEvent event = change.getEvent();
					assert event != null : change;
					events.add(event);
				} else {
					assert change.getStatus() != Status.Creating;
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
		GaeAssert.gaeAssert(eventsAreWithinRange(events, begin, endRev));
		
		/*
		 * TODO(Complete Impl) filter events (objectevents, fieldevents) if
		 * address is not a model address?
		 */
		
		return events;
	}
	
	// FIXME getValue
	@Override
	public AsyncValue getValue(long rev, int transindex) {
		
		GaeChange change = this.commitedChanges.getCachedChange(rev);
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
	
	// FIXME grabRevisionAndRegisterLocks
	@Override
	@GaeOperation(memcacheRead = true ,datastoreRead = true ,datastoreWrite = true ,memcacheWrite = true)
	public GaeChange grabRevisionAndRegisterLocks(long lastTaken, GaeLocks locks, XID actorId) {
		assert lastTaken >= -1;
		long start = lastTaken + 1;
		for(long rev = start;; rev++) {
			
			GaeChange cachedChange = this.commitedChanges.getCachedChange(rev);
			if(cachedChange != null) {
				// Revision already taken for sure
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
				
				this.instanceRevInfoManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);
				
				// transaction succeeded, we have a revision
				// progress current version
				computeCurrenRevisionFromLocalCommittedChanges(start, rev,
				        new CandidateRev(
				                new GaeModelRevision(rev, this.instanceRevInfoManager
				                        .getInstanceRevisionInfo().getGaeModelRevision()
				                        .getModelRevision())));
				return newChange;
				
			} else {
				// Revision already taken.
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
				this.instanceRevInfoManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);
				
				// Since we read the entity anyway, might as well use that
				// information.
				Status status = change.getStatus();
				if(status.isCommitted()) {
					cacheCommittedChange(change);
				} else {
					progressChange(change);
				}
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
		if(change.isTimedOut()) {
			log.debug("handleTimeout: " + change);
			commit(change, Status.FailedTimeout);
		}
	}
	
	/* Called also from GaeExecutionService */
	@Override
	public void cacheCommittedChange(GaeChange change) {
		assert change.getStatus().isCommitted();
		this.commitedChanges.cacheCommittedChange(change);
		this.instanceRevInfoManager.getInstanceRevisionInfo().setLastCommittedIfHigher(change.rev);
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

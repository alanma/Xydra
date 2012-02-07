package org.xydra.store.impl.gae.changes;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.RevisionManager;
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
 * 
 */
public class GaeChangesServiceImpl3 implements IGaeChangesService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesServiceImpl3.class);
	
	private static final long serialVersionUID = -2080744796962188941L;
	
	// Implementation.
	
	private final XAddress modelAddr;
	private final RevisionManager revManager;
	
	private UniCache<RevisionInfo> unicache;
	
	private RevisionInfo backendCached = null;
	
	@GaeOperation()
	public GaeChangesServiceImpl3(XAddress modelAddr, RevisionManager revisionManager) {
		this.modelAddr = modelAddr;
		this.revManager = revisionManager;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see IGaeChangesService#grabRevisionAndRegisterLocks(GaeLocks, XID)
	 */
	@Override
	@GaeOperation(memcacheRead = true ,datastoreRead = true ,datastoreWrite = true ,memcacheWrite = true)
	public GaeChange grabRevisionAndRegisterLocks(long lastTaken, GaeLocks locks, XID actorId) {
		assert lastTaken >= -1;
		long start = lastTaken + 1;
		for(long rev = start;; rev++) {
			
			GaeChange cachedChange = getCachedChange(rev);
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
				
				this.revManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);
				
				// transaction succeeded, we have a revision
				// progress current version
				computeCurrenRevisionFromLocalCache(start, rev, new CandidateRev(
				        new GaeModelRevision(rev, this.revManager.getInstanceRevisionInfo()
				                .getGaeModelRevision().getModelRevision())));
				
				return newChange;
				
			} else {
				// Revision already taken.
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
				this.revManager.getInstanceRevisionInfo().setLastTakenIfHigher(rev);
				
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
			this.revManager.getInstanceRevisionInfo().setCurrentGaeModelRevIfRevisionIsHigher(
			        gaeModelRev);
			// invalidate thread local cache
			this.revManager.resetThreadLocalRevisionNumber();
		}
		cacheCommittedChange(change);
	}
	
	private static final boolean USE_COMMITTED_CHANGE_CACHE = true;
	
	private static final String VM_COMMITED_CHANGES_CACHENAME = "[.c2]";
	
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
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached
	 */
	@Override
	public void cacheCommittedChange(GaeChange change) {
		assert change != null;
		if(USE_COMMITTED_CHANGE_CACHE) {
			assert change != null;
			assert change.getStatus() != null;
			assert change.getStatus().isCommitted();
			log.trace(DebugFormatter.dataPut(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
			        + change.rev, change, Timing.Now));
			Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
			synchronized(committedChangeCache) {
				committedChangeCache.put(change.rev, change);
			}
		}
		
		this.revManager.getInstanceRevisionInfo().setLastCommittedIfHigher(change.rev);
	}
	
	private boolean hasCachedChange(long rev) {
		Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
		synchronized(committedChangeCache) {
			return committedChangeCache.containsKey(rev);
		}
	}
	
	private GaeChange getCachedChange(long rev) {
		if(!USE_COMMITTED_CHANGE_CACHE) {
			return null;
		}
		GaeChange change;
		Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
		synchronized(committedChangeCache) {
			change = committedChangeCache.get(rev);
		}
		// if(change != null) {
		// /*
		// * TODO ???? @Daniel: Why update again at read-time. At write-time
		// * should suffice, shouldn't it?
		// */
		// updateCachedRevisions(change);
		// assert change.getStatus().isCommitted();
		// }
		log.trace(DebugFormatter.dataGet(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, "" + rev,
		        change, Timing.Now));
		return change;
	}
	
	@Override
	public GaeChange getChange(long rev) {
		
		GaeChange change = getCachedChange(rev);
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getModelAddress()
	 */
	@Override
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	private static final String KEY_CACHE_REVINFO = "revInfo";
	
	private static final int[] WINDOW_SIZES = { 1, 8, 32, 128 };
	
	private static final boolean MEMCACHE_CHANGES = false;
	
	private String getRevisionCacheName() {
		return KEY_CACHE_REVINFO + this.modelAddr;
	}
	
	/**
	 * @param change
	 */
	private void progressChange(GaeChange change) {
		log.debug("Progressing change " + change);
		if(change.isTimedOut()) {
			log.debug("handleTimeout: " + change);
			commit(change, Status.FailedTimeout);
		}
	}
	
	private int windowsSizeForRound(int round) {
		assert round >= 0;
		assert WINDOW_SIZES.length > 0;
		if(round < WINDOW_SIZES.length) {
			return WINDOW_SIZES[round];
		} else {
			return WINDOW_SIZES[WINDOW_SIZES.length - 1];
		}
	}
	
	/**
	 * Fetch all given revisions from memcache and those not found there from
	 * datastore. New revisions are added to local cache.
	 * 
	 * 
	 * 
	 * @param locallyMissingRevs Caller is responsible not to ask for revisions
	 *            already known locally. Removes all revisions that have been
	 *            found from this set.
	 */
	private void fetchMissingRevisionsFromMemcacheAndDatastore(Set<Long> locallyMissingRevs) {
		if(MEMCACHE_CHANGES) {
			fetchMissingRevisionsFromMemcache(locallyMissingRevs);
		}
		fetchMissingRevisionsFromDatastore(locallyMissingRevs);
	}
	
	private void fetchMissingRevisionsFromDatastore(Set<Long> locallyMissingRevs) {
		/* === Phase 3: Ask datastore === */
		if(!locallyMissingRevs.isEmpty()) {
			// prepare batch request
			List<Key> datastoreBatchRequest = new ArrayList<Key>(locallyMissingRevs.size());
			for(Long l : locallyMissingRevs) {
				Key key = KeyStructure.createChangeKey(getModelAddress(), l);
				datastoreBatchRequest.add(key);
			}
			// execute batch request
			Map<Key,Entity> datastoreResult = SyncDatastore.getEntities(datastoreBatchRequest);
			Map<String,Entity> memcacheBatchPut = new HashMap<String,Entity>();
			long newLastTaken = -1;
			long newLastCommitted = -1;
			for(Entry<Key,Entity> entry : datastoreResult.entrySet()) {
				Key key = entry.getKey();
				Entity entity = entry.getValue();
				assert entity != null;
				assert entity != Memcache.NULL_ENTITY;
				long revFromKey = KeyStructure.getRevisionFromChangeKey(key);
				
				// process status of change
				GaeChange change = new GaeChange(getModelAddress(), revFromKey, entity);
				Status status = change.getStatus();
				if(status.isCommitted()) {
					// use it
					log.debug("Found in datastore, comitted " + change.rev);
					memcacheBatchPut.put(KeyStructure.toString(key), entity);
					cacheCommittedChange(change);
					locallyMissingRevs.remove(revFromKey);
					if(revFromKey > newLastCommitted) {
						newLastCommitted = revFromKey;
					}
				} else {
					// TODO remove warning if it appears not so often
					log.warn("Change is " + change.getStatus() + " timeout?" + change.isTimedOut()
					        + ". Dump: " + change + " ||| Now = " + System.currentTimeMillis());
					assert status == Status.Creating;
					progressChange(change);
					if(change.getStatus() == Status.FailedTimeout) {
						// use it
						memcacheBatchPut.put(KeyStructure.toString(key), entity);
						cacheCommittedChange(change);
						locallyMissingRevs.remove(revFromKey);
					} else {
						log.warn("made no progress on time-out change " + change.rev);
					}
					
					if(change.rev > newLastTaken) {
						newLastTaken = change.rev;
					}
				}
			}
			if(newLastTaken >= 0) {
				this.revManager.getInstanceRevisionInfo().setLastTakenIfHigher(newLastTaken);
			}
			if(newLastCommitted >= 0) {
				this.revManager.getInstanceRevisionInfo()
				        .setLastCommittedIfHigher(newLastCommitted);
			}
			
			// update memcache IMPROVE do this async
			if(MEMCACHE_CHANGES) {
				XydraRuntime.getMemcache().putAll(memcacheBatchPut);
			}
		}
	}
	
	private void fetchMissingRevisionsFromMemcache(Set<Long> locallyMissingRevs) {
		/* === Phase 2: Ask memcache === */
		if(!locallyMissingRevs.isEmpty()) {
			// prepare batch request
			List<String> memcacheBatchRequest = new ArrayList<String>(locallyMissingRevs.size());
			for(long askRev : locallyMissingRevs) {
				Key key = KeyStructure.createChangeKey(getModelAddress(), askRev);
				memcacheBatchRequest.add(KeyStructure.toString(key));
			}
			// batch request
			Map<String,Object> memcacheResult = Memcache.getEntities(memcacheBatchRequest);
			long newLastCommitted = -1;
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
				cacheCommittedChange(change);
				if(change.rev > newLastCommitted) {
					newLastCommitted = change.rev;
				}
				locallyMissingRevs.remove(change.rev);
				log.trace("Found in memcache " + change.rev);
			}
			if(newLastCommitted >= 0) {
				this.revManager.getInstanceRevisionInfo()
				        .setLastCommittedIfHigher(newLastCommitted);
			}
			// re-use strings in memcacheBatchRequest: retain only *still*
			// missing keys (neither locally found, nor in
			// memcache)
			memcacheBatchRequest.removeAll(memcacheResult.keySet());
		} else {
			// log.debug("All found in localVmCache");
		}
	}
	
	/**
	 * @param startRevInclusive
	 * @param endRevInclusive
	 * @return
	 */
	private Set<Long> computeLocallyMissingRevs(long startRevInclusive, long endRevInclusive) {
		log.debug("computeLocallyMissingRevs [" + startRevInclusive + "," + endRevInclusive + "]");
		Set<Long> locallyMissingRevs = new HashSet<Long>();
		for(long i = startRevInclusive; i <= endRevInclusive; i++) {
			// add key only if result not known locally yet
			if(!this.hasCachedChange(i)) {
				locallyMissingRevs.add(i);
			}
			
			// GaeChange change = this.getCachedChange(i);
			// if(change == null) {
			// locallyMissingRevs.add(i);
			// } else {
			// assert change.rev == i;
			// assert change.getStatus().isCommitted();
			// // log.debug("Already locally cached: " +
			// // DebugFormatter.format(change));
			// }
		}
		return locallyMissingRevs;
	}
	
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
		long currentRev = this.revManager.getThreadLocalGaeModelRev().getModelRevision().revision();
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
			GaeChange change = getCachedChange(i);
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
				        + this.revManager.getInstanceRevisionInfo().getGaeModelRevision()
				                .getModelRevision());
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
	
	private boolean eventsAreWithinRange(List<XEvent> events, long begin, long endRev) {
		for(XEvent e : events) {
			GaeAssert.gaeAssert(e.getRevisionNumber() >= begin);
			GaeAssert.gaeAssert(e.getRevisionNumber() <= endRev);
		}
		return true;
	}
	
	@Override
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
	
	/**
	 * @return the instance-level cache of committed change objects
	 */
	@SuppressWarnings("unchecked")
	private Map<Long,GaeChange> getCommittedChangeCache() {
		String key = "changes:" + this.modelAddr;
		Map<String,Object> instanceCache = InstanceContext.getInstanceCache();
		Map<Long,GaeChange> committedChangeCache;
		synchronized(instanceCache) {
			committedChangeCache = (Map<Long,GaeChange>)instanceCache.get(key);
			if(committedChangeCache == null) {
				log.debug(DebugFormatter.init(VM_COMMITED_CHANGES_CACHENAME));
				committedChangeCache = new HashMap<Long,GaeChange>();
				InstanceContext.getInstanceCache().put(key, committedChangeCache);
			}
		}
		return committedChangeCache;
	}
	
	public void clear() {
		log.info("Cleared. Make to sure to also clear memcache.");
		this.getCommittedChangeCache().clear();
		this.revManager.resetThreadLocalRevisionNumber();
		this.revManager.getInstanceRevisionInfo().clear();
	}
	
	/**
	 * @param e atomic or txn event, never null
	 * @return true if model must exist after this event
	 */
	private boolean eventIndicatesModelExists(final XEvent e) {
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
	
	@Override
	public boolean modelHasBeenManaged() {
		GaeChange change = getChange(0);
		return change != null;
	}
	
	private UniCache<RevisionInfo> getRevCache() {
		if(this.unicache == null) {
			this.unicache = new UniCache<RevisionInfo>(uniCacheRevisionInfoEntryHandler);
		}
		return this.unicache;
	}
	
	static final UniCacheRevisionInfoEntryHandler uniCacheRevisionInfoEntryHandler = new UniCacheRevisionInfoEntryHandler();
	
	static class UniCacheRevisionInfoEntryHandler implements
	        UniCache.CacheEntryHandler<RevisionInfo> {
		
		private static final String CURR = "curr";
		private static final String SILENT = "silent";
		private static final String EXISTS = "exists";
		private static final String COMM = "comm";
		private static final String TAKEN = "taken";
		
		@Override
		public Entity toEntity(Key datastoreKey, RevisionInfo revInfo) {
			Entity e = new Entity(datastoreKey);
			e.setUnindexedProperty(SILENT, revInfo.getGaeModelRevision().getLastSilentCommitted());
			ModelRevision modelRev = revInfo.getGaeModelRevision().getModelRevision();
			if(modelRev != null) {
				e.setUnindexedProperty(CURR, modelRev.revision());
				e.setUnindexedProperty(EXISTS, modelRev.modelExists());
			} else {
				e.removeProperty(CURR);
				e.removeProperty(EXISTS);
			}
			e.setUnindexedProperty(COMM, revInfo.getLastCommitted());
			e.setUnindexedProperty(TAKEN, revInfo.getLastTaken());
			return e;
		}
		
		@Override
		public RevisionInfo fromEntity(Entity e) {
			RevisionInfo ri = new RevisionInfo("from-datastore" + e.getKey().toString());
			long lastCommitted = (Long)e.getProperty(COMM);
			ri.setLastCommittedIfHigher(lastCommitted);
			Object oLastTaken = e.getProperty(TAKEN);
			if(oLastTaken == null) {
				// should never happen again
				log.warn("entity weird: " + DebugFormatter.format(e));
			}
			long lastTaken = (Long)oLastTaken;
			ri.setLastTakenIfHigher(lastTaken);
			ModelRevision modelRev = null;
			if(e.hasProperty(CURR)) {
				assert e.hasProperty(EXISTS);
				long current = (Long)e.getProperty(CURR);
				boolean modelExists = (Boolean)e.getProperty(EXISTS);
				modelRev = new ModelRevision(current, modelExists);
			}
			long silent = (Long)e.getProperty(SILENT);
			GaeModelRevision gaeModelRev = new GaeModelRevision(silent, modelRev);
			ri.setGaeModelRev(gaeModelRev);
			log.debug("loaded from entity with curr=" + gaeModelRev);
			return ri;
		}
		
		@Override
		public Serializable toSerializable(RevisionInfo entry) {
			return entry;
		}
		
		@Override
		public RevisionInfo fromSerializable(Serializable s) {
			RevisionInfo ri = (RevisionInfo)s;
			ri.setDatasourceName("from-memcache");
			return ri;
		}
		
	}
	
	@Override
	public GaeModelRevision calculateCurrentModelRevision() {
		GaeModelRevision lastCurrentRev = this.revManager.getInstanceRevisionInfo()
		        .getGaeModelRevision();
		assert lastCurrentRev.getModelRevision() != null;
		log.info("Update currentRev from lastCurrentRev=" + lastCurrentRev);
		/*
		 * After this method it might turn out, that 'current' is in fact the
		 * current revision.
		 */
		CandidateRev candidate = new CandidateRev(lastCurrentRev);
		
		int round = 0;
		long isolatedAttempts = 0;
		boolean askedMemcacheOrDatastore = false;
		long lastCheckedRev = lastCurrentRev.getLastSilentCommitted();
		log.debug("Start searching at " + lastCheckedRev + " with last rev being "
		        + candidate.gaeModelRev.getModelRevision());
		while(true) {
			if(!askedMemcacheOrDatastore && isolatedAttempts > 0) {
				log.info("Asking cache after " + isolatedAttempts + " attempts");
				
				RevisionInfo cachedRevInfo = getRevCache().get(getRevisionCacheName(),
				        StorageOptions.create(false, true, true));
				if(cachedRevInfo != null) {
					this.backendCached = cachedRevInfo;
					ModelRevision cachedModelRev = cachedRevInfo.getGaeModelRevision()
					        .getModelRevision();
					if(cachedModelRev != null
					        && cachedModelRev.revision() > candidate.gaeModelRev.getModelRevision()
					                .revision()) {
						candidate.gaeModelRev = cachedRevInfo.getGaeModelRevision();
						assert cachedRevInfo.getLastCommitted() > lastCheckedRev;
						lastCheckedRev = cachedRevInfo.getLastCommitted();
						assert lastCheckedRev >= candidate.gaeModelRev.getModelRevision()
						        .revision();
						round = 0;
						log.debug("Cached value is better than what we had. Now using "
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
			 * Uses batch fetches in memcache and datastore to load missing
			 * changes.
			 */
			
			/*
			 * Try to fetch 'initialBatchFetchSize' changes past the last known
			 * "current" revision and put them in the local vm cache.
			 */
			log.info("=== Phase 1: Determine revisions not yet locally cached; windowsize = "
			        + windowSize);
			Set<Long> locallyMissingRevs = computeLocallyMissingRevs(beginRevInclusive,
			        endRevInclusive);
			log.trace("locallyMissingRevs: " + locallyMissingRevs.size() + " of "
			        + (endRevInclusive - beginRevInclusive + 1));
			
			log.info("=== Phase 2+3: Ask Memcache + Datastore ===");
			fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
			log.trace("number of missingRevs after asking DS&MC: " + locallyMissingRevs.size());
			
			log.info("=== Phase 4: Compute result from local cache ===");
			candidate = computeCurrenRevisionFromLocalCache(beginRevInclusive, endRevInclusive,
			        candidate);
			if(candidate.finalModelRev) {
				long rev = candidate.gaeModelRev.getModelRevision().revision();
				log.info("Computed rev of " + this.modelAddr + " = " + rev
				        + " DATA?i_type=rev&i_addr=" + this.modelAddr + "&rev=" + rev);
				return candidate.gaeModelRev;
			}
			lastCheckedRev = endRevInclusive;
			isolatedAttempts += windowSize;
			round++;
		}
	}
	
	private static class CandidateRev {
		GaeModelRevision gaeModelRev;
		private boolean finalModelRev = false;
		
		/**
		 * @param gaeModelRev never null
		 */
		public CandidateRev(GaeModelRevision gaeModelRev) {
			assert gaeModelRev != null;
			assert gaeModelRev.getModelRevision() != null;
			this.gaeModelRev = gaeModelRev;
		}
		
		@Override
		public String toString() {
			return this.gaeModelRev + " finalRev?" + this.finalModelRev;
		}
		
		public void markAsFinalRev() {
			this.finalModelRev = true;
		}
		
		public boolean isFinalModelRev() {
			return this.finalModelRev;
		}
		
		public void setModelRev(GaeModelRevision modelRev) {
			this.gaeModelRev = modelRev;
		}
	}
	
	private CandidateRev computeCurrenRevisionFromLocalCache(long beginRevInclusive,
	        long endRevInclusive, CandidateRev candidate) {
		assert candidate.gaeModelRev.getModelRevision() != null;
		assert candidate.isFinalModelRev() == false;
		assert endRevInclusive - beginRevInclusive >= 0 : "begin:" + beginRevInclusive + ",end:"
		        + endRevInclusive;
		log.info(this.modelAddr + ":: computeFromCache candidate=" + candidate + " in range ["
		        + beginRevInclusive + "," + endRevInclusive + "]");
		for(long i = beginRevInclusive; i <= endRevInclusive; i++) {
			GaeChange change = getCachedChange(i);
			// log.trace("cached change " + i + ": " +
			// DebugFormatter.format(change));
			if(change == null) {
				// Validated a candidate revision
				log.debug("Found end at " + i + " return workingRev=" + candidate);
				candidate.markAsFinalRev();
			} else {
				if(!change.getStatus().isCommitted()) {
					// Cleanup running processes if necessary
					progressChange(change);
				} else if(change.getStatus() == Status.SuccessExecuted) {
					// Candidate for new revision is changed
					XEvent event = change.getEvent();
					boolean modelExist = eventIndicatesModelExists(event);
					candidate.setModelRev(new GaeModelRevision(event.getRevisionNumber(),
					        new ModelRevision(i, modelExist)));
					log.debug(this.modelAddr + ":: New currentRev candidate " + candidate);
				}
				
				if(change.getStatus().isCommitted()) {
					this.revManager.getInstanceRevisionInfo().setLastCommittedIfHigher(i);
					candidate.gaeModelRev.setLastSilentCommittedIfHigher(i);
				} else {
					// Validated a candidate revision
					assert change.getStatus() == Status.Creating;
					// current last revision found
					log.debug("Found a 'Creating' change at " + i + " so we report rev="
					        + candidate);
					candidate.markAsFinalRev();
				}
			}
			
			boolean putInMemcache = i % 16 == 4;
			boolean putInDatastore = i % 64 == 16;
			if(putInMemcache || putInDatastore) {
				StorageOptions storeOpts = UniCache.StorageOptions.create(false, putInMemcache,
				        putInDatastore);
				if(this.backendCached == null) {
					// read cache first to prevent destroying good knowledge
					this.backendCached = getRevCache().get(getRevisionCacheName(), storeOpts);
				}
				// share findings via memcache and datastore with other
				// instances
				RevisionInfo toBeCached = new RevisionInfo("toBeCached", candidate.gaeModelRev,
				        this.revManager.getInstanceRevisionInfo().getLastCommitted(),
				        this.revManager.getInstanceRevisionInfo().getLastTaken());
				
				if(toBeCached.isBetterThan(this.backendCached)) {
					log.debug("this rev " + toBeCached + " is better than " + this.backendCached
					        + " and thus will be cached");
					getRevCache().put(getRevisionCacheName(), toBeCached, storeOpts);
				}
				
			}
			
			// are we done?
			if(candidate.finalModelRev) {
				this.revManager.getInstanceRevisionInfo().setCurrentGaeModelRevIfRevisionIsHigher(
				        candidate.gaeModelRev);
				log.debug("Updated rev from [" + candidate + " ==> " + candidate.gaeModelRev);
				return candidate;
			}
		}
		// default:
		assert candidate.finalModelRev == false;
		return candidate;
	}
	
	public static void renderChangeLog(XAddress modelAddress, Writer w) throws IOException {
		w.write("<h2>Changelog of " + modelAddress + "</h2>\n");
		w.flush();
		int i = 0;
		RevisionManager rm = new RevisionManager(modelAddress);
		GaeChangesServiceImpl3 changes = new GaeChangesServiceImpl3(modelAddress, rm);
		GaeChange c = changes.getChange(i);
		while(c != null) {
			// render c
			String s = DebugFormatter.format(c);
			w.write(s + "<br/>\n");
			w.flush();
			i++;
			c = changes.getChange(i);
		}
		w.write("End of changelog.<br/>\n");
	}
	
}

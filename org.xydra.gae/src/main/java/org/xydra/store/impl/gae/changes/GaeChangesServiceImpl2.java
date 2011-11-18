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
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.RevisionState;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.SyncDatastore;
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
 * Keys for XMODEL, XOBJECT and XFIELD entities are encoded according to
 * {@link KeyStructure#createEntityKey(XAddress)}.
 * 
 * There are two different kinds of GAE Entities that are used by this class:
 * 
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
 * set before the change has reached {@link Status#Executing}.
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
 * To synchronize access to the internal MOF tree, the first thing each change
 * does is declare it's required locks in the change entity when grabbing the
 * revision. Before executing, a change checks any uncommitted changes with
 * lower revision numbers for conflicting locks. If a conflict is found, the
 * change will have to wait or abort / roll forward the conflicting change after
 * a timeout. Other pending changes that don't conflict can be safely ignored as
 * they will never touch the same part of the MOF tree. After a change is done,
 * it removes all locks from the change entity.
 * 
 * Locks are managed by {@link GaeLocks}.
 * 
 * @author dscharrer
 * 
 */
public class GaeChangesServiceImpl2 implements IGaeChangesService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesServiceImpl2.class);
	
	private static final long serialVersionUID = -2080744796962188941L;
	
	// Implementation.
	
	private final XAddress modelAddr;
	private final RevisionCache2 revCache;
	
	@GaeOperation()
	public GaeChangesServiceImpl2(XAddress modelAddr) {
		this.modelAddr = modelAddr;
		this.revCache = new RevisionCache2(modelAddr, this);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see IGaeChangesService#grabRevisionAndRegisterLocks(GaeLocks, XID)
	 */
	@Override
	@GaeOperation(memcacheRead = true ,datastoreRead = true ,datastoreWrite = true ,memcacheWrite = true)
	public GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId) {
		
		long lastTaken = this.revCache.getLastTaken();
		assert lastTaken >= -1;
		long start = lastTaken + 1;
		for(long rev = start;; rev++) {
			
			GaeChange cachedChange = getCachedChange(rev);
			if(cachedChange != null) {
				// Revision already taken.
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
				
				this.revCache.setLastTaken(rev);
				
				// transaction succeeded, we have a revision
				return newChange;
				
			} else {
				// Revision already taken.
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
				
				// Since we read the entity anyway, might as well use that
				// information.
				Status status = change.getStatus();
				if(status.isCommitted()) {
					cacheCommittedChange(change);
				} else if(!status.canRollForward() && change.isTimedOut()) {
					commit(change, Status.FailedTimeout);
				}
				
			}
			
		}
		
		// unreachable
	}
	
	@Override
	public void commit(GaeChange change, Status status) {
		
		assert status.isCommitted();
		assert !change.getStatus().isCommitted();
		
		if(status == Status.FailedTimeout) {
			log.warn("Comitting timed out change " + change);
		}
		change.commit(status);
		
		assert change.getStatus() == status;
		
		cacheCommittedChange(change);
	}
	
	private static final boolean USE_COMMITTED_CHANGE_CACHE = true;
	
	private static final String VM_COMMITED_CHANGES_CACHENAME = "[.c2]";
	
	// IMPROVE experiment with MAX_BATCH_FETCH_SIZE
	private static final int MAX_BATCH_FETCH_SIZE = 100;
	
	private static final long MAX_REVISION_NR = 8 * 1024;
	
	/**
	 * A new last committed change has been found - update revision caches.
	 */
	private void newCurrentRev(GaeChange change) {
		
		log.debug("(r" + change.rev + ") {" + this.revCache.getCurrentRev() + "/"
		        + getLastCommited() + "} new current rev");
		
		assert change.getStatus().hasEvents();
		assert change.getStatus().isSuccess();
		
		XEvent event = change.getEvent();
		if(event instanceof XTransactionEvent) {
			XTransactionEvent trans = (XTransactionEvent)event;
			event = trans.getEvent(trans.size() - 1);
		}
		assert !event.isImplied();
		
		boolean modelExists = true;
		if(event instanceof XRepositoryEvent) {
			modelExists = (event.getChangeType() != ChangeType.REMOVE);
		}
		
		synchronized(this.revCache) {
			this.revCache.setCurrentModelRev(change.rev, modelExists);
		}
		
	}
	
	private void updateCachedRevisions(GaeChange change) {
		
		assert change.getStatus().isCommitted();
		
		RevisionState state = this.revCache.getRevisionState();
		
		if(change.rev == getLastCommited() + 1) {
			long newLastCommittedRev = change.rev;
			Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
			GaeChange newCurrentChange = change.getStatus().hasEvents() ? change : null;
			synchronized(committedChangeCache) {
				GaeChange otherChange;
				while((otherChange = committedChangeCache.get(newLastCommittedRev + 1)) != null) {
					newLastCommittedRev++;
					assert otherChange.rev == newLastCommittedRev;
					if(otherChange.getStatus().hasEvents()) {
						newCurrentChange = otherChange;
					}
				}
			}
			
			log.debug("(r" + change.rev + ") {" + this.revCache.getCurrentRev() + "/"
			        + getLastCommited() + "} new last committed rev " + newLastCommittedRev);
			this.revCache.setLastCommited(newLastCommittedRev);
			if(state != null && newCurrentChange != null && newCurrentChange.rev > state.revision()) {
				newCurrentRev(newCurrentChange);
			}
			
		} else if(state != null && change.getStatus().hasEvents()
		        && change.rev <= getLastCommited() && change.rev > state.revision()) {
			newCurrentRev(change);
		}
		
	}
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached
	 */
	@Override
	public void cacheCommittedChange(GaeChange change) {
		
		if(USE_COMMITTED_CHANGE_CACHE) {
			
			assert change != null;
			assert change.getStatus() != null;
			assert change.getStatus().isCommitted();
			log.debug(DebugFormatter.dataPut(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
			        + change.rev, change, Timing.Now));
			Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
			synchronized(committedChangeCache) {
				committedChangeCache.put(change.rev, change);
			}
			
		}
		
		updateCachedRevisions(change);
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
		if(change != null) {
			/*
			 * TODO @Daniel: Why update again at read-time. At write-time should
			 * suffice, shouldn't it?
			 */
			updateCachedRevisions(change);
			assert change.getStatus().isCommitted();
		}
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getCurrentRevisionNumber
	 * ()
	 */
	@Override
	public long getCurrentRevisionNumber() {
		RevisionState currentRev = this.revCache.getRevisionState();
		if(currentRev == null) {
			log.debug("version not locally known");
			currentRev = updateCurrentRev(null);
			this.revCache.setCurrentModelRev(currentRev);
		} else {
			log.debug("currentRev is locally defined and will not change during this request");
		}
		log.debug("getCurrentRevisionNumber = " + currentRev);
		return currentRev.revision();
	}
	
	RevisionState updateCurrentRev(RevisionState lastCurrentRev) {
		log.info("update currentRev from lastCurrentRev=" + lastCurrentRev);
		// shortcut:
		if(lastCurrentRev != null) {
			log.debug("Check datastore if currentRev is still " + lastCurrentRev);
			// quickly look in datastore if this is simply still the current rev
			// TODO ask if lastCommited+1 is still empty
			
			// prepare batch request
			long nextRev = lastCurrentRev.revision() + 1;
			Key key = KeyStructure.createChangeKey(getModelAddress(), nextRev);
			Entity e = SyncDatastore.getEntity(key);
			if(e == null) {
				// we won, we got it
				log.info("lastCurrentRev = " + lastCurrentRev + " verified via datastore");
				return lastCurrentRev;
			} else {
				// use information of entity
				// process status of change
				GaeChange change = new GaeChange(getModelAddress(), nextRev, e);
				Status status = change.getStatus();
				if(status.isCommitted()) {
					// use it
					cacheCommittedChange(change);
				} else {
					// TODO progress change
				}
			}
		}
		
		log.debug("Run normal update procedure");
		Pair<RevisionState,Boolean> current = new Pair<RevisionState,Boolean>(
		        lastCurrentRev == null ? RevisionState.MODEL_DOES_NOT_EXIST_YET : lastCurrentRev,
		        false);
		log.debug("Updating rev from lastCurrentRev=" + current.getFirst() + " ...");
		int windowSize = 1;
		while(current.getSecond() == false) {
			log.debug("windowsize = " + windowSize);
			// FIXME inline & fix it
			current = updateCurrentRev_Step(current.getFirst(), windowSize);
			// adjust probe window
			windowSize = windowSize * 2;
			// avoid too big windows
			if(windowSize > MAX_BATCH_FETCH_SIZE) {
				windowSize = MAX_BATCH_FETCH_SIZE;
			}
		}
		assert current.getSecond() == true;
		assert current.getFirst() != null : "found no rev nr";
		this.revCache.setCurrentModelRev(current.getFirst());
		log.debug("Updated rev from [" + lastCurrentRev + " ==> " + current.getFirst());
		return current.getFirst();
	}
	
	/**
	 * Uses batch fetches in memcache and datastore to load missing changes.
	 * 
	 * @param startingRevExclusive the last known revision state of the model.
	 *            Combines a revision number and a modelExists date. After this
	 *            revision step it might turn out, that this revision is in fact
	 *            the current revision.
	 * @param endRevInclusive the highest revision number (inclusive) to check
	 *            for in this revision update step
	 * @return a Pair. First: A RevisionState with the current model revision
	 *         within the checked window. The second part of the pair is true,
	 *         if this revisionState is the current revision of the model, false
	 *         otherwise.
	 */
	private Pair<RevisionState,Boolean> updateCurrentRev_Step(RevisionState startingRevExclusive,
	        long windowSize) {
		long beginRevInclusive = startingRevExclusive.revision() + 1;
		long endRevInclusive = beginRevInclusive + windowSize - 1;
		log.debug("Update rev step [" + beginRevInclusive + "," + endRevInclusive + "]");
		if(endRevInclusive >= MAX_REVISION_NR) {
			log.warn("Checking for very high revision number: " + endRevInclusive);
		}
		
		/*
		 * Try to fetch 'initialBatchFetchSize' changes past the last known
		 * "current" revision and put them in the local vm cache.
		 */
		log.info("=== Phase 1: Determine revisions not yet locally cached; windowsize = "
		        + windowSize);
		Set<Long> locallyMissingRevs = computeLocallyMissingRevs(beginRevInclusive, endRevInclusive);
		log.trace("locallyMissingRevs: " + locallyMissingRevs.size() + " of "
		        + (endRevInclusive - beginRevInclusive + 1));
		
		log.info("=== Phase 2+3: Ask Memcache + Datastore ===");
		fetchMissingRevisionsFromMemcacheAndDatastore(locallyMissingRevs);
		log.trace("number of missingRevs after asking DS&MC: " + locallyMissingRevs.size());
		
		log.info("=== Phase 4: Compute result from local cache ===");
		boolean foundEnd = false;
		RevisionState windowRev = startingRevExclusive;
		for(long i = beginRevInclusive; i <= endRevInclusive; i++) {
			GaeChange change = getCachedChange(i);
			log.trace("cached change " + i + ": " + DebugFormatter.format(change));
			
			// TODO careful: too much caching?
			if(change == null) {
				foundEnd = true;
				break;
			} else {
				switch(change.getStatus()) {
				case Creating:
				case Executing: {
					/* unclear if current version is affected by this. */
				}
					break;
				case FailedTimeout: {
					/* TODO what to do then? */
					log.debug("Change " + i + " came from cache and is FailedTimeout");
				}
					break;
				case SuccessExecuted: {
					XEvent event = change.getEvent();
					boolean modelExist = eventIndicatesModelExists(event);
					windowRev = new RevisionState(i, modelExist);
				}
					break;
				case FailedPreconditions:
				case SuccessNochange: {
					/* modelExists unchanged, revision incremented */
					windowRev = new RevisionState(i, windowRev.modelExists());
				}
					break;
				}
			}
		}
		
		if(foundEnd) {
			log.trace("Step: return currentRev = " + windowRev);
			return new Pair<RevisionState,Boolean>(windowRev, true);
		} else {
			assert locallyMissingRevs.size() == 0;
			/* === Phase 5: go on */
			/* We know these revisions are all committed */
			this.revCache.setLastCommited(endRevInclusive);
			/*
			 * All revisions we looked at have been processed. Need to repeat
			 * the process by looking at more revisions.
			 */
			return new Pair<RevisionState,Boolean>(windowRev, false);
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
		/* === Phase 2: Ask memcache === */
		List<String> memcacheBatchRequest = new ArrayList<String>(locallyMissingRevs.size());
		if(!locallyMissingRevs.isEmpty()) {
			// prepare batch request
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
				log.debug("Found in memcache " + change.rev);
			}
			if(newLastCommitted >= 0) {
				this.revCache.setLastCommited(newLastCommitted);
			}
			// re-use strings in memcacheBatchRequest: retain only *still*
			// missing keys (neither locally found, nor in
			// memcache)
			memcacheBatchRequest.removeAll(memcacheResult.keySet());
		} else {
			// log.debug("All found in localVmCache");
		}
		
		/* === Phase 3: Ask datastore === */
		if(!memcacheBatchRequest.isEmpty()) {
			// prepare batch request
			List<Key> datastoreBatchRequest = new ArrayList<Key>(memcacheBatchRequest.size());
			for(String keyStr : memcacheBatchRequest) {
				Key key = KeyStructure.toKey(keyStr);
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
					// FIXME .......
					log.warn("Change is " + change.getStatus() + " timeout?" + change.isTimedOut()
					        + ". Dump: " + change + " ||| Now = " + System.currentTimeMillis());
					if(change.isTimedOut()) {
						/* Happens if we are updating the currentRev */
						log.warn("handle timed-out change - WHY? " + change.rev);
						boolean success = handleTimeout(change);
						if(!success) {
							change.reload();
						}
						// change might be complete now (we or another process
						// might have done it)
						if(change.getStatus().isCommitted()) {
							// use it
							memcacheBatchPut.put(KeyStructure.toString(key), entity);
							cacheCommittedChange(change);
							locallyMissingRevs.remove(revFromKey);
						} else {
							log.warn("made no progress on time-out change " + change.rev);
						}
					} else {
						assert status == Status.Creating || status == Status.Executing;
						log.warn("Change " + change.rev + " is still " + change.getStatus()
						        + ". Not cached.");
						// don't cache
					}
					if(change.rev > newLastTaken) {
						newLastTaken = change.rev;
					}
				}
			}
			if(newLastTaken >= 0) {
				this.revCache.setLastTaken(newLastTaken);
			}
			if(newLastCommitted >= 0) {
				this.revCache.setLastCommited(newLastCommitted);
			}
			
			// update memcache IMPROVE do this async
			XydraRuntime.getMemcache().putAll(memcacheBatchPut);
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
			GaeChange change = this.getCachedChange(i);
			if(change == null) {
				locallyMissingRevs.add(i);
			} else {
				assert change.rev == i;
				assert change.getStatus().isCommitted();
				// log.debug("Already locally cached: " +
				// DebugFormatter.format(change));
			}
		}
		return locallyMissingRevs;
	}
	
	@Override
	public List<XEvent> getEventsBetween(long beginRevision, long endRevision) {
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
		long currentRev = getCurrentRevisionNumber();
		if(currentRev == -1) {
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
		RevisionState newRev = new RevisionState(-1, false);
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
					newRev = new RevisionState(i, eventIndicatesModelExists(event));
				} else {
					assert change.getStatus() != Status.Creating;
					assert change.getStatus() != Status.Executing;
					log.debug("Change " + i + " is " + change.getStatus().name());
				}
			} else {
				log.warn("==== Change " + i + " is null, was asking [" + begin + "," + endRev
				        + "]. Retry.");
				// FIXME RECHECK
				Set<Long> set = new HashSet<Long>();
				set.add(i);
				fetchMissingRevisionsFromMemcacheAndDatastore(set);
				Thread.yield();
				i--;
				continue;
				
				//
				// throw new IllegalStateException("Change " + i +
				// " null was asking [" + begin + ","
				// + endRev + "]");
			}
		}
		if(newRev.revision() >= 0) {
			this.revCache.setCurrentModelRev(newRev);
		}
		
		GaeAssert.gaeAssert(eventsAreWithinRange(events, begin, endRev));
		
		return events;
	}
	
	private boolean eventsAreWithinRange(List<XEvent> events, long begin, long endRev) {
		for(XEvent e : events) {
			GaeAssert.gaeAssert(e.getRevisionNumber() >= begin);
			GaeAssert.gaeAssert(e.getRevisionNumber() <= endRev);
		}
		return true;
	}
	
	/**
	 * Roll forward a timed out entity if possible, otherwise just mark it as
	 * timed out.
	 * 
	 * @return false if the entity could not be rolled forward.
	 */
	private boolean handleTimeout(GaeChange change) {
		log.debug("handleTimeout: " + change);
		if(change.getStatus().canRollForward()) {
			// FIXME Roll forward the change or it will remain timed out (and
			// hold back the "current" revision) until a conflicting change is
			// executed.
			return false;
		} else {
			commit(change, Status.FailedTimeout);
			return false;
		}
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
		this.revCache.clear();
	}
	
	protected void initialiseThreadLocalRevisionState() {
		// use current instance info
		RevisionState currentRev = this.revCache.getRevisionState();
		// update via GAE state (memcache&datastore)
		currentRev = updateCurrentRev(currentRev);
		// store in instance info and this thread
		this.revCache.setCurrentModelRev(currentRev);
	}
	
	@Override
	public boolean exists() {
		Boolean thisThread = this.revCache.modelExists();
		if(thisThread == null) {
			// this thread local cache doesn't know it
			log.debug("Re-calc existing status");
			long rev = getCurrentRevisionNumber();
			if(rev == -1) {
				return false;
			} else if(rev == 0) {
				return true;
			} else {
				GaeAssert.gaeAssert(rev >= 1);
				List<XEvent> events = getEventsBetween(rev, rev);
				GaeAssert.gaeAssert(events.size() == 1);
				XEvent e = events.get(0);
				boolean modelExists = eventIndicatesModelExists(e);
				this.revCache.setCurrentModelRev(rev, modelExists);
				return modelExists;
			}
		} else {
			// this thread knows it
			boolean result = thisThread;
			log.debug("This model " + this.modelAddr + " does " + (result ? "really" : "not")
			        + " exist according to local thread info");
			return result;
		}
	}
	
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
	public long getLastCommited() {
		return this.revCache.getLastCommited();
	}
	
}

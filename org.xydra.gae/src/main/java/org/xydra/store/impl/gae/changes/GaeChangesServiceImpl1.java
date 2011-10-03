package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


/**
 * A class responsible for executing and logging changes to a specific
 * {@link XModel} in the GAE datastore.
 * 
 * This class is the core of the GAE {@link XydraStore} implementation.
 * 
 * Keys for XMODEL, XOBJECT and XFIELD entities are encoded according to
 * {@link KeyStructure#createEntityKey(XAddress)}.
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
@SuppressWarnings("deprecation")
public class GaeChangesServiceImpl1 implements IGaeChangesService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeChangesServiceImpl1.class);
	
	private static final long serialVersionUID = -2080744796962188941L;
	
	// Implementation.
	
	private final XAddress modelAddr;
	private final RevisionCache revCache;
	
	@GaeOperation()
	public GaeChangesServiceImpl1(XAddress modelAddr) {
		this.modelAddr = modelAddr;
		this.revCache = new RevisionCache(modelAddr);
	}
	
	@Override
	public boolean exists() {
		// FIXME implement!
		return true;
	}
	
	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 */
	@Override
	public GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId) {
		
		for(long rev = this.revCache.getLastTaken() + 1;; rev++) {
			
			if(getCachedChange(rev) != null) {
				// Revision already taken.
				continue;
			}
			
			// Try to grab this revision.
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
			/* use txn to do: avoid overwriting existing change entities */
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity changeEntity = GaeUtils.getEntity_MemcachePositive_DatastoreFinal(key, trans);
			
			if(changeEntity == null) {
				
				GaeChange newChange = new GaeChange(this.modelAddr, rev, locks, actorId);
				newChange.save(trans);
				
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
				
				this.revCache.setLastTaken(rev);
				
				// transaction succeeded, we have a revision
				return newChange;
				
			} else {
				
				GaeChange change = new GaeChange(this.modelAddr, rev, changeEntity);
				
				// Revision already taken.
				
				GaeUtils.endTransaction(trans);
				
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
	
	/**
	 * Mark the given change as committed.
	 * 
	 * @param status The new (and final) status.
	 */
	@Override
	public void commit(GaeChange change, Status status) {
		assert status.isCommitted();
		assert !change.getStatus().isCommitted();
		if(status == Status.FailedTimeout) {
			log.warn("Comitting timed out change " + change);
		}
		change.commit(status);
		// TODO do we really need to ask the memcache here?
		if(this.revCache.getLastCommited() == change.rev - 1) {
			this.revCache.setLastCommited(change.rev);
		}
		cacheCommittedChange(change);
	}
	
	private static final boolean USE_COMMITTED_CHANGE_CACHE = true;
	
	private static final String VM_COMMITED_CHANGES_CACHENAME = "[vm.changes1]";
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change TODO
	 */
	@Override
	public void cacheCommittedChange(GaeChange change) {
		if(USE_COMMITTED_CHANGE_CACHE) {
			if(!change.getStatus().isCommitted()) {
				return;
			}
			log.debug(DebugFormatter.dataPut(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
			        + change.rev, change, Timing.Now));
			Map<Long,GaeChange> committedChangeCache = getCommittedChangeCache();
			synchronized(committedChangeCache) {
				committedChangeCache.put(change.rev, change);
			}
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
		if(change != null) {
			assert change.getStatus().isCommitted();
		}
		log.debug(DebugFormatter.dataGet(VM_COMMITED_CHANGES_CACHENAME + this.modelAddr, "" + rev,
		        change, Timing.Now));
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
		long currentRev = this.revCache.getCurrentModelRevIfSet();
		if(currentRev == RevisionCache.NOT_SET) {
			currentRev = updateCurrentRev(-1L);
		}
		log.debug("currentRevNr = " + currentRev);
		return currentRev;
	}
	
	private long updateCurrentRev(long lastCurrentRev) {
		log.debug("updateCurrentRev from " + lastCurrentRev);
		long currentRev = lastCurrentRev;
		long rev = currentRev;
		
		// Try to fetch one change past the last known "current" revision.
		List<AsyncChange> batch = new ArrayList<AsyncChange>(1);
		batch.add(getChangeAt(rev + 1));
		
		int pos = 0;
		
		long end = this.revCache.getLastCommitedIfSet();
		if(end == RevisionCache.NOT_SET) {
			end = Long.MAX_VALUE;
		}
		
		for(; rev <= end; rev++) {
			
			GaeChange change = batch.get(pos).get();
			if(change == null) {
				break;
			}
			
			Status status = change.getStatus();
			if(!status.isCommitted()) {
				if(change.isTimedOut()) {
					if(handleTimeout(change)) {
						change.reload();
						rev--;
						continue;
					}
				} else {
					// Found the lastCommitedRev
					break;
				}
			}
			
			// Only update the current revision if the command actually changed
			// something.
			if(status == Status.SuccessExecuted) {
				currentRev = rev + 1;
			}
			
			// Asynchronously fetch new change entities.
			batch.set(pos, getChangeAt(rev + batch.size() + 1));
			pos++;
			if(pos == batch.size()) {
				batch.add(getChangeAt(rev + batch.size() + 2));
				pos = 0;
			}
			
		}
		
		this.revCache.setLastCommited(rev);
		this.revCache.setCurrentModelRev(currentRev);
		
		return currentRev;
	}
	
	public AsyncChange getChangeAt(long rev) {
		
		GaeChange change = getCachedChange(rev);
		if(change != null) {
			log.debug("use locally cached change " + rev);
			return new AsyncChange(change);
		}
		log.debug("no locally cached change " + rev);
		return new AsyncChange(this, rev);
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
	 * org.xydra.store.impl.gae.changes.IGaeChangesService#getEventsBetween(
	 * long, long)
	 */
	@Override
	public List<XEvent> getEventsBetween(long beginRevision, long endRevision) {
		log.debug("getEventsBetween [" + beginRevision + "," + endRevision + ") @"
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
		if(endRevision <= 0) {
			return new ArrayList<XEvent>(0);
		}
		
		/* adjust range */
		long endRev = endRevision;
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
		
		List<XEvent> events = new ArrayList<XEvent>();
		
		// Asynchronously fetch an initial batch of change entities
		int initialBuffer = 1;
		if(endRev <= currentRev) {
			initialBuffer = (int)(endRev - begin + 1);
		} else {
			// IMPROVE maybe use an initial buffer size of currentRev - begin +
			// 1?
		}
		List<AsyncChange> batch = new ArrayList<AsyncChange>(initialBuffer);
		for(int i = 0; i < initialBuffer; i++) {
			batch.add(getChangeAt(begin + i));
		}
		
		int pos = 0;
		
		/*
		 * Only update the currentRev cache value if we aren't skipping any
		 * events.
		 */
		boolean trackCurrentRev = (begin <= currentRev);
		
		long rev = begin;
		for(; rev <= endRev; rev++) {
			
			// Wait for the first change entities
			GaeChange change = batch.get(pos).get();
			if(change == null) {
				// Found end of the change log
				break;
			}
			
			Status status = change.getStatus();
			if(!status.isCommitted()) {
				if(change.isTimedOut()) {
					log.warn("Changed timed out " + change);
					if(handleTimeout(change)) {
						change.reload();
						rev--;
						continue;
					}
				} else {
					// Found the lastCommitedRev
					break;
				}
			}
			
			XEvent event = change.getEvent();
			if(event != null) {
				// Something actually changed
				if(trackCurrentRev) {
					currentRev = rev;
				}
				events.add(event);
			}
			
			// Asynchronously fetch new change entities.
			if(rev + batch.size() <= endRev) {
				batch.set(pos, getChangeAt(rev + batch.size()));
			}
			pos++;
			if(pos == batch.size()) {
				if(rev + batch.size() + 1 <= endRev) {
					batch.add(getChangeAt(rev + batch.size() + 1));
				}
				pos = 0;
			}
			
		}
		
		if(currentRev == -1) {
			assert events.isEmpty();
			return null;
		}
		
		if(trackCurrentRev) {
			this.revCache.setLastCommited(rev - 1);
			this.revCache.setCurrentModelRev(currentRev);
		}
		
		return events;
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
			return true;
		} else {
			commit(change, Status.FailedTimeout);
			return false;
		}
	}
	
	/* TODO make this default visible and remove from interface */
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
	
	@Override
	public long getLastCommited() {
		return this.revCache.getLastCommited();
	}
	
}

package org.xydra.store.impl.gae.ng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.CommittedButStillApplyingException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


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
	
	private XAddress modelAddress;
	
	/**
	 * @param modelAddress required to compute keys in datastore
	 */
	public ChangeLogManager(@NeverNull XAddress modelAddress) {
		XyAssert.xyAssert(modelAddress != null);
		this.modelAddress = modelAddress;
	}
	
	/**
	 * @param change must not yet be a terminal state
	 * @param status must be a terminal state, i.e. not 'Creating'
	 */
	public void commitAndClearLocks(GaeChange change, Status status) {
		XyAssert.xyAssert(!status.canChange());
		XyAssert.xyAssert(change.getStatus().canChange());
		
		change.commitAndClearLocks(status);
		
		XyAssert.xyAssert(change.getStatus() == status);
	}
	
	public @CanBeNull
	GaeChange getChange(long rev) {
		Key key = KeyStructure.createChangeKey(this.modelAddress, rev);
		Entity entityFromGae = SyncDatastore.getEntity(key);
		if(entityFromGae == null) {
			return null;
		}
		GaeChange change = new GaeChange(this.modelAddress, rev, entityFromGae);
		return change;
	}
	
	/**
	 * @param maxSingleBatchFetchRange
	 * @return at least one null change if there were null-changes at the end
	 */
	private Collection<? extends GaeChange> getChangesInBatch(Interval maxSingleBatchFetchRange) {
		List<GaeChange> changes = new ArrayList<GaeChange>();
		
		List<Key> keys = new ArrayList<Key>();
		for(long rev = maxSingleBatchFetchRange.start; rev <= maxSingleBatchFetchRange.end; rev++) {
			Key key = KeyStructure.createChangeKey(this.modelAddress, rev);
			keys.add(key);
		}
		Map<Key,Entity> entities = SyncDatastore.getEntities(keys);
		for(Key key : keys) {
			Entity entity = entities.get(key);
			GaeChange change = null;
			long rev = KeyStructure.getRevisionFromChangeKey(key);
			if(entity != null) {
				change = new GaeChange(this.modelAddress, rev, entity);
			}
			changes.add(change);
		}
		
		log.debug("changes in batch from " + maxSingleBatchFetchRange + " got " + changes.size()
		        + " changes");
		
		return changes;
	}
	
	/**
	 * @param fetchRange
	 * @return at least one null-change at the end of there was a null change in
	 *         the given fetchRange
	 */
	public List<GaeChange> getChanges(Interval fetchRange) {
		List<GaeChange> changes = new LinkedList<GaeChange>();
		
		if(!fetchRange.isEmpty()) {
			/**
			 * get requested events in batches of MAXIMAL_CHANGES_FETCH_SIZE and
			 * use smaller ranges when exceptions occur
			 */
			Interval singleBatchFetchRange = fetchRange.getSubInterval(MAXIMAL_CHANGES_FETCH_SIZE);
			boolean hadNullChanges = false;
			do {
				try {
					Collection<? extends GaeChange> newChanges = getChangesInBatch(singleBatchFetchRange);
					for(GaeChange newChange : newChanges) {
						if(newChange == null) {
							hadNullChanges = true;
						} else {
							changes.add(newChange);
						}
					}
					// OLD: hadNullChanges = newChanges.size() <
					// singleBatchFetchRange.size();
				} catch(Throwable t) {
					log.warn("Could not read a change interval " + singleBatchFetchRange, t);
					singleBatchFetchRange = singleBatchFetchRange.firstHalf();
				}
				singleBatchFetchRange = singleBatchFetchRange
				        .moveRightAndShrinkToKeepEndMaxAt(fetchRange.end);
			} while(singleBatchFetchRange.end < fetchRange.end && !hadNullChanges);
		}
		return changes;
	}
	
	public @NeverNull
	List<XEvent> getEventsInInterval(Interval interval) {
		log.debug("Getting events from changes in " + interval + " for " + this.modelAddress);
		LinkedList<XEvent> events = new LinkedList<XEvent>();
		List<GaeChange> changes = getChanges(interval);
		for(GaeChange change : changes) {
			if(change.getStatus().changedSomething()) {
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
	
	public GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId, long start,
	        @NeverNull RevisionManager revisionManager) {
		for(long rev = start;; rev++) {
			
			// Try to grab this revision.
			Key key = KeyStructure.createChangeKey(this.modelAddress, rev);
			/* use txn to do: avoid overwriting existing change entities */
			Transaction trans = SyncDatastore.beginTransaction();
			
			Entity changeEntity = SyncDatastore.getEntity(key, trans);
			
			if(changeEntity == null) {
				
				GaeChange newChange = new GaeChange(this.modelAddress, rev, locks, actorId);
				newChange.save(trans);
				
				try {
					SyncDatastore.endTransaction(trans);
				} catch(ConcurrentModificationException cme) {
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
				} catch(DatastoreTimeoutException dte) {
					log.info("failed to take revision: " + key
					        + " GA?category=error&action=DatastoreTimeout", dte);
					
					// try this revision again
					rev--;
					continue;
				} catch(DatastoreFailureException dfe) {
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
				} catch(CommittedButStillApplyingException csa) {
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
				
				GaeChange change = new GaeChange(this.modelAddress, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
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
	 * @param change @NeverNull
	 * @param revisionManager @NeverNull
	 * @return true if change was timed-out and hence progressed
	 */
	public boolean progressChangeIfTimedOut(@NeverNull GaeChange change,
	        @NeverNull RevisionManager revisionManager) {
		XyAssert.xyAssert(change != null);
		assert change != null;
		
		Status status = change.getStatus();
		if(status.canChange()) {
			log.debug("Trying to progress change " + change);
			if(change.isTimedOut()) {
				log.debug("handleTimeout: " + change);
				if(status == Status.Creating) {
					commitAndClearLocks(change, Status.FailedTimeout);
					revisionManager.foundNewHigherCommitedChange(change);
					return true;
				} else if(status == Status.SuccessExecuted) {
					// record changes to signal other threads we work on it
					Future<Key> f = change.save();
					Key key;
					try {
						key = f.get();
						if(key != null) {
							GaeModelPersistenceNG.rollForward_updateTentativeObjectStates(
							        this.modelAddress, change, revisionManager, this);
						}
					} catch(InterruptedException e) {
					} catch(ExecutionException e) {
					}
				}
			}
		}
		return false;
	}
	
}

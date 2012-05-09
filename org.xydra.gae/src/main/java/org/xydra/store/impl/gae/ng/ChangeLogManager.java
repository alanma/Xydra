package org.xydra.store.impl.gae.ng;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
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
	
	private XAddress modelAddress;
	private RevisionManager revisionManager;
	
	/**
	 * @param modelAddress required to compute keys in datastore
	 * @param revisionManager
	 */
	public ChangeLogManager(@NeverNull XAddress modelAddress,
	        @NeverNull RevisionManager revisionManager) {
		XyAssert.xyAssert(modelAddress != null);
		this.modelAddress = modelAddress;
		this.revisionManager = revisionManager;
	}
	
	/**
	 * @param change must not yet be a terminal state
	 * @param status must be a terminal state, i.e. not 'Creating'
	 */
	public void commitAndClearLocks(GaeChange change, Status status) {
		XyAssert.xyAssert(status.isCommitted());
		XyAssert.xyAssert(!change.getStatus().isCommitted());
		
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
	
	public List<GaeChange> getChanges(Interval fetchRange) {
		List<GaeChange> changes = new LinkedList<GaeChange>();
		if(!fetchRange.isEmpty()) {
			for(long rev = fetchRange.start; rev <= fetchRange.end; rev++) {
				GaeChange change = getChange(rev);
				if(change == null) {
					return changes;
				}
				changes.add(change);
			}
		}
		return changes;
	}
	
	public List<XEvent> getEventsBetween(long start, long end) {
		LinkedList<XEvent> events = new LinkedList<XEvent>();
		List<GaeChange> changes = getChanges(new Interval(start, end));
		for(GaeChange change : changes) {
			if(change.getStatus() == Status.SuccessExecuted) {
				events.addAll(change.getAtomicEvents().getFirst());
			}
		}
		return events;
	}
	
	public XAddress getModelAddress() {
		return this.modelAddress;
	}
	
	public GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId, long start) {
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
				
				this.revisionManager.foundNewLastTaken(rev);
				
				// transaction succeeded, we have a revision
				return newChange;
				
			} else {
				// Revision already taken.
				
				GaeChange change = new GaeChange(this.modelAddress, rev, changeEntity);
				SyncDatastore.endTransaction(trans);
				this.revisionManager.foundNewLastTaken(rev);
				
				/* Since we read the entity anyway, we use the information */
				progressChangeIfTimedOut(change);
			}
		}
		
		// unreachable
	}
	
	/**
	 * Check is change is timed-out and then move to status to FailedTimeout
	 * 
	 * @param change @NeverNull
	 * @return true if change was timed-out and hence progressed
	 */
	public boolean progressChangeIfTimedOut(@NeverNull GaeChange change) {
		XyAssert.xyAssert(change != null);
		assert change != null;
		
		Status status = change.getStatus();
		if(!status.isCommitted()) {
			log.debug("Trying to progress change " + change);
			if(change.isTimedOut()) {
				log.debug("handleTimeout: " + change);
				commitAndClearLocks(change, Status.FailedTimeout);
				this.revisionManager.foundNewHigherCommitedChange(change);
				return true;
			}
		}
		return false;
	}
	
}

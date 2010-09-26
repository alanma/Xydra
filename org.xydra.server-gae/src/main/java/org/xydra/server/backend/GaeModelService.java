package org.xydra.server.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.core.model.state.impl.gae.KeyStructure;
import org.xydra.index.query.Pair;
import org.xydra.server.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public class GaeModelService {
	
	// GAE Entity (type=XCHANGE) property keys.
	
	private static final String PROP_LAST_ACTIVITY = "lastActivity";
	private static final String PROP_LOCKS = "locks";
	private static final String PROP_STATUS = "status";
	private static final String PROP_ACTOR = "actor";
	
	// status IDs
	
	/**
	 * Possible status progression:
	 * 
	 * <pre>
	 * 
	 *  STATUS_CREATING ------> STATUS_FAILED_TIMEOUT
	 *    |       |
	 *    |       v
	 *    | STATUS_CHECKING
	 *    |       |
	 *    \---+---/
	 *        |
	 *        |----> STATUS_EXECUTING ----> STATUS_SUCCESS_EXECUTED
	 *        |
	 *        |----> STATUS_SUCCESS_NOCHANGE
	 *        |
	 *        \----> STATUS_FAILED_PRECONDITIONS
	 * 
	 * </pre>
	 */
	
	/** assigned revision, waiting for locks */
	private static final int STATUS_CREATING = 0;
	/**
	 * wrote command, waiting for locks and/or checking preconditions
	 * 
	 * This is needed to prevent cascading timeouts if one change takes too
	 * long. Normally the command will not be written, only if waiting for locks
	 * takes too long / before we start to roll forward another change.
	 */
	private static final int STATUS_CHECKING = 1;
	/** events written, making changes, a.k.a. readyToExecute */
	private static final int STATUS_EXECUTING = 2;
	
	/** changes made, locks freed */
	private static final int STATUS_SUCCESS_EXECUTED = 3;
	/** there was nothing to change */
	private static final int STATUS_SUCCESS_NOCHANGE = 4;
	
	private static final int STATUS_FAILED_PRECONDITIONS = 100;
	/** timed out before saving command/events (status was STATUS_CREATING) */
	private static final int STATUS_FAILED_TIMEOUT = 101;
	
	// Parameters for waiting for other changes.
	
	// timeout for changes in milliseconds
	private static final long TIMEOUT = 3000; // TODO set
	
	// Initial time to wait before checking status again.
	private static final long WAIT_INITIAL = 100;
	// Maximum time to wait before checking status again.
	private static final long WAIT_MAX = 1000; // TODO set
	
	// Implementation.
	
	private final XAddress modelAddr;
	
	public GaeModelService(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		
		// IMPROVE maybe let the caller provide an XID that can be used to check
		// the status in case there is a GAE timeout?
		
		assert this.modelAddr.contains(command.getChangedEntity() == null ? command.getTarget()
		        : command.getChangedEntity());
		
		Set<XAddress> locks = calculateRequiredLocks(command);
		
		Pair<Long,Entity> result = grabRevisionAndRegisterLocks(locks, actorId);
		long rev = result.getFirst();
		Entity changeEntity = result.getSecond();
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		
		waitForLocks(rev, locks);
		
		List<XAtomicEvent> events = checkPreconditionsAndSaveEvents(rev, changeEntity, command);
		if(events == null) {
			return XCommand.FAILED;
		} else if(events.isEmpty()) {
			// TODO maybe return revision?
			return XCommand.NOCHANGE;
		}
		
		executeAndUnlock(rev, changeEntity, events);
		
		return rev;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	private long getCachedLastCommitedRevision() {
		return -1L; // TODO implement
	}
	
	private void setCachedLastCommitedRevision(long l) {
		// TODO implement
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	private long getCachedLastTakenRevision() {
		return -1L; // TODO implement
	}
	
	private void setCachedLastTakenRevision(long rev) {
		// TODO implement
	}
	
	private Pair<Long,Entity> grabRevisionAndRegisterLocks(Set<XAddress> locks, XID actorId) {
		
		// Prepare locks to be saved in GAE entity.
		List<String> lockStrs = new ArrayList<String>(locks.size());
		for(XAddress a : locks) {
			lockStrs.add(a.toURI());
		}
		
		for(long rev = getCachedLastTakenRevision() + 1;; rev++) {
			
			// Try to grab this revision.
			try {
				Key key = KeyStructure.createChangetKey(this.modelAddr, rev);
				XStateTransaction trans = GaeUtils.beginTransaction();
				
				Entity changeEntity = GaeUtils.getEntity(key, trans);
				
				if(changeEntity == null) {
					
					Entity newChange = new Entity(key);
					newChange.setUnindexedProperty(PROP_LOCKS, lockStrs);
					newChange.setUnindexedProperty(PROP_STATUS, STATUS_CREATING);
					newChange.setUnindexedProperty(PROP_LAST_ACTIVITY, now());
					newChange.setUnindexedProperty(PROP_ACTOR, actorId.toURI());
					
					GaeUtils.putEntity(newChange, trans);
					GaeUtils.endTransaction(trans);
					
					setCachedLastTakenRevision(rev);
					
					// transaction succeeded, we have a revision
					return new Pair<Long,Entity>(rev, newChange);
					
				} else {
					
					// Revision already taken.
					
					// IMPROVE check if commit() can fail for read-only
					// transactions, maybe use abort()?
					GaeUtils.endTransaction(trans);
					
					// IMPROVE cache this entity for later use in
					// waitForLocks()? / lastKnownCommitted calculation?
					
					// Since we read the entity anyway, might as well use that
					// information.
					int status = (Integer)changeEntity.getProperty(PROP_STATUS);
					if(!isCommitted(status) && !canRollForward(status) && isTimedOut(changeEntity)) {
						cleanupChangeEntity(changeEntity, STATUS_FAILED_TIMEOUT);
					}
					
				}
				
			} catch(DatastoreFailureException dfe) {
				// transaction failed, continue to next revision
			}
			
		}
		
		// unreachable
	}
	
	@SuppressWarnings("unchecked")
	private void waitForLocks(long ownRev, Set<XAddress> ownLocks) {
		
		long commitedRev = getCachedLastCommitedRevision();
		
		// Track if we find a greater last commitedRev.
		long newCommitedRev = -1;
		
		for(long otherRev = ownRev - 1; otherRev > commitedRev; otherRev--) {
			
			Key key = KeyStructure.createChangetKey(this.modelAddr, otherRev);
			Entity otherChange = GaeUtils.getEntity(key);
			assert otherChange != null;
			
			// Check if the change is committed.
			int status = (Integer)otherChange.getProperty(PROP_STATUS);
			if(isCommitted(status)) {
				if(newCommitedRev < 0) {
					newCommitedRev = otherRev;
				}
				// finished, so should have no locks
				continue;
			}
			
			// Check if the change needs conflicting locks.
			List<String> lockStrs = (List<String>)otherChange.getProperty(PROP_LOCKS);
			assert lockStrs != null : "locks should not be removed before change is commited";
			Set<XAddress> otherLocks = new HashSet<XAddress>((int)(lockStrs.size() / 0.75));
			for(String s : lockStrs) {
				otherLocks.add(XX.toAddress(s));
			}
			if(!isConflicting(ownLocks, otherLocks)) {
				newCommitedRev = -1;
				// not conflicting, so ignore
				continue;
			}
			
			// uncommitted, conflicting locks => need to wait
			
			long waitTime = WAIT_INITIAL;
			boolean timedOut;
			while(!(timedOut = isTimedOut(otherChange))) {
				
				// IMPROVE save own command if waitTime is too long (so that we
				// can be rolled forward in case of timeout)
				try {
					Thread.sleep(waitTime);
				} catch(InterruptedException e) {
					// ignore interrupt
				}
				// IMPROVE update own lastActivity?
				
				otherChange = GaeUtils.getEntity(key);
				assert otherChange != null;
				
				status = (Integer)otherChange.getProperty(PROP_STATUS);
				if(isCommitted(status)) {
					// now finished, so should have no locks anymore
					break;
				}
				
				// IMPROVE allow to update the locks and re-check them here?
				
				// increase wait time exponentially
				waitTime *= 2;
				if(waitTime > WAIT_MAX) {
					waitTime = WAIT_MAX;
				}
			}
			
			if(timedOut) {
				if(canRollForward(status)) {
					// IMPROVE save own command so that we can be rolled
					// forward in case of timeout
					rollForward(otherRev, otherChange);
				} else {
					cleanupChangeEntity(otherChange, STATUS_FAILED_TIMEOUT);
				}
			}
			
			// other change is now committed
			if(newCommitedRev < 0) {
				newCommitedRev = otherRev;
			}
			
			// IMPROVE: maybe re-read commitedRev?
		}
		
		if(newCommitedRev > 0) {
			setCachedLastCommitedRevision(newCommitedRev);
		}
	}
	
	private List<XAtomicEvent> checkPreconditionsAndSaveEvents(long rev, Entity changeEntity,
	        XCommand command) {
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		// TODO check preconditions and generate events
		
		if(events.isEmpty()) {
			cleanupChangeEntity(changeEntity, STATUS_SUCCESS_NOCHANGE);
			return events;
		}
		
		// TODO write events
		// IMPROVE free unneeded locks?
		
		changeEntity.setUnindexedProperty(PROP_STATUS, STATUS_EXECUTING);
		GaeUtils.putEntity(changeEntity);
		
		return events;
	}
	
	private void executeAndUnlock(long rev, Entity changeEntity, List<XAtomicEvent> events) {
		
		// TODO execute
		
		cleanupChangeEntity(changeEntity, STATUS_SUCCESS_EXECUTED);
	}
	
	long getRevisionFromKey(Key key) {
		assert key.getKind() == KeyStructure.KIND_XCHANGE;
		String keyStr = key.getName();
		int p = keyStr.lastIndexOf("/");
		assert p > 0;
		String revStr = keyStr.substring(p + 1);
		return Long.parseLong(revStr);
	}
	
	private boolean rollForward(long rev, Entity changeEntity) {
		
		int status = (Integer)changeEntity.getProperty(PROP_STATUS);
		assert !isCommitted(status) && canRollForward(status);
		
		assert getRevisionFromKey(changeEntity.getKey()) == rev;
		
		List<XAtomicEvent> events;
		if(status == STATUS_CHECKING) {
			XCommand command = loadCommand(rev, changeEntity);
			assert command != null;
			events = checkPreconditionsAndSaveEvents(rev, changeEntity, command);
			if(events == null) {
				return false;
			} else if(events.isEmpty()) {
				return true;
			}
		} else {
			assert status == STATUS_EXECUTING;
			events = loadEvents(rev, changeEntity);
		}
		
		executeAndUnlock(rev, changeEntity, events);
		
		return true;
	}
	
	private List<XAtomicEvent> loadEvents(long rev, Entity eventEntity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private XCommand loadCommand(long rev, Entity eventEntity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void cleanupChangeEntity(Entity changeEntity, int status) {
		changeEntity.removeProperty(PROP_LOCKS);
		changeEntity.setUnindexedProperty(PROP_STATUS, status);
		GaeUtils.putEntity(changeEntity);
	}
	
	private static long now() {
		return System.currentTimeMillis();
	}
	
	private boolean canRollForward(int status) {
		return status == STATUS_CHECKING || status == STATUS_EXECUTING;
	}
	
	/**
	 * Is the status either "success" or "failed".
	 */
	private boolean isCommitted(int status) {
		return (isSuccess(status) || isFailure(status));
	}
	
	private boolean isSuccess(int status) {
		return (status == STATUS_SUCCESS_EXECUTED || status == STATUS_SUCCESS_NOCHANGE);
	}
	
	private boolean isFailure(int status) {
		return (status == STATUS_FAILED_PRECONDITIONS || status == STATUS_FAILED_TIMEOUT);
	}
	
	private boolean isTimedOut(Entity changeEntity) {
		long timer = (Long)changeEntity.getProperty(PROP_LAST_ACTIVITY);
		return now() - timer > TIMEOUT;
	}
	
	/**
	 * @return true if the given set contains any locks that imply the given
	 *         lock (but are not the same).
	 */
	private static boolean hasMoreGeneralLock(Set<XAddress> locks, XAddress lock) {
		XAddress l = lock.getParent();
		while(l != null) {
			if(l.contains(l)) {
				return true;
			}
			l = l.getParent();
		}
		return false;
	}
	
	/**
	 * Calculate the locks required to execute the given command.
	 */
	private static Set<XAddress> calculateRequiredLocks(XCommand command) {
		
		Set<XAddress> locks = new HashSet<XAddress>();
		if(command instanceof XTransaction) {
			
			XTransaction trans = (XTransaction)command;
			Set<XAddress> tempLocks = new HashSet<XAddress>();
			for(XAtomicCommand ac : trans) {
				XAddress lock = ac.getChangedEntity();
				assert lock != null;
				tempLocks.add(lock);
			}
			for(XAddress lock : tempLocks) {
				if(!hasMoreGeneralLock(tempLocks, lock)) {
					locks.add(lock);
				}
			}
			
		} else {
			XAddress lock = command.getChangedEntity();
			assert lock != null;
			locks.add(lock);
		}
		
		return locks;
	}
	
	/**
	 * Check if the two sets of locks conflict.
	 */
	private static boolean isConflicting(Set<XAddress> a, Set<XAddress> b) {
		for(XAddress lock : a) {
			if(b.contains(lock) || hasMoreGeneralLock(b, lock)) {
				return true;
			}
		}
		for(XAddress lock : b) {
			if(hasMoreGeneralLock(a, lock)) {
				return true;
			}
		}
		return false;
	}
	
}

package org.xydra.server.impl.newgae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.impl.memory.AbstractChangeLog;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.XI;
import org.xydra.index.query.Pair;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;


/**
 * A class responsible for executing and logging changes to a specific XModel in
 * the GAE datastore.
 * 
 * @author dscharrer
 * 
 */
public class GaeChangesService extends AbstractChangeLog implements XChangeLog {
	
	private static final long serialVersionUID = -2080744796962188941L;
	
	// GAE Entity (type=XCHANGE) property keys.
	
	private static final String PROP_LAST_ACTIVITY = "lastActivity";
	/**
	 * Set when entering {@link #STATUS_CREATING}, removed when entering
	 * {@link #STATUS_SUCCESS_EXECUTED}, {@link #STATUS_SUCCESS_NOCHANGE},
	 * {@link #STATUS_FAILED_TIMEOUT} or {@link #STATUS_FAILED_PRECONDITIONS}.
	 */
	private static final String PROP_LOCKS = "locks";
	private static final String PROP_STATUS = "status";
	/**
	 * Set when entering {@link #STATUS_CREATING}, never removed.
	 */
	private static final String PROP_ACTOR = "actor";
	/**
	 * Set when entering {@link #STATUS_EXECUTING}, never removed.
	 */
	private static final String PROP_EVENTCOUNT = "eventCount";
	
	// GAE Entity (type=XEVENT) property keys.
	
	private static final String PROP_EVENTCONTENT = "eventContent";
	
	// status IDs
	
	/**
	 * Possible status progression:
	 * 
	 * <pre>
	 * 
	 *  STATUS_CREATING ------> STATUS_FAILED_TIMEOUT
	 *    |       |
	 *    |       v
	 *    | STATUS_CHECKING (IMPROVE not implemented yet)
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
	
	public GaeChangesService(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		
		// IMPROVE maybe let the caller provide an XID that can be used to check
		// the status in case there is a GAE timeout?
		
		assert this.modelAddr.equalsOrContains(command.getChangedEntity()) : "cannot handle command "
		        + command;
		
		Set<XAddress> locks = calculateRequiredLocks(command);
		
		// TODO keep track of own timeout and give up if exceeded
		Pair<Long,Entity> result = grabRevisionAndRegisterLocks(locks, actorId);
		long rev = result.getFirst();
		Entity changeEntity = result.getSecond();
		
		// IMPROVE save command to be able to roll back in case of timeout while
		// waiting for locks / checking preconditions?
		
		waitForLocks(rev, locks);
		
		List<XAtomicEvent> events = checkPreconditionsAndSaveEvents(rev, changeEntity, command,
		        locks, actorId);
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
		return getCachedLastCommitedRevision(); // TODO implement
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
				Key key = KeyStructure.createChangeKey(this.modelAddr, rev);
				Transaction trans = GaeUtils.beginTransaction();
				
				Entity changeEntity = GaeUtils.getEntity(key, trans);
				
				if(changeEntity == null) {
					
					Entity newChange = new Entity(key);
					newChange.setUnindexedProperty(PROP_LOCKS, lockStrs);
					newChange.setUnindexedProperty(PROP_STATUS, STATUS_CREATING);
					newChange.setUnindexedProperty(PROP_LAST_ACTIVITY, now());
					if(actorId != null) {
						newChange.setUnindexedProperty(PROP_ACTOR, actorId.toURI());
					}
					
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
					int status = getStatus(changeEntity);
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
	
	private int getStatus(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_STATUS);
		assert n != null : "All change entities should have a status";
		return n.intValue();
	}
	
	private void waitForLocks(long ownRev, Set<XAddress> ownLocks) {
		
		long commitedRev = getCachedLastCommitedRevision();
		
		// Track if we find a greater last commitedRev.
		long newCommitedRev = -1;
		
		for(long otherRev = ownRev - 1; otherRev > commitedRev; otherRev--) {
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, otherRev);
			Entity otherChange = GaeUtils.getEntity(key);
			assert otherChange != null;
			
			// Check if the change is committed.
			int status = getStatus(otherChange);
			if(isCommitted(status)) {
				if(newCommitedRev < 0) {
					newCommitedRev = otherRev;
				}
				// finished, so should have no locks
				continue;
			}
			
			// Check if the change needs conflicting locks.
			Set<XAddress> otherLocks = loadLocks(otherChange);
			assert otherLocks != null : "locks should not be removed before change is commited";
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
				
				status = getStatus(otherChange);
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
	
	@SuppressWarnings("unchecked")
	private Set<XAddress> loadLocks(Entity changeEntity) {
		List<String> lockStrs = (List<String>)changeEntity.getProperty(PROP_LOCKS);
		if(lockStrs == null) {
			return null;
		}
		Set<XAddress> otherLocks = new HashSet<XAddress>((int)(lockStrs.size() / 0.75));
		for(String s : lockStrs) {
			otherLocks.add(XX.toAddress(s));
		}
		return otherLocks;
	}
	
	private List<XAtomicEvent> checkPreconditionsAndSaveEvents(long rev, Entity changeEntity,
	        XCommand command, Set<XAddress> locks, XID actorId) {
		
		XBaseModel currentModel = InternalGaeModel.get(this, rev - 1, locks);
		
		List<XAtomicEvent> events = GaeEventHelper.checkCommandAndCreateEvents(currentModel,
		        command, actorId, rev);
		
		if(events == null) {
			cleanupChangeEntity(changeEntity, STATUS_FAILED_PRECONDITIONS);
			return null;
		}
		
		if(events.isEmpty()) {
			cleanupChangeEntity(changeEntity, STATUS_SUCCESS_NOCHANGE);
			return events;
		}
		
		Transaction trans = GaeUtils.beginTransaction();
		
		Key baseKey = changeEntity.getKey();
		for(int i = 0; i < events.size(); i++) {
			XAtomicEvent ae = events.get(i);
			Entity eventEntity = new Entity(KeyStructure.getEventKey(baseKey, i));
			
			// IMPROVE save event in a GAE-specific format
			// TODO don't save the "oldValue" again
			XmlOutStringBuffer out = new XmlOutStringBuffer();
			XmlEvent.toXml(ae, out, this.modelAddr);
			Text text = new Text(out.getXml());
			eventEntity.setUnindexedProperty(PROP_EVENTCONTENT, text);
			
			GaeUtils.putEntity(eventEntity, trans);
			
		}
		
		// IMPROVE free unneeded locks?
		
		Integer eventCount = events.size();
		changeEntity.setUnindexedProperty(PROP_EVENTCOUNT, eventCount);
		
		changeEntity.setUnindexedProperty(PROP_STATUS, STATUS_EXECUTING);
		GaeUtils.putEntity(changeEntity, trans);
		
		GaeUtils.endTransaction(trans);
		
		return events;
	}
	
	private void executeAndUnlock(long rev, Entity changeEntity, List<XAtomicEvent> events) {
		
		for(int i = 0; i < events.size(); i++) {
			XAtomicEvent event = events.get(i);
			
			if(event instanceof XFieldEvent) {
				assert Arrays.asList(ChangeType.REMOVE, ChangeType.ADD, ChangeType.CHANGE)
				        .contains(event.getChangeType());
				if(((XFieldEvent)event).getNewValue() == null) {
					InternalGaeField.set(event.getTarget(), rev);
				} else {
					InternalGaeField.set(event.getTarget(), rev, i);
				}
			} else if(event.getChangeType() == ChangeType.REMOVE) {
				InternalGaeXEntity.remove(event.getChangedEntity());
			} else if(event instanceof XObjectEvent) {
				assert event.getChangeType() == ChangeType.ADD;
				InternalGaeField.set(event.getChangedEntity(), rev);
			} else {
				assert event.getChangeType() == ChangeType.ADD;
				assert event instanceof XModelEvent || event instanceof XRepositoryEvent;
				InternalGaeXEntity.createContainer(event.getChangedEntity());
			}
			
		}
		
		cleanupChangeEntity(changeEntity, STATUS_SUCCESS_EXECUTED);
	}
	
	long getRevisionFromKey(Key key) {
		assert KeyStructure.isChangeKey(key);
		String keyStr = key.getName();
		int p = keyStr.lastIndexOf("/");
		assert p > 0;
		String revStr = keyStr.substring(p + 1);
		return Long.parseLong(revStr);
	}
	
	private boolean rollForward(long rev, Entity changeEntity) {
		
		int status = getStatus(changeEntity);
		assert !isCommitted(status) && canRollForward(status);
		
		assert getRevisionFromKey(changeEntity.getKey()) == rev;
		
		List<XAtomicEvent> events;
		if(status == STATUS_CHECKING) {
			Set<XAddress> locks = loadLocks(changeEntity);
			XCommand command = loadCommand(rev, changeEntity);
			waitForLocks(rev, locks);
			assert command != null;
			XID actorId = getActor(changeEntity);
			events = checkPreconditionsAndSaveEvents(rev, changeEntity, command, locks, actorId);
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
	
	private XID getActor(Entity changeEntity) {
		String actorStr = (String)changeEntity.getProperty(PROP_ACTOR);
		if(actorStr == null) {
			return null;
		}
		return XX.toId(actorStr);
	}
	
	private List<XAtomicEvent> loadEvents(long rev, Entity eventEntity) {
		
		assert Arrays.asList(STATUS_EXECUTING, STATUS_SUCCESS_EXECUTED).contains(
		        eventEntity.getProperty(PROP_STATUS));
		assert eventEntity.getProperty(PROP_EVENTCOUNT) != null;
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		int eventCount = getEventCount(eventEntity);
		
		for(int i = 0; i < eventCount; i++) {
			events.add(getAtomicEvent(rev, i));
		}
		
		return events;
	}
	
	private XCommand loadCommand(long rev, Entity eventEntity) {
		
		assert getStatus(eventEntity) == STATUS_CHECKING;
		
		// TODO implement when commands are saved
		assert false : "commands are not saved yet";
		
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
				// IMPROVE ADD events don't need to lock the whole added entity
				// (they don't care if children change)
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
	
	protected static boolean canRead(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(addr.equalsOrContains(lock) || lock.contains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	protected static boolean canWrite(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(lock.equalsOrContains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	public XAtomicEvent getAtomicEvent(long revisionNumber, int transindex) {
		
		Key changeKey = KeyStructure.createChangeKey(this.modelAddr, revisionNumber);
		Key eventKey = KeyStructure.getEventKey(changeKey, transindex);
		
		// IMPROVE cache events
		Entity eventEntity = GaeUtils.getEntity(eventKey);
		if(eventEntity == null) {
			return null;
		}
		Text eventData = (Text)eventEntity.getProperty(PROP_EVENTCONTENT);
		
		MiniElement eventElement = new MiniXMLParserImpl().parseXml(eventData.getValue());
		
		return XmlEvent.toAtomicEvent(eventElement, this.modelAddr);
	}
	
	public XAddress getBaseAddress() {
		return this.modelAddr;
	}
	
	public long getCurrentRevisionNumber() {
		
		long currentRev = getCachedLastCommitedRevision();
		
		// Check if the revision is up to date.
		while(true) {
			
			Key key = KeyStructure.createChangeKey(this.modelAddr, currentRev + 1);
			Entity changeEntity = GaeUtils.getEntity(key);
			if(changeEntity == null) {
				break;
			}
			
			int status = getStatus(changeEntity);
			if(!isCommitted(status)) {
				break;
			}
			
			currentRev++;
		}
		
		setCachedLastCommitedRevision(currentRev);
		
		return currentRev;
	}
	
	public XEvent getEventAt(long rev) {
		
		Entity changeEntity = GaeUtils.getEntity(KeyStructure.createChangeKey(this.modelAddr, rev));
		
		if(changeEntity == null) {
			return null;
		}
		
		int status = getStatus(changeEntity);
		
		if(status != STATUS_EXECUTING && status != STATUS_SUCCESS_EXECUTED) {
			// no events available (yet) for this revision.
			return null;
		}
		
		int eventCount = getEventCount(changeEntity);
		assert eventCount > 0 : "executed changes should have at least one event";
		
		XID actor = getActor(changeEntity);
		
		if(eventCount > 1) {
			
			// IMPROVE cache transaction event
			return new GaeTransactionEvent(this, eventCount, actor, rev);
			
		} else {
			
			XAtomicEvent ae = getAtomicEvent(rev, 0);
			assert ae != null;
			assert XI.equals(actor, ae.getActor());
			assert this.modelAddr.equalsOrContains(ae.getChangedEntity());
			assert ae.getChangeType() != ChangeType.TRANSACTION;
			assert !ae.inTransaction();
			return ae;
			
		}
		
	}
	
	private int getEventCount(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_EVENTCOUNT);
		if(n == null) {
			return 0;
		}
		return n.intValue();
	}
	
	public long getFirstRevisionNumber() {
		return 0;
	}
	
}

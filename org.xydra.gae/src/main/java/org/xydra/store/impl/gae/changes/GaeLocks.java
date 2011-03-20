package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;


/**
 * Implementation of the lock semantics used by {@link GaeChangesService}.
 * 
 * @author dscharrer
 * 
 */
public class GaeLocks {
	
	private GaeLocks() {
		// static class, do not construct
	}
	
	/**
	 * @return true if the specified locks are sufficient to write to the entity
	 *         at the given address.
	 */
	protected static boolean canWrite(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(lock.equalsOrContains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return true if the specified locks are sufficient to read from the
	 *         entity at the given address.
	 */
	protected static boolean canRead(XAddress addr, Set<XAddress> locks) {
		for(XAddress lock : locks) {
			if(addr.equalsOrContains(lock) || lock.contains(addr)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return if the two sets of locks conflict, i.e. one of them requires a
	 *         lock that is implied by the other set (wild-card locks are
	 *         respected).
	 */
	protected static boolean isConflicting(Set<XAddress> a, Set<XAddress> b) {
		for(XAddress lock : a) {
			if(b.contains(lock) || GaeLocks.hasMoreGeneralLock(b, lock)) {
				return true;
			}
		}
		for(XAddress lock : b) {
			if(GaeLocks.hasMoreGeneralLock(a, lock)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * An address in the locks means that exclusive access is required to the
	 * entity referred to by that address, as well as all descendant entities.
	 * Also, a read lock on the ancestors of that entity is implied.
	 * 
	 * @param command The command to calculate the locks for.
	 * @return the calculated locks required to execute the given command.
	 */
	protected static Set<XAddress> calculateRequiredLocks(XCommand command) {
		
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
				if(!GaeLocks.hasMoreGeneralLock(tempLocks, lock)) {
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
	 * Handles wild-cards in locks.
	 * 
	 * @return true if the given set contains any locks that imply the given
	 *         lock (but are not the same).
	 */
	protected static boolean hasMoreGeneralLock(Set<XAddress> locks, XAddress lock) {
		XAddress l = lock.getParent();
		while(l != null) {
			if(l.contains(l)) {
				return true;
			}
			l = l.getParent();
		}
		return false;
	}
	
}

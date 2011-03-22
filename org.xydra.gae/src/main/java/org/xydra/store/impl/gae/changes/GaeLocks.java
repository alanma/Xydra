package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XX;
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
	
	private static final long serialVersionUID = 4334940263327007176L;
	
	private final Set<XAddress> locks;
	
	/**
	 * Get the locks needed to execute the given {@link XCommand}.
	 * 
	 * An address in the locks means that exclusive access is required to the
	 * entity referred to by that address, as well as all descendant entities.
	 * Also, a read lock on the ancestors of that entity is implied.
	 * 
	 * @param command The command to calculate the locks for.
	 */
	protected GaeLocks(XCommand command) {
		
		this.locks = new HashSet<XAddress>();
		
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
				if(!hasMoreGeneralLock(lock)) {
					this.locks.add(lock);
				}
			}
			
		} else {
			XAddress lock = command.getChangedEntity();
			assert lock != null;
			this.locks.add(lock);
		}
		
	}
	
	/**
	 * Load a set of locks.
	 * 
	 * @param encodedLocks The locks to load.
	 */
	protected GaeLocks(List<String> encodedLocks) {
		this.locks = new HashSet<XAddress>((int)(encodedLocks.size() / 0.75));
		for(String s : encodedLocks) {
			this.locks.add(XX.toAddress(s));
		}
	}
	
	public List<String> encode() {
		List<String> lockStrs = new ArrayList<String>(this.locks.size());
		for(XAddress a : this.locks) {
			lockStrs.add(a.toURI());
		}
		return lockStrs;
	}
	
	/**
	 * @return true if the specified locks are sufficient to write to the entity
	 *         at the given address.
	 */
	protected boolean canWrite(XAddress addr) {
		for(XAddress lock : this.locks) {
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
	protected boolean canRead(XAddress addr) {
		for(XAddress lock : this.locks) {
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
	protected boolean isConflicting(GaeLocks other) {
		for(XAddress lock : this.locks) {
			if(other.locks.contains(lock) || other.hasMoreGeneralLock(lock)) {
				return true;
			}
		}
		for(XAddress lock : other.locks) {
			if(hasMoreGeneralLock(lock)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Handles wild-cards in locks.
	 * 
	 * @return true if the given set contains any locks that imply the given
	 *         lock (but are not the same).
	 */
	private boolean hasMoreGeneralLock(XAddress lock) {
		XAddress l = lock.getParent();
		while(l != null) {
			if(this.locks.contains(l)) {
				return true;
			}
			l = l.getParent();
		}
		return false;
	}
	
	/**
	 * @return true if the specified locks are sufficient to delete the entity
	 *         at the given address.
	 */
	public boolean canRemove(XAddress addr) {
		return canWrite(addr);
	}
	
}

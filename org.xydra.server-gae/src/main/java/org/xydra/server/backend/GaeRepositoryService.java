package org.xydra.server.backend;

import java.util.HashSet;
import java.util.Set;

import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.memcache.MemcacheServicePb.MemcacheService;


public class GaeRepositoryService implements XRepositoryService {
	
	private final XID repoId;
	@SuppressWarnings("unused")
	private DatastoreService ds;
	@SuppressWarnings("unused")
	private MemcacheService ms;
	
	public GaeRepositoryService(XID repoId) {
		if(repoId == null) {
			throw new NullPointerException("repoId may not be null");
		}
		this.repoId = repoId;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		
		XAddress target = command.getTarget();
		if(!this.repoId.equals(target.getRepository())) {
			return XCommand.FAILED;
		}
		
		Set<XAddress> locks = calculateRequiredLocks(command);
		
		// TODO grab revision number, mark as creating
		long rev = 0;
		
		// TODO register own locks
		// TODO save command?
		
		// TODO examine other locks
		if(isConflicting(locks, null)) {
			// TODO wait
			// TODO cleanup or roll forward other
		}
		
		// TODO check preconditions
		boolean preconditionsFailed = false;
		if(preconditionsFailed) {
			// TODO unlock
			// TODO mark as failedPreconditions
			return XCommand.FAILED;
		}
		
		// TODO list planned changes
		// IMPROVE free uneeded locks?
		// TODO mark as readyToExecute
		
		// TODO execute
		
		// TODO unlock
		// TODO mark as executed
		
		return rev;
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public XBaseModel getModelSnapshot(XID modelId, long revision) {
		// TODO Auto-generated method stub
		return null;
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

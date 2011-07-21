package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeOperation;


/**
 * There is one revision cache per {@link XModel}.
 * 
 * A model has a <em>current revision number</em>. It is incremented every time
 * a change operation succeeds. Not necessarily only one step.
 * 
 * 
 * 
 * 
 * 
 * The order of revision number is this:
 * 
 * @author dscharrer
 */
class RevisionCache {
	
	/**
	 * How many milliseconds to consider the locally cached value valid
	 */
	private static final long LOCAL_VM_CACHE_TIMEOUT = 500;
	
	private final String commitedRevCacheName;
	
	/**
	 * The name of the cached value used by {@link #getCurrentModelRev()} and
	 * {@link #setCurrentModelRev(long)}
	 */
	private final String currentRevCacheName;
	
	@GaeOperation()
	RevisionCache(XAddress modelAddr) {
		this.commitedRevCacheName = modelAddr + "-commitedRev";
		this.currentRevCacheName = modelAddr + "-currentRev";
	}
	
	// IMPROVE don't call the memcache for every request, complement it with a
	// local VM cache
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	@GaeOperation(memcacheRead = true)
	// FIXME all ops
	protected long getLastCommited() {
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long entry = (Long)cache.get(this.commitedRevCacheName);
		long rev = (entry == null) ? -1L : entry;
		
		long current = getCurrentModelRev();
		
		return (current > rev ? current : rev);
	}
	
	protected long getLastCommitedIfSet() {
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long entry = (Long)cache.get(this.commitedRevCacheName);
		return (entry == null) ? NOT_SET : entry;
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		increaseCachedValue(this.commitedRevCacheName, l);
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	protected long getLastTaken() {
		return getLastCommited(); // TODO implement
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastTaken(long rev) {
		// TODO implement
	}
	
	/** Local VM cache of the "current" revision number. */
	private long localVmCacheCurrentModelRev = NOT_SET;
	/** Age of Local VM cache of the "current" revision number. */
	private long localVmCacheCurrentModelRevTime = -1;
	
	/**
	 * @return a cached value of the current revision number as defined by
	 *         {@link GaeChangesService#getCurrentRevisionNumber()}.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 */
	protected long getCurrentModelRev() {
		long rev = getCurrentModelRevIfSet();
		return (rev == NOT_SET) ? -1L : rev;
	}
	
	protected static final long NOT_SET = -2L;
	
	/**
	 * Retrieve a cached value of the current revision number as defined by
	 * {@link GaeChangesService#getCurrentRevisionNumber()}.
	 * 
	 * The returned value may be less that the actual "current" revision number,
	 * but is guaranteed to never be greater.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentModelRevIfSet() {
		// localVmCache
		synchronized(this) {
			long now = System.currentTimeMillis();
			if(now < this.localVmCacheCurrentModelRevTime + LOCAL_VM_CACHE_TIMEOUT) {
				return this.localVmCacheCurrentModelRev;
			}
		}
		// memCache
		IMemCache cache = XydraRuntime.getMemcache();
		Long value = (Long)cache.get(this.currentRevCacheName);
		return (value == null) ? NOT_SET : value;
	}
	
	/**
	 * Set a new value to be returned by {@link #getCurrentModelRev()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setCurrentModelRev(long l) {
		
		synchronized(this) {
			if(this.localVmCacheCurrentModelRev >= l) {
				return;
			}
		}
		
		long val = increaseCachedValue(this.currentRevCacheName, l);
		
		synchronized(this) {
			if(val >= this.localVmCacheCurrentModelRev) {
				this.localVmCacheCurrentModelRev = val;
				this.localVmCacheCurrentModelRevTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Increase a cached {@link Long} value.
	 * 
	 * @param cachname The value to increase.
	 * @param l The new value to set. Ignored if it is less than the current
	 *            value.
	 */
	@GaeOperation(memcacheRead = true ,memcacheWrite = true)
	private long increaseCachedValue(String cachname, long l) {
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long current = (Long)cache.get(cachname);
		if(current != null && current > l) {
			return current;
		}
		
		Long value = l;
		while(true) {
			Long old = (Long)cache.put(cachname, value);
			if(old == null || old <= value) {
				return value;
			}
			value = old;
		}
	}
	
}

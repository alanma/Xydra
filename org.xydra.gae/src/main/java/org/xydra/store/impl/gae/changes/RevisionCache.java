package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraRuntime;


/**
 * There is one revision cache per {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
public class RevisionCache {
	
	private final XAddress modelAddr;
	
	public RevisionCache(XAddress modelAddr) {
		this.modelAddr = modelAddr;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	protected long getLastCommited() {
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long entry = (Long)cache.get(getCommitedRevCacheName());
		long rev = (entry == null) ? -1L : entry;
		
		long current = getCurrent();
		
		return (current > rev ? current : rev);
	}
	
	protected long getLastCommitedIfSet() {
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long entry = (Long)cache.get(getCommitedRevCacheName());
		return (entry == null) ? NOT_SET : entry;
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		increaseCachedValue(getCommitedRevCacheName(), l);
	}
	
	private String getCommitedRevCacheName() {
		return this.modelAddr + "-commitedRev";
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
	
	/**
	 * Retrieve a cached value of the current revision number as defined by
	 * {@link #getCurrentRevisionNumber()}.
	 * 
	 * The returned value may be less that the actual "current" revision number,
	 * but is guaranteed to never be greater.
	 */
	protected long getCurrent() {
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long value = (Long)cache.get(getCurrentRevCacheName());
		return (value == null) ? -1L : value;
	}
	
	protected static final long NOT_SET = -2L;
	
	/**
	 * Retrieve a cached value of the current revision number as defined by
	 * {@link #getCurrentRevisionNumber()}.
	 * 
	 * The returned value may be less that the actual "current" revision number,
	 * but is guaranteed to never be greater.
	 */
	protected long getCurrentIfSet() {
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long value = (Long)cache.get(getCurrentRevCacheName());
		return (value == null) ? NOT_SET : value;
	}
	
	/**
	 * Set a new value to be returned by {@link #getCurrent()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setCurrent(long l) {
		increaseCachedValue(getCurrentRevCacheName(), l);
	}
	
	/**
	 * @return the name of the cached value used by {@link #getCurrent()} and
	 *         {@link #setCurrent(long)}
	 */
	protected String getCurrentRevCacheName() {
		return this.modelAddr + "-currentRev";
	}
	
	/**
	 * Increase a cached {@link Long} value.
	 * 
	 * @param cachname The value to increase.
	 * @param l The new value to set. Ignored if it is less than the current
	 *            value.
	 */
	private void increaseCachedValue(String cachname, long l) {
		IMemCache cache = XydraRuntime.getMemcache();
		
		Long current = (Long)cache.get(cachname);
		if(current != null && current > l) {
			return;
		}
		
		Long value = l;
		while(true) {
			Long old = (Long)cache.put(cachname, value);
			if(old == null || old <= value) {
				break;
			}
			value = old;
		}
	}
	
}

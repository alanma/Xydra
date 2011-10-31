package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.IMemCache.IdentifiableValue;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeOperation;

import com.google.appengine.api.memcache.MemcacheServiceException;


/**
 * Can read from and write to memcache.
 * 
 * For concurrency, the original value read from memcache is remembered. On
 * write, a conditional put is used.
 */
public class MemcacheRevisionManager {
	
	private static final Logger log = LoggerFactory.getLogger(MemcacheRevisionManager.class);
	
	/**
	 * Compile time flag to disable the functionality of
	 * {@link #writeToMemcache()} and {@link #loadFromMemcache()}
	 */
	private static final boolean USE_MEMCACHE = true;
	
	/**
	 * @return name of cache entry in memcache
	 */
	public final String getCacheName() {
		return this.modelAddress + "/MemcacheRevisionInfo";
	}
	
	/** as loaded from memcache */
	private IdentifiableValue loadedValue;
	private RevisionInfo memcacheEntry;
	
	/**
	 * @return true if we have read from memcache at least once. Local state
	 *         might still be undefined, if memcache was empty.
	 */
	public boolean hasReadFromMemcache() {
		return this.loadedValue != null;
	}
	
	public boolean gotDataFromMemcache() {
		return this.memcacheEntry != null;
	}
	
	/**
	 * If memcache is disabled, do nothing. Else try to load. Worst case:
	 * NOT_SET.
	 */
	public void readFromMemcache() {
		if(!USE_MEMCACHE) {
			return;
		}
		IMemCache cache = XydraRuntime.getMemcache();
		this.loadedValue = cache.getIdentifiable(getCacheName());
		Object o = this.loadedValue.getValue();
		if(o != null) {
			this.memcacheEntry = (RevisionInfo)o;
		}
	}
	
	@Override
	public String toString() {
		return this.modelAddress + "= loaded?" + hasReadFromMemcache() + " " + this.memcacheEntry == null ? null
		        : this.memcacheEntry.toString();
	}
	
	private XAddress modelAddress;
	
	@GaeOperation()
	MemcacheRevisionManager(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		clear();
	}
	
	public void clear() {
		this.loadedValue = null;
		this.memcacheEntry = new RevisionInfo();
	}
	
	/**
	 * Write to memcache if value differs from the one initially loaded.
	 * Overwrites the memcache value only if it has not been changed since
	 * loading -- or if it was null in the memcache.
	 * 
	 */
	public void writeToMemcache(RevisionInfo revisionInfo) {
		log.debug("Write to memcache " + this.modelAddress);
		if(!USE_MEMCACHE) {
			return;
		}
		
		// TODO write only if local value is now higher than last written value
		
		// update locally
		this.memcacheEntry = revisionInfo;
		// update remote
		if(this.loadedValue == null || this.loadedValue.getValue() == null) {
			// write if still null
			IMemCache cache = XydraRuntime.getMemcache();
			try {
				cache.putIfValueIsNull(getCacheName(), this.memcacheEntry);
			} catch(MemcacheServiceException e) {
				log.warn("Could not set revisions in memcache", e);
			}
		} else {
			// write conditional
			IMemCache cache = XydraRuntime.getMemcache();
			cache.putIfUntouched(getCacheName(), this.loadedValue, this.memcacheEntry);
		}
	}
	
	public RevisionInfo getCacheEntry() {
		return this.memcacheEntry;
	}
	
}

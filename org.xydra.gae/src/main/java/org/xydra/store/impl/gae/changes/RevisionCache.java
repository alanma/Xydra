package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.GaeOperation;


/**
 * There is one revision cache per {@link XModel}.
 * 
 * A model has a <em>current revision number</em>. It is incremented every time
 * a change operation succeeds. Not necessarily only one step.
 * 
 * All access to memcache has to triggered explicitly by caller of this class
 * via {@link #loadFromMemcache()} and {@link #writeToMemcache()}.
 * 
 * TODO @Daniel make sure this is thread-safe
 * 
 * The order of revision number is this (highest numbers first):
 * 
 * <pre>
 * ...
 * LAST_TAKEN
 * r98 +-------- real highest taken revision (in-progress-change)
 * r97 + ....... (taken revision)
 * r96 +-------- highest known taken revision (in-progress-change)
 * r95 +         (taken revision)
 * COMMITTED
 * r94 +-------- real highest committed revision (in-progress or failed)  
 * r93 + ....... (committed)
 * r92 +-------- highest known committed revision (in-progress or failed)
 * r91 +         (committed) 
 * CURRENT
 * r90 +-------- real highest succeeded revision = current model version 
 * r89 + ....... (succeeded)
 * r88 +-------- highest known succeeded revision 
 * ...
 * </pre>
 * 
 * Invariants: LAST_TAKEN >= COMMITTED >= CURRENT
 * 
 * @author dscharrer
 * @author xamde
 */
class RevisionCache {
	
	private static final Logger log = LoggerFactory.getLogger(RevisionCache.class);
	
	/**
	 * How many milliseconds to consider the locally cached value valid
	 */
	// FIXME !!! set good value. 10-100 = ok, 1000 - 100000000 = error.
	private static final long LOCAL_VM_CACHE_TIMEOUT = 0;// 10 * 300 *
	                                                     // 24 * 60
	// * 60 * 1000;
	
	public static final boolean USE_LOCALVM_CACHE = false;
	
	protected static final long NOT_SET = -2L;
	
	/**
	 * Compile time flag to disable the functionality of
	 * {@link #writeToMemcache()} and {@link #loadFromMemcache()}
	 */
	// FIXME !!! enable
	private static final boolean USE_MEMCACHE = false;
	
	private static final String REVCACHE_NAME = "[.rc]";
	
	private final String memcacheKeyLastCommitted;
	private final String memcacheKeyLastTaken;
	private final String memcacheKeyCurrent;
	private final ArrayList<String> memcacheKeys = new ArrayList<String>(3);
	
	@SuppressWarnings("unused")
	void writeToMemcache() {
		if(!USE_MEMCACHE)
			return;
		
		log.info("revcache.writememcache");
		long incrCurrent = this.current.getDeltaSinceLastMemcacheAccess();
		long incrLastTaken = this.lastTaken.getDeltaSinceLastMemcacheAccess();
		long incrCommitted = this.committed.getDeltaSinceLastMemcacheAccess();
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		HashMap<String,Long> update = new HashMap<String,Long>();
		update.put(this.memcacheKeyCurrent, incrCurrent);
		update.put(this.memcacheKeyLastCommitted, incrCommitted);
		update.put(this.memcacheKeyLastTaken, incrLastTaken);
		cache.incrementAll(update, NOT_SET);
		this.current.resetLoadedValue();
		this.lastTaken.resetLoadedValue();
		this.committed.resetLoadedValue();
	}
	
	/**
	 * @return true if loaded and local values have been overwritten
	 */
	@SuppressWarnings("unused")
	boolean loadFromMemcache() {
		if(!USE_MEMCACHE)
			return false;
		
		IMemCache cache = XydraRuntime.getMemcache();
		
		Map<String,Object> result = cache.getAll(this.memcacheKeys);
		if(result.isEmpty()) {
			this.current.setLocalValue(NOT_SET);
			this.committed.setLocalValue(NOT_SET);
			this.lastTaken.setLocalValue(NOT_SET);
		} else {
			assert result.containsKey(this.memcacheKeyLastCommitted);
			assert result.containsKey(this.memcacheKeyCurrent);
			assert result.containsKey(this.memcacheKeyLastTaken);
			
			long current = (Long)result.get(this.memcacheKeyCurrent);
			this.current.setLoadedValue(current);
			long committed = (Long)result.get(this.memcacheKeyLastCommitted);
			this.committed.setLoadedValue(current);
			long lastTaken = (Long)result.get(this.memcacheKeyLastTaken);
			this.lastTaken.setLoadedValue(current);
		}
		return true;
	}
	
	private class CachedLong {
		/** Local VM cache of the "current" revision number. */
		private long value;
		/** Creation date of Local VM cache of the "current" revision number. */
		private long time;
		/** as loaded from memcache */
		private long loadedValue = NOT_SET;
		private final String name;
		
		private CachedLong(String name) {
			this.name = name;
			this.value = NOT_SET;
			this.time = NOT_SET;
		}
		
		public void resetLoadedValue() {
			this.loadedValue = this.value;
		}
		
		/**
		 * @return 0 if no delta or never accessed memcache or no defined local
		 *         value (expire time is ignored)
		 */
		public long getDeltaSinceLastMemcacheAccess() {
			if(this.value == NOT_SET) {
				return 0;
			} else if(this.loadedValue == NOT_SET) {
				return 0;
			} else {
				// both defined
				return this.value - this.loadedValue;
			}
		}
		
		public void setLoadedValue(long value) {
			this.loadedValue = value;
			setLocalValue(value);
		}
		
		/**
		 * @return true if value cached in local JVM is not too old
		 */
		private boolean hasValidLocalValue() {
			if(!USE_LOCALVM_CACHE) {
				return false;
			}
			long now = System.currentTimeMillis();
			return now < this.time + LOCAL_VM_CACHE_TIMEOUT;
		}
		
		/**
		 * @param value
		 * @return true if value changed
		 */
		private boolean setLocalValueIfHigher(long value) {
			if(value < this.value) {
				log.trace("Avoid setting " + this.value + " back to " + value);
				return false;
			}
			return setLocalValue(value);
		}
		
		private boolean setLocalValue(long value) {
			this.time = System.currentTimeMillis();
			if(value == this.value) {
				return false;
			}
			this.value = value;
			return true;
		}
		
		/**
		 * @return cached value or NOT_SET
		 */
		private long getValue(boolean returnMinusOneForUndefined) {
			synchronized(this) {
				long value = NOT_SET;
				if(this.hasValidLocalValue()) {
					value = this.value;
				}
				if(returnMinusOneForUndefined) {
					if(value == NOT_SET) {
						value = -1;
					}
				}
				return value;
			}
		}
		
		public void clear() {
			this.value = NOT_SET;
			this.time = 0;
			this.loadedValue = NOT_SET;
		}
		
		@Override
		public String toString() {
			return this.name + "=" + this.value + " loaded:" + this.loadedValue + " time:"
			        + this.time + " valid?" + hasValidLocalValue();
		}
	}
	
	private final CachedLong current;
	private final CachedLong committed;
	private final CachedLong lastTaken;
	
	@Override
	public String toString() {
		return this.current.toString() + "\n" + this.committed.toString() + "\n"
		        + this.lastTaken.toString() + "\n";
	}
	
	@GaeOperation()
	RevisionCache(XAddress modelAddr) {
		this.memcacheKeyCurrent = modelAddr + "/rev-current";
		this.memcacheKeyLastCommitted = modelAddr + "/rev-lastcommitted";
		this.memcacheKeyLastTaken = modelAddr + "/rev-lasttaken";
		this.memcacheKeys.add(this.memcacheKeyCurrent);
		this.memcacheKeys.add(this.memcacheKeyLastCommitted);
		this.memcacheKeys.add(this.memcacheKeyLastTaken);
		// init caches
		this.current = new CachedLong("current");
		this.committed = new CachedLong("committed");
		this.lastTaken = new CachedLong("lastTaken");
	}
	
	/**
	 * @return a cached value of the current revision number as defined by
	 *         {@link IGaeChangesService#getCurrentRevisionNumber()}.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentModelRev() {
		long l = this.current.getValue(true);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "current", l));
		return l;
	}
	
	/**
	 * @return a cached value of the current revision number as defined by
	 *         {@link IGaeChangesService#getCurrentRevisionNumber()} or NOT_SET.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentModelRevIfSet() {
		long l = this.current.getValue(false);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "currentIfSet", l));
		return l;
	}
	
	protected long getLastTakenIfSet() {
		long l = this.lastTaken.getValue(false);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastTakenIfSet", l));
		return l;
	}
	
	/**
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getLastCommited() {
		long l = this.lastTaken.getValue(true);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastCommitted", l));
		return l;
	}
	
	protected long getLastCommitedIfSet() {
		long l = this.committed.getValue(false);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastCommittedIfSet", l));
		return l;
	}
	
	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	protected long getLastTaken() {
		long l = this.lastTaken.getValue(true);
		log.trace(DebugFormatter.dataGet(REVCACHE_NAME, "lastTaken", l));
		return l;
	}
	
	/**
	 * Set a new value to be returned by {@link #getCurrentModelRev()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setCurrentModelRev(long l) {
		log.trace(DebugFormatter.dataPut(REVCACHE_NAME, "current", l));
		boolean changes = this.current.setLocalValueIfHigher(l);
		if(changes) {
			maintainInvariants(true);
		}
	}
	
	private void maintainInvariants(boolean currentHasChanged) {
		/* Make sure: current <= committed <= lastTaken */
		long committed = this.committed.getValue(true);
		if(currentHasChanged) {
			long current = this.current.getValue(true);
			if(current > committed) {
				this.committed.setLocalValueIfHigher(current);
				committed = current;
			}
		}
		long lastTaken = this.lastTaken.getValue(true);
		if(committed > lastTaken) {
			this.lastTaken.setLocalValueIfHigher(committed);
		}
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		log.trace(DebugFormatter.dataPut(REVCACHE_NAME, "lastCommited", l));
		boolean changes = this.committed.setLocalValueIfHigher(l);
		if(changes) {
			maintainInvariants(false);
		}
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken()}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastTaken(long l) {
		log.trace(DebugFormatter.dataPut(REVCACHE_NAME, "lastTaken", l));
		this.lastTaken.setLocalValueIfHigher(l);
	}
	
	protected void clear() {
		log.trace("revCache cleared");
		this.committed.clear();
		this.current.clear();
		this.lastTaken.clear();
	}
	
}

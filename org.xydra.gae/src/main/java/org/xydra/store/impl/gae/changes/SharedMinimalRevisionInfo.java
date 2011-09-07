package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.IMemCache;
import org.xydra.store.IMemCache.IdentifiableValue;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeOperation;


/**
 * Can read from and write to memcache. For concurrency, the original value read
 * from memcache is remembered. On write, a conditional put is used.
 * 
 * @author xamde
 * 
 */
public class SharedMinimalRevisionInfo implements IRevisionInfo {
	
	private class DeltaLong {
		/** as loaded from memcache */
		private IdentifiableValue loadedValue;
		private final String memcacheKey;
		/** Local VM cache of the "current" value */
		private long value;
		
		private DeltaLong(String memcacheKey) {
			this.memcacheKey = memcacheKey;
			clear();
		}
		
		public void clear() {
			this.value = NOT_SET;
			this.loadedValue = null;
		}
		
		private long getLoadedLong() {
			if(this.loadedValue == null) {
				return NOT_SET;
			} else {
				Object a = this.loadedValue.getValue();
				if(a == null) {
					return NOT_SET;
				} else {
					return (Long)a;
				}
			}
		}
		
		public long getValue() {
			return this.value;
		}
		
		public boolean isNotSet() {
			return this.value == NOT_SET;
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
			this.loadedValue = cache.getIdentifiable(this.memcacheKey);
			this.value = getLoadedLong();
		}
		
		public void setValue(long value) {
			if(value < this.value) {
				// don't change anything
			} else {
				this.value = value;
			}
		}
		
		@Override
		public String toString() {
			return this.memcacheKey + "=" + this.value + " loaded:" + this.loadedValue;
		}
		
		/**
		 * Write to memcache if value differs from the one initially loaded.
		 * Overwrites the memcache value only if it has not been changed since
		 * loading -- or if it was null in the memcache.
		 * 
		 */
		public void writeToMemcache() {
			if(!USE_MEMCACHE) {
				return;
			}
			if(this.loadedValue == null || this.loadedValue.getValue() == null) {
				// write if still null
				IMemCache cache = XydraRuntime.getMemcache();
				cache.putIfValueIsNull(this.memcacheKey, this.value);
			} else {
				// check if any difference since load
				if(this.getLoadedLong() == this.value) {
					// nothing changed
				} else {
					// write conditional
					IMemCache cache = XydraRuntime.getMemcache();
					cache.putIfUntouched(this.memcacheKey, this.loadedValue, this.value);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SharedMinimalRevisionInfo.class);
	
	/**
	 * Compile time flag to disable the functionality of
	 * {@link #writeToMemcache()} and {@link #loadFromMemcache()}
	 */
	// FIXME !!! test USE_MEMCACHE
	private static final boolean USE_MEMCACHE = true;
	public static final String getCacheName(XAddress modelAddress) {
		return modelAddress + "/SharedMinimalRevisionInfo";
	}
	private final DeltaLong currentRev;
	
	private final DeltaLong lastCommitted;
	
	private final DeltaLong lastTaken;
	
	@GaeOperation()
	SharedMinimalRevisionInfo(XAddress modelAddr) {
		this.currentRev = new DeltaLong(modelAddr + "/currentRev");
		this.lastCommitted = new DeltaLong(modelAddr + "/lastCommitted");
		this.lastTaken = new DeltaLong(modelAddr + "/lastTaken");
	}
	
	public void clear() {
		this.lastTaken.clear();
		this.lastCommitted.clear();
		this.currentRev.clear();
	}
	
	@Override
	public long getCurrentRev(boolean mayAsk) {
		synchronized(this.currentRev) {
			if(this.currentRev.isNotSet()) {
				this.currentRev.readFromMemcache();
			}
			return this.currentRev.getValue();
		}
	}
	
	@Override
	public long getLastCommitted(boolean mayAsk) {
		synchronized(this.lastCommitted) {
			if(this.lastCommitted.isNotSet()) {
				this.lastCommitted.readFromMemcache();
			}
			return this.lastCommitted.getValue();
		}
	}
	
	@Override
	public long getLastTaken(boolean mayAsk) {
		synchronized(this.lastTaken) {
			if(this.lastTaken.isNotSet()) {
				this.lastTaken.readFromMemcache();
			}
			return this.lastTaken.getValue();
		}
	}
	
	@Override
	public void setCurrentRev(long currentRev) {
		this.currentRev.setValue(currentRev);
		RevisionInfoUtils.maintainInvariants(this, true);
	}
	
	@Override
	public void setLastCommitted(long lastCommitted) {
		this.lastCommitted.setValue(lastCommitted);
		RevisionInfoUtils.maintainInvariants(this, false);
	}
	
	@Override
	public void setLastTaken(long lastTaken) {
		this.lastTaken.setValue(lastTaken);
	}
	
	@Override
	public String toString() {
		return this.lastTaken.toString() + "\n" + this.lastCommitted.toString() + "\n"
		        + this.currentRev.toString();
	}
	
	public void writeToMemcache() {
		this.lastTaken.writeToMemcache();
		this.lastCommitted.writeToMemcache();
		this.currentRev.writeToMemcache();
	}
	
}

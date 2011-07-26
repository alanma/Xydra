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
 * TODO improve on first fetch to memcache, fetch all 3 values
 * 
 * @author dscharrer
 * @author xamde
 */
class RevisionCache {
	
	/**
	 * How many milliseconds to consider the locally cached value valid
	 */
	private static final long LOCAL_VM_CACHE_TIMEOUT = 30 * 1000; // was 500
	
	protected static final long NOT_SET = -2L;
	
	private class CachedLong {
		private CachedLong(String memcacheKey) {
			this.memcacheKey = memcacheKey;
			this.value = NOT_SET;
			this.time = NOT_SET;
		}
		
		/** Local VM cache of the "current" revision number. */
		private long value;
		/** Age of Local VM cache of the "current" revision number. */
		private long time;
		private final String memcacheKey;
		
		/**
		 * @return true if value cached in local JVM is not too old
		 */
		private boolean hasValidLocalValue() {
			long now = System.currentTimeMillis();
			return now < this.time + LOCAL_VM_CACHE_TIMEOUT;
		}
		
		/**
		 * @return the value from memcache or NOT_SET, never null.
		 */
		@GaeOperation(memcacheRead = true)
		private long askMemcache() {
			IMemCache cache = XydraRuntime.getMemcache();
			Long value = (Long)cache.get(this.memcacheKey);
			if(value == null) {
				// cache the fact that memcache doesn't know it
				setLocalValue(NOT_SET);
				return NOT_SET;
			} else {
				setLocalValue(value);
				return value;
			}
		}
		
		/**
		 * @param value
		 * @return true if value changed
		 */
		private boolean setLocalValue(long value) {
			if(value < this.value) {
				return false;
				// TODO IMPROVE change caller code so that this doesnt happen
			}
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
		private long getValue(boolean mayAskMemcache, boolean returnMinusOneForUndefined) {
			synchronized(this) {
				long value;
				if(this.hasValidLocalValue()) {
					value = this.value;
				} else if(mayAskMemcache) {
					value = this.askMemcache();
				} else {
					value = NOT_SET;
				}
				if(returnMinusOneForUndefined) {
					if(value == NOT_SET) {
						value = -1;
					}
				}
				return value;
			}
		}
		
		/**
		 * @param value to set
		 * @param writeMemcache if true, value is also written to memcache
		 */
		private void setValue(long value, boolean writeMemcache) {
			if(value < this.value) {
				return;
				// TODO IMPROVE change caller code so that this doesnt happen
			}
			setLocalValue(value);
			if(writeMemcache) {
				IMemCache cache = XydraRuntime.getMemcache();
				Object previous = cache.put(this.memcacheKey, value);
				assert previous == null || ((Long)previous) <= value;
			}
		}
	}
	
	private final CachedLong current, committed, lastTaken;
	
	@GaeOperation()
	RevisionCache(XAddress modelAddr) {
		// init caches
		this.current = new CachedLong(modelAddr + "-currentRev");
		this.committed = new CachedLong(modelAddr + "-commitedRev");
		this.lastTaken = new CachedLong(modelAddr + "-lastTakenRev");
	}
	
	/**
	 * @param mayAskMemcache TODO
	 * @return a cached value of the current revision number as defined by
	 *         {@link GaeChangesService#getCurrentRevisionNumber()}.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentModelRev(boolean mayAskMemcache) {
		return this.current.getValue(mayAskMemcache, true);
	}
	
	/**
	 * @param mayAskMemcache TODO
	 * @return a cached value of the current revision number as defined by
	 *         {@link GaeChangesService#getCurrentRevisionNumber()} or NOT_SET.
	 * 
	 *         The returned value may be less that the actual "current" revision
	 *         number, but is guaranteed to never be greater.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getCurrentModelRevIfSet(boolean mayAskMemcache) {
		return this.current.getValue(mayAskMemcache, false);
	}
	
	/**
	 * @param mayAskMemcache TODO
	 * @return a revision number such that all changes up to and including that
	 *         revision number are guaranteed to be committed. This is not
	 *         guaranteed to be the highest revision number that fits this
	 *         requirement.
	 */
	@GaeOperation(memcacheRead = true)
	protected long getLastCommited(boolean mayAskMemcache) {
		return this.lastTaken.getValue(mayAskMemcache, true);
	}
	
	protected long getLastCommitedIfSet(boolean mayAskMemcache) {
		return this.committed.getValue(mayAskMemcache, false);
	}
	
	/**
	 * @param mayAskMemcache TODO
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already.
	 */
	protected long getLastTaken(boolean mayAskMemcache) {
		return this.lastTaken.getValue(mayAskMemcache, true);
	}
	
	/**
	 * Set a new value to be returned by {@link #getCurrentModelRev(boolean)}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setCurrentModelRev(long l) {
		boolean changes = this.current.setLocalValue(l);
		if(changes) {
			maintainInvariants(true);
		}
	}
	
	private void maintainInvariants(boolean currentHasChanged) {
		/* Make sure: current <= committed <= lastTaken */
		long committed = this.committed.getValue(false, true);
		if(currentHasChanged) {
			long current = this.current.getValue(false, true);
			if(current > committed) {
				this.committed.setValue(current, true);
				committed = current;
			}
		}
		long lastTaken = this.lastTaken.getValue(false, true);
		if(committed > lastTaken) {
			this.lastTaken.setLocalValue(committed);
		}
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastCommited(boolean)}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastCommited(long l) {
		boolean changes = this.committed.setLocalValue(l);
		if(changes) {
			maintainInvariants(false);
		}
	}
	
	/**
	 * Set a new value to be returned by {@link #getLastTaken(boolean)}.
	 * 
	 * @param l The value is set. It is ignored if the current cached value is
	 *            less than this.
	 */
	protected void setLastTaken(long l) {
		this.lastTaken.setLocalValue(l);
	}
	
}

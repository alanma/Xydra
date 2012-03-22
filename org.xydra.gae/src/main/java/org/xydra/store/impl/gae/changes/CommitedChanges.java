package org.xydra.store.impl.gae.changes;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.InstanceContext;


/**
 * In-memory representation for some change events. Required for
 * {@link GaeChangesServiceImpl3}
 * 
 * IMPROVE Shares this cache with other threads on the same instance
 * 
 * This class is NOT thread-safe.
 * 
 * @author xamde
 */
public class CommitedChanges {
	
	private static final Logger log = LoggerFactory.getLogger(CommitedChanges.class);
	
	/** Just the debug name */
	private static final String LOCAL_COMMITED_CHANGES_CACHENAME = "[.c1]";
	private static final String INSTANCE_COMMITED_CHANGES_CACHENAME = "[.c2]";
	private XAddress modelAddr;
	private Map<Long,GaeChange> localMap = new HashMap<Long,GaeChange>();
	
	/**
	 * @param modelAddress
	 */
	public CommitedChanges(XAddress modelAddress) {
		this.modelAddr = modelAddress;
	}
	
	/**
	 * @param rev
	 * @return cached change for this revisions. Can be null if (1) is really
	 *         null, (2) was just never indexed, (3) got removed from cache
	 */
	GaeChange getCachedChange(long rev) {
		GaeChange change = this.localMap.get(rev);
		log.trace(DebugFormatter.dataGet(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
		        + rev, change, Timing.Now));
		return change;
	}
	
	/**
	 * @param rev
	 * @return cached change for this revisions. Can be null if (1) is really
	 *         null, (2) was just never indexed, (3) got removed from cache
	 */
	GaeChange getInstanceCachedChange(long rev) {
		GaeChange change;
		Map<Long,GaeChange> committedChangeCache = getInstanceCommittedChangeCache();
		synchronized(committedChangeCache) {
			change = committedChangeCache.get(rev);
		}
		log.trace(DebugFormatter.dataGet(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
		        + rev, change, Timing.Now));
		return change;
	}
	
	/**
	 * @param rev
	 * @return true if this change is cached (and therefore committed)
	 */
	boolean hasCachedChange(long rev) {
		return this.localMap.containsKey(rev);
	}
	
	/**
	 * CODE SAMPLE TO HELP IMPLEMENTING INSTANCE-WIDE SHARING
	 * 
	 * @param rev
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean hasInstanceCachedChange(long rev) {
		Map<Long,GaeChange> committedChangeCache = getInstanceCommittedChangeCache();
		synchronized(committedChangeCache) {
			return committedChangeCache.containsKey(rev);
		}
	}
	
	/**
	 * CODE SAMPLE TO HELP IMPLEMENTING INSTANCE-WIDE SHARING
	 * 
	 * @return the instance-level cache of committed change objects
	 */
	private Map<Long,GaeChange> getInstanceCommittedChangeCache() {
		String key = "changes:" + this.modelAddr;
		Map<String,Object> instanceCache = InstanceContext.getInstanceCache();
		Map<Long,GaeChange> committedChangeCache;
		synchronized(instanceCache) {
			committedChangeCache = (Map<Long,GaeChange>)instanceCache.get(key);
			if(committedChangeCache == null) {
				log.debug(DebugFormatter.init(INSTANCE_COMMITED_CHANGES_CACHENAME));
				committedChangeCache = new HashMap<Long,GaeChange>();
				InstanceContext.getInstanceCache().put(key, committedChangeCache);
			}
		}
		return committedChangeCache;
	}
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached; must have status == committed
	 */
	public void cacheCommittedChange(GaeChange change) {
		GaeAssert.gaeAssert(change != null);
		assert change != null;
		GaeAssert.gaeAssert(change.getStatus().isCommitted());
		assert change.getStatus() != null;
		assert change.getStatus().isCommitted();
		log.trace(DebugFormatter.dataPut(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
		        + change.rev, change, Timing.Now));
		this.localMap.put(change.rev, change);
	}
	
	/**
	 * CODE SAMPLE TO HELP IMPLEMENTING INSTANCE-WIDE SHARING
	 * 
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached; must have status == committed
	 */
	@SuppressWarnings("unused")
	private void cacheInstanceCommittedChange(GaeChange change) {
		GaeAssert.gaeAssert(change != null);
		assert change != null;
		GaeAssert.gaeAssert(change.getStatus().isCommitted());
		assert change.getStatus() != null;
		assert change.getStatus().isCommitted();
		log.trace(DebugFormatter.dataPut(INSTANCE_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
		        + change.rev, change, Timing.Now));
		Map<Long,GaeChange> committedChangeCache = getInstanceCommittedChangeCache();
		synchronized(committedChangeCache) {
			committedChangeCache.put(change.rev, change);
		}
	}
	
}

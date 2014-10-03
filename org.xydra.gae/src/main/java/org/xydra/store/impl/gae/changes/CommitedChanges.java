package org.xydra.store.impl.gae.changes;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

import com.google.common.cache.Cache;


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
     *         null, (2) was just never indexed
     */
    GaeChange getCachedChange(long rev) {
        GaeChange change = this.localMap.get(rev);
        log.trace(XGaeDebugHelper.dataGet(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
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
        log.trace(XGaeDebugHelper.dataGet(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
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
    @SuppressWarnings("unchecked")
	private Map<Long,GaeChange> getInstanceCommittedChangeCache() {
        String key = "changes:" + this.modelAddr;
        Cache<String,Object> instanceCache = InstanceContext.getInstanceCache();
        Map<Long,GaeChange> committedChangeCache;
        synchronized(instanceCache) {
            committedChangeCache = (Map<Long,GaeChange>)instanceCache.getIfPresent(key);
            if(committedChangeCache == null) {
                log.debug(XGaeDebugHelper.init(INSTANCE_COMMITED_CHANGES_CACHENAME));
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
    public void cacheStableChange(GaeChange change) {
        XyAssert.xyAssert(change != null);
        assert change != null;
        assert change.getStatus() != null;
        XyAssert.xyAssert(!change.getStatus().canChange());
        log.trace(XGaeDebugHelper.dataPut(LOCAL_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
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
        XyAssert.xyAssert(change != null);
        assert change != null;
        XyAssert.xyAssert(!change.getStatus().canChange());
        assert change.getStatus() != null;
        log.trace(XGaeDebugHelper.dataPut(INSTANCE_COMMITED_CHANGES_CACHENAME + this.modelAddr, ""
                + change.rev, change, Timing.Now));
        Map<Long,GaeChange> committedChangeCache = getInstanceCommittedChangeCache();
        synchronized(committedChangeCache) {
            committedChangeCache.put(change.rev, change);
        }
    }
    
}

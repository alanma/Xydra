package org.xydra.store.impl.gae.ng;

import java.io.Serializable;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.CacheEntryHandler;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;



/**
 * Helper class to convert from {@link GaeModelRevInfo} and different cache
 * formats
 * 
 * @author xamde
 */
class UniCacheRevisionInfoEntryHandler implements UniCache.CacheEntryHandler<GaeModelRevInfo> {
    
    private static UniCacheRevisionInfoEntryHandler instance;
    
    private static final String LastStableCommitted = "stableComm";
    private static final String LastStableSuccess = "stableSucc";
    private static final String LastSuccess = "succ";
    private static final String LastTaken = "taken";
    private static final Logger log = LoggerFactory
            .getLogger(UniCacheRevisionInfoEntryHandler.class);
    private static final String ModelExists = "exists";
    private static final String Timestamp = "time";
    private static final String Precision = "prec";
    
    public static synchronized CacheEntryHandler<GaeModelRevInfo> instance() {
        if(instance == null) {
            instance = new UniCacheRevisionInfoEntryHandler();
        }
        return instance;
    }
    
    @Override
    public GaeModelRevInfo fromEntity(SEntity e) {
        long timestamp = (Long)e.getAttribute(Timestamp);
        boolean modelExists = (Boolean)e.getAttribute(ModelExists);
        long lastStableSuccess = (Long)e.getAttribute(LastStableSuccess);
        long lastStableCommitted = (Long)e.getAttribute(LastStableCommitted);
        long lastSuccess = (Long)e.getAttribute(LastSuccess);
        long lastTaken = (Long)e.getAttribute(LastTaken);
        String precStr = (String)e.getAttribute(Precision);
        org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision prec = org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision
                .valueOf(precStr);
        GaeModelRevInfo ri = new GaeModelRevInfo(timestamp, modelExists, lastStableSuccess,
                lastStableCommitted, lastSuccess, lastTaken, prec);
        log.debug("loaded from entity " + ri);
        return ri;
    }
    
    @Override
    public GaeModelRevInfo fromSerializable(Serializable s) {
        GaeModelRevInfo ri = (GaeModelRevInfo)s;
        return ri;
    }
    
    @Override
    public SEntity toEntity(SKey datastoreKey, GaeModelRevInfo revInfo) {
        SEntity e = XGae.get().datastore().createEntity(datastoreKey);
        e.setAttribute(LastStableCommitted, revInfo.getLastStableCommitted());
        e.setAttribute(LastStableSuccess, revInfo.getLastStableSuccessChange());
        e.setAttribute(ModelExists, revInfo.isModelExists());
        e.setAttribute(LastSuccess, revInfo.getLastSuccessChange());
        e.setAttribute(LastTaken, revInfo.getLastTaken());
        e.setAttribute(Timestamp, revInfo.getTimestamp());
        e.setAttribute(Precision, revInfo.getPrecision().name());
        return e;
    }
    
    @Override
    public Serializable toSerializable(GaeModelRevInfo entry) {
        return entry;
    }
    
}

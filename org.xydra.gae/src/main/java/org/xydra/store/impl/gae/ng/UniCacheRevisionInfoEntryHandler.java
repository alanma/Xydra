package org.xydra.store.impl.gae.ng;

import java.io.Serializable;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.CacheEntryHandler;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


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
	public GaeModelRevInfo fromEntity(Entity e) {
		long timestamp = (Long)e.getProperty(Timestamp);
		boolean modelExists = (Boolean)e.getProperty(ModelExists);
		long lastStableSuccess = (Long)e.getProperty(LastStableSuccess);
		long lastStableCommitted = (Long)e.getProperty(LastStableCommitted);
		long lastSuccess = (Long)e.getProperty(LastSuccess);
		long lastTaken = (Long)e.getProperty(LastTaken);
		String precStr = (String)e.getProperty(Precision);
		org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision prec = org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision
		        .valueOf(precStr);
		if(prec == org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision.Precise) {
			prec = org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision.Loaded;
		}
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
	public Entity toEntity(Key datastoreKey, GaeModelRevInfo revInfo) {
		Entity e = new Entity(datastoreKey);
		e.setUnindexedProperty(LastStableCommitted, revInfo.getLastStableCommitted());
		e.setUnindexedProperty(LastStableSuccess, revInfo.getLastStableSuccessChange());
		e.setUnindexedProperty(ModelExists, revInfo.isModelExists());
		e.setUnindexedProperty(LastSuccess, revInfo.getLastSuccessChange());
		e.setUnindexedProperty(LastTaken, revInfo.getLastTaken());
		e.setUnindexedProperty(Timestamp, revInfo.getTimestamp());
		e.setUnindexedProperty(Precision, revInfo.getPrecision().name());
		return e;
	}
	
	@Override
	public Serializable toSerializable(GaeModelRevInfo entry) {
		return entry;
	}
	
}

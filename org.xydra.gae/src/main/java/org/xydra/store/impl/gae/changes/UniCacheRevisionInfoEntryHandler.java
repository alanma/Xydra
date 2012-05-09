package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.UniCache;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Helper class to convert from {@link RevisionInfo} and different cache formats
 * 
 * @author xamde
 */
class UniCacheRevisionInfoEntryHandler implements UniCache.CacheEntryHandler<RevisionInfo> {
	
	private static final Logger log = LoggerFactory
	        .getLogger(UniCacheRevisionInfoEntryHandler.class);
	
	private static final String COMM = "comm";
	private static final String CURR = "curr";
	/** modelExists */
	private static final String EXISTS = "exists";
	private static final String SILENT = "silent";
	private static final String TAKEN = "taken";
	
	@Override
	public RevisionInfo fromEntity(Entity e) {
		RevisionInfo ri = new RevisionInfo("from-datastore" + e.getKey().toString());
		long lastCommitted = (Long)e.getProperty(COMM);
		ri.setLastCommittedIfHigher(lastCommitted);
		Object oLastTaken = e.getProperty(TAKEN);
		if(oLastTaken == null) {
			// should never happen again
			log.warn("entity weird: " + DebugFormatter.format(e));
		}
		assert oLastTaken != null;
		long lastTaken = (Long)oLastTaken;
		ri.setLastTakenIfHigher(lastTaken);
		ModelRevision modelRev = null;
		if(e.hasProperty(CURR)) {
			assert e.hasProperty(EXISTS);
			long current = (Long)e.getProperty(CURR);
			boolean modelExists = (Boolean)e.getProperty(EXISTS);
			modelRev = new ModelRevision(current, modelExists);
		}
		long silent = (Long)e.getProperty(SILENT);
		GaeModelRevision gaeModelRev = new GaeModelRevision(silent, modelRev);
		ri.setGaeModelRev(gaeModelRev);
		log.debug("loaded from entity with curr=" + gaeModelRev);
		return ri;
	}
	
	@Override
	public RevisionInfo fromSerializable(Serializable s) {
		RevisionInfo ri = (RevisionInfo)s;
		ri.setDatasourceName("from-memcache");
		return ri;
	}
	
	@Override
	public Entity toEntity(Key datastoreKey, RevisionInfo revInfo) {
		Entity e = new Entity(datastoreKey);
		e.setUnindexedProperty(SILENT, revInfo.getGaeModelRevision().getLastSilentCommitted());
		ModelRevision modelRev = revInfo.getGaeModelRevision().getModelRevision();
		if(modelRev != null) {
			e.setUnindexedProperty(CURR, modelRev.revision());
			e.setUnindexedProperty(EXISTS, modelRev.modelExists());
		} else {
			e.removeProperty(CURR);
			e.removeProperty(EXISTS);
		}
		e.setUnindexedProperty(COMM, revInfo.getLastCommitted());
		e.setUnindexedProperty(TAKEN, revInfo.getLastTaken());
		return e;
	}
	
	@Override
	public Serializable toSerializable(RevisionInfo entry) {
		return entry;
	}
	
}

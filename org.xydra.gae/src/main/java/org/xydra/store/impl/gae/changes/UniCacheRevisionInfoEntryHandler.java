package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

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
	public RevisionInfo fromEntity(final SEntity e) {
		final RevisionInfo ri = new RevisionInfo("from-datastore" + e.getKey().toString());
		final long lastCommitted = (Long) e.getAttribute(COMM);
		ri.setLastCommittedIfHigher(lastCommitted);
		final Object oLastTaken = e.getAttribute(TAKEN);
		if (oLastTaken == null) {
			// should never happen again
			log.warn("entity weird: " + DebugFormatter.format(e));
		}
		assert oLastTaken != null;
		final long lastTaken = (Long) oLastTaken;
		ri.setLastTakenIfHigher(lastTaken);
		ModelRevision modelRev = null;
		if (e.hasAttribute(CURR)) {
			assert e.hasAttribute(EXISTS);
			final long current = (Long) e.getAttribute(CURR);
			final boolean modelExists = (Boolean) e.getAttribute(EXISTS);
			modelRev = new ModelRevision(current, modelExists);
		}
		final long silent = (Long) e.getAttribute(SILENT);
		final GaeModelRevision gaeModelRev = new GaeModelRevision(silent, modelRev);
		ri.setGaeModelRev(gaeModelRev);
		log.debug("loaded from entity with curr=" + gaeModelRev);
		return ri;
	}

	@Override
	public RevisionInfo fromSerializable(final Serializable s) {
		final RevisionInfo ri = (RevisionInfo) s;
		ri.setDatasourceName("from-memcache");
		return ri;
	}

	@Override
	public SEntity toEntity(final SKey datastoreKey, final RevisionInfo revInfo) {
		final SEntity e = XGae.get().datastore().createEntity(datastoreKey);
		e.setAttribute(SILENT, revInfo.getGaeModelRevision().getLastSilentCommitted());
		final ModelRevision modelRev = revInfo.getGaeModelRevision().getModelRevision();
		if (modelRev != null) {
			e.setAttribute(CURR, modelRev.revision());
			e.setAttribute(EXISTS, modelRev.modelExists());
		} else {
			e.removeAttribute(CURR);
			e.removeAttribute(EXISTS);
		}
		e.setAttribute(COMM, revInfo.getLastCommitted());
		e.setAttribute(TAKEN, revInfo.getLastTaken());
		return e;
	}

	@Override
	public Serializable toSerializable(final RevisionInfo entry) {
		return entry;
	}

}

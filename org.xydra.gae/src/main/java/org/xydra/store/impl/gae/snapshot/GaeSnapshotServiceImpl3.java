package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.EventUtils;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.collect.Iterators;


/**
 * Computes *Snapshots ( {@link SimpleField}, {@link SimpleObject},
 * {@link SimpleModel}) from a given {@link XChangeLog}.
 * 
 * An entity of kind 'XSNAPSHOT' with the key (model-address) + '/-/-/' +
 * (revNr) contains a snapshot for a model with the given address and the given
 * revNr.
 * 
 * Recent improvements:
 * <ul>
 * <li>Doesn't ask memcache or datastore when there is a snapshot in the local
 * vm cache that is less than n (=2) revisions older than the requested one.
 * <li>When no matching revision was found in either local vm cache, memcache or
 * datastore, look if any any of the revisions in the batch are in the local vm
 * cache before asking memcache.
 * <li>Avoid unnecessary copies when updating snapshots / while generating a
 * partial snapshot.
 * </ul>
 * 
 * @author dscharrer
 * @author xamde
 */
public class GaeSnapshotServiceImpl3 extends AbstractGaeSnapshotServiceImpl {
	
	private static final class NonexistantModel implements XRevWritableModel {
		
		@Override
		public XAddress getAddress() {
			return null;
		}
		
		@Override
		public XID getID() {
			return XX.toId("_NonExistant");
		}
		
		@Override
		public XRevWritableObject getObject(XID objectId) {
			return null;
		}
		
		@Override
		public long getRevisionNumber() {
			return -3;
		}
		
		@Override
		public XType getType() {
			return XType.XMODEL;
		}
		
		@Override
		public boolean hasObject(XID objectId) {
			return false;
		}
		
		@Override
		public boolean isEmpty() {
			return true;
		}
		
		@Override
		public Iterator<XID> iterator() {
			return Iterators.emptyIterator();
		}
		
		@Override
		public void addObject(XRevWritableObject object) {
			// stub
		}
		
		@Override
		public XRevWritableObject createObject(XID id) {
			return null;
		}
		
		@Override
		public void setRevisionNumber(long rev) {
			// stub
		}
		
		@Override
		public boolean removeObject(XID objectId) {
			return false;
		}
		
	}
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotServiceImpl3.class);
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	private static final NonexistantModel NONEXISTANT_MODEL = new NonexistantModel();
	
	/** property name for storing serialised XML content of a snapshot */
	private static final String PROP_XML = "xml";
	
	private static final long SNAPSHOT_PERSISTENCE_THRESHOLD = 10;
	
	private static final boolean USE_MEMCACHE = true;
	
	public static boolean USE_SNAPSHOT_CACHE = true;
	
	private final IGaeChangesService changesService;
	
	private final XAddress modelAddress;
	
	/**
	 * Number of previous revisions to look for in local vm cache before
	 * checking the memcache / datastore at all.
	 */
	private static final long SNAPSHOT_LOAD_THRESHOLD = 2;
	private static final String DATASOURCE_SNAPSHOTS_VM = "[.snap]";
	
	/**
	 * @param changesService The change log to load snapshots from.
	 */
	public GaeSnapshotServiceImpl3(IGaeChangesService changesService) {
		this.modelAddress = changesService.getModelAddress();
		this.changesService = changesService;
	}
	
	private boolean cacheResultIsConsistent(Map<String,Object> batchResult) {
		for(Entry<String,Object> entry : batchResult.entrySet()) {
			String key = entry.getKey();
			XRevWritableModel value = (XRevWritableModel)entry.getValue();
			boolean consistent = isConsistent(key, value);
			if(!consistent) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Compute requested snapshot by using an older snapshot version (if one is
	 * found in memcache). Puts intermediary version in the respective caches.
	 * 
	 * @param requestedRevNr which is required but has no direct match in the
	 *            datastore or memcache FIXME 2012-02 make sure too high numbers
	 *            are handled well. This allows callers to retrieve a new
	 *            version which has still uncommited versions below it.
	 * @return a computed model snapshot
	 */
	private XRevWritableModel computeSnapshot(long requestedRevNr) {
		assert requestedRevNr >= 0;
		log.debug("compute snapshot " + requestedRevNr);
		
		Map<String,Object> batchResult = Collections.emptyMap();
		long askNr = requestedRevNr - 1;
		while(askNr >= 0 && batchResult.isEmpty()) {
			// prepare a fetch in the mem-cache
			String possiblyMemcachedKey = null;
			int loops = 0;
			while(askNr >= 0 && loops <= SNAPSHOT_PERSISTENCE_THRESHOLD) {
				// TODO With Daniel: do we really allow null here?
				XRevWritableModel modelOrNull = localVmCacheGet(askNr);
				if(modelOrNull != null) {
					return computeAndCacheSnapshotFromBase(requestedRevNr, modelOrNull);
				}
				if(revCanBeMemcached(askNr)) {
					possiblyMemcachedKey = KeyStructure.toString(getSnapshotKey(askNr));
				}
				askNr--;
				loops++;
			}
			// execute
			if(USE_MEMCACHE) {
				batchResult = Memcache.getEntities(Collections.singleton(possiblyMemcachedKey));
				GaeAssert.gaeAssert(batchResult.size() <= 1, "got 0 or 1 results");
				GaeAssert.gaeAssert(cacheResultIsConsistent(batchResult),
				        "cache inconsistent, see logs");
				if(batchResult.isEmpty()) {
					// memcache got flushed?
					log.info("Memcache got flushed? Found no snapshot at " + possiblyMemcachedKey);
					
					// executeCommand should periodically create snapshots in
					// memcache?
				} else {
					// batch result will usually contain 1 hits
					if(askNr >= 0 && batchResult.size() != 1) {
						/*
						 * The sure changes service puts every K versions a
						 * snapshot in memcache
						 */
						log.info("Got a batch result of size " + batchResult.size()
						        + " from memcache when asking for snapshot: "
						        + DebugFormatter.format(possiblyMemcachedKey));
					}
					// Pump all results (if any) to localVmcache
					for(Object o : batchResult.values()) {
						assert o instanceof XRevWritableModel;
						XRevWritableModel snapshotFromMemcache = (XRevWritableModel)o;
						localVmCachePut(snapshotFromMemcache);
					}
				}
			}
		}
		
		// TODO SCALE if no hits, ask datastore (make sure to put it there, too)
		
		XRevWritableModel base;
		if(batchResult.isEmpty()) {
			log.debug("we start from scratch, nobody has ever saved a snapshot");
			base = new SimpleModel(this.modelAddress);
			base.setRevisionNumber(MODEL_DOES_NOT_EXIST);
		} else {
			// IMPROVE PERFORMANCE use *highest* revision within response
			// any element can serve as base for further computation
			Object snapshotfromMemcache = batchResult.values().iterator().next();
			assert snapshotfromMemcache instanceof XRevWritableModel;
			base = (XRevWritableModel)snapshotfromMemcache;
			assert base.getAddress().equals(this.modelAddress);
		}
		log.debug("compute from " + base.getRevisionNumber() + " up to " + requestedRevNr);
		
		return computeAndCacheSnapshotFromBase(requestedRevNr, base);
	}
	
	/**
	 * @param requestedRevNr
	 * @param base content will be changed; never null
	 * @return a snapshot in revision 'requestedRevNr' by applying changes to
	 *         given base model
	 */
	private XRevWritableModel computeAndCacheSnapshotFromBase(long requestedRevNr,
	        XRevWritableModel base) {
		assert base != null;
		assert this.modelAddress.equals(base.getAddress());
		XRevWritableModel requestedSnapshot = computeSnapshotFromBase(base, requestedRevNr);
		localVmCachePut(requestedSnapshot);
		// // cache it in memcache
		// XydraRuntime.getMemcache().put(
		// KeyStructure.toString(this.getSnapshotKey(requestedSnapshot.getRevisionNumber())),
		// requestedSnapshot);
		// return it
		return requestedSnapshot;
	}
	
	/**
	 * Compute a snapshot by applying all events that happened between base's
	 * revision and the requested revisionNumber.
	 * 
	 * @param base might have revNr == -1; content *will* be changed. Never
	 *            null.
	 * @param requestedRevNr
	 * @return a serialisable, computed snapshot
	 */
	private XRevWritableModel computeSnapshotFromBase(XRevWritableModel base, long requestedRevNr) {
		assert base != null;
		assert base.getRevisionNumber() < requestedRevNr : "otherwise it makes no sense to compute it";
		GaeAssert.gaeAssert(requestedRevNr > 0);
		XRevWritableModel snapshot = base;
		GaeAssert.gaeAssert(requestedRevNr > snapshot.getRevisionNumber());
		log.debug("Compute snapshot of model '" + this.modelAddress + "' from rev="
		        + snapshot.getRevisionNumber() + " to rev=" + requestedRevNr);
		
		// get events between [ start, end )
		long start = Math.max(snapshot.getRevisionNumber() + 1, 0);
		List<XEvent> events = this.changesService.getEventsBetween(this.modelAddress, start,
		        requestedRevNr);
		
		// This should not happen and should be fixed somewhere else
		if(events == null) {
			log.warn("There are no events for " + this.modelAddress + " in range [" + start + ","
			        + requestedRevNr + "]");
		}
		assert events != null;
		
		// apply events to base
		for(XEvent event : events) {
			log.debug("Basemodel[" + snapshot.getRevisionNumber() + "], applying event["
			        + event.getRevisionNumber() + "]=" + DebugFormatter.format(event));
			
			snapshot = EventUtils.applyEventNonDestructive(snapshot, event);
			
			localVmCachePut(snapshot);
			long rev = snapshot.getRevisionNumber();
			if(USE_MEMCACHE && revCanBeMemcached(rev)) {
				// cache every 10th snapshot in memcache
				Key key = getSnapshotKey(rev);
				Memcache.put(key, snapshot);
				// TODO SCALE put also in datastore (async)
				assert isConsistent(KeyStructure.toString(key), snapshot);
			}
		}
		
		return snapshot;
	}
	
	/**
	 * For this model: revNr -> modelSnapshot
	 * 
	 * TODO SCALE avoid growing large, keep only most recent version?
	 */
	private SortedMap<Long,XRevWritableModel> getModelSnapshotsCache() {
		String key = "snapshots:" + this.changesService.getModelAddress();
		Map<String,Object> instanceCache = InstanceContext.getInstanceCache();
		SortedMap<Long,XRevWritableModel> modelSnapshotsCache;
		synchronized(instanceCache) {
			modelSnapshotsCache = (SortedMap<Long,XRevWritableModel>)instanceCache.get(key);
			if(modelSnapshotsCache == null) {
				log.debug("localVmcache for snapshots missing, creating one");
				modelSnapshotsCache = new TreeMap<Long,XRevWritableModel>();
				instanceCache.put(key, modelSnapshotsCache);
			} else if(modelSnapshotsCache.size() > 100) {
				modelSnapshotsCache.clear();
			}
		}
		return modelSnapshotsCache;
	}
	
	/**
	 * @param requestedRevNr ..
	 * @return the requested model snapshot
	 */
	synchronized public XRevWritableModel getModelSnapshot(long requestedRevNr) {
		log.debug("Get snapshot " + this.modelAddress + " rev " + requestedRevNr);
		
		/* if localVmCache has exact requested version, use it */
		XRevWritableModel cached = localVmCacheGet(requestedRevNr);
		if(cached != null) {
			log.debug("return locally cached");
			return cached;
		}
		
		return createModelSnapshot(requestedRevNr);
	}
	
	/**
	 * We know already that the requestedRevNr is not in the local vmCache
	 * 
	 * @param requestedRevNr
	 * @return
	 */
	private XRevWritableModel createModelSnapshot(long requestedRevNr) {
		// IMPROVE make this dependent on whether the needed changes are cached
		// locally?
		
		for(long i = 1; i <= SNAPSHOT_LOAD_THRESHOLD; i++) {
			XRevWritableModel oldCached = localVmCacheGet(requestedRevNr - i);
			if(oldCached != null) {
				log.debug("Found oldCached rev " + oldCached.getRevisionNumber()
				        + " and will compute from there on");
				return computeAndCacheSnapshotFromBase(requestedRevNr, oldCached);
			}
			
		}
		
		/*
		 * IMPROVE PERFORMANCE if the local VM cache is only behind a few
		 * revisions, it might be faster to update it than to load a snapshot
		 * from the memcache
		 */
		XRevWritableModel snapshot = getSnapshotFromMemcacheOrDatastore(requestedRevNr);
		return snapshot;
	}
	
	/**
	 * Implementation note: As XEntites are not {@link Serializable} by default,
	 * an XML-serialisation is stored in data store and memcache.
	 * 
	 * @param requestedRevNr for which to retrieve a snapshot.
	 * 
	 *            FIXME FIXME 2012-02 make sure too high numbers are handled
	 * @return a snapshot with the requested revisionNumber or null if model was
	 *         null at that revision.
	 */
	@GaeOperation(datastoreRead = true ,memcacheRead = true)
	synchronized private XRevWritableModel getSnapshotFromMemcacheOrDatastore(long requestedRevNr) {
		assert requestedRevNr > 0;
		log.debug("getSnapshotFromMemcacheOrDatastore " + requestedRevNr);
		// try to retrieve an exact match for the required revisionNumber
		// memcache + datastore read
		Key snapshotKey = getSnapshotKey(requestedRevNr);
		Object o = null;
		if(USE_MEMCACHE && revCanBeMemcached(requestedRevNr)) {
			o = Memcache.get(snapshotKey);
			if(o != null) {
				log.debug("return from memcache");
				if(o.equals(Memcache.NULL_ENTITY)) {
					localVmCachePutNull(requestedRevNr);
					return null;
				}
				assert isConsistent(KeyStructure.toString(snapshotKey), (XRevWritableModel)o);
				XRevWritableModel snapshot = (XRevWritableModel)o;
				assert snapshot.getRevisionNumber() == requestedRevNr;
				localVmCachePut(snapshot);
				return snapshot;
			}
		}
		// else: look for direct match in datastore
		Entity e = SyncDatastore.getEntity(snapshotKey);
		if(e != null) {
			log.debug("return from datastore");
			Text xmlText = (Text)e.getProperty(PROP_XML);
			if(xmlText == null) {
				// model was null at that revision
				return null;
			}
			String xml = xmlText.getValue();
			XydraElement snapshotXml = new XmlParser().parse(xml);
			XRevWritableModel snapshot = SerializedModel.toModelState(snapshotXml,
			        this.modelAddress);
			localVmCachePut(snapshot);
			return snapshot;
		}
		// else: need to compute snapshot from an older version
		XRevWritableModel snapshot = computeSnapshot(requestedRevNr);
		return snapshot;
	}
	
	private static boolean revCanBeMemcached(long requestedRevNr) {
		return requestedRevNr % SNAPSHOT_PERSISTENCE_THRESHOLD == 0;
	}
	
	private synchronized Key getSnapshotKey(long revNr) {
		return KeyFactory.createKey(KIND_SNAPSHOT, this.modelAddress.toURI() + "/" + revNr);
	}
	
	/**
	 * @param revisionNumber will be equal or less than the revision of the
	 *            returned snapshot
	 * @return a model snapshot with the given revisionNumber of an even more
	 *         recent one (with a higher revision number)
	 */
	synchronized public XRevWritableModel getModelSnapshotNewerOrAtRevision(long revisionNumber) {
		if(!USE_SNAPSHOT_CACHE) {
			return getModelSnapshot(revisionNumber);
		}
		
		/* look for all versions at this or higher revNrs */
		SortedMap<Long,XRevWritableModel> modelSnapshotsCache = getModelSnapshotsCache();
		SortedMap<Long,XRevWritableModel> matchOrNewer;
		synchronized(modelSnapshotsCache) {
			matchOrNewer = modelSnapshotsCache.subMap(revisionNumber, Long.MAX_VALUE);
		}
		if(matchOrNewer.isEmpty()) {
			// not cached
			return createModelSnapshot(revisionNumber);
		} else {
			XRevWritableModel cached = matchOrNewer.values().iterator().next();
			assert cached.getRevisionNumber() >= revisionNumber;
			log.debug("re-using locamVmCache with revNr " + cached.getRevisionNumber() + " for "
			        + revisionNumber);
			return cached;
		}
	}
	
	private boolean isConsistent(String key, XRevWritableModel value) {
		String generatedKey = KeyStructure.toString(getSnapshotKey(value.getRevisionNumber()));
		if(!key.equals(generatedKey)) {
			log.warn("entry.key = " + key + " vs. gen.key = " + generatedKey);
			return false;
		}
		return true;
	}
	
	/**
	 * @param requestedRevNr
	 * @return a copy of the cached model or null
	 */
	@GaeOperation()
	private XRevWritableModel localVmCacheGet(long requestedRevNr) {
		if(!USE_SNAPSHOT_CACHE)
			return null;
		
		XRevWritableModel cachedModel = getModelSnapshotsCache().get(requestedRevNr);
		
		log.debug(DebugFormatter.dataGet(DATASOURCE_SNAPSHOTS_VM, "" + requestedRevNr, cachedModel,
		        Timing.Now));
		
		if(cachedModel == null) {
			return null;
		} else if(cachedModel.equals(NONEXISTANT_MODEL)) {
			return null;
		} else {
			assert cachedModel.getRevisionNumber() == requestedRevNr;
			return cachedModel;
		}
	}
	
	/**
	 * @param snapshot is stored
	 */
	@GaeOperation()
	private void localVmCachePut(XRevWritableModel snapshot) {
		log.debug(DebugFormatter.dataPut(DATASOURCE_SNAPSHOTS_VM,
		        "" + snapshot.getRevisionNumber(), snapshot, Timing.Now));
		getModelSnapshotsCache().put(snapshot.getRevisionNumber(), snapshot);
	}
	
	@GaeOperation()
	private void localVmCachePutNull(long revNr) {
		log.debug(DebugFormatter.dataPut(DATASOURCE_SNAPSHOTS_VM, "" + revNr, null, Timing.Now));
		getModelSnapshotsCache().put(revNr, NONEXISTANT_MODEL);
	}
	
	@Override
	public XRevWritableModel getModelSnapshot(long requestedRevNr, boolean precise) {
		if(requestedRevNr == -1) {
			return null;
		}
		if(requestedRevNr == 0) {
			// model must be empty
			return new SimpleModel(this.modelAddress);
		}
		if(precise) {
			return XCopyUtils.createSnapshot(getModelSnapshot(requestedRevNr));
		} else {
			return XCopyUtils.createSnapshot(getModelSnapshotNewerOrAtRevision(requestedRevNr));
		}
	}
	
	@Override
	public XAddress getModelAddress() {
		return this.modelAddress;
	}
	
	@Override
	public XRevWritableModel getPartialSnapshot(long snapshotRev, Iterable<XAddress> locks) {
		log.debug("getPartialSnapshot[" + snapshotRev + "]");
		
		if(snapshotRev == -1) {
			return null;
		}
		if(snapshotRev == 0) {
			// model must be empty
			return new SimpleModel(this.modelAddress);
		}
		
		Iterator<XAddress> it = locks.iterator();
		if(!it.hasNext()) {
			return null;
		}
		if(it.next().equals(getModelAddress())) {
			assert !it.hasNext();
			return getModelSnapshot(snapshotRev, true);
		}
		
		XRevWritableModel src = getModelSnapshot(snapshotRev);
		
		SimpleModel model = new SimpleModel(getModelAddress());
		
		for(XAddress lock : locks) {
			
			if(lock.getObject() == null) {
				continue;
			}
			
			XRevWritableObject objectSrc = src.getObject(lock.getObject());
			if(objectSrc == null) {
				continue;
			}
			
			if(lock.getField() == null) {
				assert !model.hasObject(lock.getObject());
				model.addObject(objectSrc);
				continue;
			}
			
			XRevWritableObject object = model.createObject(lock.getObject());
			assert object != objectSrc;
			object.setRevisionNumber(objectSrc.getRevisionNumber());
			
			XRevWritableField fieldSrc = objectSrc.getField(lock.getField());
			if(fieldSrc == null) {
				continue;
			}
			
			object.addField(fieldSrc);
		}
		
		return model;
	}
}

package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XWritableModel;
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
 * @author dscharrer
 * @author xamde
 */
public class GaeSnapshotService2 {
	
	private static final class NonexistantModel implements XReadableModel {
		
		@Override
		public XAddress getAddress() {
			return null;
		}
		
		@Override
		public XID getID() {
			return XX.toId("_NonExistant");
		}
		
		@Override
		public XReadableObject getObject(XID objectId) {
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
		
	}
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotService2.class);
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	private static final NonexistantModel NONEXISTANT_MODEL = new NonexistantModel();
	
	/** property name for storing serialised XML content of a snapshot */
	private static final String PROP_XML = "xml";
	
	private static final long SNAPSHOT_PERSISTENCE_THRESHOLD = 10;
	
	private static final boolean USE_MEMCACHE = false;
	
	public static boolean USE_SNAPSHOT_CACHE = true;
	
	private final IGaeChangesService changesService;
	
	private final XAddress modelAddress;
	
	/**
	 * @param modelAddress of which model is this the changes service
	 * @param changesService The change log to load snapshots from.
	 */
	public GaeSnapshotService2(XAddress modelAddress, IGaeChangesService changesService) {
		this.modelAddress = modelAddress;
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
	 *            datastore or memcache
	 * @return a computed model snapshot
	 */
	private XRevWritableModel computeSnapshot(long requestedRevNr) {
		assert requestedRevNr >= 0;
		assert requestedRevNr <= this.changesService.getCurrentRevisionNumber() : "Requested snapshot version "
		        + requestedRevNr
		        + " higher than latest model version "
		        + this.changesService.getCurrentRevisionNumber();
		log.debug("compute snapshot " + requestedRevNr);
		
		Map<String,Object> batchResult = Collections.emptyMap();
		long askNr = requestedRevNr - 1;
		while(askNr >= 0 && batchResult.isEmpty()) {
			// prepare a batch-fetch in the mem-cache
			List<String> keys = new LinkedList<String>();
			while(askNr >= 0 && keys.size() <= SNAPSHOT_PERSISTENCE_THRESHOLD) {
				keys.add(KeyStructure.toString(getSnapshotKey(askNr)));
				askNr--;
			}
			// execute
			if(USE_MEMCACHE) {
				// FIXME ask only for 'possibly cached' (mod 10) numbers
				batchResult = Memcache.getEntities(keys);
				GaeAssert.gaeAssert(cacheResultIsConsistent(batchResult),
				        "cache inconsistent, see logs");
				if(batchResult.isEmpty()) {
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
						        + DebugFormatter.format(keys));
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
	 * @param base might have revNr == -1; content will not be changed.
	 * @param requestedRevNr
	 * @return a serialisable, computed snapshot
	 */
	private XRevWritableModel computeSnapshotFromBase(XRevWritableModel base, long requestedRevNr) {
		GaeAssert.gaeAssert(requestedRevNr > 0);
		GaeAssert.gaeAssert(requestedRevNr > base.getRevisionNumber());
		log.debug("Compute snapshot of model '" + this.modelAddress + "' from rev="
		        + base.getRevisionNumber() + " to rev=" + requestedRevNr);
		// prepare base IMPROVE not necessary if coming from memcache?
		XRevWritableModel baseModel = XCopyUtils.createSnapshot(base);
		
		// get events between [ start, end )
		List<XEvent> events = this.changesService.getEventsBetween(
		        Math.max(baseModel.getRevisionNumber() + 1, 0), requestedRevNr);
		
		// apply events to base
		for(XEvent event : events) {
			log.debug("Basemodel[" + baseModel.getRevisionNumber() + "], applying event["
			        + event.getRevisionNumber() + "]=" + event);
			EventUtils.applyEvent(baseModel, event);
			
			localVmCachePut(baseModel);
			long rev = baseModel.getRevisionNumber();
			// cache every 10th snapshot in memcache
			if(rev % 10 == 0) {
				Key key = getSnapshotKey(rev);
				if(USE_MEMCACHE) {
					Memcache.put(key, baseModel);
				}
				// TODO SCALE put also in datastore (async)
				assert isConsistent(KeyStructure.toString(key), baseModel);
			}
		}
		return baseModel;
	}
	
	/**
	 * For this model: revNr -> modelSnapshot
	 * 
	 * FIXME SCALE avoid growing large, keep only most recent version?
	 */
	@SuppressWarnings("unchecked")
	private SortedMap<Long,XReadableModel> getModelSnapshotsCache() {
		String key = "snapshots:" + this.changesService.getModelAddress();
		Map<String,Object> instanceCache = InstanceContext.getInstanceCache();
		SortedMap<Long,XReadableModel> modelSnapshotsCache;
		synchronized(instanceCache) {
			modelSnapshotsCache = (SortedMap<Long,XReadableModel>)instanceCache.get(key);
			if(modelSnapshotsCache == null) {
				log.debug("localVmcache for snapshots missing, creating one");
				modelSnapshotsCache = new TreeMap<Long,XReadableModel>();
				instanceCache.put(key, modelSnapshotsCache);
			} else if(modelSnapshotsCache.size() > 100) {
				modelSnapshotsCache.clear();
			}
		}
		return modelSnapshotsCache;
	}
	
	/**
	 * Precise, slower.
	 * 
	 * @param requestedRevNr of the returned snapshot
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	synchronized public XWritableModel getSnapshot(long requestedRevNr) {
		// assert requestedRevNr > 0 &
		// this.changesService.getCurrentRevisionNumber() > 0 : "requested:"
		// + requestedRevNr + " current:" +
		// this.changesService.getCurrentRevisionNumber();
		log.debug("Get snapshot " + this.modelAddress + " " + requestedRevNr);
		
		/* if localVmCache has exact requested version, use it */
		XWritableModel cached = localVmCacheGet(requestedRevNr);
		if(cached != null) {
			log.debug("return locally cached");
			return cached;
		}
		
		/*
		 * IMPROVE PERFORMANCE if the local VM cache is only behind a few
		 * revisions, it might be faster to update it than to load a snapshot
		 * from the memcache
		 */
		XWritableModel snapshot = getSnapshotFromMemcacheOrDatastore(requestedRevNr);
		return snapshot;
	}
	
	/**
	 * Implementation note: As XEntites are not {@link Serializable} by default,
	 * an XML-serialisation is stored in data store and memcache.
	 * 
	 * @param requestedRevNr for which to retrieve a snapshot.
	 * @return a snapshot with the requested revisionNumber or null if model was
	 *         null at that revision.
	 */
	@GaeOperation(datastoreRead = true ,memcacheRead = true)
	synchronized private XWritableModel getSnapshotFromMemcacheOrDatastore(long requestedRevNr) {
		assert requestedRevNr > 0 & this.changesService.getCurrentRevisionNumber() > 0;
		log.debug("getSnapshotFromMemcacheOrDatastore " + requestedRevNr);
		// try to retrieve an exact match for the required revisionNumber
		// memcache + datastore read
		Key snapshotKey = getSnapshotKey(requestedRevNr);
		Object o = null;
		if(USE_MEMCACHE) {
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
	
	private synchronized Key getSnapshotKey(long revNr) {
		return KeyFactory.createKey(KIND_SNAPSHOT, this.modelAddress.toURI() + "/" + revNr);
	}
	
	/**
	 * Fast, imprecise.
	 * 
	 * @param revisionNumber which is the minimum revision number that the
	 *            returned snapshot will have, i.e. the caller might get an even
	 *            more recent snapshot
	 * @return a snapshot with the given revisionNumber or a higher one. Or
	 *         null, if snapshot was not present at requested revisionNumber.
	 */
	synchronized public XWritableModel getSnapshotNewerOrAtRevision(long revisionNumber) {
		/* look for all versions at this or higher revNrs */
		SortedMap<Long,XReadableModel> modelSnapshotsCache = getModelSnapshotsCache();
		SortedMap<Long,XReadableModel> matchOrNewer;
		synchronized(modelSnapshotsCache) {
			matchOrNewer = modelSnapshotsCache.subMap(revisionNumber, Long.MAX_VALUE);
		}
		if(!USE_SNAPSHOT_CACHE || matchOrNewer.isEmpty()) {
			// not cached
			return getSnapshot(revisionNumber);
		} else {
			XReadableModel cached = matchOrNewer.values().iterator().next();
			assert cached.getRevisionNumber() >= revisionNumber;
			log.debug("re-using locamVmCache with revNr " + cached.getRevisionNumber() + " for "
			        + revisionNumber);
			return XCopyUtils.createSnapshot(cached);
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
	private XWritableModel localVmCacheGet(long requestedRevNr) {
		if(!USE_SNAPSHOT_CACHE)
			return null;
		
		XReadableModel cachedModel = getModelSnapshotsCache().get(requestedRevNr);
		if(cachedModel == null) {
			return null;
		} else if(cachedModel.equals(NONEXISTANT_MODEL)) {
			return null;
		} else {
			assert cachedModel.getRevisionNumber() == requestedRevNr;
			return XCopyUtils.createSnapshot(cachedModel);
		}
	}
	
	/**
	 * @param snapshot a copy of it is stored
	 */
	@GaeOperation()
	private void localVmCachePut(XRevWritableModel snapshot) {
		getModelSnapshotsCache().put(snapshot.getRevisionNumber(),
		        XCopyUtils.createSnapshot(snapshot));
	}
	
	private void localVmCachePutNull(long revNr) {
		getModelSnapshotsCache().put(revNr, NONEXISTANT_MODEL);
	}
	
}

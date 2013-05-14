package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.EventUtils;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.util.DumpUtils;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.Memcache;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.gae.snapshot.AbstractGaeSnapshotServiceImpl;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.common.collect.Sets;


/**
 * Computes *Snapshots ( {@link SimpleField}, {@link SimpleObject},
 * {@link SimpleModel}) from a given {@link XChangeLog}.
 * 
 * An entity of kind 'XMSNAPSHOT' with the key (model-address) + '/-/-/' +
 * (revNr) contains a snapshot for a model with the given address and the given
 * revNr - containing ONLY the referenced objectIds.
 * 
 * Recent improvements:
 * <ul>
 * <li>Avoid unnecessary copies when updating snapshots / while generating a
 * partial snapshot.
 * </ul>
 * 
 * <h3>New strategy for caching snapshots</h3> There are basically three
 * operation modes:
 * <ul>
 * <li>Low traffic, working window size == 1</li>
 * <li>High traffic, working window grows larger, concurrent requests are
 * handled</li>
 * <li>Memcache clear: All cached snapshots are lost and need to be recomputed</li>
 * </ul>
 * therefore we define:
 * <ul>
 * <li>STANDARD_DISTANCE how many snapshots to always NOT cache, as it is so
 * cheap to re-compute them given another snapshot an up to STANDARD_DISTANCE
 * events</li>
 * <li>MAX_WORKING_WINDOW is the maximal working window size that is efficiently
 * supported</li>
 * <li>EVENTS_PER_WORKER is the number of events the average request handler can
 * safely apply before timing out. This is the interval in which we must persist
 * to datastore, in order to let other threads continue from there.</li>
 * </ul>
 * 
 * 
 * 
 * @author dscharrer
 * @author xamde
 */
public class GaeSnapshotServiceImpl5 extends AbstractGaeSnapshotServiceImpl {
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotServiceImpl5.class);
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	
	/** property name for storing serialised XML content of a snapshot */
	private static final String PROP_XML = "xml";
	
	private static final long STANDARD_DISTANCE = 10;
	
	@Setting("if memcache should be used to cache snapshots")
	private static final boolean USE_MEMCACHE = true;
	
	/**
	 * @param requestedRevNr
	 * @return true if the given revision number is a candidate for memcache
	 */
	private static boolean revCanBeMemcached(long requestedRevNr) {
		return requestedRevNr % STANDARD_DISTANCE == 0;
	}
	
	private final ChangeLogManager changelogManager;
	
	private final XAddress modelAddress;
	
	/**
	 * @param changelogManager The change log to load snapshots from.
	 */
	public GaeSnapshotServiceImpl5(ChangeLogManager changelogManager) {
		this.modelAddress = changelogManager.getModelAddress();
		this.changelogManager = changelogManager;
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
	 * @param requestedRevNr
	 * @param base content will be changed; @NeverNull
	 * @return a snapshot in revision 'requestedRevNr' by applying changes to
	 *         given base model
	 */
	private XRevWritableModel computeAndCacheSnapshotFromBase(long requestedRevNr,
	        @NeverNull XRevWritableModel base) {
		XyAssert.xyAssert(base != null);
		assert base != null;
		XyAssert.xyAssert(this.modelAddress.equals(base.getAddress()));
		XRevWritableModel requestedSnapshot = computeSnapshotFromBase(base, requestedRevNr);
		return requestedSnapshot;
	}
	
	/**
	 * Compute requested snapshot by using an older snapshot version (if one is
	 * found in memcache). Puts intermediary version in the respective caches.
	 * 
	 * @param requestedRevNr which is required but has no direct match in the
	 *            datastore or memcache
	 * 
	 *            FIXME 2012-02 make sure too high numbers are handled well.
	 *            This allows callers to retrieve a new version which has still
	 *            uncommitted versions below it.
	 * @return a computed model snapshot
	 */
	private XRevWritableModel computeSnapshot(long requestedRevNr) {
		log.debug("compute snapshot " + requestedRevNr);
		XyAssert.xyAssert(requestedRevNr >= 0);
		
		Map<String,Object> batchResult = Collections.emptyMap();
		List<String> requestKeys = new LinkedList<String>();
		long askNr = requestedRevNr - 1;
		
		long askedRevs = 0;
		Object snapshotfromMemcache = null;
		if(USE_MEMCACHE) {
			while(askNr >= 0 && askedRevs < 5) {
				askNr--;
				if(revCanBeMemcached(askNr) && askedRevs < 5) {
					String possiblyMemcachedKey = KeyStructure.toString(getSnapshotKey(askNr));
					requestKeys.add(possiblyMemcachedKey);
					askedRevs++;
				}
			}
			// prepare a fetch in the mem-cache
			XyAssert.xyAssert(requestKeys.size() <= 5, "asking for %s keys", requestKeys.size());
			batchResult = Memcache.getEntities(requestKeys);
			XyAssert.xyAssert(batchResult.size() <= 5, "got %s results", batchResult.size());
			XyAssert.xyAssert(cacheResultIsConsistent(batchResult), "cache inconsistent, see logs");
			
			// IMPROVE PERFORMANCE use *highest* revision within response
			// any element can serve as base for further computation
			for(Entry<String,Object> entry : batchResult.entrySet()) {
				Object v = entry.getValue();
				if(v != null && v != Memcache.NULL_ENTITY) {
					snapshotfromMemcache = v;
					break;
				}
			}
		}
		
		// TODO SCALE if no hits, ask datastore (make sure to put it there, too)
		
		XRevWritableModel base;
		if(snapshotfromMemcache == null) {
			log.debug("we start from scratch, nobody has ever saved a snapshot. Found no snapshots at "
			        + Arrays.toString(requestKeys.toArray()));
			base = new SimpleModel(this.modelAddress);
			base.setRevisionNumber(MODEL_DOES_NOT_EXIST);
		} else {
			XyAssert.xyAssert(snapshotfromMemcache instanceof XRevWritableModel);
			base = (XRevWritableModel)snapshotfromMemcache;
			XyAssert.xyAssert(base.getAddress().equals(this.modelAddress));
		}
		log.debug("compute from " + base.getRevisionNumber() + " up to " + requestedRevNr);
		
		return computeAndCacheSnapshotFromBase(requestedRevNr, base);
	}
	
	/**
	 * Compute a snapshot by applying all events that happened between base's
	 * revision and the requested revisionNumber.
	 * 
	 * @param base might have revNr == -1; content *will* be changed. @NeverNull
	 * @param requestedRevNr
	 * @return a serialisable, computed snapshot
	 */
	private XRevWritableModel computeSnapshotFromBase(@NeverNull XRevWritableModel base,
	        long requestedRevNr) {
		XyAssert.xyAssert(base != null);
		assert base != null;
		XyAssert.xyAssert(base.getRevisionNumber() < requestedRevNr,
		        "otherwise it makes no sense to compute it");
		XyAssert.xyAssert(requestedRevNr > 0);
		XRevWritableModel snapshot = base;
		XyAssert.xyAssert(requestedRevNr > snapshot.getRevisionNumber());
		log.debug("Compute snapshot of model '" + this.modelAddress + "' from rev="
		        + snapshot.getRevisionNumber() + " to rev=" + requestedRevNr);
		
		// get events between [ start, end )
		long start = Math.max(snapshot.getRevisionNumber() + 1, 0);
		Interval requestedRange = new Interval(start, requestedRevNr);
		
		/*
		 * if interval is large, compute intermediate snapshots and store them
		 */
		if(!requestedRange.isEmpty()) {
			/**
			 * get requested events in batches of MAXIMAL_CHANGES_FETCH_SIZE and
			 * use smaller ranges when exceptions occur
			 */
			Interval stepRange = requestedRange
			        .getSubInterval(ChangeLogManager.MAXIMAL_CHANGES_FETCH_SIZE);
			log.debug("Fetching events " + stepRange);
			List<XEvent> events;
			do {
				log.debug("Get events in range " + stepRange);
				events = this.changelogManager.getEventsInInterval(stepRange);
				// This should not happen and should be fixed somewhere else
				if(events.isEmpty()) {
					log.warn("There are no events for " + this.modelAddress + " in range [" + start
					        + "," + requestedRevNr + "]");
				}
				assert events != null;
				
				// apply events to base
				long memcachedSnapshots = 0;
				for(XEvent event : events) {
					log.trace("Basemodel[" + snapshot.getRevisionNumber() + "], applying event["
					        + event.getRevisionNumber() + "]=" + DebugFormatter.format(event));
					snapshot = EventUtils.applyEventNonDestructive(snapshot, event);
					
					long rev = snapshot.getRevisionNumber();
					if(USE_MEMCACHE && revCanBeMemcached(rev) && memcachedSnapshots < 10) {
						// cache some snapshots in memcache
						putInMemcacheIfNotTooLarge(rev, snapshot);
						memcachedSnapshots++;
						// TODO SCALE put also some in datastore (async)
					}
					
				}
				stepRange = stepRange.moveRightAndShrinkToKeepEndMaxAt(requestedRange.end);
			} while(stepRange.start <= requestedRange.end && events.size() > 0);
		}
		
		XyAssert.xyAssert(snapshot.getRevisionNumber() == requestedRevNr,
		        "got %s when I requested %s", snapshot.getRevisionNumber(), requestedRevNr);
		
		// when done, always cache 1 more snapshots
		if(USE_MEMCACHE) {
			putInMemcacheIfNotTooLarge(requestedRevNr, snapshot);
		}
		
		return snapshot;
	}
	
	private static Set<XAddress> uncacheable = Sets
	        .newSetFromMap(new ConcurrentHashMap<XAddress,Boolean>());
	
	private void putInMemcacheIfNotTooLarge(long rev, XRevWritableModel snapshot) {
		Key key = getSnapshotKey(rev);
		XyAssert.xyAssert(isConsistent(KeyStructure.toString(key), snapshot));
		if(!uncacheable.contains(snapshot.getAddress())) {
			try {
				Memcache.put(key, snapshot);
			} catch(MemcacheServiceException e) {
				/*
				 * remember on this instance that the snapshot is probably too
				 * big and don't try to store it again
				 */
				uncacheable.add(snapshot.getAddress());
				log.info("Snapshot " + snapshot.getAddress() + " [" + snapshot.getRevisionNumber()
				        + "] could not be memcached, most likely too big. Size estimated as "
				        + estimateSizeOf(snapshot, 1024 * 1024), e);
			}
		}
	}
	
	/**
	 * @param model
	 * @param max as soon as this limit is reached the method stops calculating
	 *            further. Use Long.MAX_VALUE for most accurate estimation.
	 * @return a lower bound for the size in bytes
	 */
	public static long estimateSizeOf(XReadableModel model, long max) {
		long bytes = 0;
		if(model == null)
			return 0;
		for(XId oid : model) {
			bytes += oid.toString().length() * 2;
			XReadableObject obj = model.getObject(oid);
			if(obj != null) {
				for(XId fid : obj) {
					bytes += fid.toString().length() * 2;
					XReadableField field = obj.getField(fid);
					XValue value = field.getValue();
					if(value != null) {
						bytes += value.toString().length();
						if(bytes >= max) {
							return bytes;
						}
					}
				}
			}
		}
		return bytes;
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
		
		// for(long i = 1; i <= SNAPSHOT_LOAD_THRESHOLD; i++) {
		// XRevWritableModel oldCached = localVmCacheGet(requestedRevNr - i);
		// if(oldCached != null) {
		// log.debug("Found oldCached rev " + oldCached.getRevisionNumber()
		// + " and will compute from there on");
		// return computeAndCacheSnapshotFromBase(requestedRevNr, oldCached);
		// }
		//
		// }
		
		/*
		 * IMPROVE PERFORMANCE if the local VM cache is only behind a few
		 * revisions, it might be faster to update it than to load a snapshot
		 * from the memcache
		 */
		XRevWritableModel snapshot = getSnapshotFromMemcacheOrDatastore(requestedRevNr);
		return snapshot;
	}
	
	@Override
	public XAddress getModelAddress() {
		return this.modelAddress;
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
		return XCopyUtils.createSnapshot(createModelSnapshot(requestedRevNr));
	}
	
	/*
	 * we can safely return references to existing parts, the caller knows not
	 * to change them
	 */
	@Override
	public XRevWritableModel getPartialSnapshot(long snapshotRev, Iterable<XAddress> locks) {
		log.debug("getPartialSnapshot[" + snapshotRev + "]");
		
		if(snapshotRev == -1) {
			// model cannot exist
			return null;
		}
		if(snapshotRev == 0) {
			// model must be empty
			return new SimpleModel(this.modelAddress);
		}
		
		/* no locks requested? */
		if(IteratorUtils.isEmpty(locks)) {
			return null;
		}
		
		/* locking the whole model? */
		Iterator<XAddress> it = locks.iterator();
		if(it.next().equals(getModelAddress())) {
			XyAssert.xyAssert(!it.hasNext());
			return getModelSnapshot(snapshotRev, true);
		}
		
		// FIXME use object snapshots down here???
		
		XRevWritableModel fullModel = createModelSnapshot(snapshotRev);
		SimpleModel partialModel = new SimpleModel(getModelAddress());
		for(XAddress lock : locks) {
			switch(lock.getAddressedType()) {
			case XREPOSITORY:
				throw new AssertionError("Encountered REPO lock while computing partial snapshot");
			case XMODEL:
				throw new AssertionError("Encountered MODEL lock - was processed already");
			case XOBJECT:
				XRevWritableObject fullObject = fullModel.getObject(lock.getObject());
				if(fullObject == null) {
					log.info("Locking an object not yet present in snapshot: " + lock);
				} else {
					partialModel.addObject(fullObject);
				}
				break;
			case XFIELD:
				// maybe create object first
				XId oid = lock.getObject();
				/*
				 * must be partial. If there was an object lock there would not
				 * have been a field lock in the same object. Locks are minimal.
				 */
				XRevWritableObject partialObject = partialModel.createObject(oid);
				partialObject.setRevisionNumber(XEvent.REVISION_NOT_AVAILABLE);
				// copy field
				XRevWritableField fullField = fullModel.getObject(oid).getField(lock.getField());
				if(fullField == null) {
					log.info("Locking a field not yet present in snapshot: " + lock);
				} else {
					partialObject.addField(fullField);
				}
				break;
			default:
				break;
			}
		}
		
		log.trace("Partial snapshot: " + DumpUtils.toStringBuffer(partialModel));
		
		return partialModel;
	}
	
	// /**
	// * @param requestedRevNr
	// * @return a copy of the cached model or null
	// */
	// @GaeOperation()
	// private XRevWritableModel localVmCacheGet(long requestedRevNr) {
	// if(!USE_SNAPSHOT_CACHE)
	// return null;
	//
	// XRevWritableModel cachedModel =
	// getModelSnapshotsCache().get(requestedRevNr);
	//
	// log.debug(DebugFormatter.dataGet(DATASOURCE_SNAPSHOTS_VM, "" +
	// requestedRevNr, cachedModel,
	// Timing.Now));
	//
	// if(cachedModel == null) {
	// return null;
	// } else if(cachedModel.equals(NONEXISTANT_MODEL)) {
	// return null;
	// } else {
	// assert cachedModel.getRevisionNumber() == requestedRevNr;
	// return cachedModel;
	// }
	// }
	//
	// /**
	// * @param snapshot is stored
	// */
	// @GaeOperation()
	// private void localVmCachePut(XRevWritableModel snapshot) {
	// log.debug(DebugFormatter.dataPut(DATASOURCE_SNAPSHOTS_VM,
	// "" + snapshot.getRevisionNumber(), snapshot, Timing.Now));
	// getModelSnapshotsCache().put(snapshot.getRevisionNumber(), snapshot);
	// }
	//
	// @GaeOperation()
	// private void localVmCachePutNull(long revNr) {
	// log.debug(DebugFormatter.dataPut(DATASOURCE_SNAPSHOTS_VM, "" + revNr,
	// null, Timing.Now));
	// getModelSnapshotsCache().put(revNr, NONEXISTANT_MODEL);
	// }
	
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
		XyAssert.xyAssert(requestedRevNr > 0);
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
					// localVmCachePutNull(requestedRevNr);
					return null;
				}
				XyAssert.xyAssert(isConsistent(KeyStructure.toString(snapshotKey),
				        (XRevWritableModel)o));
				XRevWritableModel snapshot = (XRevWritableModel)o;
				XyAssert.xyAssert(snapshot.getRevisionNumber() == requestedRevNr);
				// localVmCachePut(snapshot);
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
			// localVmCachePut(snapshot);
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
	 * @param key
	 * @param value
	 * @return true if the key matches the revision number of the value
	 */
	private boolean isConsistent(String key, XRevWritableModel value) {
		String generatedKey = KeyStructure.toString(getSnapshotKey(value.getRevisionNumber()));
		if(!key.equals(generatedKey)) {
			log.warn("entry.key = " + key + " vs. gen.key = " + generatedKey);
			return false;
		}
		return true;
	}
	
	@Override
	public XWritableModel getTentativeModelSnapshot(long currentRevNr) {
		
		/* prepare query for all TOS parts */
		List<TentativeObjectState> list = ContextBeforeCommand
		        .getAllTentativeObjectStatesOfModel(getModelAddress());
		
		SimpleModel model = new SimpleModel(getModelAddress());
		model.setRevisionNumber(currentRevNr);
		for(TentativeObjectState tos : list) {
			if(tos.exists()) {
				model.addObject(tos);
			}
		}
		
		return model;
	}
	
}

package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
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
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.changes.GaeChangesService;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;


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
	
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotService2.class);
	private final GaeChangesService changesService;
	private final XAddress modelAddress;
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	/** property name for storing serialised XML content of a snapshot */
	private static final String PROP_XML = "xml";
	
	private static final long SNAPSHOT_PERSISTENCE_THRESHOLD = 10;
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	
	/**
	 * @param modelAddress of which model is this the changes service
	 * @param changesService The change log to load snapshots from.
	 */
	public GaeSnapshotService2(XAddress modelAddress, GaeChangesService changesService) {
		this.modelAddress = modelAddress;
		this.changesService = changesService;
	}
	
	private SortedMap<Long,XReadableModel> localVmModelSnapshotsCache = new TreeMap<Long,XReadableModel>();
	
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
		SortedMap<Long,XReadableModel> matchOrNewer = this.localVmModelSnapshotsCache.subMap(
		        revisionNumber, Long.MAX_VALUE);
		if(matchOrNewer.isEmpty()) {
			// not cached
			return getSnapshot(revisionNumber);
		} else {
			XReadableModel cached = matchOrNewer.values().iterator().next();
			assert cached.getRevisionNumber() >= revisionNumber;
			log.trace("re-using locamVmCache with revNr " + cached.getRevisionNumber() + " for "
			        + revisionNumber);
			return XCopyUtils.createSnapshot(cached);
		}
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
		assert requestedRevNr > 0 & this.changesService.getCurrentRevisionNumber() > 0;
		log.debug("Get snapshot " + this.modelAddress + " " + requestedRevNr);
		
		/* if localVmCache has exact requested version, use it */
		XWritableModel cached = localVmCacheGet(requestedRevNr);
		if(cached != null) {
			return cached;
		}
		
		// IMPROVE if the local VM cache is only behind a few revisions, it
		// might be faster to update it than to load a snapshot from the
		// memcache
		XWritableModel snapshot = getSnapshotFromMemcacheOrDatastore(requestedRevNr);
		if(snapshot.getRevisionNumber() == MODEL_DOES_NOT_EXIST)
			return null;
		else
			return snapshot;
	}
	
	/**
	 * @param requestedRevNr
	 * @return a copy of the cached model or null
	 */
	@GaeOperation()
	private XWritableModel localVmCacheGet(long requestedRevNr) {
		XReadableModel cachedModel = this.localVmModelSnapshotsCache.get(requestedRevNr);
		if(cachedModel == null)
			return null;
		else {
			assert cachedModel.getRevisionNumber() == requestedRevNr;
			return XCopyUtils.createSnapshot(cachedModel);
		}
	}
	
	/**
	 * Implementation note: As XEntites are not {@link Serializable} by default,
	 * an XML-serialisation is stored in data store and memcache.
	 * 
	 * @param requestedRevNr for which to retrieve a snapshot.
	 * @return a snapshot with the requested revisionNumber of null if model was
	 *         null at that revision.
	 */
	@GaeOperation(datastoreRead = true ,memcacheRead = true)
	synchronized private XWritableModel getSnapshotFromMemcacheOrDatastore(long requestedRevNr) {
		assert requestedRevNr > 0 & this.changesService.getCurrentRevisionNumber() > 0;
		// try to retrieve an exact match for the required revisionNumber
		// memcache + datastore read
		Key key = getSnapshotKey(requestedRevNr);
		Object o = XydraRuntime.getMemcache().get(key);
		if(o != null) {
			XRevWritableModel snapshot = (XRevWritableModel)o;
			assert snapshot.getRevisionNumber() == requestedRevNr;
			localVmCachePut(snapshot);
			return snapshot;
		}
		// else: look for direct match in datastore
		Entity e = GaeUtils.getEntityFromDatastore(key);
		if(e != null) {
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
	
	/**
	 * @param snapshot a copy of it is stored
	 */
	@GaeOperation()
	private void localVmCachePut(XRevWritableModel snapshot) {
		this.localVmModelSnapshotsCache.put(snapshot.getRevisionNumber(),
		        XCopyUtils.createSnapshot(snapshot));
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
		assert requestedRevNr <= this.changesService.getCurrentRevisionNumber() : "Requested snapshot version "
		        + requestedRevNr
		        + " higher than latest model version "
		        + this.changesService.getCurrentRevisionNumber();
		
		Map<Object,Object> batchResult = Collections.emptyMap();
		long askNr = requestedRevNr - 1;
		while(askNr >= 0 && batchResult.isEmpty()) {
			// prepare a batch-fetch in the mem-cache
			List<Object> keys = new LinkedList<Object>();
			while(askNr >= 0 && keys.size() <= SNAPSHOT_PERSISTENCE_THRESHOLD) {
				keys.add(getSnapshotKey(askNr));
				askNr--;
			}
			// execute
			batchResult = XydraRuntime.getMemcache().getAll(keys);
			assert cacheResultIsConsistent(batchResult);
			// batch result will usually contain 1 hits
			if(askNr >= 0 && batchResult.size() != 1) {
				/*
				 * TODO IMPROVE make sure changes service puts every K versions
				 * a snapshot in memcache
				 */
				log.info("Got a batch result from memcache when asking for snapshot with "
				        + batchResult.size() + " entries for keys "
				        + new ArrayList<Object>(keys).toArray());
			}
			// Pump all results (if any) to localVmcache
			for(Object o : batchResult.values()) {
				assert o instanceof XRevWritableModel;
				XRevWritableModel snapshotFromMemcache = (XRevWritableModel)o;
				localVmCachePut(snapshotFromMemcache);
			}
		}
		
		// TODO if no hits, ask datastore for older snapshots
		
		XRevWritableModel base;
		if(batchResult.isEmpty()) {
			// we start from scratch, nobody has ever saved a snapshot
			base = new SimpleModel(this.modelAddress);
			base.setRevisionNumber(MODEL_DOES_NOT_EXIST);
		} else {
			// first element can serve as base for further computation
			Object snapshotfromMemcache = batchResult.values().iterator().next();
			assert snapshotfromMemcache instanceof XRevWritableModel;
			base = (XRevWritableModel)snapshotfromMemcache;
			assert base.getAddress().equals(this.modelAddress);
		}
		
		XRevWritableModel requestedSnapshot = computeSnapshotFromBase(base, requestedRevNr);
		localVmCachePut(requestedSnapshot);
		// cache it in memcache
		XydraRuntime.getMemcache().put(this.getSnapshotKey(requestedSnapshot.getRevisionNumber()),
		        requestedSnapshot);
		// return it
		return requestedSnapshot;
	}
	
	private boolean cacheResultIsConsistent(Map<Object,Object> batchResult) {
		for(Entry<Object,Object> entry : batchResult.entrySet()) {
			if(!entry.getKey().equals(
			        getSnapshotKey(((XRevWritableModel)entry.getValue()).getRevisionNumber()))) {
				return false;
			}
		}
		return true;
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
		assert requestedRevNr > 0;
		assert requestedRevNr > base.getRevisionNumber();
		log.debug("Compute snapshot of model '" + this.modelAddress + "' from rev="
		        + base.getRevisionNumber() + " to rev=" + requestedRevNr);
		// prepare base
		XRevWritableModel baseModel = XCopyUtils.createSnapshot(base);
		
		// get events between [ start, end )
		List<XEvent> events = this.changesService.getEventsBetween(
		        Math.max(baseModel.getRevisionNumber() + 1, 0), requestedRevNr);
		
		// apply events to base
		for(XEvent event : events) {
			log.trace("Applying " + event);
			EventUtils.applyEvent(baseModel, event);
			localVmCachePut(baseModel);
		}
		return baseModel;
	}
	
	private synchronized Key getSnapshotKey(long revNr) {
		return KeyFactory.createKey(KIND_SNAPSHOT, this.modelAddress.toURI() + "/" + revNr);
	}
	
}

package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.base.XAddress;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
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
 * 
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
	 * @param modelAddress TODO
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
	 * @param revisionNumber of the returned snapshot
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	synchronized public XWritableModel getSnapshot(long revisionNumber) {
		if(revisionNumber < 0) {
			return null;
		}
		
		XWritableModel snapshot;
		
		/* if localVmCache has exact requested version, use it */
		XReadableModel cached = this.localVmModelSnapshotsCache.get(revisionNumber);
		if(cached != null) {
			assert cached.getRevisionNumber() == revisionNumber;
			snapshot = XCopyUtils.createSnapshot(cached);
		}
		
		// IMPROVE if the local VM cache is only behind a few revisions, it
		// might be faster to update it than to load a snapshot from the
		// memcache
		snapshot = getSnapshotFromMemcacheOrDatastore(revisionNumber);
		
		if(snapshot.getRevisionNumber() == MODEL_DOES_NOT_EXIST)
			return null;
		else
			return snapshot;
	}
	
	/**
	 * Implementation note: As XEntites are not {@link Serializable} by default,
	 * an XML-serialisation is stored in data store and memcache.
	 * 
	 * @param revisionNumber for which to retrieve a snapshot.
	 * @return a snapshot with the requested revisionNumber of null if model was
	 *         null at that revision.
	 */
	@GaeOperation(datastoreRead = true ,memcacheRead = true)
	synchronized private XWritableModel getSnapshotFromMemcacheOrDatastore(long revisionNumber) {
		XRevWritableModel snapshot;
		// try to retrieve an exact match for the required revisionNumber
		// memcache + datastore read
		
		Key key = getSnapshotKey(revisionNumber);
		Object o = XydraRuntime.getMemcache().get(key);
		if(o != null) {
			snapshot = (XRevWritableModel)o;
		} else {
			// ask datastore
			Entity e = GaeUtils.getEntityFromDatastore(key);
			if(e != null) {
				Text xmlText = (Text)e.getProperty(PROP_XML);
				if(xmlText == null) {
					// model was null at that revision
					return null;
				}
				String xml = xmlText.getValue();
				XydraElement snapshotXml = new XmlParser().parse(xml);
				snapshot = SerializedModel.toModelState(snapshotXml, this.modelAddress);
				
			} else {
				// this snapshot has not been computed OR there exists no
				// snapshot
				// at such high numbers
				long modelRev = this.changesService.getCurrentRevisionNumber();
				if(revisionNumber > modelRev) {
					log.warn("Requested snapshot version " + revisionNumber
					        + " higher than latest model version " + modelRev);
					return null;
				}
				// need to compute snapshot from an older version
				snapshot = computeSnapshot(revisionNumber);
			}
		}
		
		// cache in local vm cache
		this.localVmModelSnapshotsCache.put(snapshot.getRevisionNumber(), snapshot);
		return snapshot;
	}
	
	/**
	 * Compute requested snapshot by using an older snapshot version (if one is
	 * found in memcache). Puts intermediary version in the respective caches.
	 * 
	 * @param revisionNumber which is required but has no direct match in the
	 *            datastore or memcache
	 * @return a computed model snapshot
	 */
	private XRevWritableModel computeSnapshot(long revisionNumber) {
		
		long askNr = revisionNumber - 1;
		Map<Object,Object> batchResult = Collections.emptyMap();
		
		while(askNr >= 0 && batchResult.isEmpty()) {
			// prepare a batch-fetch in the mem-cache
			List<Object> keys = new LinkedList<Object>();
			while(askNr >= 0 && keys.size() <= SNAPSHOT_PERSISTENCE_THRESHOLD) {
				keys.add(getSnapshotKey(askNr));
				askNr--;
			}
			// execute
			batchResult = XydraRuntime.getMemcache().getAll(keys);
			// batch result will usually contain 1 hits
			if(askNr >= 0 && batchResult.size() != 1) {
				log.warn("Got a batch result from memcache when asking for snapshot with "
				        + batchResult.size() + " entries for keys "
				        + new ArrayList<Object>(keys).toArray());
			}
			// Pump all results (if any) to localVmcache
			for(Object snapshotfromMemcache : batchResult.values()) {
				assert snapshotfromMemcache instanceof XRevWritableModel;
				XRevWritableModel snapshot = (XRevWritableModel)snapshotfromMemcache;
				this.localVmModelSnapshotsCache.put(snapshot.getRevisionNumber(), snapshot);
			}
		}
		
		// TODO if no hits, ask datastore for older snapshots
		
		// first element can serve as base for further computation
		XRevWritableModel base;
		if(batchResult.isEmpty()) {
			// we start from scratch, nobody has ever saved a snapshot
			base = new SimpleModel(this.modelAddress);
			base.setRevisionNumber(MODEL_DOES_NOT_EXIST);
		} else {
			Object snapshotfromMemcache = batchResult.values().iterator().next();
			assert snapshotfromMemcache instanceof XRevWritableModel;
			base = (XRevWritableModel)snapshotfromMemcache;
			assert base.getAddress().equals(this.modelAddress);
		}
		
		XRevWritableModel requestedSnapshot = computeSnapshotFromBase(base, revisionNumber);
		// cache it
		this.localVmModelSnapshotsCache.put(requestedSnapshot.getRevisionNumber(),
		        requestedSnapshot);
		// cache it in memcache
		XydraRuntime.getMemcache().put(this.getSnapshotKey(requestedSnapshot.getRevisionNumber()),
		        requestedSnapshot);
		// return it
		return requestedSnapshot;
	}
	
	/**
	 * Compute a snapshot by applying all events that happened between base's
	 * revision and the requested revisionNumber.
	 * 
	 * @param base might have revNr == -1
	 * @param revisionNumber
	 * @return a serialisable, computed snapshot
	 */
	private XRevWritableModel computeSnapshotFromBase(XRevWritableModel base, long revisionNumber) {
		assert revisionNumber >= 0;
		log.info("Compute snapshot of model '" + this.modelAddress + "' from rev="
		        + base.getRevisionNumber() + " to rev=" + revisionNumber);
		// prepare base
		XRevWritableModel model = XCopyUtils.createSnapshot(base);
		
		// FIXME ???
		if(revisionNumber == 0) {
			return base;
		}
		
		// get events between [ start, end )
		List<XEvent> events = this.changesService.getEventsBetween(
		        Math.max(model.getRevisionNumber(), 0), revisionNumber);
		
		// apply events to base
		for(XEvent event : events) {
			// FIXME
			System.out.println("Applying " + event);
			
			applyEvent(model, event);
			
			// cache
			this.localVmModelSnapshotsCache.put(model.getRevisionNumber(), model);
		}
		return model;
	}
	
	private static void applyEvent(XRevWritableModel model, XEvent event) {
		if(event instanceof XTransactionEvent) {
			for(XEvent txnEvent : ((XTransactionEvent)event)) {
				applyEvent(model, txnEvent);
			}
		} else {
			assert event instanceof XAtomicEvent;
			XAtomicEvent atomicEvent = (XAtomicEvent)event;
			switch(atomicEvent.getTarget().getAddressedType()) {
			case XREPOSITORY:
				applyRepositoryEvent(model, (XRepositoryEvent)event);
				break;
			case XMODEL:
				assert model.getRevisionNumber() >= 0;
				applyModelEvent(model, (XModelEvent)event);
				break;
			case XOBJECT:
				assert model.getRevisionNumber() >= 0;
				applyObjectEvent(model, (XObjectEvent)event);
				break;
			case XFIELD:
				assert model.getRevisionNumber() >= 0;
				applyFieldEvent(model, (XFieldEvent)event);
			}
		}
	}
	
	private static void applyFieldEvent(XRevWritableModel model, XFieldEvent event) {
		assert event.getTarget().getParent().getParent().equals(model.getAddress());
		XRevWritableObject object = model.getObject(event.getObjectId());
		assert object != null;
		XRevWritableField field = object.getField(event.getFieldId());
		assert field != null;
		
		switch(event.getChangeType()) {
		case ADD:
			// FIXME seems not no hold: assert field.isEmpty() :
			// field.getValue();
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			assert !field.isEmpty();
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
		field.setRevisionNumber(event.getRevisionNumber());
		object.setRevisionNumber(event.getRevisionNumber());
		model.setRevisionNumber(event.getRevisionNumber());
	}
	
	private static void applyObjectEvent(XRevWritableModel model, XObjectEvent event) {
		assert event.getTarget().getParent().equals(model.getAddress());
		XRevWritableObject object = model.getObject(event.getObjectId());
		assert object != null;
		
		switch(event.getChangeType()) {
		case ADD: {
			XRevWritableField field = object.createField(event.getFieldId());
			field.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
		object.setRevisionNumber(event.getRevisionNumber());
		model.setRevisionNumber(event.getRevisionNumber());
	}
	
	private static void applyModelEvent(XRevWritableModel model, XModelEvent event) {
		assert event.getTarget().equals(model.getAddress());
		switch(event.getChangeType()) {
		case ADD: {
			XRevWritableObject object = model.createObject(event.getObjectId());
			object.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
		model.setRevisionNumber(event.getRevisionNumber());
	}
	
	private static void applyRepositoryEvent(XRevWritableModel model, XRepositoryEvent event) {
		assert event.getChangedEntity().equals(model.getAddress());
		switch(event.getChangeType()) {
		case ADD: {
			assert model.getRevisionNumber() == MODEL_DOES_NOT_EXIST;
			model.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert model.getRevisionNumber() >= 0;
			model.setRevisionNumber(MODEL_DOES_NOT_EXIST);
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XRepositoryEvent cannot be this " + event);
		}
	}
	
	private synchronized Key getSnapshotKey(long revNr) {
		return KeyFactory.createKey(KIND_SNAPSHOT, this.modelAddress.toURI() + "/" + revNr);
	}
	
}

package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.ChangeType;
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
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.changes.AsyncChange;
import org.xydra.store.impl.gae.changes.GaeChangesServiceImpl1;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;


/**
 * Computes *Snapshots ( {@link SimpleField}, {@link SimpleObject},
 * {@link SimpleModel}) from a given {@link XChangeLog}.
 * 
 * @author dscharrer
 * 
 */
@SuppressWarnings("deprecation")
public class GaeSnapshotService1 {
	
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotService1.class);
	private final GaeChangesServiceImpl1 changesService;
	private final XAddress modelAddress;
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	private static final String PROPERTY_SNAPSHOT = "snapshot";
	private static final String PROPERTY_REVISION = "revision";
	
	/** a revision-specific key string */
	private final String snapshotRevKey;
	
	private static final long SNAPSHOT_PERSISTENCE_THRESHOLD = 10;
	
	/**
	 * @param changesService The change log to load snapshots from.
	 */
	public GaeSnapshotService1(GaeChangesServiceImpl1 changesService) {
		this.modelAddress = changesService.getModelAddress();
		this.changesService = changesService;
		this.snapshotRevKey = this.modelAddress + "-snapshot-Rev";
	}
	
	/**
	 * A holder for an model snapshot
	 */
	private static class CachedModel implements Serializable {
		private static final long serialVersionUID = -6271321788554091709L;
		long revision = -1;
		XRevWritableModel modelState = null; // can be null
	}
	
	private CachedModel localVmCache = null;
	private Key snapshotKey;
	
	/**
	 * @param revisionNumber which is the minimum revision number that the
	 *            returned snapshot will have, i.e. the caller might get an even
	 *            more recent snapshot
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog}
	 */
	synchronized public XWritableModel getSnapshot(long revisionNumber) {
		log.debug("Get snapshot " + this.modelAddress + " " + revisionNumber);
		/* if localVmCache has requested version or newer, use it */
		if(this.localVmCache != null && this.localVmCache.revision >= revisionNumber) {
			log.debug("re-using locamVmCache with revNr " + this.localVmCache.revision + " for "
			        + revisionNumber);
			return XCopyUtils.createSnapshot(this.localVmCache.modelState);
		}
		
		// IMPROVE if the local VM cache is only behind a few revisions, it
		// might be faster to update it than to load a snapshot from the
		// memcache
		
		/* try to get the (one and only cached) snapshot from memcache */
		final String cachname = this.modelAddress + "-snapshot";
		CachedModel entry = (CachedModel)XydraRuntime.getMemcache().get(cachname);
		if(entry == null) {
			/*
			 * if memcache has no snapshot, use localVmCache and ignore wrong
			 * revNr
			 */
			entry = this.localVmCache;
			if(entry == null) {
				/* if nothing else works: load snapshot */
				entry = loadSnapshot();
			}
		}
		assert entry != null;
		
		/* update entry with new events from change log */
		updateCachedModel(entry, revisionNumber);
		/* cache it locally */
		this.localVmCache = entry;
		/* update latest snapshot in memcache for other threads/instances */
		XydraRuntime.getMemcache().put(cachname, entry);
		/* store revision-specific snapshots in datastore and memcache */
		saveSnapshot(entry);
		
		/*
		 * cache result is directly returned without copy, because cache results
		 * are always de-deserialized from byte[] arrays anyways
		 */
		if(AboutAppEngine.onAppEngine()) {
			return entry.modelState;
		} else
			return XCopyUtils.createSnapshot(entry.modelState);
		
	}
	
	/** lazy init */
	private synchronized Key getSnapshotKey() {
		if(this.snapshotKey == null) {
			this.snapshotKey = KeyFactory.createKey(KIND_SNAPSHOT, this.modelAddress.toURI());
		}
		return this.snapshotKey;
	}
	
	private CachedModel loadSnapshot() {
		
		CachedModel entry = new CachedModel();
		
		Entity snapshotEntity = GaeUtils.getEntity_MemcacheFirst_DatastoreFinal(getSnapshotKey());
		if(snapshotEntity == null) {
			return entry;
		}
		
		long rev = (Long)snapshotEntity.getProperty(PROPERTY_REVISION);
		
		Text snapshotStr = (Text)snapshotEntity.getProperty(PROPERTY_SNAPSHOT);
		
		XRevWritableModel snapshot = null;
		if(snapshotStr != null) {
			XydraElement snapshotXml = new XmlParser().parse(snapshotStr.getValue());
			snapshot = SerializedModel.toModelState(snapshotXml, this.modelAddress);
		}
		
		entry.revision = rev;
		entry.modelState = snapshot;
		
		XydraRuntime.getMemcache().put(this.snapshotRevKey, rev);
		
		return entry;
	}
	
	/**
	 * Save revision-specific snapshot as XML in data store (every k writes) and
	 * put also in memcache
	 * 
	 * @param entry to be stored
	 */
	private void saveSnapshot(CachedModel entry) {
		
		Long savedRevLong = (Long)XydraRuntime.getMemcache().get(this.snapshotRevKey);
		long savedRev = (savedRevLong == null ? -1L : savedRevLong);
		
		if(entry.revision <= savedRev || entry.revision - savedRev < SNAPSHOT_PERSISTENCE_THRESHOLD) {
			// Don't persist the snapshot.
			return;
		}
		
		Entity snapshotEntity = new Entity(getSnapshotKey());
		snapshotEntity.setUnindexedProperty(PROPERTY_REVISION, entry.revision);
		
		if(entry.modelState != null) {
			XydraOut out = new XmlOut();
			SerializedModel.serialize(entry.modelState, out);
			snapshotEntity.setUnindexedProperty(PROPERTY_SNAPSHOT, new Text(out.getData()));
		}
		
		XydraRuntime.getMemcache().put(this.snapshotRevKey, entry.revision);
		
	}
	
	/**
	 * Update snapshots in caches with new events from change log
	 * 
	 * @param entry to be updated
	 * @param curRev current revision
	 */
	private void updateCachedModel(CachedModel entry, long curRev) {
		if(entry.revision == curRev) {
			return;
		}
		log.debug("udating cached model " + this.modelAddress + " from lastRev=" + entry.revision
		        + " to curRev=" + curRev);
		
		// Fetch one event from the back of the change log.
		List<AsyncChange> batch = new ArrayList<AsyncChange>(1);
		batch.add(this.changesService.getChangeAt(curRev));
		
		int pos = 0;
		
		// Check if we can skip any events.
		// Looks for the last XRepositoryEvent after entry.revision
		List<XEvent> events = new ArrayList<XEvent>();
		for(long i = curRev; i > entry.revision; i--) {
			
			XEvent event = batch.get(pos).get().getEvent();
			if(event == null) {
				// Nothing changed at this revision, ignore.
			} else {
				
				// Check if this is the first event we need.
				if(event instanceof XRepositoryEvent) {
					entry.modelState = null;
					if(event.getChangeType() == ChangeType.REMOVE) {
						assert i > 0;
						assert i == curRev;
						entry.revision = curRev;
						log.debug("-> removed, rev=" + entry.revision);
						return;
					} else {
						entry.revision = i - 1;
						events.add(0, event);
						log.debug("-> reset, rev=" + entry.revision);
						break;
					}
					
				} else if(event instanceof XTransactionEvent) {
					XTransactionEvent trans = (XTransactionEvent)event;
					assert trans.size() > 1;
					if(trans.getEvent(0) instanceof XRepositoryEvent) {
						assert trans.getEvent(0).getChangeType() == ChangeType.ADD;
						assert !(trans.getEvent(trans.size() - 1) instanceof XRepositoryEvent);
						entry.modelState = null;
						entry.revision = i - 1;
						events.add(0, event);
						log.debug("-> reset, rev=" + entry.revision);
						break;
					} else if(trans.getEvent(trans.size() - 1) instanceof XRepositoryEvent) {
						assert trans.getEvent(trans.size() - 1).getChangeType() == ChangeType.REMOVE;
						assert i > 0;
						assert i == curRev;
						entry.modelState = null;
						entry.revision = curRev;
						log.debug("-> removed, rev=" + entry.revision);
						return;
					}
				}
				events.add(0, event);
				
			}
			
			// Asynchronously fetch new change entities.
			if(i - batch.size() > entry.revision) {
				batch.set(pos, this.changesService.getChangeAt(i - batch.size()));
			}
			pos++;
			if(pos == batch.size()) {
				if(i - batch.size() - 1 > entry.revision) {
					batch.add(this.changesService.getChangeAt(i - batch.size() - 1));
				}
				pos = 0;
			}
			
		}
		
		log.debug("-> " + events.size() + " events, rev=" + entry.revision);
		
		// Apply all changes.
		for(XEvent event : events) {
			
			if(event instanceof XTransactionEvent) {
				XTransactionEvent trans = (XTransactionEvent)event;
				for(XAtomicEvent ae : trans) {
					if(ae.isImplied()) {
						continue;
					}
					entry.modelState = applyEvent(entry.modelState, ae);
				}
			} else {
				assert event instanceof XAtomicEvent;
				XRevWritableModel entryModelState = entry.modelState;
				entry.modelState = applyEvent(entryModelState, (XAtomicEvent)event);
			}
			
			entry.revision = event.getRevisionNumber();
		}
		
		log.debug("-> updated to new rev=" + entry.revision);
		assert entry.modelState == null || entry.modelState.getRevisionNumber() == entry.revision;
		
		log.debug("udated cached model " + this.modelAddress + " to newRev=" + entry.revision);
	}
	
	/**
	 * Apply the changes described by an {@link XAtomicEvent} to an
	 * {@link XReadableModel}.
	 * 
	 * It is the responsibility of the caller to ensure that the provided model
	 * is at a state where the given event applies.
	 * 
	 * Note: maybe this should be moved to core?
	 * 
	 * @param model The model to change. This can be null. If not null, it will
	 *            be modified.
	 * @param event The event to apply.
	 * @return the changed model or null if the model was removed.
	 */
	static XRevWritableModel applyEvent(XRevWritableModel model, XAtomicEvent event) {
		log.debug("--> applying " + event);
		long rev = event.getRevisionNumber();
		
		// repository events
		if(event instanceof XRepositoryEvent) {
			if(event.getChangeType() == ChangeType.ADD) {
				assert model == null;
				return new SimpleModel(event.getChangedEntity(), rev);
			} else {
				assert event.getChangeType() == ChangeType.REMOVE;
				assert model != null;
				return null;
			}
		}
		
		// model, object and field events
		if(model == null) {
			throw new IllegalArgumentException("Model may not be null for event " + event);
		}
		model.setRevisionNumber(rev);
		
		if(event instanceof XModelEvent) {
			XModelEvent me = (XModelEvent)event;
			if(me.getChangeType() == ChangeType.ADD) {
				assert !model.hasObject(me.getObjectId());
				SimpleObject object = new SimpleObject(me.getChangedEntity(), rev);
				model.addObject(object);
			} else {
				assert me.getChangeType() == ChangeType.REMOVE;
				assert model.hasObject(me.getObjectId());
				model.removeObject(me.getObjectId());
			}
			return model;
		}
		
		// object and field events
		XRevWritableObject object = model.getObject(event.getTarget().getObject());
		assert object != null;
		object.setRevisionNumber(rev);
		
		if(event instanceof XObjectEvent) {
			XObjectEvent oe = (XObjectEvent)event;
			if(oe.getChangeType() == ChangeType.ADD) {
				assert !object.hasField(oe.getFieldId());
				SimpleField field = new SimpleField(oe.getChangedEntity(), rev);
				object.addField(field);
			} else {
				assert oe.getChangeType() == ChangeType.REMOVE;
				assert object.hasField(oe.getFieldId());
				object.removeField(oe.getFieldId());
			}
			return model;
		}
		
		// field events
		XRevWritableField field = object.getField(event.getTarget().getField());
		assert field != null;
		field.setRevisionNumber(rev);
		
		assert event instanceof XFieldEvent;
		XFieldEvent fe = (XFieldEvent)event;
		
		if(fe.getChangeType() == ChangeType.ADD) {
			assert field.isEmpty();
			field.setValue(fe.getNewValue());
		} else if(fe.getChangeType() == ChangeType.CHANGE) {
			assert !field.isEmpty();
			field.setValue(fe.getNewValue());
		} else {
			assert fe.getChangeType() == ChangeType.REMOVE;
			assert !field.isEmpty();
			field.setValue(null);
		}
		
		return model;
	}
	
}

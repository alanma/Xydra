package org.xydra.store.impl.gae.snapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import org.xydra.core.model.XChangeLog;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.MiniParserXml;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.changes.AsyncChange;
import org.xydra.store.impl.gae.changes.GaeChangesService;

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
public class GaeSnapshotService {
	
	private static final Logger log = LoggerFactory.getLogger(GaeSnapshotService.class);
	private final GaeChangesService changes;
	
	private static final String KIND_SNAPSHOT = "XSNAPSHOT";
	private static final String PROPERTY_SNAPSHOT = "snapshot";
	private static final String PROPERTY_REVISION = "revision";
	
	private static final long SNAPSHOT_PERSISTENCE_THRESHOLD = 10;
	
	/**
	 * @param changesService The change log to load snapshots from.
	 */
	public GaeSnapshotService(GaeChangesService changesService) {
		this.changes = changesService;
	}
	
	private static class CachedModel implements Serializable {
		
		private static final long serialVersionUID = -6271321788554091709L;
		long revision = -1;
		XRevWritableModel modelState = null; // can be null
		
	}
	
	/**
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog}
	 */
	synchronized public XWritableModel getSnapshot() {
		
		final String cachname = this.changes.getModelAddress() + "-snapshot";
		
		CachedModel entry = (CachedModel)XydraRuntime.getMemcache().get(cachname);
		
		if(entry == null) {
			entry = loadSnapshot();
		}
		
		// TODO less locking
		synchronized(entry) {
			updateCachedModel(entry);
			
			XydraRuntime.getMemcache().put(cachname, entry);
			
			saveSnapshot(entry);
			
			/*
			 * cache result is directly returned without copy, because cache
			 * results are always de-deserialized from byte[] arrays anyways
			 */
			return entry.modelState;
		}
		
	}
	
	private Key getSnapshotKey() {
		return KeyFactory.createKey(KIND_SNAPSHOT, this.changes.getModelAddress().toURI());
	}
	
	private String getSnapshotRevKey() {
		return this.changes.getModelAddress() + "-snapshot-Rev";
	}
	
	private CachedModel loadSnapshot() {
		
		CachedModel entry = new CachedModel();
		
		Entity snapshotEntity = GaeUtils.getEntity(getSnapshotKey());
		if(snapshotEntity == null) {
			return entry;
		}
		
		long rev = (Long)snapshotEntity.getProperty(PROPERTY_REVISION);
		
		Text snapshotStr = (Text)snapshotEntity.getProperty(PROPERTY_SNAPSHOT);
		
		XRevWritableModel snapshot = null;
		if(snapshotStr != null) {
			MiniElement snapshotXml = new MiniParserXml().parse(snapshotStr.getValue());
			snapshot = SerializedModel.toModelState(snapshotXml, this.changes.getModelAddress());
		}
		
		entry.revision = rev;
		entry.modelState = snapshot;
		
		XydraRuntime.getMemcache().put(getSnapshotRevKey(), rev);
		
		return entry;
	}
	
	private void saveSnapshot(CachedModel entry) {
		
		Long savedRevLong = (Long)XydraRuntime.getMemcache().get(getSnapshotRevKey());
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
		
		XydraRuntime.getMemcache().put(getSnapshotRevKey(), entry.revision);
		
	}
	
	private void updateCachedModel(CachedModel entry) {
		
		log.debug("udating cached model " + this.changes.getModelAddress() + " from rev="
		        + entry.revision);
		
		long curRev = this.changes.getCurrentRevisionNumber();
		if(curRev == entry.revision) {
			log.debug("-> nothing to update");
			return;
		}
		
		// Fetch one event from the back of the change log.
		List<AsyncChange> batch = new ArrayList<AsyncChange>(1);
		batch.add(this.changes.getChangeAt(curRev));
		
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
				batch.set(pos, this.changes.getChangeAt(i - batch.size()));
			}
			pos++;
			if(pos == batch.size()) {
				if(i - batch.size() - 1 > entry.revision) {
					batch.add(this.changes.getChangeAt(i - batch.size() - 1));
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
		
	}
	
	/**
	 * Apply the changes described by an {@link XAtomicEvent} to an
	 * {@link XReadableModel}.
	 * 
	 * It is the responsibility of the caller to ensure that the provided model
	 * is at a state where the given event applies.
	 * 
	 * TODO maybe this should be moved to core?
	 * 
	 * @param model The model to change. This can be null. If not null, it will
	 *            be modified.
	 * @param event The event to apply.
	 * @return the changed model or null if the model was removed.
	 */
	private XRevWritableModel applyEvent(XRevWritableModel model, XAtomicEvent event) {
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

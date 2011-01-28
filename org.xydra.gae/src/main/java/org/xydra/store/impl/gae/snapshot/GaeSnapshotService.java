package org.xydra.store.impl.gae.snapshot;

import java.util.Iterator;
import java.util.Map;

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
import org.xydra.store.XydraRuntime;


/**
 * Computes *Snapshots ( {@link SimpleField}, {@link SimpleObject},
 * {@link SimpleModel}) from a given {@link XChangeLog}.
 * 
 * @author dscharrer
 * 
 */
public class GaeSnapshotService {
	
	private final XChangeLog log;
	
	/**
	 * @param changeLog The change log to load snapshots from.
	 */
	public GaeSnapshotService(XChangeLog changeLog) {
		this.log = changeLog;
	}
	
	private static class CachedModel {
		
		long revision = -1;
		XRevWritableModel modelState = null; // can be null
		
	}
	
	/**
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog}
	 */
	public XWritableModel getSnapshot() {
		
		Map<Object,Object> cache = XydraRuntime.getMemcache();
		
		CachedModel entry = null;
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			entry = (CachedModel)cache.get(this.log.getBaseAddress());
			if(entry == null) {
				entry = new CachedModel();
				cache.put(this.log.getBaseAddress(), entry);
			}
		}
		
		// TODO less locking
		synchronized(entry) {
			updateCachedModel(entry);
			
			// TODO save snapshot to datastore from time to time
			
			return XCopyUtils.createSnapshot(entry.modelState);
		}
		
	}
	
	private void updateCachedModel(CachedModel entry) {
		
		Iterator<XEvent> events = this.log.getEventsSince(entry.revision + 1);
		
		while(events.hasNext()) {
			XEvent event = events.next();
			
			if(event instanceof XTransactionEvent) {
				XTransactionEvent trans = (XTransactionEvent)event;
				for(int i = 0; i < trans.size(); i++) {
					entry.modelState = applyEvent(entry.modelState, trans.getEvent(i));
				}
			} else {
				assert event instanceof XAtomicEvent;
				entry.modelState = applyEvent(entry.modelState, (XAtomicEvent)event);
			}
			
			entry.revision = event.getRevisionNumber();
		}
		
	}
	
	private XRevWritableModel applyEvent(XRevWritableModel model, XAtomicEvent event) {
		
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
		assert model != null;
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

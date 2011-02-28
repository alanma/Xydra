package org.xydra.store.impl.gae.snapshot;

import java.util.ArrayList;
import java.util.List;
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
import org.xydra.store.impl.gae.changes.GaeChangesService;


/**
 * Computes *Snapshots ( {@link SimpleField}, {@link SimpleObject},
 * {@link SimpleModel}) from a given {@link XChangeLog}.
 * 
 * @author dscharrer
 * 
 */
public class GaeSnapshotService {
	
	private final GaeChangesService log;
	
	/**
	 * @param changeLog The change log to load snapshots from.
	 */
	public GaeSnapshotService(GaeChangesService changeLog) {
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
		
		String cachname = this.log.getBaseAddress() + "-snapshot";
		
		CachedModel entry = null;
		synchronized(cache) {
			// TODO how is cache access supposed to be synchronized?
			entry = (CachedModel)cache.get(cachname);
			if(entry == null) {
				entry = new CachedModel();
			}
		}
		
		// TODO less locking
		synchronized(entry) {
			updateCachedModel(entry);
			
			cache.put(cachname, entry);
			
			// TODO save snapshot to datastore from time to time
			
			// TODO is this even needed? Does the cache return
			return XCopyUtils.createSnapshot(entry.modelState);
		}
		
	}
	
	private void updateCachedModel(CachedModel entry) {
		
		long curRev = this.log.getCurrentRevisionNumber();
		if(curRev == entry.revision) {
			return;
		}
		
		// Check if we can skip any events.
		List<XEvent> rawEvents = new ArrayList<XEvent>();
		for(long i = curRev; i > entry.revision; i--) {
			// IMPROVE make async
			XEvent event = this.log.getEventAt(i).get();
			if(event == null) {
				continue;
			}
			if(event instanceof XRepositoryEvent) {
				entry.modelState = null;
				if(event.getChangeType() == ChangeType.REMOVE) {
					assert i > 0;
					assert i == curRev;
					entry.revision = curRev;
					return;
				} else {
					entry.revision = i - 1;
					rawEvents.add(0, event);
					break;
				}
				
			} else if(event instanceof XTransactionEvent) {
				XTransactionEvent trans = (XTransactionEvent)event;
				assert trans.size() > 1;
				if(trans.getEvent(0) instanceof XRepositoryEvent) {
					assert trans.getEvent(0).getChangeType() == ChangeType.ADD;
					entry.modelState = null;
					entry.revision = i - 1;
					rawEvents.add(0, event);
					break;
				} else if(trans.getEvent(trans.size() - 1) instanceof XRepositoryEvent) {
					assert trans.getEvent(trans.size() - 1).getChangeType() == ChangeType.REMOVE;
					assert i > 0;
					assert i == curRev;
					entry.modelState = null;
					entry.revision = curRev;
					return;
				}
			}
			rawEvents.add(0, event);
		}
		
		for(XEvent event : rawEvents) {
			
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

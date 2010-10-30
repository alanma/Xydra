package org.xydra.server.impl.newgae;

import java.util.Iterator;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XBaseModel;


public class GaeSnapshotService {
	
	private final GaeChangesService changesService;
	
	public GaeSnapshotService(GaeChangesService changesService) {
		this.changesService = changesService;
	}
	
	public XBaseModel getSnapshot() {
		
		// IMPROVE save & cache snapshots
		
		ModelSnapshot model = null;
		
		Iterator<XEvent> events = this.changesService.getEventsSince(0);
		
		while(events.hasNext()) {
			XEvent event = events.next();
			
			if(event instanceof XTransactionEvent) {
				XTransactionEvent trans = (XTransactionEvent)event;
				for(int i = 0; i < trans.size(); i++) {
					model = applyEvent(model, trans.getEvent(i));
				}
			} else {
				assert event instanceof XAtomicEvent;
				model = applyEvent(model, (XAtomicEvent)event);
			}
			
		}
		
		return model;
		
	}
	
	private ModelSnapshot applyEvent(ModelSnapshot model, XAtomicEvent event) {
		
		if(event instanceof XRepositoryEvent) {
			if(event.getChangeType() == ChangeType.ADD) {
				assert model == null;
				return new ModelSnapshot(event.getChangedEntity(), event.getRevisionNumber());
			} else {
				assert event.getChangeType() == ChangeType.REMOVE;
				assert model != null;
				return null;
			}
		}
		
		assert model != null;
		
		if(event instanceof XModelEvent) {
			XModelEvent me = (XModelEvent)event;
			if(me.getChangeType() == ChangeType.ADD) {
				assert !model.hasObject(me.getObjectID());
				ObjectSnapshot object = new ObjectSnapshot(me.getChangedEntity(), me
				        .getRevisionNumber());
				model.objects.put(me.getObjectID(), object);
			} else {
				assert me.getChangeType() == ChangeType.REMOVE;
				assert model.hasObject(me.getObjectID());
				model.objects.remove(me.getObjectID());
			}
			return model;
		}
		
		ObjectSnapshot object = model.getObject(event.getTarget().getObject());
		assert object != null;
		
		if(event instanceof XObjectEvent) {
			XObjectEvent oe = (XObjectEvent)event;
			if(oe.getChangeType() == ChangeType.ADD) {
				assert !object.hasField(oe.getFieldID());
				FieldSnapshot field = new FieldSnapshot(oe.getChangedEntity(), oe
				        .getRevisionNumber());
				object.fields.put(oe.getFieldID(), field);
			} else {
				assert oe.getChangeType() == ChangeType.REMOVE;
				assert object.hasField(oe.getFieldID());
				object.fields.remove(oe.getFieldID());
			}
			return model;
		}
		
		FieldSnapshot field = object.getField(event.getTarget().getField());
		assert field != null;
		
		assert event instanceof XFieldEvent;
		XFieldEvent fe = (XFieldEvent)event;
		
		if(fe.getChangeType() == ChangeType.ADD) {
			assert field.isEmpty();
			field.value = fe.getNewValue();
		} else if(fe.getChangeType() == ChangeType.CHANGE) {
			assert !field.isEmpty();
			field.value = fe.getNewValue();
		} else {
			assert fe.getChangeType() == ChangeType.REMOVE;
			assert !field.isEmpty();
			field.value = null;
		}
		
		return model;
	}
}

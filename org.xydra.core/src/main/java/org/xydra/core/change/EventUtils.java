package org.xydra.core.change;

import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XID;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;


public class EventUtils {
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	
	/**
	 * Applies an event to a model. Event must be addresses to the model.
	 * 
	 * It is the responsibility of the caller to ensure that the provided model
	 * is at a state where the given event applies.
	 * 
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	public static void applyEvent(XRevWritableModel model, XEvent event) {
		if(event instanceof XTransactionEvent) {
			for(XEvent txnEvent : ((XTransactionEvent)event)) {
				if(txnEvent.isImplied()) {
					continue;
				}
				applyEvent(model, txnEvent);
			}
		} else {
			assert event instanceof XAtomicEvent;
			XAtomicEvent atomicEvent = (XAtomicEvent)event;
			
			if(atomicEvent instanceof XRepositoryEvent) {
				assert event.getChangedEntity().equals(model.getAddress());
				applyRepositoryEvent(model, (XRepositoryEvent)event);
				return;
			}
			assert model.getRevisionNumber() >= 0;
			model.setRevisionNumber(event.getRevisionNumber());
			if(atomicEvent instanceof XModelEvent) {
				assert event.getTarget().equals(model.getAddress());
				applyModelEvent(model, (XModelEvent)event);
			} else {
				// object & field events
				XRevWritableObject object = model.getObject(event.getTarget().getObject());
				assert object != null : "object null for event " + event;
				object.setRevisionNumber(event.getRevisionNumber());
				if(atomicEvent instanceof XObjectEvent) {
					assert event.getTarget().getParent().equals(model.getAddress());
					applyObjectEvent(object, (XObjectEvent)event);
				} else {
					assert atomicEvent instanceof XFieldEvent;
					assert event.getTarget().getParent().getParent().equals(model.getAddress());
					XFieldEvent fieldEvent = (XFieldEvent)event;
					XRevWritableField field = object.getField(fieldEvent.getFieldId());
					field.setRevisionNumber(event.getRevisionNumber());
					applyFieldEvent(field, fieldEvent);
				}
			}
		}
	}
	
	/**
	 * @param field to which the event is applied
	 * @param event to be applied
	 */
	private static void applyFieldEvent(XRevWritableField field, XFieldEvent event) {
		assert field != null;
		switch(event.getChangeType()) {
		case ADD:
			assert field.isEmpty() : field.getValue();
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			assert !field.isEmpty();
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			assert !field.isEmpty();
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
	}
	
	/**
	 * @param object to which the event is applied
	 * @param event to be applied
	 */
	private static void applyObjectEvent(XRevWritableObject object, XObjectEvent event) {
		assert object != null;
		switch(event.getChangeType()) {
		case ADD: {
			assert !object.hasField(event.getFieldId());
			XRevWritableField field = object.createField(event.getFieldId());
			field.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert object.hasField(event.getFieldId());
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
	}
	
	/**
	 * @param model to which the event is applied
	 * @param event to be applied
	 */
	private static void applyModelEvent(XRevWritableModel model, XModelEvent event) {
		assert event.getTarget().equals(model.getAddress());
		switch(event.getChangeType()) {
		case ADD: {
			assert !model.hasObject(event.getObjectId());
			XRevWritableObject object = model.createObject(event.getObjectId());
			object.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert model.hasObject(event.getObjectId());
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
	}
	
	/**
	 * Apply the {@link XRepositoryEvent} to the given model.
	 * 
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	private static void applyRepositoryEvent(XRevWritableModel model, XRepositoryEvent event) {
		assert event.getChangedEntity().equals(model.getAddress());
		switch(event.getChangeType()) {
		case ADD: {
			assert model.isEmpty() : " event " + event;
			model.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert model.getRevisionNumber() >= 0;
			model.setRevisionNumber(MODEL_DOES_NOT_EXIST);
			// clear model
			List<XID> ids = new LinkedList<XID>();
			for(XID id : model) {
				ids.add(id);
			}
			for(XID id : ids) {
				model.removeObject(id);
			}
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XRepositoryEvent cannot be this " + event);
		}
	}
	
}

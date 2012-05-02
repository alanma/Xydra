package org.xydra.core.change;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.sharedutils.XyAssert;


/**
 * Note: This is similar to {@link DeltaUtils}.
 * 
 * FIXME heavily document this
 * 
 * @author xamde
 */
public class EventUtils {
	
	private static final long MODEL_DOES_NOT_EXIST = -1;
	
	/**
	 * @param model
	 * @param atomicEvent
	 * @param inTxn
	 */
	private static void applyAtomicEventIgnoreRev(@NeverNull XModel model,
	        @NeverNull XAtomicEvent atomicEvent, boolean inTxn) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(atomicEvent.getChangedEntity() != null);
		assert atomicEvent.getChangedEntity() != null;
		
		if(atomicEvent instanceof XRepositoryEvent) {
			XyAssert.xyAssert(atomicEvent.getChangedEntity().equals(model.getAddress()));
			applyRepositoryEventIgnoreRev(model, (XRepositoryEvent)atomicEvent);
			return;
		}
		assert model.getRevisionNumber() >= 0 : model.getRevisionNumber();
		if(atomicEvent instanceof XModelEvent) {
			XyAssert.xyAssert(atomicEvent.getTarget().equals(model.getAddress()));
			applyModelEventIgnoreRev(model, (XModelEvent)atomicEvent);
		} else {
			// object & field events
			XWritableObject object = model.getObject(atomicEvent.getTarget().getObject());
			assert object != null : "object null for event " + atomicEvent;
			if(atomicEvent instanceof XObjectEvent) {
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().equals(model.getAddress()));
				applyObjectEventIgnoreRev(object, (XObjectEvent)atomicEvent);
			} else {
				XyAssert.xyAssert(atomicEvent instanceof XFieldEvent);
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().getParent()
				        .equals(model.getAddress()));
				XFieldEvent fieldEvent = (XFieldEvent)atomicEvent;
				XWritableField field = object.getField(fieldEvent.getFieldId());
				assert field != null : "field is null";
				applyFieldEventIgnoreRev(field, fieldEvent);
			}
		}
	}
	
	private static void applyAtomicEvent(XRevWritableModel model, XAtomicEvent atomicEvent,
	        boolean inTxn) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(atomicEvent.getChangedEntity() != null);
		assert atomicEvent.getChangedEntity() != null;
		
		if(atomicEvent instanceof XRepositoryEvent) {
			XyAssert.xyAssert(atomicEvent.getChangedEntity().equals(model.getAddress()));
			applyRepositoryEvent(model, (XRepositoryEvent)atomicEvent);
			return;
		}
		assert model.getRevisionNumber() >= 0 : model.getRevisionNumber();
		if(atomicEvent instanceof XModelEvent) {
			XyAssert.xyAssert(atomicEvent.getTarget().equals(model.getAddress()));
			applyModelEvent(model, (XModelEvent)atomicEvent);
		} else {
			// object & field events
			XRevWritableObject object = model.getObject(atomicEvent.getTarget().getObject());
			assert object != null : "object null for event " + atomicEvent;
			object.setRevisionNumber(atomicEvent.getRevisionNumber());
			if(atomicEvent instanceof XObjectEvent) {
				assert atomicEvent.getTarget().getParent().equals(model.getAddress()) : "targetParent="
				        + atomicEvent.getTarget().getParent() + " vs. model=" + model.getAddress();
				applyObjectEvent(object, (XObjectEvent)atomicEvent);
			} else {
				XyAssert.xyAssert(atomicEvent instanceof XFieldEvent);
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().getParent()
				        .equals(model.getAddress()));
				XFieldEvent fieldEvent = (XFieldEvent)atomicEvent;
				XRevWritableField field = object.getField(fieldEvent.getFieldId());
				assert field != null : "field is null";
				field.setRevisionNumber(atomicEvent.getRevisionNumber());
				applyFieldEvent(field, fieldEvent);
			}
		}
	}
	
	private static void applyAtomicEventNonDestructive(XReadableModel reference,
	        XRevWritableModel model, XAtomicEvent atomicEvent, boolean inTxn) {
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(model != null);
		assert model != null;
		
		XID objectId = atomicEvent.getTarget().getObject();
		if(objectId != null) {
			XyAssert.xyAssert(model != null);
			assert model != null;
			XRevWritableObject object = model.getObject(objectId);
			XyAssert.xyAssert(object != null, "%s: applying event %s inTxn?%s", model.getAddress(),
			        atomicEvent, inTxn);
			assert object != null;
			XReadableObject srcObject = (reference == null) ? null : reference.getObject(objectId);
			if(object == srcObject) {
				object = SimpleObject.shallowCopy(object);
				model.addObject(object);
			}
			XID fieldId = atomicEvent.getTarget().getField();
			if(fieldId != null) {
				XRevWritableField field = object.getField(fieldId);
				XReadableField srcField = (srcObject == null) ? null : srcObject.getField(fieldId);
				XyAssert.xyAssert(field != null);
				assert field != null;
				if(field == srcField) {
					object.addField(new SimpleField(srcField.getAddress(), srcField
					        .getRevisionNumber(), srcField.getValue()));
				}
			}
		}
		
		applyAtomicEvent(model, atomicEvent, inTxn);
		assert reference != model : "just applied event " + atomicEvent + " inTxn?" + inTxn;
	}
	
	/**
	 * @param model
	 * @param event
	 */
	public static void applyEventIgnoreRev(XModel model, @NeverNull XEvent event) {
		XyAssert.xyAssert(event != null);
		assert event != null;
		if(event instanceof XTransactionEvent) {
			for(XEvent txnEvent : ((XTransactionEvent)event)) {
				if(txnEvent.isImplied()) {
					continue;
				}
				applyAtomicEventIgnoreRev(model, (XAtomicEvent)txnEvent, true);
			}
		} else {
			assert event instanceof XAtomicEvent : event.getClass().getName();
			applyAtomicEventIgnoreRev(model, (XAtomicEvent)event, false);
		}
	}
	
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
		XyAssert.xyAssert(event != null);
		assert event != null;
		if(event instanceof XTransactionEvent) {
			for(XEvent txnEvent : ((XTransactionEvent)event)) {
				if(txnEvent.isImplied()) {
					continue;
				}
				applyAtomicEvent(model, (XAtomicEvent)txnEvent, true);
			}
		} else {
			assert event instanceof XAtomicEvent : event.getClass().getName();
			applyAtomicEvent(model, (XAtomicEvent)event, false);
		}
		/*
		 * As Model.ADD and REMOVE events are technically only managed within
		 * the change log of the model itself, we must even then record the
		 * changed revision number
		 */
		model.setRevisionNumber(event.getRevisionNumber());
	}
	
	/**
	 * Calculate the result of applying events to a model without changing the
	 * original model but copying as little as possible.
	 * 
	 * @param reference The original model. Nothing contained in this model will
	 *            be changed, even if those parts are also contained in model.
	 * @param model The model to be modified. Any parts that need to be modified
	 *            but are also in the reference model are copied first. Never
	 *            null.
	 * @param event The events to apply.
	 * 
	 * @return The result after applying the events. This model may share object
	 *         and/or fields with the original model, in which case modifying
	 *         them will change both models.
	 */
	public static XRevWritableModel applyEventNonDestructive(XReadableModel reference,
	        @NeverNull XRevWritableModel model, @NeverNull XEvent event) {
		XyAssert.xyAssert(event != null);
		assert event != null;
		XyAssert.xyAssert(model != null);
		assert model != null;
		
		XRevWritableModel result = model;
		
		if(event instanceof XTransactionEvent) {
			XTransactionEvent trans = (XTransactionEvent)event;
			
			// If the whole model was removed, there is nothing else to process.
			XAtomicEvent last = trans.getEvent(trans.size() - 1);
			if(last instanceof XRepositoryEvent && last.getChangeType() == ChangeType.REMOVE) {
				result = new SimpleModel(model.getAddress(), MODEL_DOES_NOT_EXIST);
			} else {
				
				if(result == reference) {
					result = SimpleModel.shallowCopy(result);
					assert result != model : "copied";
				}
				
				for(XAtomicEvent ae : ((XTransactionEvent)event)) {
					if(ae.isImplied()) {
						continue;
					}
					applyAtomicEventNonDestructive(reference, result, ae, true);
				}
			}
		} else {
			assert event instanceof XAtomicEvent : event.getClass().getName();
			
			if(result == reference) {
				result = SimpleModel.shallowCopy(result);
			}
			
			applyAtomicEventNonDestructive(reference, result, (XAtomicEvent)event, false);
		}
		
		result.setRevisionNumber(event.getRevisionNumber());
		return result;
	}
	
	/**
	 * Calculate the result of applying events to a model without changing the
	 * original model but copying as little as possible.
	 * 
	 * @param model The original model.
	 * @param event The events to apply.
	 * 
	 * @return The result after applying the events. This model may share object
	 *         and/or fields with the original model, in which case modifying
	 *         them will change both models.
	 */
	public static XRevWritableModel applyEventNonDestructive(@NeverNull XRevWritableModel model,
	        XEvent event) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		return applyEventNonDestructive(model, model, event);
	}
	
	/**
	 * @param field to which the event is applied
	 * @param event to be applied
	 */
	private static void applyFieldEvent(XRevWritableField field, XFieldEvent event) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		switch(event.getChangeType()) {
		case ADD:
			assert field.isEmpty() : field.getValue();
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			XyAssert.xyAssert(!field.isEmpty());
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			assert !field.isEmpty() : "cannot remove value from empty field";
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
	}
	
	/**
	 * @param field to which the event is applied
	 * @param event to be applied
	 */
	private static void applyFieldEventIgnoreRev(XWritableField field, XFieldEvent event) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		switch(event.getChangeType()) {
		case ADD:
			assert field.isEmpty() : field.getValue();
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			XyAssert.xyAssert(!field.isEmpty());
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			assert !field.isEmpty() : "cannot remove value from empty field";
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
	}
	
	/**
	 * @param model to which the event is applied
	 * @param event to be applied
	 */
	private static void applyModelEventIgnoreRev(XModel model, XModelEvent event) {
		XyAssert.xyAssert(event.getTarget().equals(model.getAddress()));
		switch(event.getChangeType()) {
		case ADD: {
			XyAssert.xyAssert(!model.hasObject(event.getObjectId()));
			model.createObject(event.getObjectId());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.hasObject(event.getObjectId()));
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
	}
	
	/**
	 * @param model to which the event is applied
	 * @param event to be applied
	 */
	private static void applyModelEvent(XRevWritableModel model, XModelEvent event) {
		XyAssert.xyAssert(event.getTarget().equals(model.getAddress()));
		switch(event.getChangeType()) {
		case ADD: {
			assert !model.hasObject(event.getObjectId()) : "Trying to add object '"
			        + event.getObjectId() + "' bust is already present in model "
			        + model.getAddress() + " rev:" + model.getRevisionNumber();
			XRevWritableObject object = model.createObject(event.getObjectId());
			object.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.hasObject(event.getObjectId()));
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
	}
	
	/**
	 * @param object to which the event is applied
	 * @param event to be applied
	 */
	private static void applyObjectEvent(XRevWritableObject object, XObjectEvent event) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		switch(event.getChangeType()) {
		case ADD: {
			XyAssert.xyAssert(!object.hasField(event.getFieldId()));
			XRevWritableField field = object.createField(event.getFieldId());
			field.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert object.hasField(event.getFieldId()) : "object " + object.getAddress()
			        + " should have field " + event.getFieldId();
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
	}
	
	/**
	 * @param object to which the event is applied
	 * @param event to be applied
	 */
	private static void applyObjectEventIgnoreRev(XWritableObject object, XObjectEvent event) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		switch(event.getChangeType()) {
		case ADD: {
			XyAssert.xyAssert(!object.hasField(event.getFieldId()));
			object.createField(event.getFieldId());
			break;
		}
		case REMOVE: {
			assert object.hasField(event.getFieldId()) : "object " + object.getAddress()
			        + " should have field " + event.getFieldId();
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
	}
	
	/**
	 * Apply the {@link XRepositoryEvent} to the given model.
	 * 
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	private static void applyRepositoryEventIgnoreRev(XModel model, XRepositoryEvent event) {
		XyAssert.xyAssert(event.getChangedEntity().equals(model.getAddress()));
		switch(event.getChangeType()) {
		case ADD: {
			/*
			 * if this triggers a bug, repo.clear might have failed -- maybe a
			 * test -only problem
			 */
			if(!model.isEmpty()) {
				throw new IllegalStateException(
				        "Cannot apply repository ADD event to non-empty model "
				                + model.getAddress() + " " + model.getRevisionNumber());
			}
			break;
		}
		case REMOVE: {
			assert model.getRevisionNumber() >= 0 : model.getRevisionNumber();
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
	
	/**
	 * Apply the {@link XRepositoryEvent} to the given model.
	 * 
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	private static void applyRepositoryEvent(XRevWritableModel model, XRepositoryEvent event) {
		XyAssert.xyAssert(event.getChangedEntity().equals(model.getAddress()));
		switch(event.getChangeType()) {
		case ADD: {
			/*
			 * if this triggers a bug, repo.clear might have failed -- maybe a
			 * test -only problem
			 */
			if(!model.isEmpty()) {
				throw new IllegalStateException(
				        "Cannot apply repository ADD event to non-empty model "
				                + model.getAddress() + " " + model.getRevisionNumber());
			}
			model.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			assert model.getRevisionNumber() >= 0 : model.getRevisionNumber();
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

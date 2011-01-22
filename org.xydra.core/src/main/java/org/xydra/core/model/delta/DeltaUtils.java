package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.index.query.Pair;


/**
 * Helper class for generating the necessary events when executing commands.
 * 
 * TODO move this to core and merge with event generation in MemoryModel if
 * possible
 * 
 * @author dscharrer
 * 
 */
public abstract class DeltaUtils {
	
	public enum ModelChange {
		CREATED, NOCHANGE, REMOVED
	}
	
	/**
	 * IMPROVE some of this could be shared with {@link SynchronizesChangesImpl}
	 * , but revision numbers are handled differently.
	 */
	public static void applyChanges(SimpleModel model, ChangedModel changedModel, long rev) {
		
		for(XID objectId : changedModel.getRemovedObjects()) {
			assert model.hasObject(objectId);
			model.removeObject(objectId);
		}
		
		for(XReadableObject object : changedModel.getNewObjects()) {
			assert !model.hasObject(object.getID());
			XRevWritableObject newObject = model.createObject(object.getID());
			for(XID fieldId : object) {
				applyChanges(newObject, object.getField(fieldId), rev);
			}
			newObject.setRevisionNumber(rev);
		}
		
		for(ChangedObject changedObject : changedModel.getChangedObjects()) {
			
			boolean objectChanged = false;
			
			XRevWritableObject object = model.getObject(changedObject.getID());
			assert object != null;
			
			for(XID fieldId : changedObject.getRemovedFields()) {
				assert object.hasField(fieldId);
				object.removeField(fieldId);
				objectChanged = true;
			}
			
			for(XReadableField field : changedObject.getNewFields()) {
				applyChanges(object, field, rev);
				objectChanged = true;
			}
			
			for(ChangedField changedField : changedObject.getChangedFields()) {
				if(changedField.isChanged()) {
					XRevWritableField field = object.getField(changedField.getID());
					assert field != null;
					boolean valueChanged = field.setValue(changedField.getValue());
					assert valueChanged;
					field.setRevisionNumber(rev);
					objectChanged = true;
				}
			}
			
			if(objectChanged) {
				object.setRevisionNumber(rev);
			}
			
		}
		
		model.setRevisionNumber(rev);
		
	}
	
	/**
	 * @param modelAddr
	 * @param modelToChange
	 * @param change
	 * @param rev
	 * @return a model with the changes applied or null if model has been
	 *         removed by the changes.
	 */
	public static SimpleModel applyChanges(XAddress modelAddr, SimpleModel modelToChange,
	        Pair<ChangedModel,ModelChange> change, long rev) {
		
		SimpleModel model = modelToChange;
		ChangedModel changedModel = change.getFirst();
		ModelChange mc = change.getSecond();
		
		if(mc == ModelChange.REMOVED) {
			return null;
		} else if(mc == ModelChange.CREATED) {
			assert model == null;
			model = new SimpleModel(modelAddr);
			model.setRevisionNumber(rev);
		}
		
		if(changedModel != null) {
			assert model != null;
			applyChanges(model, changedModel, rev);
		}
		
		return model;
	}
	
	public static void applyChanges(XRevWritableObject object, XReadableField field, long rev) {
		assert !object.hasField(field.getID());
		XRevWritableField newField = object.createField(field.getID());
		newField.setValue(field.getValue());
		newField.setRevisionNumber(rev);
	}
	
	/**
	 * @return the appropriate events for the change (as returned by
	 *         {@link #executeCommand(XReadableModel, XCommand)}
	 */
	public static List<XAtomicEvent> createEvents(XAddress modelAddr,
	        Pair<ChangedModel,ModelChange> change, XID actorId, long rev) {
		
		assert change != null;
		
		ChangedModel model = change.getFirst();
		ModelChange mc = change.getSecond();
		
		assert model == null || rev - 1 == model.getRevisionNumber();
		
		int nChanges = (mc == ModelChange.NOCHANGE ? 0 : 1);
		if(model != null) {
			nChanges += model.countEventsNeeded(2 - nChanges);
		}
		boolean inTrans = nChanges > 1;
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		if(mc == ModelChange.CREATED) {
			events.add(MemoryRepositoryEvent.createAddEvent(actorId, modelAddr.getParent(),
			        modelAddr.getModel(), rev - 1, false));
		}
		
		if(model != null) {
			createEventsForChangedModel(events, actorId, model, inTrans, mc == ModelChange.REMOVED);
		}
		
		if(mc == ModelChange.REMOVED) {
			events.add(MemoryRepositoryEvent.createRemoveEvent(actorId, modelAddr.getParent(),
			        modelAddr.getModel(), rev - 1, inTrans));
		}
		
		return events;
	}
	
	public static void createEventsForChangedModel(List<XAtomicEvent> events, XID actorId,
	        ChangedModel changedModel, boolean inTrans, boolean implied) {
		
		long rev = changedModel.getRevisionNumber();
		
		for(XID objectId : changedModel.getRemovedObjects()) {
			XReadableObject removedObject = changedModel.getOldObject(objectId);
			DeltaUtils.createEventsForRemovedObject(events, rev, actorId, removedObject, inTrans,
			        implied);
		}
		
		for(XReadableObject object : changedModel.getNewObjects()) {
			events.add(MemoryModelEvent.createAddEvent(actorId, changedModel.getAddress(),
			        object.getID(), rev, inTrans));
			for(XID fieldId : object) {
				DeltaUtils.createEventsForNewField(events, rev, actorId, object,
				        object.getField(fieldId), inTrans);
			}
		}
		
		for(ChangedObject object : changedModel.getChangedObjects()) {
			
			assert !implied;
			
			for(XID fieldId : object.getRemovedFields()) {
				DeltaUtils.createEventsForRemovedField(events, rev, actorId, object,
				        object.getOldField(fieldId), inTrans, false);
			}
			
			for(XReadableField field : object.getNewFields()) {
				DeltaUtils.createEventsForNewField(events, rev, actorId, object, field, inTrans);
			}
			
			for(ChangedField field : object.getChangedFields()) {
				if(field.isChanged()) {
					XValue oldValue = field.getOldValue();
					XValue newValue = field.getValue();
					XAddress target = field.getAddress();
					long objectRev = object.getRevisionNumber();
					long fieldRev = field.getRevisionNumber();
					if(newValue == null) {
						assert oldValue != null;
						events.add(MemoryFieldEvent.createRemoveEvent(actorId, target, oldValue,
						        rev, objectRev, fieldRev, inTrans, false));
					} else if(oldValue == null) {
						events.add(MemoryFieldEvent.createAddEvent(actorId, target, newValue, rev,
						        objectRev, fieldRev, inTrans));
					} else {
						events.add(MemoryFieldEvent.createChangeEvent(actorId, target, oldValue,
						        newValue, rev, objectRev, fieldRev, inTrans));
					}
					
				}
			}
			
		}
		
	}
	
	public static void createEventsForNewField(List<XAtomicEvent> events, long rev, XID actorId,
	        XReadableObject object, XReadableField field, boolean inTrans) {
		long objectRev = object.getRevisionNumber();
		events.add(MemoryObjectEvent.createAddEvent(actorId, object.getAddress(), field.getID(),
		        rev, objectRev, inTrans));
		if(!field.isEmpty()) {
			events.add(MemoryFieldEvent.createAddEvent(actorId, field.getAddress(),
			        field.getValue(), rev, objectRev, field.getRevisionNumber(), inTrans));
		}
	}
	
	public static void createEventsForRemovedField(List<XAtomicEvent> events, long modelRev,
	        XID actorId, XReadableObject object, XReadableField field, boolean inTrans,
	        boolean implied) {
		long objectRev = object.getRevisionNumber();
		long fieldRev = field.getRevisionNumber();
		if(!field.isEmpty()) {
			events.add(MemoryFieldEvent.createRemoveEvent(actorId, field.getAddress(),
			        field.getValue(), modelRev, objectRev, fieldRev, inTrans, true));
		}
		events.add(MemoryObjectEvent.createRemoveEvent(actorId, object.getAddress(), field.getID(),
		        modelRev, objectRev, fieldRev, inTrans, implied));
	}
	
	public static void createEventsForRemovedObject(List<XAtomicEvent> events, long modelRev,
	        XID actorId, XReadableObject object, boolean inTrans, boolean implied) {
		for(XID fieldId : object) {
			DeltaUtils.createEventsForRemovedField(events, modelRev, actorId, object,
			        object.getField(fieldId), inTrans, true);
		}
		events.add(MemoryModelEvent.createRemoveEvent(actorId, object.getAddress().getParent(),
		        object.getID(), modelRev, object.getRevisionNumber(), inTrans, implied));
	}
	
	/**
	 * Execute the current command on the model.
	 * 
	 * @param model The model to modify. Null if the model currently doesn't
	 *            exist. This instance is modified.
	 * @return The changed model after executing the command (may be null if
	 *         there are no other changes except creating/removing the model)
	 *         (Pair#getFirst()) and if the model was added or removed by the
	 *         command (Pair#getSecond()). Returns null if the command failed.
	 */
	public static Pair<ChangedModel,ModelChange> executeCommand(XReadableModel model,
	        XCommand command) {
		
		if(command instanceof XRepositoryCommand) {
			
			XRepositoryCommand rc = (XRepositoryCommand)command;
			
			switch(rc.getChangeType()) {
			case ADD:
				if(model == null) {
					return new Pair<ChangedModel,ModelChange>(null, ModelChange.CREATED);
				} else if(rc.isForced()) {
					return new Pair<ChangedModel,ModelChange>(null, ModelChange.NOCHANGE);
				} else {
					return null;
				}
				
			case REMOVE:
				if((model == null || model.getRevisionNumber() != rc.getRevisionNumber())
				        && !rc.isForced()) {
					return null;
				} else if(model != null) {
					ChangedModel changedModel = new ChangedModel(model);
					changedModel.clear();
					return new Pair<ChangedModel,ModelChange>(changedModel, ModelChange.REMOVED);
				} else {
					return new Pair<ChangedModel,ModelChange>(null, ModelChange.NOCHANGE);
				}
				
			default:
				throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
			}
			
		} else {
			
			if(model == null) {
				return null;
			}
			
			ChangedModel changedModel = new ChangedModel(model);
			
			// apply changes to the delta-model
			if(!changedModel.executeCommand(command)) {
				return null;
			}
			
			return new Pair<ChangedModel,ModelChange>(changedModel, ModelChange.NOCHANGE);
			
		}
		
	}
	
}

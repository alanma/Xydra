package org.xydra.server.impl.newgae.changes;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.change.impl.memory.MemoryObjectEvent;
import org.xydra.core.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.core.value.XValue;


/**
 * Helper class for generating the necessary events when executing commands.
 * 
 * TODO move this to core and merge with event generation in MemoryModel if
 * possible
 * 
 * @author dscharrer
 * 
 */
public abstract class GaeEventHelper {
	
	public static List<XAtomicEvent> checkRepositoryCommandAndCreateEvents(XBaseModel currentModel,
	        XRepositoryCommand rc, XID actorId, long rev) throws AssertionError {
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		switch(rc.getChangeType()) {
		case ADD:
			if(currentModel == null) {
				events.add(MemoryRepositoryEvent.createAddEvent(actorId, rc.getTarget(), rc
				        .getModelID(), rev - 1, false));
			} else if(!rc.isForced()) {
				return null;
			}
			break;
		
		case REMOVE:
			if(currentModel == null || currentModel.getRevisionNumber() != rc.getRevisionNumber()) {
				if(!rc.isForced()) {
					return null;
				}
			}
			if(currentModel != null) {
				assert rev - 1 == currentModel.getRevisionNumber();
				boolean inTrans = false;
				
				for(XID objectId : currentModel) {
					inTrans = true;
					GaeEventHelper.createEventsForRemovedObject(events, rev - 1, actorId,
					        currentModel.getObject(objectId), inTrans, true);
				}
				events.add(MemoryRepositoryEvent.createRemoveEvent(actorId, rc.getTarget(), rc
				        .getModelID(), rev - 1, inTrans));
			}
			break;
		
		default:
			throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
		}
		
		return events;
	}
	
	public static List<XAtomicEvent> createEventsForChangedModel(XID actorId,
	        ChangedModel changedModel) {
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		boolean inTrans = changedModel.countEventsNeeded(2) > 1;
		long rev = changedModel.getRevisionNumber();
		
		for(XID objectId : changedModel.getRemovedObjects()) {
			XBaseObject removedObject = changedModel.getOldObject(objectId);
			GaeEventHelper.createEventsForRemovedObject(events, rev, actorId, removedObject,
			        inTrans, false);
		}
		
		for(XBaseObject object : changedModel.getNewObjects()) {
			events.add(MemoryModelEvent.createAddEvent(actorId, changedModel.getAddress(), object
			        .getID(), rev, inTrans));
			for(XID fieldId : object) {
				GaeEventHelper.createEventsForNewField(events, rev, actorId, object, object
				        .getField(fieldId), inTrans);
			}
		}
		
		for(ChangedObject object : changedModel.getChangedObjects()) {
			
			for(XID fieldId : object.getRemovedFields()) {
				GaeEventHelper.createEventsForRemovedField(events, rev, actorId, object, object
				        .getOldField(fieldId), inTrans, false);
			}
			
			for(XBaseField field : object.getNewFields()) {
				GaeEventHelper
				        .createEventsForNewField(events, rev, actorId, object, field, inTrans);
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
		
		return events;
	}
	
	public static void createEventsForNewField(List<XAtomicEvent> events, long rev, XID actorId,
	        XBaseObject object, XBaseField field, boolean inTrans) {
		long objectRev = object.getRevisionNumber();
		events.add(MemoryObjectEvent.createAddEvent(actorId, object.getAddress(), field.getID(),
		        rev, objectRev, inTrans));
		if(!field.isEmpty()) {
			events.add(MemoryFieldEvent.createAddEvent(actorId, field.getAddress(), field
			        .getValue(), rev, objectRev, field.getRevisionNumber(), inTrans));
		}
	}
	
	public static void createEventsForRemovedObject(List<XAtomicEvent> events, long modelRev,
	        XID actorId, XBaseObject object, boolean inTrans, boolean implied) {
		for(XID fieldId : object) {
			GaeEventHelper.createEventsForRemovedField(events, modelRev, actorId, object, object
			        .getField(fieldId), inTrans, true);
		}
		events.add(MemoryModelEvent.createRemoveEvent(actorId, object.getAddress().getParent(),
		        object.getID(), modelRev, object.getRevisionNumber(), inTrans, implied));
	}
	
	public static void createEventsForRemovedField(List<XAtomicEvent> events, long modelRev,
	        XID actorId, XBaseObject object, XBaseField field, boolean inTrans, boolean implied) {
		long objectRev = object.getRevisionNumber();
		long fieldRev = field.getRevisionNumber();
		if(!field.isEmpty()) {
			events.add(MemoryFieldEvent.createRemoveEvent(actorId, field.getAddress(), field
			        .getValue(), modelRev, objectRev, fieldRev, inTrans, true));
		}
		events.add(MemoryObjectEvent.createRemoveEvent(actorId, object.getAddress(), field.getID(),
		        modelRev, objectRev, fieldRev, inTrans, implied));
	}
	
	public static List<XAtomicEvent> checkCommandAndCreateEvents(XBaseModel currentModel,
	        XCommand command, XID actorId, long rev) {
		
		if(command instanceof XRepositoryCommand) {
			XRepositoryCommand rc = (XRepositoryCommand)command;
			
			return checkRepositoryCommandAndCreateEvents(currentModel, rc, actorId, rev);
			
		} else {
			
			if(currentModel == null) {
				return null;
			}
			
			assert rev - 1 == currentModel.getRevisionNumber();
			
			ChangedModel changedModel = new ChangedModel(currentModel);
			
			// apply changes to the delta-model
			if(!changedModel.executeCommand(command)) {
				return null;
			}
			
			// create events
			
			return createEventsForChangedModel(actorId, changedModel);
			
		}
		
	}
	
}

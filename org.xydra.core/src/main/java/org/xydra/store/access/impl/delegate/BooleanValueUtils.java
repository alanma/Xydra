package org.xydra.store.access.impl.delegate;

import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.X;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.InternalStoreException;
import org.xydra.store.impl.delegate.XydraPersistence;


public class BooleanValueUtils {
	
	private static final Logger log = LoggerFactory.getLogger(BooleanValueUtils.class);
	
	public static void setValueInObject(XHalfWritableModel model, XID objectId, XID fieldId,
	        boolean value) {
		log.trace(objectId + " " + fieldId + " " + value + " .");
		
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			object = model.createObject(objectId);
		}
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			field = object.createField(fieldId);
		}
		field.setValue(X.getValueFactory().createBooleanValue(value));
	}
	
	public static void removeValueInObject(XHalfWritableModel model, XID objectId, XID fieldId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
	
	public static boolean hasValue(XHalfWritableModel model, XID objectId, XID fieldId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return false;
		}
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		return field.getValue() != null;
	}
	
	public static void setValueInObject(XydraPersistence persistence, XID actorId, XID modelId,
	        XID objectId, XID fieldId, boolean value) {
		long result = persistence.executeCommand(
		        actorId,
		        X.getCommandFactory().createChangeValueCommand(persistence.getRepositoryId(),
		                modelId, objectId, fieldId, 0,
		                X.getValueFactory().createBooleanValue(value), true));
		
		if(result < 0)
			throw new InternalStoreException("Could not set field " + persistence.getRepositoryId()
			        + "/" + modelId + "/" + objectId + "/" + fieldId + " to value '" + value
			        + "' with actorId " + actorId);
	}
	
	public static void removeValueInObject(XydraPersistence persistence, XID actorId, XID modelId,
	        XID objectId, XID fieldId) {
		long fieldResult = persistence.executeCommand(
		        actorId,
		        X.getCommandFactory().createRemoveFieldCommand(persistence.getRepositoryId(),
		                modelId, objectId, fieldId, 0, true));
		if(fieldResult >= 0)
			return;
		// else: try to remove complete object
		long objectResult = persistence.executeCommand(
		        actorId,
		        X.getCommandFactory().createRemoveObjectCommand(persistence.getRepositoryId(),
		                modelId, objectId, 0, true));
		if(objectResult >= 0)
			return;
		else
			throw new InternalStoreException("Could neither remove field nor object in "
			        + persistence.getRepositoryId() + "/" + modelId + "/" + objectId + "/"
			        + fieldId + " with actorId " + actorId);
	}
	
}

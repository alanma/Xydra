package org.xydra.base;

import java.util.Collection;
import java.util.LinkedList;

import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XValue;


public class WritableUtils {
	
	public static void deleteAllModels(XWritableRepository repository) {
		// TODO if the model.iterator() support remove(), document it and change
		// this code
		Collection<XID> toBeDeleted = new LinkedList<XID>();
		for(XID xid : repository) {
			toBeDeleted.add(xid);
		}
		for(XID xid : toBeDeleted) {
			repository.removeModel(xid);
		}
	}
	
	public static void deleteAllObjects(XWritableModel model) {
		// TODO if the model.iterator() support remove(), document it and change
		// this code
		Collection<XID> toBeDeleted = new LinkedList<XID>();
		for(XID xid : model) {
			toBeDeleted.add(xid);
		}
		for(XID xid : toBeDeleted) {
			model.removeObject(xid);
		}
	}
	
	public static XValue getValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return null;
		}
		return getValue(object, fieldId);
	}
	
	public static XValue getValue(XWritableObject object, XID fieldId) {
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return null;
		}
		return field.getValue();
	}
	
	public static XValue getValue(XWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId) {
		XWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return null;
		}
		return getValue(model, objectId, fieldId);
	}
	
	public static void removeValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		removeValue(object, fieldId);
	}
	
	public static void removeValue(XWritableObject object, XID fieldId) {
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
	
	public static void removeValue(XWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId) {
		XWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return;
		}
		removeValue(model, objectId, fieldId);
	}
	
	public static boolean setValue(XWritableModel model, XID objectId, XID fieldId, XValue value) {
		XWritableObject object = model.getObject(objectId);
		boolean changed = false;
		if(object == null) {
			object = model.createObject(objectId);
			changed = true;
		}
		assert model.hasObject(objectId);
		return changed | setValue(object, fieldId, value);
	}
	
	public static boolean setValue(XWritableObject object, XID fieldId, XValue value) {
		XWritableField field = object.getField(fieldId);
		boolean changed = false;
		if(field == null) {
			field = object.createField(fieldId);
			changed = true;
		}
		assert object.hasField(fieldId);
		return changed | field.setValue(value);
	}
	
	/**
	 * @param repository
	 * @param modelId
	 * @param objectId
	 * @param fieldId
	 * @param value
	 * @return true if the operation changed something
	 */
	public static boolean setValue(XWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId, XValue value) {
		XWritableModel model = repository.getModel(modelId);
		boolean changed = false;
		if(model == null) {
			changed = true;
			model = repository.createModel(modelId);
		}
		assert repository.hasModel(modelId);
		return changed | setValue(model, objectId, fieldId, value);
	}
}

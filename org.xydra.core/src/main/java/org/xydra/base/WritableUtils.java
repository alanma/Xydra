package org.xydra.base;

import java.util.Collection;
import java.util.LinkedList;

import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;


public class WritableUtils {
	
	public static void deleteAllModels(XWritableRepository repository) {
		// TODO if the model.iterator() support remove(), document it and change
		// this code
		Collection<XId> toBeDeleted = new LinkedList<XId>();
		for(XId xid : repository) {
			toBeDeleted.add(xid);
		}
		for(XId xid : toBeDeleted) {
			repository.removeModel(xid);
		}
	}
	
	public static void deleteAllObjects(XWritableModel model) {
		// TODO if the model.iterator() support remove(), document it and change
		// this code
		Collection<XId> toBeDeleted = new LinkedList<XId>();
		for(XId xid : model) {
			toBeDeleted.add(xid);
		}
		for(XId xid : toBeDeleted) {
			model.removeObject(xid);
		}
	}
	
	public static XValue getValue(XWritableModel model, XId objectId, XId fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return null;
		}
		return getValue(object, fieldId);
	}
	
	public static XValue getValue(XWritableObject object, XId fieldId) {
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return null;
		}
		return field.getValue();
	}
	
	public static XValue getValue(XWritableRepository repository, XId modelId, XId objectId,
	        XId fieldId) {
		XWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return null;
		}
		return getValue(model, objectId, fieldId);
	}
	
	public static void removeValue(XWritableModel model, XId objectId, XId fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		removeValue(object, fieldId);
	}
	
	public static void removeValue(XWritableObject object, XId fieldId) {
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
	
	public static void removeValue(XWritableRepository repository, XId modelId, XId objectId,
	        XId fieldId) {
		XWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return;
		}
		removeValue(model, objectId, fieldId);
	}
	
	public static boolean setValue(XWritableModel model, XId objectId, XId fieldId, XValue value) {
		XWritableObject object = model.getObject(objectId);
		boolean changed = false;
		if(object == null) {
			object = model.createObject(objectId);
			changed = true;
		}
		assert model.hasObject(objectId) : model.getAddress() + " should have " + objectId;
		return changed | setValue(object, fieldId, value);
	}
	
	public static boolean setValue(XWritableObject object, XId fieldId, XValue value) {
		XWritableField field = object.getField(fieldId);
		boolean changed = false;
		if(field == null) {
			field = object.createField(fieldId);
			changed = true;
		}
		XyAssert.xyAssert(object.hasField(fieldId));
		return changed | field.setValue(value);
	}
	
	/**
	 * @param repository where to set the value
	 * @param modelId not null
	 * @param objectId not null
	 * @param fieldId not null
	 * @param value can be null
	 * @return true if the operation changed something
	 */
	public static boolean setValue(XWritableRepository repository, XId modelId, XId objectId,
	        XId fieldId, XValue value) {
		XWritableModel model = repository.getModel(modelId);
		boolean changed = false;
		if(model == null) {
			changed = true;
			model = repository.createModel(modelId);
		}
		XyAssert.xyAssert(repository.hasModel(modelId));
		return changed | setValue(model, objectId, fieldId, value);
	}
}

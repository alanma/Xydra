package org.xydra.base;

import org.xydra.base.value.XValue;


public class HalfWritableUtils {
	
	/**
	 * @param repository
	 * @param modelId
	 * @param objectId
	 * @param fieldId
	 * @param value
	 * @return true if the operation changed something
	 */
	public static boolean setValue(XHalfWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId, XValue value) {
		XHalfWritableModel model = repository.getModel(modelId);
		boolean changed = false;
		if(model == null) {
			changed = true;
			model = repository.createModel(modelId);
		}
		assert repository.hasModel(modelId);
		return changed | setValue(model, objectId, fieldId, value);
	}
	
	public static XValue getValue(XHalfWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId) {
		XHalfWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return null;
		}
		return getValue(model, objectId, fieldId);
	}
	
	public static void removeValue(XHalfWritableRepository repository, XID modelId, XID objectId,
	        XID fieldId) {
		XHalfWritableModel model = repository.getModel(modelId);
		if(model == null) {
			return;
		}
		removeValue(model, objectId, fieldId);
	}
	
	public static boolean setValue(XHalfWritableModel model, XID objectId, XID fieldId, XValue value) {
		XHalfWritableObject object = model.getObject(objectId);
		boolean changed = false;
		if(object == null) {
			changed = true;
			object = model.createObject(objectId);
		}
		assert model.hasObject(objectId);
		return changed | setValue(object, fieldId, value);
	}
	
	public static XValue getValue(XHalfWritableModel model, XID objectId, XID fieldId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return null;
		}
		return getValue(object, fieldId);
	}
	
	public static void removeValue(XHalfWritableModel model, XID objectId, XID fieldId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		removeValue(object, fieldId);
	}
	
	public static boolean setValue(XHalfWritableObject object, XID fieldId, XValue value) {
		XHalfWritableField field = object.getField(fieldId);
		boolean changed = false;
		if(field == null) {
			changed = true;
			field = object.createField(fieldId);
		}
		assert object.hasField(fieldId);
		return changed | field.setValue(value);
	}
	
	public static XValue getValue(XHalfWritableObject object, XID fieldId) {
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			return null;
		}
		return field.getValue();
	}
	
	public static void removeValue(XHalfWritableObject object, XID fieldId) {
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
}

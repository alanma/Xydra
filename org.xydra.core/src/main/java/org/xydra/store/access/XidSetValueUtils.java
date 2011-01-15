package org.xydra.store.access;

import java.util.Collections;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Helper class to deal with {@link XIDSetValue}.
 * 
 * TODO IMPROVE consider merging into a more general utility class
 * 
 * @author xamde
 */
public class XidSetValueUtils {
	
	private static final Logger log = LoggerFactory.getLogger(XidSetValueUtils.class);
	
	/**
	 * Helper method. Post condition: model.object.field is an
	 * {@link XIDSetValue} which contains addedValue.
	 * 
	 * 
	 * @param model
	 * @param objectId
	 * @param fieldId
	 * @param addedValue
	 */
	public static void addToXIDSetValueInObject(XWritableModel model, XID objectId, XID fieldId,
	        XID addedValue) {
		log.trace(objectId + " " + fieldId + " " + addedValue + " .");
		
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			object = model.createObject(objectId);
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			field = object.createField(fieldId);
		}
		XValue currentValue = field.getValue();
		if(currentValue == null) {
			currentValue = X.getValueFactory().createIDSetValue(new XID[] { addedValue });
		} else {
			currentValue = ((XIDSetValue)currentValue).add(addedValue);
		}
		field.setValue(currentValue);
	}
	
	/**
	 * Post condition: If there ever was a model.object.field, it now no longer
	 * contains the removedValue.
	 * 
	 * @param model
	 * @param objectId
	 * @param fieldId
	 * @param removedValue
	 */
	public static void removeFromXIDSetValueInObject(XWritableModel model, XID objectId,
	        XID fieldId, XID removedValue) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		XValue currentValue = field.getValue();
		if(currentValue == null) {
			return;
		} else {
			currentValue = ((XIDSetValue)currentValue).remove(removedValue);
		}
		field.setValue(currentValue);
	}
	
	/**
	 * @param model
	 * @param objectId
	 * @param fieldId
	 * @return model.object.field as a Set<XID>, never null (returns emtpy set)
	 */
	public static Set<XID> getXIDSetValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return Collections.emptySet();
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return Collections.emptySet();
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).toSet();
	}
	
	/**
	 * @param model
	 * @param objectId
	 * @param fieldId
	 * @param valueId
	 * @return true if model.object.field exists and contains the valueId
	 */
	public static boolean valueContainsId(XWritableModel model, XID objectId, XID fieldId,
	        XID valueId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalArgumentException("Object " + objectId + " not found");
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).contains(valueId);
	}
	
}

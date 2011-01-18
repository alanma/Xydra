package org.xydra.store.access;

import java.util.Collections;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.NamingUtils;
import org.xydra.store.impl.delegate.XydraPersistence;


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
	public static void addToXIDSetValueInObject(XHalfWritableModel model, XID objectId, XID fieldId,
	        XID addedValue) {
		log.trace(objectId + " " + fieldId + " " + addedValue + " .");
		
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			object = model.createObject(objectId);
		}
		XHalfWritableField field = object.getField(fieldId);
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
	 * Helper method. Post condition: model.object.field is an
	 * {@link XIDSetValue} which contains addedValue.
	 * 
	 * @param persistence
	 * @param previousRevNr
	 * @param executingActorId
	 * @param objectId
	 * @param fieldId
	 * @param value
	 * @return the resulting revNr
	 */
	public static long setValueInObject(XydraPersistence persistence, long previousRevNr,
	        XID executingActorId, XID objectId, XID fieldId, XValue value) {
		long revNr = previousRevNr;
		// try: repo.model.object.field = value
		long result = tryToSetValueInField(persistence, revNr, executingActorId, objectId, fieldId,
		        value);
		if(result == XCommand.FAILED) {
			log.info("failed to set value; creating field...");
			// try: repo.model.object.NEW field
			result = tryToCreateField(persistence, executingActorId, objectId, fieldId);
			if(result == XCommand.FAILED) {
				log.info("failed to create field; creating object...");
				// try: repo.model.NEW object
				result = tryToCreateObject(persistence, executingActorId, objectId);
				if(result == XCommand.FAILED) {
					log.info("failed to create object; creating model...");
					// try: repo.NEW model
					result = tryToCreateModel(persistence, executingActorId);
					assert result != XCommand.FAILED : "failed to create model";
					if(result >= 0) {
						revNr = result;
					} // repo.model.NEW object
					result = tryToCreateObject(persistence, executingActorId, objectId);
					assert result != XCommand.FAILED : "failed to create object in new model";
					if(result >= 0) {
						revNr = result;
					} // repo.model.object.NEW field
					result = tryToCreateField(persistence, executingActorId, objectId, fieldId);
					assert result != XCommand.FAILED : "failed to create field in new object";
					if(result >= 0) {
						revNr = result;
					} // repo.model.object.field = value
					result = tryToSetValueInField(persistence, revNr, executingActorId, objectId,
					        fieldId, value);
					assert result != XCommand.FAILED : "failed to set value in new field";
					if(result >= 0) {
						revNr = result;
					}
				} else if(result >= 0) {
					revNr = result;
				}
				log.info("created object with " + result);
				// repo.model.object.NEW field
				result = tryToCreateField(persistence, executingActorId, objectId, fieldId);
				assert result != XCommand.FAILED : "failed to create field in new object";
				if(result >= 0) {
					revNr = result;
				}
				log.info("created field with " + result);
				// repo.model.object.field = value
				result = tryToSetValueInField(persistence, revNr, executingActorId, objectId,
				        fieldId, value);
				assert result != XCommand.FAILED : "failed to set value in new field";
				if(result >= 0) {
					revNr = result;
				}
				log.info("set value with " + result);
			} else if(result >= 0) {
				revNr = result;
			}
		} else if(result >= 0) {
			revNr = result;
		}
		assert result != XCommand.FAILED : objectId + "/" + fieldId + " = " + value
		        + " -> result = " + result;
		return result;
	}
	
	private static long tryToCreateModel(XydraPersistence persistence, XID executingActorId) {
		return persistence.executeCommand(
		        executingActorId,
		        X.getCommandFactory().createAddModelCommand(persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, false));
	}
	
	private static long tryToCreateField(XydraPersistence persistence, XID executingActorId,
	        XID objectId, XID fieldId) {
		return persistence.executeCommand(
		        executingActorId,
		        X.getCommandFactory().createAddFieldCommand(persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, objectId, fieldId, false));
	}
	
	private static long tryToCreateObject(XydraPersistence persistence, XID executingActorId,
	        XID objectId) {
		return persistence.executeCommand(
		        executingActorId,
		        X.getCommandFactory().createAddObjectCommand(persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, objectId, false));
	}
	
	/**
	 * @param persistence
	 * @param previousRevNr
	 * @param executingActorId
	 * @param objectId
	 * @param fieldId
	 * @param value
	 * @return try: repo.model.object.field = value
	 */
	private static long tryToSetValueInField(XydraPersistence persistence, long previousRevNr,
	        XID executingActorId, XID objectId, XID fieldId, XValue value) {
		return persistence.executeCommand(
		        executingActorId,
		        X.getCommandFactory().createAddValueCommand(persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, objectId, fieldId, previousRevNr, value,
		                false));
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
	public static void removeFromXIDSetValueInObject(XHalfWritableModel model, XID objectId,
	        XID fieldId, XID removedValue) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		XHalfWritableField field = object.getField(fieldId);
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
	public static Set<XID> getXIDSetValue(XHalfWritableModel model, XID objectId, XID fieldId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			return Collections.emptySet();
		}
		XHalfWritableField field = object.getField(fieldId);
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
	public static boolean valueContainsId(XHalfWritableModel model, XID objectId, XID fieldId,
	        XID valueId) {
		XHalfWritableObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalArgumentException("Object " + objectId + " not found");
		}
		XHalfWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).contains(valueId);
	}
	
}

package org.xydra.core;

import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XType;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XV;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


/**
 * A utility class that uses {@link X} to provide convenience methods for
 * working with/editing Xydra Instances.
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 * 
 */

public class XX {
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param object The {@link XObject} containing the {@link XField}
	 * @param fieldID The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	public static XField setValue(XID actorID, XObject object, XID fieldID, XValue value) {
		XField field = object.getField(fieldID);
		if(field == null) {
			field = object.createField(actorID, fieldID);
		}
		field.setValue(actorID, value);
		return field;
	}
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param model The {@link XModel} containing the {@link XObject}
	 * @param object The {@link XObject} containing the {@link XField}
	 * @param fieldID The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	public static XField setValue(XID actorID, XModel model, XID objectID, XID fieldID, XValue value) {
		XObject object = safeGetObject(model, objectID);
		return setValue(actorID, object, fieldID, value);
	}
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param repository The {@link XRepository} containing the {@link XModel}
	 *            which contains the rest
	 * @param model The {@link XModel} containing the {@link XObject}
	 * @param object The {@link XObject} containing the {@link XField}
	 * @param fieldID The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	public static XField setValue(XID actorID, XRepository repository, XID modelID, XID objectID,
	        XID fieldID, XValue value) {
		XModel model = safeGetModel(repository, modelID);
		return setValue(actorID, model, objectID, fieldID, value);
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceRepository The {@link XRepository} which is to be copied
	 * @param targetRepository The {@link XRepository} in which the data of
	 *            sourceRepository is to be pasted.
	 */
	public static void copy(XID actorID, XRepository sourceRepository, XRepository targetRepository) {
		// copy repository to _repository
		for(XID modelID : sourceRepository) {
			XModel model = sourceRepository.getModel(modelID);
			XModel localModel = targetRepository.createModel(actorID, model.getID());
			copy(actorID, model, localModel);
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceModel The {@link XModel} which is to be copied
	 * @param targetModel The {@link XModel} in which the data of sourceModel is
	 *            to be pasted.
	 */
	public static void copy(XID actorID, XModel sourceModel, XModel targetModel) {
		// copy model to _model
		for(XID objectID : sourceModel) {
			XObject object = sourceModel.getObject(objectID);
			XObject localObject = targetModel.createObject(actorID, object.getID());
			copy(actorID, object, localObject);
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param sourceObject The {@link XObject} which is to be copied
	 * @param targetObject The {@link XObject} in which the data of sourceObject
	 *            is to be pasted.
	 */
	public static void copy(XID actorID, XObject sourceObject, XObject targetObject) {
		for(XID fieldID : sourceObject) {
			XField field = sourceObject.getField(fieldID);
			XField localField = targetObject.createField(actorID, fieldID);
			localField.setValue(actorID, field.getValue());
		}
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldID'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param object The {@link XObject} which should contain the specified
	 *            {@link XField}
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XField} doesn't exist
	 */
	public static XValue safeGetValue(XObject object, XID fieldID) throws MissingPieceException {
		XField field = safeGetField(object, fieldID);
		XValue value = field.getValue();
		if(value == null) {
			throw new MissingPieceException("Field with ID '" + fieldID + "' in object with ID "
			        + object.getID() + " has no value");
		}
		return value;
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldID'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectID'.
	 * @param objectID The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField}
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject}/{@link XField} doesn't exist
	 */
	public static XValue safeGetValue(XModel model, XID objectID, XID fieldID) {
		XObject object = safeGetObject(model, objectID);
		return safeGetValue(object, fieldID);
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldID'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelID'
	 * @param modelID The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectID'
	 * @param objectID The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldID'
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject}/{@link XField} doesn't exist
	 */
	public static XValue safeGetValue(XRepository repository, XID modelID, XID objectID, XID fieldID) {
		XObject object = safeGetObject(repository, modelID, objectID);
		return safeGetValue(object, fieldID);
	}
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldID' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * 
	 * @param object The {@link XObject} which should contain the {@link XField}
	 *            specified by 'fieldID'
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XField} doesn't exist
	 */
	public static XField safeGetField(XObject object, XID fieldID) throws MissingPieceException {
		XField field = object.getField(fieldID);
		if(field == null) {
			throw new MissingPieceException("No field with ID '" + fieldID
			        + "' found in object with ID " + object.getID());
		}
		return field;
	}
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldID' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectID'
	 * @param objectID The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldID'
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject}/{@link XField} doesn't exist
	 */
	public static XField safeGetField(XModel model, XID objectID, XID fieldID) {
		XObject object = safeGetObject(model, objectID);
		return safeGetField(object, fieldID);
	}
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldID' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelID'
	 * @param modelID The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectID'
	 * @param objectID The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldID'
	 * @param fieldID The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject}/{{@link XField} doesn't exist
	 */
	public static XField safeGetField(XRepository repository, XID modelID, XID objectID, XID fieldID) {
		XModel model = safeGetModel(repository, modelID);
		return safeGetField(model, objectID, fieldID);
	}
	
	/**
	 * Tries to get the {@link XModel} with {@link XID} 'modelID' from the given
	 * {@link XRepository}. If the specified model is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelID'
	 * @param modelID The {@link XID} of the {@link XModel}
	 * @return The specified {{@link XModel}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel} doesn't exist
	 */
	public static XModel safeGetModel(XRepository repository, XID modelID) {
		XModel model = repository.getModel(modelID);
		if(model == null) {
			throw new MissingPieceException("No model with ID '" + modelID
			        + "' found in repository with ID " + repository.getID());
		}
		return model;
	}
	
	/**
	 * Tries to get the {@link XObject} with {@link XID} 'objectID' from the
	 * given {@link XModel}. If the specified object is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectID'
	 * @param objectID The {@link XID} of the {@link XObject}
	 * @return The specified {@link XObject}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject} doesn't exist
	 */
	public static XObject safeGetObject(XModel model, XID objectID) {
		XObject object = model.getObject(objectID);
		if(object == null) {
			throw new MissingPieceException("No object with ID '" + objectID
			        + "' found in model with ID " + model.getID());
		}
		return object;
	}
	
	/**
	 * Tries to get the {@link XObject} with {@link XID} 'objectID' from the
	 * given {@link XRepository}. If the specified object is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelID'
	 * @param modelID The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectID'
	 * @param objectID The {@link XID} of the {@link XObject}
	 * @return The specified {@link XObject}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject} doesn't exist
	 */
	public static XObject safeGetObject(XRepository repository, XID modelID, XID objectID) {
		XModel model = safeGetModel(repository, modelID);
		return safeGetObject(model, objectID);
	}
	
	/**
	 * Sets the {@link XField} specified by 'fieldID' of the given
	 * {@link XObject} to given stringValue on behalf of the actor with
	 * {@link XID} 'actorID'
	 * 
	 * @param actorID The {@link XID} of the actor.
	 * @param object The {@link XObject} containing the {@link XField} specified
	 *            by'fieldID'.
	 * @param fieldID The {@link XID} of the {@link XField} which value is to be
	 *            set. {@link XField} will be created if it doesn't exist.
	 * @param stringValue The new String, which will be set as the value of the
	 *            specified {@link XField}.
	 */
	public static void safeSetStringValue(XID actorID, XObject object, XID fieldID,
	        String stringValue) {
		if(object != null) {
			try {
				XField field = safeGetField(object, fieldID);
				field.setValue(actorID, XV.toValue(stringValue));
			} catch(MissingPieceException mpe) {
				object.createField(actorID, fieldID).setValue(actorID, XV.toValue(stringValue));
			}
		}
	}
	
	// TODO are these add*ToList and remove*FromList methods really needed?
	// ~Daniel
	
	/**
	 * Appends a new {@link XID} to the end of the {@link XIDListValue} of the
	 * given {@link XField} (only works if the {@link XValue} of the
	 * {@link XField} actually is a {@link XIDListValue})
	 * 
	 * @param actorID The {@link XID} of the actor executing this method
	 * @param field The {@link XField} containing the {@link XIDListValue}
	 * @param id The {@link XID} which is to be added
	 * @return the new {@link XIDListValue} which the given {@link XID} was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XIDListValue})
	 */
	public static XIDListValue addIDToList(XID actorID, XField field, XID id) {
		XValue value = field.getValue();
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(id);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new {@link XID} at the given index to the {@link XIDListValue} of
	 * the given {@link XField} (only works if the {@link XValue} of the
	 * {@link XField} actually is a {@link XIDListValue})
	 * 
	 * @param actorID The {@link XID} of the actor executing this method
	 * @param field The {@link XField} containing the {@link XIDListValue}
	 * @param id The {@link XID} which is to be added
	 * @param index The index at which the specified element is to be inserted
	 * @return the new {@link XIDListValue} which the given {@link XID} was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XIDListValue})
	 */
	public static XIDListValue addIDToList(XID actorID, XField field, int index, XID id) {
		XValue value = field.getValue();
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, id);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of the given {@link XID} from the
	 * {@link XIDListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIDListValue}
	 * @param id The {@link XID} which is to be removed
	 * @return the new {@link XIDListValue} which the given {@link XID} was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XIDListValue})
	 */
	public static XIDListValue removeIDFromList(XID actorID, XField field, XID id) {
		XValue value = field.getValue();
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(id);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the {@link XID} at the specified index from the
	 * {@link XIDListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIDListValue}
	 * @param index The index of the {@link XID} which is to be removed
	 * @return the new {@link XIDListValue} which the given {@link XID} was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XIDListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XIDListValue removeIDFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Appends a new boolean to end of the {@link XBooleanListValue} of the
	 * given {@link XField}.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XBooleanListValue}
	 * @param bool The boolean which is to be added
	 * @return the new {@link XBooleanListValue} which the given Boolean was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XBooleanListValue})
	 */
	public static XBooleanListValue addBooleanToList(XID actorID, XField field, boolean bool) {
		XValue value = field.getValue();
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(bool);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new boolean at the specified index to the
	 * {@link XBooleanListValue} of the given {@link XField}.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The field containing the {@link XBooleanListValue}
	 * @param bool The boolean which is to be added
	 * @param index The index at which the specified element is to be inserted
	 * @return the new {@link XBooleanListValue} which the given Boolean was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XBooleanListValue})
	 */
	public static XBooleanListValue addBooleanToList(XID actorID, XField field, int index,
	        boolean bool) {
		XValue value = field.getValue();
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, bool);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of the given boolean from the
	 * {@link XBooleanListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XBooleanListValue}
	 * @param bool The boolean which is to be removed
	 * @return the new {@link XBooleanListValue} which the specified Boolean was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XBooleanListValue})
	 */
	public static XBooleanListValue removeBooleanFromList(XID actorID, XField field, boolean bool) {
		XValue value = field.getValue();
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(bool);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes a boolean from the {@link XBooleanListValue} of the given
	 * {@link XField}.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XBooleanListValue}
	 * @param index The index of the boolean which is to be removed
	 * @return the new {@link XBooleanListValue} which the specified Boolean was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XBooleanListValue})
	 * @throws IndexOutOfBoundsException if index is out of range (index < 0 ||
	 *             index >= size())
	 */
	public static XBooleanListValue removeBooleanFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Appends a new Double to end of the {@link XDoubleListValue} of the given
	 * {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XDoubleListValue}
	 * @param doub The Double which is to be added
	 * @return the new {@link XDoubleListValue} which the specified Double was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XDoubleListValue})
	 */
	public static XDoubleListValue addDoubleToList(XID actorID, XField field, double doub) {
		XValue value = field.getValue();
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(doub);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Double at the specified index to the {@link XDoubleListValue}
	 * of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XDoubleListValue}
	 * @param index The index at which the specified element is to be added
	 * @param doub The Double which is to be added
	 * @return the new {@link XDoubleListValue} which the specified Double was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XDoubleListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XDoubleListValue addDoubleToList(XID actorID, XField field, int index, double doub) {
		XValue value = field.getValue();
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, doub);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of the given Double from the
	 * {@link XDoubleListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XDoubleListValue}
	 * @param doubleVal The Double which is to be removed
	 * @return the new {@link XDoubleListValue} which the given Double was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XDoubleListValue})
	 */
	public static XDoubleListValue removeDoubleFromList(XID actorID, XField field, double doubleVal) {
		XValue value = field.getValue();
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(doubleVal);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes a Double from the {@link XDoubleListValue} of the given
	 * {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XDoubleListValue}
	 * @param index The index of the Double which is to be removed
	 * @return the new {@link XDoubleListValue} which the given Double was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XDoubleListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XDoubleListValue removeDoubleFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Appends a new Integer to end of the {@link XIntegerListValue} of the
	 * given {{@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIntegerListValue}
	 * @param integer The Integer which is to be added
	 * @return the new {@link XIntegerListValue} which the specified Integer was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XIntegerListValue})
	 */
	public static XIntegerListValue addIntegerToList(XID actorID, XField field, int integer) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(integer);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Integer at the specified index to the
	 * {@link XIntegerListValue} of the given {@link XField}.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIntegerListValue}
	 * @param integer The Integer which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XIntegerListValue} which the specified Integer was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XIntegerListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XIntegerListValue addIntegerToList(XID actorID, XField field, int index,
	        int integer) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, integer);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes an Integer from the {@link XIntegerListValue} of the given
	 * {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIntegerListValue}
	 * @param index The index of the Integer which is to be removed
	 * @return the new {@link XIntegerListValue} which the given Integer was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XIntegerListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XIntegerListValue removeIntegerFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of the given Integer from the
	 * {@link XIntegerListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XIntegerListValue}
	 * @param integer The Integer which is to be removed
	 * @return the new {@link XIntegerListValue} which the given Integer was
	 *         removed from (null if the given field had no
	 *         {@link XIntegerListValue})
	 */
	public static XIntegerListValue removeIntegerFromList(XID actorID, XField field, Integer integer) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(integer);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Appends a new Long at the end of the {@link XLongListValue} of the given
	 * {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XLongListValue}
	 * @param longVal The Long which is to be added
	 * @return the new {@link XLongListValue} which the specified Long was added
	 *         to (null if the given {@link XField} had no
	 *         {@link XLongListValue})
	 */
	public static XLongListValue addLongToList(XID actorID, XField field, long longVal) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(longVal);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Long at specified index to the {@link XLongListValue} of the
	 * given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XLongListValue}
	 * @param longVal The Long which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XLongListValue} which the specified Long was added
	 *         to (null if the given {@link XField} had no
	 *         {@link XLongListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XLongListValue addLongToList(XID actorID, XField field, int index, long longVal) {
		XValue value = field.getValue();
		
		if(value instanceof XIntegerListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, longVal);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes a Long from the {@link XLongListValue} of the given
	 * {@link XField}.
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XLongListValue}
	 * @param index The index of the Long which is to be removed
	 * @return the new {@link XLongListValue} which the given Long was removed
	 *         from (null if the given {@link XField} had no
	 *         {@link XLongListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XLongListValue removeLongFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XLongListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of a given Long from the given
	 * {@link XLongListValue}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param value The {@link XField} containing the {@link XLongListValue}
	 * @param longVal The Long which is to be removed
	 * @return the new {@link XLongListValue} which the given Long was removed
	 *         from (null if the given {@link XField} had no
	 *         {@link XLongListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XLongListValue removeLongFromList(XID actorID, XField field, long longVal) {
		XValue value = field.getValue();
		
		if(value instanceof XLongListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(longVal);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Appends a new String at the end of the {@link XStringListValue} of the
	 * given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XStringListValue}
	 * @param string The String which is to be added
	 * @return the new {@link XStringListValue} which the specified String was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XStringListValue})
	 */
	public static XStringListValue addStringToList(XID actorID, XField field, String string) {
		XValue value = field.getValue();
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(string);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new String at the specified index to the {@link XStringListValue}
	 * of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XStringListValue}
	 * @param string The String which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XStringListValue} which the specified String was
	 *         added to (null if the given {@link XField} had no
	 *         {@link XStringListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XStringListValue addStringToList(XID actorID, XField field, int index,
	        String string) {
		XValue value = field.getValue();
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = listValue.add(index, string);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes a String from the given {@link XStringListValue}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XStringListValue}
	 * @param index The index of the String which is to be removed
	 * @return the new {@link XStringListValue} which the given String was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XStringListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	public static XStringListValue removeStringFromList(XID actorID, XField field, int index) {
		XValue value = field.getValue();
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(index);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes the first occurrence of the given String from the
	 * {@link XStringListValue} of the given {@link XField}
	 * 
	 * @param actorID The {@link XID} of the actor
	 * @param field The {@link XField} containing the {@link XStringListValue}
	 * @param string The String which is to be removed
	 * @return the new {@link XStringListValue} which the given String was
	 *         removed from (null if the given {@link XField} had no
	 *         {@link XStringListValue})
	 */
	public static XStringListValue removeStringFromList(XID actorID, XField field, String string) {
		XValue value = field.getValue();
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = listValue.remove(string);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	// TODO do we still need these URI methods now that we have XAddress?
	// ~daniel
	
	/**
	 * Gets the {@link XModel} with specified URI from the given
	 * {@link XRepository}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = modelURI/objectURI/fieldURI (objectURI & fieldURI not needed for
	 * this method)
	 * 
	 * This method will split up the given URI and try to get the {@link XModel}
	 * with {@link XID} modelURI from the given {@link XRepository}.
	 * 
	 * @param repository The {@link XRepository} which is supposed to hold the
	 *            specified {@link XModel}
	 * @param uri The URI of the {@link XModel}
	 * @return the {@link XModel} corresponding to the given URI, null if it
	 *         doesn't exist
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XModel getModelFromURI(XRepository repository, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 3) {
			throw new URIFormatException("The URI " + uri + " contains too many components.");
		}
		
		try {
			XModel model = safeGetModel(repository, toId(uriArray[0]));
			
			return model;
		} catch(MissingPieceException mpe) {
			return null;
		}
		
	}
	
	/**
	 * Gets the {@link XObject} with specified URI from the given
	 * {@link XRepository}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = modelURI/objectURI/fieldURI (fieldURI not needed for this method)
	 * 
	 * This method will split up the given URI and try to get the
	 * {@link XObject} with {@link XID} objectURI from the {@link XModel} with
	 * {@link XID} modelURI from the the given {@link XRepository}.
	 * 
	 * @param repository The {@link repository} which is supposed to hold the
	 *            specified {@link XModel} and {@link XObject}.
	 * @param uri The URI of the {@link XObject}.
	 * @return the {@link XObject} corresponding to the given URI, null if it
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XObject getObjectFromURI(XRepository repository, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 3) {
			throw new URIFormatException("The URI " + uri + " contains too many components.");
		}
		
		if(uriArray.length < 2) {
			throw new URIFormatException("The URI " + uri + " doesn't contain an ID for an object.");
		}
		
		try {
			XModel model = safeGetModel(repository, toId(uriArray[0]));
			XObject object = safeGetObject(model, toId(uriArray[1]));
			
			return object;
		} catch(MissingPieceException mpe) {
			return null;
		}
		
	}
	
	/**
	 * Gets the {@link XObject} with specified URI from the given {@link XModel}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = objectURI/fieldURI (fieldURI not needed for this method)
	 * 
	 * This method will split up the given URI and try to get the
	 * {@link XObject} with {@link XID} objectURI from the given model.
	 * 
	 * @param model The {@link XModel} which is supposed to hold the given
	 *            {@link XObject}.
	 * @param uri The URI of the object.
	 * @return The {@link XObject} corresponding to the given URI, null if it
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XObject getObjectFromURI(XModel model, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 2) {
			throw new URIFormatException("The URI " + uri + " contains too many components.");
		}
		
		try {
			XObject object = safeGetObject(model, toId(uriArray[0]));
			
			return object;
		} catch(MissingPieceException mpe) {
			return null;
		}
		
	}
	
/**
	 * Gets the {@link XField} with specified URI from the given {@link XRepository}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = modelURI/objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XField} with the
	 * {@link XID} fieldID from the {@link XObject} with {@link XID} objectURI from the {@link XModel} with {@link XID}
	 * modelURI from the given {@link XRepository}.
	 * 
	 * @param repository The {@link XRepository} which is supposed to hold the given
	 *            {@link XField.
	 * @param uri The URI of the {@link XField}.
	 * @return The {@link XField} corresponding to the given URI, null if it doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XField getFieldFromURI(XRepository repository, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 3) {
			throw new URIFormatException("The URI " + uri + " contains too many components.");
		}
		
		if(uriArray.length < 3) {
			if(uriArray.length == 1) {
				throw new URIFormatException("The URI " + uri
				        + " doesn't contain IDs for an object and a field.");
			} else {
				throw new URIFormatException("The URI " + uri
				        + " doesn't contain an ID for a field.");
			}
		}
		
		try {
			XModel model = safeGetModel(repository, toId(uriArray[0]));
			XObject object = safeGetObject(model, toId(uriArray[1]));
			XField field = safeGetField(object, toId(uriArray[2]));
			
			return field;
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the {@link XField} with specified URI from the given {@link XModel}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XField}
	 * with the {@link XID} fieldID from the {@link XObject} with {@link XID}
	 * objectURI from the given {@link XModel}.
	 * 
	 * @param model The {@link XModel} which is supposed to hold the given
	 *            {@link XField}.
	 * @param uri The URI of the {@link XField}.
	 * @return The {@link XField} corresponding to the given URI, null if it
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XField getFieldFromURI(XModel model, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 2) {
			throw new URIFormatException("The given URI contains too many components.");
		}
		
		if(uriArray.length < 2) {
			throw new URIFormatException("The given URI didn't contain an ID for a field.");
		}
		
		try {
			XObject object = safeGetObject(model, toId(uriArray[0]));
			XField field = safeGetField(object, toId(uriArray[1]));
			
			return field;
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the {@link XField} with specified URI from the given {@link XObject}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XField}
	 * with the {@link XID} fieldID from the given {@link XObject}.
	 * 
	 * @param object The {@link XObject} which is supposed to hold the specified
	 *            {@link XField}.
	 * @param uri The URI of the {@link XField}.
	 * @return The {@link XField} with the given {@link XID}, null if it doesn't
	 *         exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XField getFieldFromURI(XObject object, String uri) {
		
		if(uri.contains("/")) {
			throw new URIFormatException("The given URI contains too many components.");
		}
		
		try {
			XField field = safeGetField(object, toId(uri));
			
			return field;
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the {@link XValue} of the {@link XField} with specified URI from the
	 * given {@link XRepository}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = modelURI/objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XValue}
	 * of the {@link XField} with the {@link XID} fieldID from the
	 * {@link XObject} with {@link XID} objectURI from the {@link XModel} with
	 * {@link XID} modelURI from the given {@link XRepository}.
	 * 
	 * @param repository The {@link XRepository} which is supposed to hold the
	 *            specified {@link XValue}.
	 * @param uri The URI of the value.
	 * @return The {@link XValue} of the {@link XField} with the given
	 *         {@link XID}, null if the {@link XField} doesn't exist or if its
	 *         {@link XValue} is not set.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XValue getValueFromURI(XRepository repository, String uri) {
		
		XField field = getFieldFromURI(repository, uri);
		
		if(field == null) {
			return null;
		}
		
		return field.getValue();
	}
	
	/**
	 * Gets the {@link XValue} of the {@link XField} with specified URI from the
	 * given {@link XModel}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XValue}
	 * of the {@link XField} with the {@link XID} fieldID from the
	 * {@link XObject} with {@link XID} objectURI from the given {@link XModel}.
	 * 
	 * @param model The {@link Xmodel} which is supposed to hold the specified
	 *            {@link XValue}.
	 * @param uri The URI of the {@link XValue}.
	 * @return The {@link XValue} of the {@link XField} with the given
	 *         {@link XID}, null if the {@link XField} doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XValue getValueFromURI(XModel model, String uri) {
		
		XField field = getFieldFromURI(model, uri);
		
		if(field == null) {
			return null;
		}
		
		return field.getValue();
	}
	
	/**
	 * Gets the {@link XValue} of the {@link XField} with specified URI from the
	 * given {@link XObject}
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = fieldURI
	 * 
	 * This method will split up the given URI and try to get the {@link XValue}
	 * of the {@link XField} with the {@link XID} fieldID from the given
	 * {@link XObject}.
	 * 
	 * @param object The {@link XObject} which is supposed to hold the specified
	 *            {@link XValue}.
	 * @param uri The URI of the {@link XValue}.
	 * @return The {@link XValue} of the {@link XField} with the given
	 *         {@link XID}, null if the field doesn't exist or if its
	 *         {@link XValue} is not set.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid {@link XID} string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XValue getValueFromURI(XObject object, String uri) {
		
		XField field = getFieldFromURI(object, uri);
		
		if(field == null) {
			return null;
		}
		
		return field.getValue();
	}
	
	/**
	 * Check if two {@link XBaseRepository}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseModel}s as defined by
	 * {@link #equalState(XBaseModel, XBaseModel)}.
	 * 
	 * This is similar to {@link #equalTree(XBaseRepository, XBaseRepository)}
	 * but also checks the revision number.
	 * 
	 * @return true if the two {@link XBaseRepository}s have the same state.
	 */
	public static boolean equalState(XBaseRepository repoA, XBaseRepository repoB) {
		
		if(repoA == null && repoB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(repoA == null || repoB == null) {
			return false;
		}
		
		if(!repoA.getID().equals(repoB.getID())) {
			return false;
		}
		
		for(XID modelId : repoA) {
			
			XBaseModel modelA = repoA.getModel(modelId);
			XBaseModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				return false;
			}
			
			if(!equalState(modelA, modelB)) {
				return false;
			}
			
		}
		
		for(XID modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseModel}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseObject}s as defined by
	 * {@link #equalState(XBaseObject, XBaseObject)}.
	 * 
	 * This is similar to {@link #equalTree(XBaseModel, XBaseModel)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseModel}s have the same state.
	 */
	public static boolean equalState(XBaseModel modelA, XBaseModel modelB) {
		
		if(modelA == null && modelB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(modelA == null || modelB == null) {
			return false;
		}
		
		if(!modelA.getID().equals(modelB.getID())) {
			return false;
		}
		
		if(modelA.getRevisionNumber() != modelB.getRevisionNumber()) {
			return false;
		}
		
		for(XID objectId : modelA) {
			
			XBaseObject objectA = modelA.getObject(objectId);
			XBaseObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				return false;
			}
			
			if(!equalState(objectA, objectB)) {
				return false;
			}
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseObject}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseField}s as defined by
	 * {@link #equalState(XBaseField, XBaseField)}.
	 * 
	 * This is similar to {@link #equalTree(XBaseObject, XBaseObject)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XBaseObject}s have the same state.
	 */
	public static boolean equalState(XBaseObject objectA, XBaseObject objectB) {
		
		if(objectA == null && objectB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(objectA == null || objectB == null) {
			return false;
		}
		
		if(!objectA.getID().equals(objectB.getID())) {
			return false;
		}
		
		if(objectA.getRevisionNumber() != objectB.getRevisionNumber()) {
			return false;
		}
		
		for(XID fieldId : objectA) {
			
			XBaseField fieldA = objectA.getField(fieldId);
			XBaseField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				return false;
			}
			
			if(!equalState(fieldA, fieldB)) {
				return false;
			}
			
		}
		
		for(XID fieldId : objectB) {
			
			if(objectA.getField(fieldId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseField}s have the same ID, the same revision and
	 * the same {@link XValue}.
	 * 
	 * This is similar to {@link #equalTree(XBaseField, XBaseField)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XBaseObject}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XBaseField}s have the same state.
	 */
	public static boolean equalState(XBaseField fieldA, XBaseField fieldB) {
		
		if(fieldA == null && fieldB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(fieldA == null || fieldB == null) {
			return false;
		}
		
		if(!XI.equals(fieldA.getValue(), fieldB.getValue())) {
			return false;
		}
		
		if(!fieldA.getID().equals(fieldB.getID())) {
			return false;
		}
		
		if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseRepository}s have the same {@link XID} and the
	 * same {@link XBaseModel}s as defined by
	 * {@link #equalTree(XBaseModel, XBaseModel)}.
	 * 
	 * This is similar to {@link #equalState(XBaseRepository, XBaseRepository)}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseRepository}s represent the same tree.
	 */
	public static boolean equalTree(XBaseRepository repoA, XBaseRepository repoB) {
		
		if(repoA == null && repoB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(repoA == null || repoB == null) {
			return false;
		}
		
		if(!repoA.getID().equals(repoB.getID())) {
			return false;
		}
		
		for(XID modelId : repoA) {
			
			XBaseModel modelA = repoA.getModel(modelId);
			XBaseModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				return false;
			}
			
			if(!equalTree(modelA, modelB)) {
				return false;
			}
			
		}
		
		for(XID modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseModel}s have the same {@link XID} and the same
	 * {@link XBaseObject}s as defined by
	 * {@link #equalTree(XBaseObject, XBaseObject)}.
	 * 
	 * This is similar to {@link #equalState(XBaseModel, XBaseModel)} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseModel}s represent the same tree.
	 */
	public static boolean equalTree(XBaseModel modelA, XBaseModel modelB) {
		
		if(modelA == null && modelB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(modelA == null || modelB == null) {
			return false;
		}
		
		if(!modelA.getID().equals(modelB.getID())) {
			return false;
		}
		
		for(XID objectId : modelA) {
			
			XBaseObject objectA = modelA.getObject(objectId);
			XBaseObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				return false;
			}
			
			if(!equalTree(objectA, objectB)) {
				return false;
			}
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseObject}s have the same {@link XID} and the same
	 * {@link XBaseField}s as defined by
	 * {@link #equalTree(XBaseField, XBaseField)}.
	 * 
	 * This is similar to {@link #equalState(XBaseObject, XBaseObject)} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseObject}s represent the same subtree.
	 */
	public static boolean equalTree(XBaseObject objectA, XBaseObject objectB) {
		
		if(objectA == null && objectB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(objectA == null || objectB == null) {
			return false;
		}
		
		if(!objectA.getID().equals(objectB.getID())) {
			return false;
		}
		
		for(XID fieldId : objectA) {
			
			XBaseField fieldA = objectA.getField(fieldId);
			XBaseField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				return false;
			}
			
			if(!equalTree(fieldA, fieldB)) {
				return false;
			}
			
		}
		
		for(XID fieldId : objectB) {
			
			if(objectA.getField(fieldId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XBaseField}s have the same {@link XID} and the same
	 * {@link XValue}.
	 * 
	 * This is similar to {@link #equalState(XBaseField, XBaseField)} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseObject}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseField}s represent the same subtree.
	 */
	public static boolean equalTree(XBaseField fieldA, XBaseField fieldB) {
		
		if(fieldA == null && fieldB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(fieldA == null || fieldB == null) {
			return false;
		}
		
		if(!XI.equals(fieldA.getValue(), fieldB.getValue())) {
			return false;
		}
		
		if(!fieldA.getID().equals(fieldB.getID())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XModel} with the
	 * given modelId in the {@link XRepository} referred to by
	 * repositoryAddress.
	 * 
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for a model without a parent is constructed.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveModel(XAddress repositoryAddress, XID modelId) {
		if(repositoryAddress == null) {
			return toAddress(null, modelId, null, null);
		}
		if(repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, null, null);
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XObject} with
	 * the given objectId in the {@link XModel} referred to by modelAddress.
	 * 
	 * @param modelAddress An address of an {@link XModel}. If this is null an
	 *            address for an object without a parent is constructed.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             {@link XModel}
	 */
	public static XAddress resolveObject(XAddress modelAddress, XID objectId) {
		if(modelAddress == null) {
			return toAddress(null, null, objectId, null);
		}
		if(modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, null);
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XField} with the
	 * given fieldId in the {@link XObject} referred to by objectAddress.
	 * 
	 * @param objectAddress An address of an {@link XObject}. If this is null an
	 *            address for a field without a parent is constructed.
	 * @throws IllegalArgumentException if objectAddress doesn't refer to an
	 *             {@link XObject}
	 */
	public static XAddress resolveField(XAddress objectAddress, XID fieldId) {
		if(objectAddress == null) {
			return toAddress(null, null, null, fieldId);
		}
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new IllegalArgumentException(objectAddress + " is not an object address");
		}
		return toAddress(objectAddress.getRepository(), objectAddress.getModel(), objectAddress
		        .getObject(), fieldId);
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XField} with the
	 * given fieldId in the {link XObject} with the given objectId in the
	 * {@link XModel} referred to by modelAddress.
	 * 
	 * @param modelAddress An address of an {@link XModel}. If this is null an
	 *            address for a field without a model is constructed.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             {@link XModel}
	 */
	public static XAddress resolveField(XAddress modelAddress, XID objectId, XID fieldId) {
		if(modelAddress == null) {
			return toAddress(null, null, objectId, fieldId);
		}
		if(modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, fieldId);
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XObject} width
	 * the given objectId in the {@link XModel} with the given modelId in the
	 * {@link XRepository} referred to by repositoryAddress.
	 * 
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for an object without a repository is
	 *            constructed.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveObject(XAddress repositoryAddress, XID modelId, XID objectId) {
		if(repositoryAddress == null) {
			return toAddress(null, modelId, objectId, null);
		}
		if(repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, objectId, null);
	}
	
	/**
	 * Constructs an {@link XAddress} that refers to the {@link XField} with the
	 * given fieldId in the {@link XObject} with the given objectId in the
	 * {@link XModel} with the given modelId in the {@link XRepository} referred
	 * to by repositoryAddress.
	 * 
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for a field without a repository is
	 *            constructed.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveField(XAddress repositoryAddress, XID modelId, XID objectId,
	        XID fieldId) {
		if(repositoryAddress == null) {
			return toAddress(null, modelId, objectId, fieldId);
		}
		if(repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, objectId, fieldId);
	}
	
	/**
	 * Creates an {@link XID} from a given {@link String} using the default
	 * {@link XIDProvider}. The {@link String} must be a valid XML name and may
	 * not contain any ':' characters.
	 * 
	 * @param uriString The String which will be used to create the {@link XID}.
	 * @return a new unique {@link XID} object calculated from the given URI
	 * @throws IllegalArgumentException if the given name is not a valid
	 *             {@link XID} string
	 */
	public static XID toId(String str) {
		return X.getIDProvider().fromString(str);
	}
	
	/**
	 * Creates an {@link XAddress} object from its string representation using
	 * the default {@link XIDProvider}. Valid string representations of
	 * {@link XAddress XAddresses} are "modelId/objectId/fieldId",
	 * "modelId/objectId" and "modelId" where modelId, objectId and fieldId are
	 * valid string representations of {@link XID XIDs} (ie: allowed parameters
	 * for the fromString() method).
	 * 
	 * @param address A string with the described format, never null
	 * @return an new {@link XAddress} object representing the specified address
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid {@link XID} string or if the given String equals
	 *             null
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	public static XAddress toAddress(String str) {
		return X.getIDProvider().fromAddress(str);
	}
	
	/**
	 * Creates a new {@link XAddress} from the given components using the
	 * default {@link XIDProvider}. The {@link XAddress} will have the following
	 * format: repositoryId/modelId/objectId/fieldId
	 * 
	 * Some parameters can be null. An {@link XAddress} can address an
	 * {@link XRepository} (repositoryId set, the rest null) {@link XModel}
	 * (modelId set, repositoryId may be set, rest null), a {@link XObject}
	 * (objectId not null, fieldID null, rest is set), or an {@link XField}
	 * (fieldId not null, rest may or may not be null).
	 * 
	 * @param repositoryId The {@link XID} for the repository field of the
	 *            {@link XAddress}
	 * @param modelId The {@link XID} for the model field of the
	 *            {@link XAddress}
	 * @param objectId The {@link XID} for the object field of the
	 *            {@link XAddress}
	 * @param fieldId The {@link XID} for the field field of the
	 *            {@link XAddress}
	 * @return an {@link XAddress} with the given components.
	 * @throws IllegalArgumentException if the given set of {@link XID XIDs}
	 *             does not fit into one of the patterns described above (for
	 *             example if repositoryId is set, modelId not set and objectId
	 *             is set)
	 */
	public static XAddress toAddress(XID repositoryId, XID modelId, XID objectId, XID fieldId) {
		return X.getIDProvider().fromComponents(repositoryId, modelId, objectId, fieldId);
	}
	
	/**
	 * @return a new random unique {@link XID} created by the default
	 *         {@link XIDProvider}
	 */
	public static XID createUniqueID()

	{
		return X.getIDProvider().createUniqueID();
	}
	
}

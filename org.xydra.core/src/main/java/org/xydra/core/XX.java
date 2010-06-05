package org.xydra.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.change.impl.memory.MemoryTransaction;
import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XType;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaField;
import org.xydra.core.model.delta.DeltaModel;
import org.xydra.core.model.delta.DeltaObject;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XValue;
import org.xydra.index.iterator.SingleValueIterator;


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
		if(field == null)
			throw new MissingPieceException("No field with ID '" + fieldID
			        + "' found in object with ID " + object.getID());
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
		if(model == null)
			throw new MissingPieceException("No model with ID '" + modelID
			        + "' found in repository with ID " + repository.getID());
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
		if(object == null)
			throw new MissingPieceException("No object with ID '" + objectID
			        + "' found in model with ID " + model.getID());
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
				XField field = XX.safeGetField(object, fieldID);
				field.setValue(actorID, X.getValueFactory().createStringValue(stringValue));
			} catch(MissingPieceException mpe) {
				object.createField(actorID, fieldID).setValue(actorID,
				        X.getValueFactory().createStringValue(stringValue));
			}
		}
	}
	
	/**
	 * A method for copying the contents of an array into a list
	 * 
	 * @param <E> The content type
	 * @param array The array which contents will be copied into a list
	 * @return A list containing copies of the contents of the given array
	 */
	private static <E> List<E> asList(E[] array) {
		ArrayList<E> list = new ArrayList<E>();
		
		for(int i = 0; i < array.length; i++) {
			list.add(array[i]);
		}
		
		return list;
	}
	
	/**
	 * Returns the content of the given {@link XIDListValue} as a java.util.list
	 * 
	 * @param listValue The {@link XIDListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<XID> asList(XIDListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given {@link XBooleanListValue} as a
	 * java.util.list
	 * 
	 * @param listValue The {@link XBooleanListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<Boolean> asList(XBooleanListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given {@link XDoubleListValue} as a
	 * java.util.list
	 * 
	 * @param listValue The {@link XDoubleListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<Double> asList(XDoubleListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given {@link XIntegerListValue} as a
	 * java.util.list
	 * 
	 * @param listValue The {@link XIntegerListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<Integer> asList(XIntegerListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given {@link XLongListValue} as a
	 * java.util.list
	 * 
	 * @param listValue The {@link XLongListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<Long> asList(XLongListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given {@link XStringListValue} as a
	 * java.util.list
	 * 
	 * @param listValue The {@link XStringListValue}
	 * @return a copy of the listValue as a java.util.List
	 */
	public static List<String> asList(XStringListValue listValue) {
		return asList(listValue.contents());
	}
	
	/**
	 * Returns the content of the given java.util.list<XID> as an
	 * {@link XIDListValue}
	 * 
	 * @param list The java.util.list<XID> which is to be converted into an
	 *            {@link XIDListValue}
	 * @return an {@link XIDListValue} with the content of the given list
	 */
	public static XIDListValue toIDListValue(List<XID> list) {
		return X.getValueFactory().createIDListValue(list.toArray(new XID[list.size()]));
	}
	
/**
	 * Returns the content of the given java.util.list<Boolean> as an
	 * {@link XBooleanListValue]
	 * 
	 * @param list The java.util.list<Boolean> which is to be converted into an
	 *            {@link XBooleanListValue}
	 * @return an {@link XBooleanListValue} with the content of the given list
	 */
	public static XBooleanListValue toBooleanListValue(List<Boolean> list) {
		return X.getValueFactory().createBooleanListValue(list.toArray(new Boolean[list.size()]));
	}
	
	/**
	 * Returns the content of the given java.util.list<Double> as an
	 * {@link XDoubleListValue}
	 * 
	 * @param list The java.util.list<Double> which is to be converted into an
	 *            {@link XDoubleListValue}
	 * @return an {@link XDoubleListValue} with the content of the given list
	 */
	public static XDoubleListValue toDoubleListValue(List<Double> list) {
		return X.getValueFactory().createDoubleListValue(list.toArray(new Double[list.size()]));
	}
	
	/**
	 * Returns the content of the given java.util.list<Integer> as an
	 * {@link XIntegerListValue}
	 * 
	 * @param list The java.util.list<Integer> which is to be converted into an
	 *            {@link XIntegerListValue}
	 * @return an {@link XIntegerListValue} with the content of the given list
	 */
	public static XIntegerListValue toIntegerListValue(List<Integer> list) {
		return X.getValueFactory().createIntegerListValue(list.toArray(new Integer[list.size()]));
	}
	
	/**
	 * Returns the content of the given java.util.list<Long> as an
	 * {@link XLongListValue}
	 * 
	 * @param list The java.util.list<Long> which is to be converted into an
	 *            {@link XLongListValue}
	 * @return an {@link XLongListValue} with the content of the given list
	 */
	public static XLongListValue toLongListValue(List<Long> list) {
		return X.getValueFactory().createLongListValue(list.toArray(new Long[list.size()]));
	}
	
	/**
	 * Returns the content of the given java.util.list<String> as an
	 * {@link XStringListValue}
	 * 
	 * @param list The java.util.list<String> which is to be converted into an
	 *            {@link XStringListValue}
	 * @return an {@link XStringListValue} with the content of the given list
	 */
	public static XStringListValue toStringListValue(List<String> list) {
		return X.getValueFactory().createStringListValue(list.toArray(new String[list.size()]));
	}
	
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = addIDToList(listValue, listValue.size(), id);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			listValue = addIDToList(listValue, index, id);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds the given {@link XID} to the given {@link XIDListValue} at the
	 * specified index.
	 * 
	 * Note: This will actually create a new {@link XIDListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue The {@link XIDListValue} which the given {@link XID} is
	 *            to be added to.
	 * @param index The index at which the given {@link XID} is to be inserted.
	 * @param id The {@link XID} which is to be added.
	 * @return An {@link XIDListValue} in which the given {@link XID }was
	 *         inserted at the specified index.
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	private static XIDListValue addIDToList(XIDListValue listValue, int index, XID id) {
		List<XID> list = XX.asList(listValue);
		list.add(index, id);
		
		return toIDListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			List<XID> list = XX.asList(listValue);
			list.remove(id);
			
			listValue = toIDListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIDListValue) {
			XIDListValue listValue = (XIDListValue)value;
			
			// manipulate the contained list
			List<XID> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toIDListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = addBooleanToList(listValue, listValue.size(), bool);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			listValue = addBooleanToList(listValue, index, bool);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new boolean at the specified index to the given
	 * {@link XBooleanListValue}
	 * 
	 * Note: This will actually create a new {@link XBooleanListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue the {@link XBooleanListValue}
	 * @param bool The boolean which is to be added
	 * @param index The index at which the specified element is to be inserted
	 * @return the new {@link XBooleanListValue} which the given Boolean was
	 *         added to
	 */
	private static XBooleanListValue addBooleanToList(XBooleanListValue listValue, int index,
	        boolean bool) {
		List<Boolean> list = XX.asList(listValue);
		list.add(index, bool);
		
		return toBooleanListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			List<Boolean> list = XX.asList(listValue);
			list.remove(bool);
			
			listValue = toBooleanListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XBooleanListValue) {
			XBooleanListValue listValue = (XBooleanListValue)value;
			
			// manipulate the contained list
			List<Boolean> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toBooleanListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = addDoubleToList(listValue, listValue.size(), doub);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			listValue = addDoubleToList(listValue, index, doub);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Double at the specified index to the given
	 * {@link XDoubleListValue}
	 * 
	 * Note: This will actually create a new {@link XDoubleListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue The {@link XDoubleListValue}
	 * @param index The index at which the specified element is to be added
	 * @param doub The Double which is to be added
	 * @return the new {@link XDoubleListValue} which the specified Double was
	 *         added to
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	private static XDoubleListValue addDoubleToList(XDoubleListValue listValue, int index,
	        double doub) {
		List<Double> list = XX.asList(listValue);
		list.add(index, doub);
		
		return toDoubleListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			List<Double> list = XX.asList(listValue);
			list.remove(doubleVal);
			
			listValue = toDoubleListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XDoubleListValue) {
			XDoubleListValue listValue = (XDoubleListValue)value;
			
			// manipulate the contained list
			List<Double> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toDoubleListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = addIntegerToList(listValue, listValue.size(), integer);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			listValue = addIntegerToList(listValue, index, integer);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Integer at the specified index to the given
	 * {@link XIntegerListValue}
	 * 
	 * Note: This will actually create a new {@link XIntegerListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue The {@link XIntegerListValue}
	 * @param integer The Integer which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XIntegerListValue} which the specified Integer was
	 *         added to
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	
	private static XIntegerListValue addIntegerToList(XIntegerListValue listValue, int index,
	        int integer) {
		List<Integer> list = XX.asList(listValue);
		list.add(index, integer);
		
		return toIntegerListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			List<Integer> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toIntegerListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XIntegerListValue listValue = (XIntegerListValue)value;
			
			// manipulate the contained list
			List<Integer> list = XX.asList(listValue);
			list.remove(integer);
			
			listValue = toIntegerListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = addLongToList(listValue, listValue.size(), longVal);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XIntegerListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			listValue = addLongToList(listValue, index, longVal);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new Long at specified index to the given {@link XLongListValue}
	 * 
	 * Note: This will actually create a new {@link XLongListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue The {@link XLongListValue}
	 * @param longVal The Long which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XLongListValue} which the specified Long was added
	 *         to (null if the given field had no {@link XLongListValue})
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	private static XLongListValue addLongToList(XLongListValue listValue, int index, long longVal) {
		List<Long> list = XX.asList(listValue);
		list.add(index, longVal);
		
		return toLongListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XLongListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			List<Long> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toLongListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XLongListValue) {
			XLongListValue listValue = (XLongListValue)value;
			
			// manipulate the contained list
			List<Long> list = XX.asList(listValue);
			list.remove(longVal);
			
			listValue = toLongListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = addStringToList(listValue, listValue.size(), string);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			listValue = addStringToList(listValue, index, string);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a new String at the specified index to the given
	 * {@link XStringListValue}
	 * 
	 * Note: This will actually create a new {@link XStringListValue} with the
	 * content of the given list value plus the new element, the element will
	 * not be added directly to the given list value.
	 * 
	 * @param listValue The {@link XStringListValue}
	 * @param string The String which is to be added
	 * @param index The index at which the specified element is to be added
	 * @return the new {@link XStringListValue} which the specified String was
	 *         added to (null if the given field had no {@link XStringListValue}
	 *         )
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0
	 *             || index >= size())
	 */
	private static XStringListValue addStringToList(XStringListValue listValue, int index,
	        String string) {
		List<String> list = XX.asList(listValue);
		list.add(index, string);
		
		return toStringListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			List<String> list = XX.asList(listValue);
			list.remove(index);
			
			listValue = toStringListValue(list);
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
		
		if(value == null) {
			return null;
		}
		
		if(value instanceof XStringListValue) {
			XStringListValue listValue = (XStringListValue)value;
			
			// manipulate the contained list
			List<String> list = XX.asList(listValue);
			list.remove(string);
			
			listValue = toStringListValue(list);
			field.setValue(actorID, listValue);
			return listValue;
		} else {
			return null;
		}
	}
	
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
			throw new URIFormatException("The given URI contains too many components.");
		}
		
		try {
			XModel model = safeGetModel(repository, X.getIDProvider().fromString(uriArray[0]));
			
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
			throw new URIFormatException("The given URI contains too many components.");
		}
		
		try {
			XModel model = safeGetModel(repository, X.getIDProvider().fromString(uriArray[0]));
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[1]));
			
			return object;
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			assert (uriArray.length == 1);
			throw new URIFormatException("The given URI didn't contain an ID for an object.");
			
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
		
		if(uriArray.length > 2)
			throw new URIFormatException("The given URI contains too many components.");
		try {
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[0]));
			
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
			throw new URIFormatException("The given URI contains too many components.");
		}
		
		try {
			XModel model = safeGetModel(repository, X.getIDProvider().fromString(uriArray[0]));
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[1]));
			XField field = safeGetField(object, X.getIDProvider().fromString(uriArray[2]));
			
			return field;
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			if(uriArray.length == 1) {
				throw new URIFormatException(
				        "The given URI didn't contain IDs for an object and a field.");
			} else {
				// this can only happen if uriArray[2] doesn't exist
				throw new URIFormatException("The given URI didn't contain an ID for a field.");
			}
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
		
		try {
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[0]));
			XField field = safeGetField(object, X.getIDProvider().fromString(uriArray[1]));
			
			return field;
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			// this can only happen if uriArray[1] doesn't exist
			throw new URIFormatException("The given URI didn't contain an ID for a field.");
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the field with specified URI from the given XObject
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = fieldURI
	 * 
	 * This method will split up the given URI and try to get the field with the
	 * ID fieldID from the given object.
	 * 
	 * @param object The object which is supposed to hold the given field.
	 * @param uri The URI of the field.
	 * @return The field with the given ID. null if it doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid XID string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	public static XField getFieldFromURI(XObject object, String uri) {
		
		if(uri.contains("/"))
			throw new URIFormatException("The given URI contains too many components.");
		try {
			XField field = safeGetField(object, X.getIDProvider().fromString(uri));
			
			return field;
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the value of the field with specified URI from the given XRepository
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = modelURI/objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the value of the
	 * field with the ID fieldID from the object with ID objectURI from the
	 * model with ID modelURI from the given repository.
	 * 
	 * @param repository The repository which is supposed to hold the given
	 *            value.
	 * @param uri The URI of the value.
	 * @return The value of the field with the given ID. null if the field
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid XID string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	
	public static XValue getValueFromURI(XRepository repository, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 3)
			throw new URIFormatException("The given URI contains too many components.");
		try {
			XModel model = safeGetModel(repository, X.getIDProvider().fromString(uriArray[0]));
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[1]));
			XField field = safeGetField(object, X.getIDProvider().fromString(uriArray[2]));
			
			return field.getValue();
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			if(uriArray.length == 1) {
				throw new URIFormatException(
				        "The given URI didn't contain IDs for an object and a field.");
			} else {
				// this can only happen if uriArray[2] doesn't exist
				throw new URIFormatException("The given URI didn't contain an ID for a field");
			}
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the value of the field with specified URI from the given XModel
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = objectURI/fieldURI
	 * 
	 * This method will split up the given URI and try to get the value of the
	 * field with the ID fieldID from the object with ID objectURI from the
	 * given model.
	 * 
	 * @param model The model which is supposed to hold the given value.
	 * @param uri The URI of the value.
	 * @return The value of the field with the given ID. null if the field
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid XID string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	
	public static XValue getValueFromURI(XModel model, String uri) {
		String[] uriArray = uri.split("/");
		
		if(uriArray.length > 2)
			throw new URIFormatException("The given URI contains too many components.");
		try {
			XObject object = safeGetObject(model, X.getIDProvider().fromString(uriArray[0]));
			XField field = safeGetField(object, X.getIDProvider().fromString(uriArray[1]));
			
			return field.getValue();
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			// this can only happen if uriArray[1] doesn't exist
			throw new URIFormatException("The given URI didn't contain an ID for a field.");
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Gets the value of the field with specified URI from the given XObject
	 * 
	 * A correct URI for this method looks like this:
	 * 
	 * URI = fieldURI
	 * 
	 * This method will split up the given URI and try to get the value of field
	 * with the ID fieldID from the given object.
	 * 
	 * @param object The object which is supposed to hold the given value.
	 * @param uri The URI of the value.
	 * @return The value of the field with the given ID. null if the field
	 *         doesn't exist.
	 * @throws IllegalArgumentException if any component of the given URI is not
	 *             a valid XID string (see
	 *             {@link XIDProvider#fromString(String)} for further
	 *             information)
	 * @throws URIFormatException if the given URI contains too many or too few
	 *             components
	 */
	
	public static XField getValueFromURI(XObject object, String uri) {
		if(uri.contains("/"))
			throw new URIFormatException("The given URI contains too many components.");
		try {
			XField field = safeGetField(object, X.getIDProvider().fromString(uri));
			
			return field;
		} catch(MissingPieceException mpe) {
			return null;
		}
	}
	
	/**
	 * Check if two models have the same ID, the same revision and the same
	 * objects as defined by {@link XX#equalState(XObject, XObject)}.
	 * 
	 * This is similar to {@link XX#equalTree(XModel, XModel)} but also checks
	 * the revision number.
	 * 
	 * Parents, if any, are not compared.
	 * 
	 * @return true if the two models have the same state.
	 */
	public static boolean equalState(XModel modelA, XModel modelB) {
		if(modelA == null && modelB == null)
			return true;
		
		// one of them is null, the other isn't
		if(modelA == null || modelB == null)
			return false;
		
		if(!modelA.getID().equals(modelB.getID()))
			return false;
		
		if(modelA.getRevisionNumber() != modelB.getRevisionNumber())
			return false;
		
		for(XID objectId : modelA) {
			
			XObject objectA = modelA.getObject(objectId);
			XObject objectB = modelB.getObject(objectId);
			
			if(objectB == null)
				return false;
			
			if(!equalState(objectA, objectB))
				return false;
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null)
				return false;
			
		}
		
		return true;
	}
	
	/**
	 * Check if two objects have the same ID, the same revision and the same
	 * fields as defined by {@link XX#equalState(XField, XField)}.
	 * 
	 * This is similar to {@link XX#equalTree(XObject, XObject)} but also checks
	 * the revision number.
	 * 
	 * @return true if the two objects have the same state.
	 */
	public static boolean equalState(XObject objectA, XObject objectB) {
		
		if(!objectA.getID().equals(objectB.getID()))
			return false;
		
		if(objectA.getRevisionNumber() != objectB.getRevisionNumber())
			return false;
		
		for(XID fieldId : objectA) {
			
			XField fieldA = objectA.getField(fieldId);
			XField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null)
				return false;
			
			if(!equalState(fieldA, fieldB))
				return false;
			
		}
		
		for(XID fieldId : objectB) {
			
			if(objectA.getField(fieldId) == null)
				return false;
			
		}
		
		return true;
	}
	
	/**
	 * Check if two fields have the same ID, the same revision and the same
	 * value.
	 * 
	 * This is similar to {@link XX#equalTree(XField, XField)} but also checks
	 * the revision number.
	 * 
	 * @return true if the two fields have the same state.
	 */
	public static boolean equalState(XField fieldA, XField fieldB) {
		
		if(!equals(fieldA.getValue(), fieldB.getValue()))
			return false;
		
		if(!fieldA.getID().equals(fieldB.getID()))
			return false;
		
		if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber())
			return false;
		
		return true;
	}
	
	/**
	 * Check if two models have the same ID and the same objects as defined by
	 * {@link XX#equalTree(XObject, XObject)}.
	 * 
	 * This is similar to {@link XX#equalState(XModel, XModel)} but ignores the
	 * revision number.
	 * 
	 * @return true if the two models represent the same tree.
	 */
	public static boolean equalTree(XModel modelA, XModel modelB) {
		
		if(!modelA.getID().equals(modelB.getID()))
			return false;
		
		for(XID objectId : modelA) {
			
			XObject objectA = modelA.getObject(objectId);
			XObject objectB = modelB.getObject(objectId);
			
			if(objectB == null)
				return false;
			
			if(!equalTree(objectA, objectB))
				return false;
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null)
				return false;
			
		}
		
		return true;
	}
	
	/**
	 * Check if two objects have the same ID and the same fields as defined by
	 * {@link XX#equalTree(XField, XField)}.
	 * 
	 * This is similar to {@link XX#equalState(XObject, XObject)} but ignores
	 * the revision number.
	 * 
	 * @return true if the two objects represent the same subtree.
	 */
	public static boolean equalTree(XObject objectA, XObject objectB) {
		
		if(!objectA.getID().equals(objectB.getID()))
			return false;
		
		for(XID fieldId : objectA) {
			
			XField fieldA = objectA.getField(fieldId);
			XField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null)
				return false;
			
			if(!equalTree(fieldA, fieldB))
				return false;
			
		}
		
		for(XID fieldId : objectB) {
			
			if(objectA.getField(fieldId) == null)
				return false;
			
		}
		
		return true;
	}
	
	/**
	 * Check if two fields have the same ID and the same value.
	 * 
	 * This is similar to {@link XX#equalState(XField, XField)} but ignores the
	 * revision number.
	 * 
	 * @return true if the two fields represent the same subtree.
	 */
	public static boolean equalTree(XField fieldA, XField fieldB) {
		
		if(!equals(fieldA.getValue(), fieldB.getValue()))
			return false;
		
		if(!fieldA.getID().equals(fieldB.getID()))
			return false;
		
		return true;
	}
	
	/**
	 * @return true if either both a and b are null or both are not null and
	 *         a.equals(b) is true
	 */
	public static boolean equals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
	
	/**
	 * @return true of descendant refers to an entity contained in the entity
	 *         referred to by parent.
	 */
	public static boolean contains(XAddress parent, XAddress descendant) {
		if(parent.getRepository() == null) {
			if(!XX.equals(parent.getRepository(), descendant.getRepository()))
				return false;
			if(parent.getModel() == null) {
				if(!XX.equals(parent.getModel(), descendant.getModel()))
					return false;
				if(parent.getObject() == null) {
					return false;
				} else {
					if(!parent.getObject().equals(descendant.getObject()))
						return false;
					if(parent.getField() == null)
						return descendant.getField() != null;
					return false;
				}
			} else {
				if(!parent.getModel().equals(descendant.getModel()))
					return false;
				if(parent.getObject() == null)
					return descendant.getObject() != null;
				if(!parent.getObject().equals(descendant.getObject()))
					return false;
				if(parent.getField() == null)
					return descendant.getField() != null;
				return false;
			}
		} else {
			if(!parent.getRepository().equals(descendant.getRepository()))
				return false;
			if(parent.getModel() == null)
				return descendant.getModel() != null;
			if(!parent.getModel().equals(descendant.getModel()))
				return false;
			if(parent.getObject() == null)
				return descendant.getObject() != null;
			if(!parent.getObject().equals(descendant.getObject()))
				return false;
			if(parent.getField() == null)
				return descendant.getField() != null;
			return false;
		}
		
	}
	
	/**
	 * @return true of descendant refers to an entity contained in the entity
	 *         referred to by parent or of descendant and parent refer to the
	 *         same entity.
	 */
	public static boolean equalsOrContains(XAddress parent, XAddress descendant) {
		if(parent.getRepository() == null) {
			if(descendant.getRepository() != null)
				return false;
			if(parent.getModel() == null) {
				if(descendant.getModel() != null)
					return false;
				if(parent.getObject() == null) {
					if(descendant.getObject() != null)
						return false;
					return XX.equals(parent.getField(), descendant.getField());
				} else {
					if(!parent.getObject().equals(descendant.getObject()))
						return false;
					if(parent.getField() == null)
						return true;
					return parent.getField().equals(descendant.getField());
				}
			} else {
				if(!parent.getModel().equals(descendant.getModel()))
					return false;
				if(parent.getObject() == null)
					return true;
				if(!parent.getObject().equals(descendant.getObject()))
					return false;
				if(parent.getField() == null)
					return true;
				return parent.getField().equals(descendant.getField());
			}
		} else {
			if(!parent.getRepository().equals(descendant.getRepository()))
				return false;
			if(parent.getModel() == null)
				return true;
			if(!parent.getModel().equals(descendant.getModel()))
				return false;
			if(parent.getObject() == null)
				return true;
			if(!parent.getObject().equals(descendant.getObject()))
				return false;
			if(parent.getField() == null)
				return true;
			return parent.getField().equals(descendant.getField());
		}
		
	}
	
	/**
	 * @return true of child refers to an entity directly contained in the
	 *         entity referred to by parent.
	 */
	public static boolean isChild(XAddress parent, XAddress child) {
		if(parent.getRepository() == null) {
			if(!XX.equals(parent.getRepository(), child.getRepository()))
				return false;
			if(parent.getModel() == null) {
				if(!XX.equals(parent.getModel(), child.getModel()))
					return false;
				if(parent.getObject() == null) {
					return false;
				} else {
					if(!parent.getObject().equals(child.getObject()))
						return false;
					if(parent.getField() == null)
						return child.getField() != null;
					return false;
				}
			} else {
				if(!parent.getModel().equals(child.getModel()))
					return false;
				if(parent.getObject() == null)
					return child.getObject() != null && child.getField() == null;
				if(!parent.getObject().equals(child.getObject()))
					return false;
				if(parent.getField() == null)
					return child.getField() != null;
				return false;
			}
		} else {
			if(!parent.getRepository().equals(child.getRepository()))
				return false;
			if(parent.getModel() == null)
				return child.getModel() != null && child.getObject() == null;
			if(!parent.getModel().equals(child.getModel()))
				return false;
			if(parent.getObject() == null)
				return child.getObject() != null && child.getField() == null;
			if(!parent.getObject().equals(child.getObject()))
				return false;
			if(parent.getField() == null)
				return child.getField() != null;
			return false;
		}
		
	}
	
	public static XAddress resolveModel(XAddress repositoryAddress, XID modelId) {
		if(repositoryAddress == null)
			return X.getIDProvider().fromComponents(null, modelId, null, null);
		if(MemoryAddress.getAddressedType(repositoryAddress) != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return X.getIDProvider().fromComponents(repositoryAddress.getRepository(), modelId, null,
		        null);
	}
	
	public static XAddress resolveObject(XAddress modelAddress, XID objectId) {
		if(modelAddress == null)
			return X.getIDProvider().fromComponents(null, null, objectId, null);
		if(MemoryAddress.getAddressedType(modelAddress) != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return X.getIDProvider().fromComponents(modelAddress.getRepository(),
		        modelAddress.getModel(), objectId, null);
	}
	
	/**
	 * @param objectAddress may be null, if not null must be a valid object
	 *            address
	 * @param fieldId never null
	 * @return
	 */
	public static XAddress resolveField(XAddress objectAddress, XID fieldId) {
		if(objectAddress == null)
			return X.getIDProvider().fromComponents(null, null, null, fieldId);
		
		if(MemoryAddress.getAddressedType(objectAddress) != XType.XOBJECT) {
			throw new IllegalArgumentException(objectAddress
			        + " is not an object address. Field should be null.");
		}
		return X.getIDProvider().fromComponents(objectAddress.getRepository(),
		        objectAddress.getModel(), objectAddress.getObject(), fieldId);
	}
	
	/**
	 * Create a forced {@link XCommand} that undoes the given {@link XEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the target it
	 * operates upon no longer exists.
	 */
	static public XCommand createForcedUndoCommand(XEvent event) {
		
		if(event instanceof XAtomicEvent)
			return createForcedUndoCommand((XAtomicEvent)event);
		if(event instanceof XTransactionEvent)
			return createForcedUndoCommand((XTransactionEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a forced {@link XAtomicCommand} that undoes the given
	 * {@link XAtomicEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the target it
	 * operates upon no longer exists.
	 */
	static public XAtomicCommand createForcedUndoCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createForcedUndoCommand((XFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createForcedUndoCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createForcedUndoCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createForcedUndoCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a forced {@link XTransaction} that undoes the given
	 * {@link XTransactionEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the targets it
	 * or only of the contained commands operate upon no longer exist.
	 */
	static public XTransaction createForcedUndoCommand(XTransactionEvent event) {
		
		XAtomicCommand[] result = new XAtomicCommand[event.size()];
		
		for(int i = 0, j = event.size() - 1; j >= 0; i++, j--) {
			result[i] = createForcedUndoCommand(event.getEvent(j));
		}
		
		return MemoryTransaction.createTransaction(event.getTarget(), result);
	}
	
	/**
	 * Create a forced {@link XFieldCommand} that undoes the given
	 * {@link XFieldEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the field it
	 * operates upon no longer exists.
	 */
	static public XFieldCommand createForcedUndoCommand(XFieldEvent event) {
		
		switch(event.getChangeType()) {
		
		case REMOVE:
			return MemoryFieldCommand.createAddCommand(event.getTarget(), XCommand.FORCED, event
			        .getOldValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(), XCommand.FORCED, event
			        .getOldValue());
			
		case ADD:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED);
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XObjectCommand} that undoes the given
	 * {@link XObjectEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the object it
	 * operates upon no longer exists.
	 */
	static public XObjectCommand createForcedUndoCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.FORCED, event
			        .getFieldID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for object events: "
			        + event.getChangeType();
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED,
			        event.getFieldID());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XModelCommand} that undoes the given
	 * {@link XModelEvent}.
	 * 
	 * Beware that the given command may still fail to apply if the model it
	 * operates upon no longer exists.
	 */
	static public XModelCommand createForcedUndoCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.FORCED, event
			        .getObjectID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for model events: "
			        + event.getChangeType();
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED, event
			        .getObjectID());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent}.
	 */
	static public XRepositoryCommand createForcedUndoCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.FORCED,
			        event.getModelID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED,
			        event.getRepositoryID());
			
		}
		
	}
	
	/**
	 * Create a {@link XAtomicCommand} that undoes the given
	 * {@link XAtomicEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 */
	static public XAtomicCommand createImmediateUndoCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createImmediateUndoCommand((XFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createImmediateUndoCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createImmediateUndoCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createImmediateUndoCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XFieldCommand} that undoes the given {@link XFieldEvent}
	 * but will fail if there have been any conflicting changes since then, even
	 * if they have also been undone as the revision number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 */
	static public XFieldCommand createImmediateUndoCommand(XFieldEvent event) {
		
		long newRev = event.getModelRevisionNumber() + 1;
		
		switch(event.getChangeType()) {
		
		case REMOVE:
			return MemoryFieldCommand.createAddCommand(event.getTarget(), newRev, event
			        .getOldValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(), newRev, event
			        .getOldValue());
			
		case ADD:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(), newRev);
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a {@link XObjectCommand} that undoes the given
	 * {@link XObjectEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 */
	static public XObjectCommand createImmediateUndoCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getFieldID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for object events: "
			        + event.getChangeType();
			
			long newRev = event.getModelRevisionNumber() + 1;
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(), newRev, event
			        .getFieldID());
			
		}
		
	}
	
	/**
	 * Create a {@link XModelCommand} that undoes the given {@link XModelEvent}
	 * but will fail if there have been any conflicting changes since then, even
	 * if they have also been undone as the revision number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 */
	static public XModelCommand createImmediateUndoCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getObjectID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for model events: "
			        + event.getChangeType();
			
			long newRev = event.getModelRevisionNumber() + 1;
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(), newRev, event
			        .getObjectID());
			
		}
		
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 */
	static public XRepositoryCommand createImmediateUndoCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getModelID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), 0, event
			        .getRepositoryID());
			
		}
		
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent} but will fail if there have been any conflicting
	 * changes since then that have not been undone already.
	 * 
	 * The relevant parts of the given {@link XRepository} must be in the same
	 * state as they where directly after the event, only the revision numbers
	 * may differ.
	 * 
	 * @throws IllegalStateException if the given {@link XRepository} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XRepository} doesn't
	 *             contain the target of the event
	 */
	static public XRepositoryCommand createUndoCommand(XRepository repo, XRepositoryEvent event) {
		
		if(!repo.getAddress().equals(event.getTarget()))
			throw new IllegalArgumentException("repository and event don't match");
		
		XID modelId = event.getTarget().getModel();
		
		assert modelId != null;
		
		XModel model = repo.getModel(modelId);
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			if(model != null) {
				throw new IllegalStateException("model already exists, cannot undo " + event);
			}
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getModelID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			if(model == null) {
				throw new IllegalStateException("model no longer exists, cannot undo " + event);
			}
			
			if(!model.isEmpty()) {
				throw new IllegalStateException("model should be empty, cannot undo " + event);
			}
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), event
			        .getModelRevisionNumber(), event.getRepositoryID());
			
		}
		
	}
	
	/**
	 * Create a {@link XCommand} that undoes the given {@link XEvent} but will
	 * fail if there have been any conflicting changes since then that have not
	 * been undone already.
	 * 
	 * The relevant parts of the given {@link XModel} must be in the same state
	 * as they where directly after the event, only the revision numbers may
	 * differ.
	 * 
	 * @throws IllegalStateException if the given {@link XModel} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the given event or the event is an
	 *             {@link XRepositoryEvent}
	 */
	public static XCommand createUndoCommand(XBaseModel base, XEvent event) {
		return createUndoCommand(base, new SingleValueIterator<XEvent>(event));
	}
	
	/**
	 * Create a {@link XCommand} that undoes the given {@link XEvent}s in the
	 * order provided by the iterator but will fail if there have been any
	 * conflicting changes since then that have not been undone already.
	 * 
	 * The relevant parts of the given {@link XModel} must be in the same state
	 * as they where directly after the event, only the revision numbers may
	 * differ.
	 * 
	 * @throws IllegalStateException if the given {@link XModel} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of any of the events or if any of the
	 *             events is a {@link XRepositoryEvent}
	 */
	public static XCommand createUndoCommand(XBaseModel base, Iterator<XEvent> events) {
		
		ChangedModel model = new ChangedModel(base);
		
		while(events.hasNext()) {
			XEvent event = events.next();
			
			if(event instanceof XTransactionEvent) {
				createUndoChanges(model, (XTransactionEvent)event);
			} else if(event instanceof XAtomicEvent) {
				createUndoChanges(model, (XAtomicEvent)event);
			} else {
				assert false : "events are either transactions or atomic";
			}
			
		}
		
		XTransactionBuilder builder = new XTransactionBuilder(base.getAddress());
		
		builder.applyChanges(model);
		
		if(builder.isEmpty()) {
			// no changes needed
			return null;
		}
		
		return builder.buildCommand();
	}
	
	/**
	 * Undo the changes represented by the given {@link XTransaction} on the
	 * given delta model.
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of any of the events
	 */
	public static void createUndoChanges(DeltaModel model, XTransactionEvent trans) {
		for(int i = trans.size(); i >= 0; i--) {
			createUndoChanges(model, trans.getEvent(i));
		}
	}
	
	/**
	 * Undo the changes represented by the given {@link XAtomicEvent} on the
	 * given delta model.
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event or if the events is a
	 *             {@link XRepositoryEvent}
	 */
	public static void createUndoChanges(DeltaModel model, XAtomicEvent event) {
		if(event instanceof XModelEvent) {
			createUndoChanges(model, (XModelEvent)event);
			return;
		} else if(event instanceof XObjectEvent) {
			createUndoChanges(model, (XObjectEvent)event);
			return;
		} else if(event instanceof XFieldEvent) {
			createUndoChanges(model, (XFieldEvent)event);
			return;
		} else if(event instanceof XRepositoryEvent) {
			throw new IllegalArgumentException("need repository to undo repository changes");
		}
		throw new AssertionError("unknown event class: " + event);
	}
	
	/**
	 * Undo the changes represented by the given {@link XModelEvent} on the
	 * given delta model.
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void createUndoChanges(DeltaModel model, XModelEvent event) {
		
		if(!XX.equals(model.getAddress(), event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XID objectId = event.getObjectID();
		
		switch(event.getChangeType()) {
		
		case ADD:
			if(!model.hasObject(objectId)) {
				throw new IllegalStateException();
			}
			model.removeObject(objectId);
			break;
		
		case REMOVE:
			if(model.hasObject(objectId)) {
				throw new IllegalStateException();
			}
			model.createObject(objectId);
			break;
		
		default:
			throw new AssertionError("impossible type for model commands");
		}
		
	}
	
	/**
	 * Undo the changes represented by the given {@link XObjectEvent} on the
	 * given delta model.
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void createUndoChanges(DeltaModel model, XObjectEvent event) {
		
		if(!XX.contains(model.getAddress(), event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XID objectId = event.getObjectID();
		DeltaObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalStateException();
		}
		
		XID fieldId = event.getFieldID();
		
		switch(event.getChangeType()) {
		
		case ADD:
			if(!object.hasField(fieldId)) {
				throw new IllegalStateException();
			}
			object.removeField(fieldId);
			break;
		
		case REMOVE:
			if(object.hasField(fieldId)) {
				throw new IllegalStateException();
			}
			object.createField(fieldId);
			break;
		
		default:
			throw new AssertionError("impossible type for object commands");
		}
		
	}
	
	/**
	 * Undo the changes represented by the given {@link XFieldEvent} on the
	 * given delta model.
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void createUndoChanges(DeltaModel model, XFieldEvent event) {
		
		if(!XX.contains(model.getAddress(), event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XID objectId = event.getObjectID();
		DeltaObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalStateException();
		}
		
		XID fieldId = event.getFieldID();
		DeltaField field = object.getField(fieldId);
		if(field == null) {
			throw new IllegalStateException();
		}
		
		if(!XX.equals(field.getValue(), event.getNewValue())) {
			throw new IllegalStateException();
		}
		
		field.setValue(event.getOldValue());
	}
	
	/**
	 * Create a {@link XCommand} that would have caused the given {@link XEvent}
	 */
	static public XCommand createReplayCommand(XEvent event) {
		
		if(event instanceof XAtomicEvent)
			return createReplayCommand((XAtomicEvent)event);
		if(event instanceof XTransactionEvent)
			return createReplayCommand((XTransactionEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XAtomicCommand} that would have caused the given
	 * {@link XAtomicEvent}
	 */
	static public XAtomicCommand createReplayCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createReplayCommand((XFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createReplayCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createReplayCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createReplayCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XTransaction} that would have caused the given
	 * {@link XTransactionEvent}
	 */
	static public XTransaction createReplayCommand(XTransactionEvent event) {
		
		XAtomicCommand[] result = new XAtomicCommand[event.size()];
		
		for(int i = 0; i <= event.size(); i++) {
			result[i] = createReplayCommand(event.getEvent(i));
		}
		
		return MemoryTransaction.createTransaction(event.getTarget(), result);
	}
	
	/**
	 * Create a {@link XFieldCommand} that would have caused the given
	 * {@link XFieldEvent}
	 */
	static public XFieldCommand createReplayCommand(XFieldEvent event) {
		
		switch(event.getChangeType()) {
		
		case ADD:
			return MemoryFieldCommand.createAddCommand(event.getTarget(), event
			        .getFieldRevisionNumber(), event.getNewValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(), event
			        .getFieldRevisionNumber(), event.getNewValue());
			
		case REMOVE:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(), event
			        .getFieldRevisionNumber());
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a {@link XObjectCommand} that would have caused the given
	 * {@link XObjectEvent}
	 */
	static public XObjectCommand createReplayCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getFieldID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for object events: "
			        + event.getChangeType();
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(), event
			        .getFieldRevisionNumber(), event.getFieldID());
			
		}
		
	}
	
	/**
	 * Create a {@link XModelCommand} that would have caused the given
	 * {@link XModelEvent}
	 */
	static public XModelCommand createReplayCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getObjectID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for model events: "
			        + event.getChangeType();
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(), event
			        .getObjectRevisionNumber(), event.getObjectID());
			
		}
		
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that would have caused the given
	 * {@link XRepositoryEvent}
	 */
	static public XRepositoryCommand createReplayCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE, event
			        .getModelID());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), event
			        .getModelRevisionNumber(), event.getRepositoryID());
			
		}
		
	}
	
}

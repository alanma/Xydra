package org.xydra.core;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * A utility class that uses {@link X} to provide convenience methods for
 * working with/editing Xydra Instances.
 */
public class CoreUtils {
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldId' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectId'
	 * @param objectId The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldId'
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject}/{@link XField} doesn't exist
	 */
	// 2010-10-27: used only in this class
	public static XField safeGetField(XModel model, XID objectId, XID fieldId) {
		XObject object = safeGetObject(model, objectId);
		return safeGetField(object, fieldId);
	}
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldId' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * 
	 * @param object The {@link XObject} which should contain the {@link XField}
	 *            specified by 'fieldId'
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XField} doesn't exist
	 */
	// 2010-10-27: used in this class + tests
	public static XField safeGetField(XObject object, XID fieldId) throws MissingPieceException {
		XField field = object.getField(fieldId);
		if(field == null) {
			throw new MissingPieceException("No field with ID '" + fieldId
			        + "' found in object with ID " + object.getId());
		}
		return field;
	}
	
	/**
	 * Tries to get the {@link XField} with {@link XID} 'fieldId' from the given
	 * {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelId'
	 * @param modelId The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectId'
	 * @param objectId The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldId'
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject}/{{@link XField} doesn't exist
	 */
	// 2010-10-27: used nowhere
	public static XField safeGetField(XRepository repository, XID modelId, XID objectId, XID fieldId) {
		XModel model = safeGetModel(repository, modelId);
		return safeGetField(model, objectId, fieldId);
	}
	
	/**
	 * Tries to get the {@link XModel} with {@link XID} 'modelId' from the given
	 * {@link XRepository}. If the specified model is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelId'
	 * @param modelId The {@link XID} of the {@link XModel}
	 * @return The specified {{@link XModel}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel} doesn't exist
	 */
	// 2010-10-27: used in this class + tests
	public static XModel safeGetModel(XRepository repository, XID modelId) {
		XModel model = repository.getModel(modelId);
		if(model == null) {
			throw new MissingPieceException("No model with ID '" + modelId
			        + "' found in repository with ID " + repository.getId());
		}
		return model;
	}
	
	/**
	 * Tries to get the {@link XObject} with {@link XID} 'objectId' from the
	 * given {@link XModel}. If the specified object is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectId'
	 * @param objectId The {@link XID} of the {@link XObject}
	 * @return The specified {@link XObject}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject} doesn't exist
	 */
	// 2010-10-27: used in this class + tests
	public static XObject safeGetObject(XModel model, XID objectId) {
		XObject object = model.getObject(objectId);
		if(object == null) {
			throw new MissingPieceException("No object with ID '" + objectId
			        + "' found in model with ID " + model.getId());
		}
		return object;
	}
	
	/**
	 * Tries to get the {@link XObject} with {@link XID} 'objectId' from the
	 * given {@link XRepository}. If the specified object is not present, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelId'
	 * @param modelId The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectId'
	 * @param objectId The {@link XID} of the {@link XObject}
	 * @return The specified {@link XObject}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject} doesn't exist
	 */
	// 2010-10-27: used in this class + tests
	public static XObject safeGetObject(XRepository repository, XID modelId, XID objectId) {
		XModel model = safeGetModel(repository, modelId);
		return safeGetObject(model, objectId);
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldId'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param model The {@link XModel} which should contain the {@link XObject}
	 *            specified by 'objectId'.
	 * @param objectId The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField}
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XObject}/{@link XField} doesn't exist
	 */
	// 2010-10-27: used only in tests
	public static XValue safeGetValue(XModel model, XID objectId, XID fieldId) {
		XObject object = safeGetObject(model, objectId);
		return safeGetValue(object, fieldId);
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldId'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param object The {@link XObject} which should contain the specified
	 *            {@link XField}
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XField} doesn't exist
	 */
	// 2010-10-27: used only in this class + tests
	public static XValue safeGetValue(XObject object, XID fieldId) throws MissingPieceException {
		XField field = safeGetField(object, fieldId);
		XValue value = field.getValue();
		if(value == null) {
			throw new MissingPieceException("Field with ID '" + fieldId + "' in object with ID "
			        + object.getId() + " has no value");
		}
		return value;
	}
	
	/**
	 * Tries to get the value of the {@link XField} with {@link XID} 'fieldId'
	 * from the given {@link XObject}. If the specified field does not exist, a
	 * {@link MissingPieceException} will be thrown.
	 * 
	 * @param repository The {@link XRepository} which should contain the
	 *            {@link XModel} specified by 'modelId'
	 * @param modelId The {@link XID} of the {@link XModel} which should contain
	 *            the {@link XObject} specified by 'objectId'
	 * @param objectId The {@link XID} of the {@link XObject} which should
	 *            contain the {@link XField} specified by 'fieldId'
	 * @param fieldId The {@link XID} of the {@link XField}
	 * @return The value of the specified {@link XField}
	 * @throws MissingPieceException Will be thrown if the specified
	 *             {@link XModel}/{@link XObject}/{@link XField} doesn't exist
	 */
	// 2010-10-27: used nowhere
	public static XValue safeGetValue(XRepository repository, XID modelId, XID objectId, XID fieldId) {
		XObject object = safeGetObject(repository, modelId, objectId);
		return safeGetValue(object, fieldId);
	}
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param model The {@link XModel} containing the {@link XObject}
	 * @param objectId The {@link XObject} containing the {@link XField}
	 * @param fieldId The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	// 2010-10-27: used only in this class
	public static XField setValue(XModel model, XID objectId, XID fieldId, XValue value) {
		XObject object = safeGetObject(model, objectId);
		return setValue(object, fieldId, value);
	}
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param object The {@link XObject} containing the {@link XField}
	 * @param fieldId The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	// 2010-10-27: used only in tests
	public static XField setValue(XObject object, XID fieldId, XValue value) {
		XField field = object.createField(fieldId);
		field.setValue(value);
		return field;
	}
	
	/**
	 * Sets the value of the given {@link XField} with the given {@link XValue}.
	 * If the given {@link XField} doesn't exist it will be created.
	 * 
	 * @param repository The {@link XRepository} containing the {@link XModel}
	 *            which contains the rest
	 * @param modelId The {@link XModel} containing the {@link XObject}
	 * @param objectId The {@link XObject} containing the {@link XField}
	 * @param fieldId The {@link XID} of the {@link XField} which value is to be
	 *            set
	 * @param value The new {@link XValue}
	 * @return The {@link XField} with newly set {@link XValue}
	 */
	// 2010-10-27: not used anywhere
	public static XField setValue(XRepository repository, XID modelId, XID objectId, XID fieldId,
	        XValue value) {
		XModel model = safeGetModel(repository, modelId);
		return setValue(model, objectId, fieldId, value);
	}
	
}

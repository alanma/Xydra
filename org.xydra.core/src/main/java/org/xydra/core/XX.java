package org.xydra.core;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


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
	// 2010-10-27: used only in tests
	public static XField setValue(XObject object, XID fieldID, XValue value) {
		XField field = object.createField(fieldID);
		field.setValue(value);
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
	// 2010-10-27: used only in this class
	public static XField setValue(XModel model, XID objectID, XID fieldID, XValue value) {
		XObject object = safeGetObject(model, objectID);
		return setValue(object, fieldID, value);
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
	// 2010-10-27: not used anywhere
	public static XField setValue(XRepository repository, XID modelID, XID objectID, XID fieldID,
	        XValue value) {
		XModel model = safeGetModel(repository, modelID);
		return setValue(model, objectID, fieldID, value);
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
	// 2010-10-27: used only in this class + tests
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
	// 2010-10-27: used only in tests
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
	// 2010-10-27: used nowhere
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
	// 2010-10-27: used in this class + tests
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
	// 2010-10-27: used only in this class
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
	// 2010-10-27: used nowhere
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
	// 2010-10-27: used in this class + tests
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
	// 2010-10-27: used in this class + tests
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
	// 2010-10-27: used in this class + tests
	public static XObject safeGetObject(XRepository repository, XID modelID, XID objectID) {
		XModel model = safeGetModel(repository, modelID);
		return safeGetObject(model, objectID);
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

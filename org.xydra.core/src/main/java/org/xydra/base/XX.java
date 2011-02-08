package org.xydra.base;

import org.xydra.core.URIFormatException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * A utility class for using {@link XID} and {@link XAddress}.
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 */
public class XX {
	
	/**
	 * @return a new random unique {@link XID} created by the default
	 *         {@link XIDProvider}
	 */
	/*
	 * TODO Should this be renamed to createUniqueId, since where no longer
	 * using "ID" (D in upper case) anywhere anymore?
	 */
	public static XID createUniqueID()

	{
		return X.getIDProvider().createUniqueID();
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
		return toAddress(objectAddress.getRepository(), objectAddress.getModel(),
		        objectAddress.getObject(), fieldId);
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
	 * @param mofAddress XAddress of a model, object or field.
	 * @throws IllegalArgumentException if the given address addresses a
	 *             repository
	 */
	public static XAddress resolveModel(XAddress mofAddress) {
		if(mofAddress.getAddressedType() == XType.XREPOSITORY) {
			throw new IllegalArgumentException("Given address '" + mofAddress
			        + "' cannot be resolved to a model");
		}
		return X.getIDProvider().fromComponents(mofAddress.getRepository(), mofAddress.getModel(),
		        null, null);
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
	
	public static XAddress resolveObject(XAddress ofAddress) {
		if(ofAddress.getAddressedType() == XType.XREPOSITORY
		        || ofAddress.getAddressedType() == XType.XMODEL) {
			throw new IllegalArgumentException("Given address '" + ofAddress
			        + "' cannot be resolved to an object");
		}
		return X.getIDProvider().fromComponents(ofAddress.getRepository(), ofAddress.getModel(),
		        ofAddress.getObject(), null);
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
	
	public static XAddress resolveRepository(XAddress repositoryAddress) {
		if(repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException("Given address '" + repositoryAddress
			        + "' cannot be resolved to a repository");
		}
		return X.getIDProvider()
		        .fromComponents(repositoryAddress.getRepository(), null, null, null);
	}
	
	/**
	 * Creates an {@link XAddress} object from its string representation using
	 * the default {@link XIDProvider}. Valid string representations of
	 * {@link XAddress XAddresses} are "modelId/objectId/fieldId",
	 * "modelId/objectId" and "modelId" where modelId, objectId and fieldId are
	 * valid string representations of {@link XID XIDs} (ie: allowed parameters
	 * for the fromString() method).
	 * 
	 * @param addressString A string with the described format, never null
	 * @return an new {@link XAddress} object representing the specified address
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid {@link XID} string or if the given String equals
	 *             null
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	public static XAddress toAddress(String addressString) {
		return X.getIDProvider().fromAddress(addressString);
	}
	
	/**
	 * Creates a new {@link XAddress} from the given components using the
	 * default {@link XIDProvider}. The {@link XAddress} will have the following
	 * format: repositoryId/modelId/objectId/fieldId
	 * 
	 * Some parameters can be null. An {@link XAddress} can address an
	 * {@link XRepository} (repositoryId set, the rest null) {@link XModel}
	 * (modelId set, repositoryId may be set, rest null), a {@link XObject}
	 * (objectId not null, fieldId null, rest is set), or an {@link XField}
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
	 * Creates an {@link XID} from a given {@link String} using the default
	 * {@link XIDProvider}. The {@link String} must be a valid XML name and may
	 * not contain any ':' characters.
	 * 
	 * @param idString The String which will be used to create the {@link XID}.
	 * @return a new unique {@link XID} object calculated from the given URI
	 * @throws IllegalArgumentException if the given name is not a valid
	 *             {@link XID} string
	 */
	public static XID toId(String idString) {
		return X.getIDProvider().fromString(idString);
	}
	
}

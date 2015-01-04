package org.xydra.base;

import org.xydra.annotations.NeverNull;

/**
 * Main facade class for accessing Xydra Base functionality.
 * 
 * @author xamde
 */
public class Base {

	/**
	 * @param repositoryAddress An address of an repository. If this is null an
	 *            address for a model without a parent is constructed.
	 * @param modelId
	 * @return an {@link XAddress} that refers to the model with the given
	 *         modelId in the repository referred to by repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             repository
	 */
	public static XAddress resolveModel(XAddress repositoryAddress, XId modelId) {
		if (repositoryAddress == null) {
			return toAddress(null, modelId, null, null);
		}
		if (repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, null, null);
	}

	public static XAddress resolveObject(XAddress objectAddress) {
		if (objectAddress.getAddressedType() == XType.XREPOSITORY
				|| objectAddress.getAddressedType() == XType.XMODEL) {
			throw new IllegalArgumentException("Given address '" + objectAddress
					+ "' cannot be resolved to an object");
		}
		return BaseRuntime.getIDProvider().fromComponents(objectAddress.getRepository(),
				objectAddress.getModel(), objectAddress.getObject(), null);
	}

	/**
	 * @param modelAddress An address of an model. If this is null an address
	 *            for an object without a parent is constructed.
	 * @param objectId
	 * @return an {@link XAddress} that refers to the object with the given
	 *         objectId in the model referred to by modelAddress.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             model
	 */
	public static XAddress resolveObject(XAddress modelAddress, XId objectId) {
		if (modelAddress == null) {
			return toAddress(null, null, objectId, null);
		}
		if (objectId == null) {
			throw new IllegalArgumentException("ObjectId is null");
		}
		if (modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, null);
	}

	/**
	 * @param repositoryAddress An address of an repository. If this is null an
	 *            address for an object without a repository is constructed.
	 * @param modelId
	 * @param objectId
	 * @return an {@link XAddress} that refers to the object width the given
	 *         objectId in the model with the given modelId in the repository
	 *         referred to by repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             repository
	 */
	public static XAddress resolveObject(XAddress repositoryAddress, XId modelId, XId objectId) {
		if (repositoryAddress == null) {
			return toAddress(null, modelId, objectId, null);
		}
		if (repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, objectId, null);
	}

	public static XAddress resolveRepository(XAddress repositoryAddress) {
		if (repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException("Given address '" + repositoryAddress
					+ "' cannot be resolved to a repository");
		}
		return BaseRuntime.getIDProvider().fromComponents(repositoryAddress.getRepository(), null,
				null, null);
	}

	/**
	 * @return a new random unique {@link XId} created by the default
	 *         {@link XIdProvider}
	 */
	public static XId createUniqueId() {
		return BaseRuntime.getIDProvider().createUniqueId();
	}

	/**
	 * @param repositoryAddress An address of an repository. If this is null an
	 *            address for a field without a repository is constructed.
	 * @param modelId
	 * @param objectId
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the field with the given
	 *         fieldId in the object with the given objectId in the model with
	 *         the given modelId in the repository referred to by
	 *         repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             repository
	 */
	public static XAddress resolveField(XAddress repositoryAddress, XId modelId, XId objectId,
			XId fieldId) {
		if (repositoryAddress == null) {
			return toAddress(null, modelId, objectId, fieldId);
		}
		if (repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, objectId, fieldId);
	}

	/**
	 * @param mofAddress XAddress of a model, object or field.
	 * @return a model address
	 * @throws IllegalArgumentException if the given address addresses a
	 *             repository
	 */
	public static XAddress resolveModel(XAddress mofAddress) {
		if (mofAddress.getAddressedType() == XType.XREPOSITORY) {
			throw new IllegalArgumentException("Given address '" + mofAddress
					+ "' cannot be resolved to a model");
		}
		return BaseRuntime.getIDProvider().fromComponents(mofAddress.getRepository(),
				mofAddress.getModel(), null, null);
	}

	/**
	 * Creates an {@link XAddress} object from its string representation using
	 * the default {@link XIdProvider}. Valid string representations of
	 * {@link XAddress XAddresses} are "modelId/objectId/fieldId",
	 * "modelId/objectId" and "modelId" where modelId, objectId and fieldId are
	 * valid string representations of {@link XId XIds} (ie: allowed parameters
	 * for the fromString() method).
	 * 
	 * @param addressString A string with the described format
	 * @return an new {@link XAddress} object representing the specified address
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid {@link XId} string or if the given String equals
	 *             null
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	public static @NeverNull XAddress toAddress(@NeverNull String addressString) {
		return BaseRuntime.getIDProvider().fromAddress(addressString);
	}

	/**
	 * @param objectAddress An address of an object. If this is null an address
	 *            for a field without a parent is constructed.
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the field with the given
	 *         fieldId in the object referred to by objectAddress.
	 * @throws IllegalArgumentException if objectAddress doesn't refer to an
	 *             object
	 */
	public static XAddress resolveField(XAddress objectAddress, XId fieldId) {
		if (objectAddress == null) {
			return toAddress(null, null, null, fieldId);
		}
		if (objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new IllegalArgumentException(objectAddress + " is not an object address");
		}
		return toAddress(objectAddress.getRepository(), objectAddress.getModel(),
				objectAddress.getObject(), fieldId);
	}

	/**
	 * @param modelAddress An address of an model. If this is null an address
	 *            for a field without a model is constructed.
	 * @param objectId
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the field with the given
	 *         fieldId in the {link XObject} with the given objectId in the
	 *         model referred to by modelAddress.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             model
	 */
	public static XAddress resolveField(XAddress modelAddress, XId objectId, XId fieldId) {
		if (modelAddress == null) {
			return toAddress(null, null, objectId, fieldId);
		}
		if (modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, fieldId);
	}

	/**
	 * Creates a new {@link XAddress} from the given components using the
	 * default {@link XIdProvider}. The {@link XAddress} will have the following
	 * format: repositoryId/modelId/objectId/fieldId
	 * 
	 * Some parameters can be null. An {@link XAddress} can address an
	 * repository (repositoryId set, the rest null) model (modelId set,
	 * repositoryId may be set, rest null), a object (objectId not null, fieldId
	 * null, rest is set), or an field (fieldId not null, rest may or may not be
	 * null).
	 * 
	 * Legal addresses are (R---),(RM--),(-M--), (RMO-), (RMOF),(---F),
	 * (--O-),(-MO-),(-MOF)? ...?
	 * 
	 * Illegal addresses are (R-M-),(R-MF),(-M-F),(RM-F),(R--F),(R-O-),(----)
	 * 
	 * TODO what about (
	 * 
	 * @param repositoryId The {@link XId} for the repository field of the
	 *            {@link XAddress}
	 * @param modelId The {@link XId} for the model field of the
	 *            {@link XAddress}
	 * @param objectId The {@link XId} for the object field of the
	 *            {@link XAddress}
	 * @param fieldId The {@link XId} for the field field of the
	 *            {@link XAddress}
	 * @return an {@link XAddress} with the given components.
	 * @throws IllegalArgumentException if the given set of {@link XId XIds}
	 *             does not fit into one of the patterns described above (for
	 *             example if repositoryId is set, modelId not set and objectId
	 *             is set)
	 */
	public static XAddress toAddress(XId repositoryId, XId modelId, XId objectId, XId fieldId) {
		return BaseRuntime.getIDProvider().fromComponents(repositoryId, modelId, objectId, fieldId);
	}

	/**
	 * Creates an {@link XId} from a given {@link String} using the default
	 * {@link XIdProvider}. The {@link String} must be a valid XML name and may
	 * not contain any ':' characters. The string SHOULD be at most 100
	 * characters long for maximum compatibility with all back-ends (e.g. Google
	 * AppEngine).
	 * 
	 * @param idString The String which will be used to create the {@link XId}.
	 * @return a new unique {@link XId} object calculated from the given URI
	 * @throws IllegalArgumentException if the given name is not a valid
	 *             {@link XId} string
	 */
	public static XId toId(String idString) {
		return BaseRuntime.getIDProvider().fromString(idString);
	}

	public static XAddress resolveRepository(XId repositoryId) {
		return toAddress(repositoryId, null, null, null);
	}

	public static XAddress resolveModel(XId repositoryId, XId modelId) {
		return toAddress(repositoryId, modelId, null, null);
	}

	public static XAddress resolveObject(XId repositoryId, XId modelId, XId objectId) {
		return toAddress(repositoryId, modelId, objectId, null);
	}

	public static XAddress resolveField(XId repositoryId, XId modelId, XId objectId, XId fieldId) {
		return toAddress(repositoryId, modelId, objectId, fieldId);
	}

}

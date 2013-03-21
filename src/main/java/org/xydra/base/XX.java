package org.xydra.base;

import org.xydra.annotations.NeverNull;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.URIFormatException;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A utility class for using {@link XId} and {@link XAddress}.
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 */
public class XX {
	
	/**
	 * @return a new random unique {@link XId} created by the default
	 *         {@link XIdProvider}
	 */
	public static XId createUniqueId() {
		return X.getIDProvider().createUniqueId();
	}
	
	/**
	 * @param objectAddress An address of an {@link XObject}. If this is null an
	 *            address for a field without a parent is constructed.
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the {@link XField} with the
	 *         given fieldId in the {@link XObject} referred to by
	 *         objectAddress.
	 * @throws IllegalArgumentException if objectAddress doesn't refer to an
	 *             {@link XObject}
	 */
	public static XAddress resolveField(XAddress objectAddress, XId fieldId) {
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
	 * @param modelAddress An address of an {@link XModel}. If this is null an
	 *            address for a field without a model is constructed.
	 * @param objectId
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the {@link XField} with the
	 *         given fieldId in the {link XObject} with the given objectId in
	 *         the {@link XModel} referred to by modelAddress.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             {@link XModel}
	 */
	public static XAddress resolveField(XAddress modelAddress, XId objectId, XId fieldId) {
		if(modelAddress == null) {
			return toAddress(null, null, objectId, fieldId);
		}
		if(modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, fieldId);
	}
	
	/**
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for a field without a repository is
	 *            constructed.
	 * @param modelId
	 * @param objectId
	 * @param fieldId
	 * @return an {@link XAddress} that refers to the {@link XField} with the
	 *         given fieldId in the {@link XObject} with the given objectId in
	 *         the {@link XModel} with the given modelId in the
	 *         {@link XRepository} referred to by repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveField(XAddress repositoryAddress, XId modelId, XId objectId,
	        XId fieldId) {
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
	 * @return a model address
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
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for a model without a parent is constructed.
	 * @param modelId
	 * @return an {@link XAddress} that refers to the {@link XModel} with the
	 *         given modelId in the {@link XRepository} referred to by
	 *         repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveModel(XAddress repositoryAddress, XId modelId) {
		if(repositoryAddress == null) {
			return toAddress(null, modelId, null, null);
		}
		if(repositoryAddress.getAddressedType() != XType.XREPOSITORY) {
			throw new IllegalArgumentException(repositoryAddress + " is not a repository address");
		}
		return toAddress(repositoryAddress.getRepository(), modelId, null, null);
	}
	
	public static XAddress resolveObject(XAddress objectAddress) {
		if(objectAddress.getAddressedType() == XType.XREPOSITORY
		        || objectAddress.getAddressedType() == XType.XMODEL) {
			throw new IllegalArgumentException("Given address '" + objectAddress
			        + "' cannot be resolved to an object");
		}
		return X.getIDProvider().fromComponents(objectAddress.getRepository(),
		        objectAddress.getModel(), objectAddress.getObject(), null);
	}
	
	/**
	 * @param modelAddress An address of an {@link XModel}. If this is null an
	 *            address for an object without a parent is constructed.
	 * @param objectId
	 * @return an {@link XAddress} that refers to the {@link XObject} with the
	 *         given objectId in the {@link XModel} referred to by modelAddress.
	 * @throws IllegalArgumentException if modelAddress doesn't refer to an
	 *             {@link XModel}
	 */
	public static XAddress resolveObject(XAddress modelAddress, XId objectId) {
		if(modelAddress == null) {
			return toAddress(null, null, objectId, null);
		}
		if(objectId == null) {
			throw new IllegalArgumentException("ObjectId is null");
		}
		if(modelAddress.getAddressedType() != XType.XMODEL) {
			throw new IllegalArgumentException(modelAddress + " is not a model address");
		}
		return toAddress(modelAddress.getRepository(), modelAddress.getModel(), objectId, null);
	}
	
	/**
	 * @param repositoryAddress An address of an {@link XRepository}. If this is
	 *            null an address for an object without a repository is
	 *            constructed.
	 * @param modelId
	 * @param objectId
	 * @return an {@link XAddress} that refers to the {@link XObject} width the
	 *         given objectId in the {@link XModel} with the given modelId in
	 *         the {@link XRepository} referred to by repositoryAddress.
	 * @throws IllegalArgumentException if repositoryAddress doesn't refer to an
	 *             {@link XRepository}
	 */
	public static XAddress resolveObject(XAddress repositoryAddress, XId modelId, XId objectId) {
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
	public static @NeverNull
	XAddress toAddress(@NeverNull String addressString) {
		return X.getIDProvider().fromAddress(addressString);
	}
	
	/**
	 * Creates a new {@link XAddress} from the given components using the
	 * default {@link XIdProvider}. The {@link XAddress} will have the following
	 * format: repositoryId/modelId/objectId/fieldId
	 * 
	 * Some parameters can be null. An {@link XAddress} can address an
	 * {@link XRepository} (repositoryId set, the rest null) {@link XModel}
	 * (modelId set, repositoryId may be set, rest null), a {@link XObject}
	 * (objectId not null, fieldId null, rest is set), or an {@link XField}
	 * (fieldId not null, rest may or may not be null).
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
		return X.getIDProvider().fromComponents(repositoryId, modelId, objectId, fieldId);
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
		return X.getIDProvider().fromString(idString);
	}
	
	/**
	 * Use {@link XCopyUtils#copyModel(XId, String, XReadableModel)} if the
	 * resulting model should not be backed by the XReadableModel.
	 * 
	 * @param actor The session actor to use for the returned model.
	 * @param password The password corresponding to the given actor.
	 * @param modelSnapshot
	 * @return a {@link XModel} with the same initial state as the given model
	 *         snapshot. The returned model may be backed by the provided
	 *         XReadableModel instance, so it should no longer be modified
	 *         directly or the behavior of the model is undefined.
	 */
	public static XModel wrap(XId actor, String password, XReadableModel modelSnapshot) {
		if(modelSnapshot instanceof XRevWritableModel) {
			return new MemoryModel(actor, password, (XRevWritableModel)modelSnapshot);
		} else {
			return XCopyUtils.copyModel(actor, password, modelSnapshot);
		}
	}
	
	/**
	 * Use {@link XCopyUtils#copyObject(XId, String, XReadableObject)} if the
	 * resulting object should not be backed by the XReadableObject.
	 * 
	 * @param actor The session actor to use for the returned object.
	 * @param password The password corresponding to the given actor.
	 * @param objectSnapshot
	 * @return an {@link XObject} with the same initial state as the given
	 *         object snapshot. The returned object may be backed by the
	 *         provided XReadableObject instance, so it should no longer be
	 *         modified directly or the behavior of the model is undefined.
	 */
	public static XObject wrap(XId actor, String password, XReadableObject objectSnapshot) {
		if(objectSnapshot instanceof XRevWritableObject) {
			return new MemoryObject(actor, password, (XRevWritableObject)objectSnapshot);
		} else {
			return XCopyUtils.copyObject(actor, password, objectSnapshot);
		}
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

package org.xydra.base;

import org.xydra.annotations.NeverNull;


/**
 * A provider of {@link XId} objects.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XIdProvider {
	
	/**
	 * @return a new random unique {@link XId}
	 */
	XId createUniqueId();
	
	/**
	 * Creates an {@link XAddress} object from its string representation. Valid
	 * string representations of {@link XAddress XAddresses} are
	 * "modelId/objectId/fieldId", "modelId/objectId" and "modelId" where
	 * modelId, objectId and fieldId are valid string representations of
	 * {@link XId XIds} (ie: allowed parameters for the fromString() method).
	 * 
	 * @param address A string with the described format, never null
	 * @return an new {@link XAddress} object representing the specified address
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid {@link XId} string or if the given String equals
	 *             null
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	@NeverNull XAddress fromAddress(String address) throws IllegalArgumentException, URIFormatException;
	
	/**
	 * Creates a new {@link XAddress} from the given components. The
	 * {@link XAddress} will have the following format:
	 * repositoryId/modelId/objectId/fieldId
	 * 
	 * Some parameters can be null. An {@link XAddress} can address an
	 * repository (repositoryId set, the rest null) model
	 * (modelId set, repositoryId may be set, rest null), a object
	 * (objectId not null, fieldId null, rest is set), or an field
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
	XAddress fromComponents(XId repositoryId, XId modelId, XId objectId, XId fieldId);
	
	/**
	 * Creates an {@link XId} from a given string. The string must be a valid
	 * XML name and may not contain any ':' characters. The string SHOULD be at
	 * most 100 characters long for maximal compatibility with all back-ends
	 * such as Google AppEngine.
	 * 
	 * @param name the String which will be used to create the {@link XId}.
	 * @return a new unique {@link XId} object calculated from the given name
	 * @throws IllegalArgumentException if the given name is not a valid
	 *             {@link XId} string
	 */
	XId fromString(String name) throws IllegalArgumentException;
	
}

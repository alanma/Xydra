package org.xydra.base;

import org.xydra.core.URIFormatException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * A provider of {@link XID} objects.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XIDProvider {
	
	/**
	 * @return a new random unique {@link XID}
	 */
	XID createUniqueID();
	
	/**
	 * Creates an {@link XAddress} object from its string representation. Valid
	 * string representations of {@link XAddress XAddresses} are
	 * "modelId/objectId/fieldId", "modelId/objectId" and "modelId" where
	 * modelId, objectId and fieldId are valid string representations of
	 * {@link XID XIDs} (ie: allowed parameters for the fromString() method).
	 * 
	 * @param address A string with the described format, never null
	 * @return an new {@link XAddress} object representing the specified address
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid {@link XID} string or if the given String equals
	 *             null
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	XAddress fromAddress(String address) throws IllegalArgumentException, URIFormatException;
	
	/**
	 * Creates a new {@link XAddress} from the given components. The
	 * {@link XAddress} will have the following format:
	 * repositoryId/modelId/objectId/fieldId
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
	XAddress fromComponents(XID repositoryId, XID modelId, XID objectId, XID fieldId);
	
	/**
	 * Creates an {@link XID} from a given string. The string must be a valid
	 * XML name and may not contain any ':' characters. The string SHOULD be at
	 * most 100 characters long. TODO Why?
	 * 
	 * @param name the String which will be used to create the {@link XID}.
	 * @return a new unique {@link XID} object calculated from the given name
	 * @throws IllegalArgumentException if the given name is not a valid
	 *             {@link XID} string
	 */
	XID fromString(String name) throws IllegalArgumentException;
	
}

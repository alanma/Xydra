package org.xydra.core.model;

import org.xydra.core.URIFormatException;


/**
 * A provider of ID objects.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XIDProvider {
	
	/**
	 * @return a new random unique ID object
	 */
	XID createUniqueID();
	
	/**
	 * Creates an XID from a given string. The string must be a valid XML name
	 * and may not contain any ':' characters.
	 * 
	 * @param uriString The String which will be used to create the XID.
	 * @return a new unique ID object calculated from the given URI
	 * @throws IllegalArgumentException if the given name is not a valid XID
	 *             string
	 */
	XID fromString(String name) throws IllegalArgumentException;
	
	/**
	 * Creates an XAddress onject from it's string representation. Valid string
	 * representations of XAddresses are "modelId/objectId/fieldId",
	 * "modelId/objectId" and "modelId" where modelId, objectId and fieldId are
	 * valid string representations of XIDs (ie: allowed parameters for the
	 * fromString() method).
	 * 
	 * @param address Model/Object/Field, never null
	 * @return an new XAddress
	 * @throws IllegalArgumentException if one of the given URI components is
	 *             not a valid XID string or if there are too many components
	 * @throws URIFormatException if the given address contains too many
	 *             components.
	 */
	XAddress fromAddress(String address) throws IllegalArgumentException;
	
	/**
	 * Some parameters can be null. An {@link XAddress} can address a
	 * {@link XModel} (object=null, field=null), a {@link XObject} (field=null),
	 * or an {@link XField} (no nulls).
	 * 
	 * @param modelID never null
	 * @param objectID null, if addressing a {@link XModel}
	 * @param fieldID null, if addressing a {@link XModel}, or {@link XObject}
	 * @return an {@link XAddress} with the given components.
	 */
	XAddress fromComponents(XID repositoryId, XID modelId, XID objectId, XID fieldId);
	
}

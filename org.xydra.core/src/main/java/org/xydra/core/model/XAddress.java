package org.xydra.core.model;

import java.io.Serializable;


/**
 * An identifier for a specific instance of an {@link XRepository},
 * {@link XModel}, {@link XObject} or {@link XField}. An XAddress allows to
 * uniquely identify an entity within a given {@link XRepository}.
 * 
 * @author dscharrer
 */
public interface XAddress extends Serializable {
	
	/**
	 * @return the {@link XID} of the {@link XField} identified by this address
	 *         or null if this address does not identify an {@link XField}
	 */
	XID getField();
	
	/**
	 * @return either the {@link XID} of the {@link XObject} identified by this
	 *         address or the {@link XID} of the {@link XObject} containing the
	 *         {@link XField}, if this address identifies an {@link XField}. May
	 *         be null if this address does not identify an {@link XObject}.
	 */
	XID getObject();
	
	/**
	 * @return the {@link XID} of the {@link XModel} identified by this address
	 *         or the {@link XID} of the {@link XModel} containing the entity
	 *         identified by this address. May be null if this address does not
	 *         identify an {@link XModel}.
	 */
	XID getModel();
	
	/**
	 * @return the {@link XID} of the {@link XRepository} identified by this
	 *         address or the {@link XID} of the {@link XRepository} containing
	 *         the entity identified by this address. May be null if this
	 *         address does not identify an {@link XRepositry}.
	 */
	XID getRepository();
	
	/**
	 * @return The address of the parent entity of the entity identified by this
	 *         address or null if there is no parent.
	 */
	XAddress getParent();
	
}

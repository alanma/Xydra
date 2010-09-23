package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.core.XX;


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
	
	/**
	 * @return the {@link XType} of the entity which this {@link XAddress}
	 *         refers to.
	 */
	XType getAddressedType();
	
	/**
	 * Checks whether the entity referred to by {@link XAdress} descendant is a
	 * descendant of the entity referred to by this address.
	 * 
	 * @return true if 'descendant' refers to an entity contained in the entity
	 *         referred to by this address.
	 */
	boolean contains(XAddress descendant);
	
	/**
	 * Checks whether the entity referred to by {@link XAdress} descendant is a
	 * child of the entity referred to by this address or if both
	 * {@link XAddress}es refer to the same entity.
	 * 
	 * @return true if 'descendant' refers to an entity contained in the entity
	 *         referred to by this address or if 'descendant' refers to the same
	 *         entity as this address.
	 */
	boolean equalsOrContains(XAddress descendant);
	
	/**
	 * Checks whether the entity referred to by the {@link XAddress} child is a
	 * direct child of the entity referred to by this address.
	 * 
	 * @return true if 'child' refers to an entity directly contained in the
	 *         entity referred to by this address.
	 */
	boolean isParentOf(XAddress child);
	
	/**
	 * @return a web-safe String (needs no URL encoding). Can be parsed via
	 *         {@link XX#toAddress(String)}
	 */
	String toURI();
	
}

package org.xydra.base;

import java.io.Serializable;

import org.xydra.base.value.XSingleValue;
import org.xydra.core.model.XRepository;


/**
 * An identifier for a specific instance of an repository, model, object or
 * field. An XAddress allows to uniquely identify an entity within a given
 * repository.
 * 
 * @author dscharrer
 */
public interface XAddress extends Serializable, XSingleValue<XAddress>, Comparable<XAddress> {
	
	/**
	 * Checks whether the entity referred to by {@link XAddress} descendant is a
	 * descendant of the entity referred to by this address.
	 * 
	 * @param descendant
	 * 
	 * @return true if 'descendant' refers to an entity contained in the entity
	 *         referred to by this address.
	 */
	boolean contains(XAddress descendant);
	
	/**
	 * Checks whether the entity referred to by {@link XAddress} descendant is a
	 * child of the entity referred to by this address or if both
	 * {@link XAddress}es refer to the same entity.
	 * 
	 * @param descendant
	 * 
	 * @return true if 'descendant' refers to an entity contained in the entity
	 *         referred to by this address or if 'descendant' refers to the same
	 *         entity as this address.
	 */
	boolean equalsOrContains(XAddress descendant);
	
	/**
	 * @return the {@link XType} of the entity which this {@link XAddress}
	 *         refers to.
	 */
	XType getAddressedType();
	
	/**
	 * @return the {@link XId} of the field identified by this address or null
	 *         if this address does not identify a field.
	 */
	XId getField();
	
	/**
	 * @return the {@link XId} of the model identified by this address or the
	 *         {@link XId} of the model containing the entity identified by this
	 *         address. May be null if this address does not identify an model.
	 */
	XId getModel();
	
	/**
	 * @return either the {@link XId} of the object identified by this address
	 *         or the {@link XId} of the object containing the field, if this
	 *         address identifies an field. May be null if this address does not
	 *         identify an object.
	 */
	XId getObject();
	
	/**
	 * @return The address of the parent entity of the entity identified by this
	 *         address or null if there is no parent.
	 */
	XAddress getParent();
	
	/**
	 * @return the {@link XId} of the repository identified by this address or
	 *         the {@link XId} of the repository containing the entity
	 *         identified by this address. May be null if this address does not
	 *         identify an {@link XRepository}.
	 */
	XId getRepository();
	
	/**
	 * Checks whether the entity referred to by the {@link XAddress} child is a
	 * direct child of the entity referred to by this address.
	 * 
	 * @param child
	 * 
	 * @return true if 'child' refers to an entity directly contained in the
	 *         entity referred to by this address.
	 */
	boolean isParentOf(XAddress child);
	
	/**
	 * @return a web-safe string (i.e. it needs no URL encoding). Can be parsed
	 *         via {@link XX#toAddress(String)}
	 */
	String toURI();
	
}

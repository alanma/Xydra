package org.xydra.core.model;

/**
 * An identifier for a specific instance of an XModel, XObject or XField. Such
 * an identifier allows to uniquely identify an X-entity within a given
 * repository.
 * 
 * @author dscharrer
 */
public interface XAddress {
	
	/**
	 * @return the id of the field identified by this address or null if this
	 *         address identifies an object or model.
	 */
	XID getField();
	
	/**
	 * @return the id of the object identified by this address or the object
	 *         containing the identified field or null if this address
	 *         identifies a model.
	 */
	XID getObject();
	
	/**
	 * @return the id of the model identified by this address or the model
	 *         containing the identified object or field
	 */
	XID getModel();
	
	/**
	 * @return the id of the repository identified by this address or the
	 *         repository containing the identified model, object or field
	 */
	XID getRepository();
	
	/**
	 * @return The address of the parent entity or null of there is no parent.
	 */
	XAddress getParent();
	
}

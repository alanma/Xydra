package org.xydra.xgae.datastore.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An entity in an key value store. I.e. the 'value' of a key-value store.
 * 
 * @author xamde
 */
public interface SEntity extends SWrapper {

	Object getAttribute(String name);

	/**
	 * @return a map of all defined attributes (called properties on GAE)
	 */
	Map<String, Object> getAttributes();

	/**
	 * @return the key
	 */
	SKey getKey();

	/**
	 * @param name
	 * @return true iff this entity has this property
	 */
	boolean hasAttribute(String name);

	/**
	 * @param name
	 */
	void removeAttribute(String name);

	/**
	 * E.g. for GAE: setUnindexedProperty
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, boolean value);

	/**
	 * @param name
	 * @param list
	 *            must be of a type that is also accepted in a setAttribute
	 *            method
	 */
	void setAttribute(String name, List<?> list);

	/**
	 * E.g. for GAE: setUnindexedProperty
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, long value);

	/**
	 * @param name
	 * @param serializable
	 */
	void setAttribute(String name, Serializable serializable);

	/**
	 * E.g. for GAE: setUnindexedProperty
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, String value);

	/**
	 * E.g. for GAE: setUnindexedProperty
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, SValue value);

}

package org.xydra.core.index;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * Indexes any number of objects by the value of a field. The fieldId of this
 * field is defined at index construction time. The value of these fields must
 * be unique.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public interface IUniqueObjectIndex {
	
	/**
	 * Delete all data.
	 */
	void clear();
	
	/**
	 * @param value
	 * @return true if the index contains an entry for the given value
	 */
	boolean contains(XValue value);
	
	/**
	 * De-index the given object. A lookup on the index-field is performed.
	 * 
	 * @param xo the given object by the fieldId configured for this index
	 * @return XID of indexed object, if found. Null otherwise.
	 */
	XID deindex(XObject xo);
	
	/**
	 * De-index the object with the given value.
	 * 
	 * @param value
	 * @return XID of indexed object, if found. Null otherwise.
	 */
	XID deindex(XValue value);
	
	/**
	 * New objects overwrite existing objects silently.
	 * 
	 * @param xo
	 * @return XID of previously indexed object if such an object has been
	 *         indexed here, null otherwise
	 * @throws IllegalStateException
	 */
	XID index(XObject xo) throws IllegalStateException;
	
	/**
	 * @param model used to load objects from internally stored XIDs
	 * @param value
	 * @return all previously indexed objects (in the given model) that have
	 *         'value' as the value of the fieldId configured for this index
	 */
	XObject lookup(XModel model, XValue value);
	
}

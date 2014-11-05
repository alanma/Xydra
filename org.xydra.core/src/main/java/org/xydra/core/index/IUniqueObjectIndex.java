package org.xydra.core.index;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;


/**
 * Indexes any number of objects by the value of a field. The fieldId of this
 * field is defined at index construction time. The value of these fields must
 * be unique.
 * 
 * @author xamde
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
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
	 * @return XId of indexed object, if found. Null otherwise.
	 */
	XId deindex(XReadableObject xo);
	
	/**
	 * De-index the object with the given value.
	 * 
	 * @param value
	 * @return XId of indexed object, if found. Null otherwise.
	 */
	XId deindex(XValue value);
	
	/**
	 * New objects overwrite existing objects silently.
	 * 
	 * @param xo
	 * @return XId of previously indexed object if such an object has been
	 *         indexed here, null otherwise
	 * @throws IllegalStateException
	 */
	XId index(XReadableObject xo) throws IllegalStateException;
	
	/**
	 * @param userModel used to load objects from internally stored XIds
	 * @param value
	 * @return all previously indexed objects (in the given model) that have
	 *         'value' as the value of the fieldId configured for this index
	 */
	XWritableObject lookup(XWritableModel userModel, XValue value);
	
	XId lookupID(XValue indexKey);
	
}

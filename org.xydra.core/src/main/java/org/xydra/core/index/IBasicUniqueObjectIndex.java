package org.xydra.core.index;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.value.XValue;


/**
 * Indexes any number of objects by a XValue.
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public interface IBasicUniqueObjectIndex {
	
	/**
	 * @param indexKey to be looked up
	 * @return the currently indexed XId or null if none found.
	 */
	XId lookupID(XValue indexKey);
	
	/**
	 * @param key to be indexed
	 * @param value to be indexed
	 * @return the previously indexed value, if any, or null.
	 */
	XId index(XValue key, XId value);
	
	/**
	 * @param indexKey never null
	 * @return true if index contains the given key
	 */
	boolean contains(XValue indexKey);
	
	/**
	 * De-index the given value.
	 * 
	 * @param key to de-index
	 * @return the previously indexed XId or null of not found
	 */
	XId deindex(XValue key);
	
}

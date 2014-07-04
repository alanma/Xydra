package org.xydra.core.index;

import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;


/**
 * Indexes any number of objects by the value of a field. The fieldId of this
 * field is defined at index construction time. Several objects can have the
 * same value.
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public interface IObjectIndex {
	
	/**
	 * @param xo the given object by the fieldId configured for this index
	 */
	void deindex(XReadableObject xo);
	
	/**
	 * @param xo the given object by the fieldId configured for this index
	 */
	void index(XReadableObject xo);
	
	/**
	 * @param model used to load objects from internally stored XIds
	 * @param value
	 * @return all previously indexed objects (in the given model) that have
	 *         'value' as the value of the fieldId configured for this index
	 */
	Set<XWritableObject> lookup(XWritableModel model, XValue value);
	
}

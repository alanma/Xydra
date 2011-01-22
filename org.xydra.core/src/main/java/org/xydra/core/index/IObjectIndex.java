package org.xydra.core.index;

import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * Indexes any number of objects by the value of a field. The fieldId of this
 * field is defined at index construction time. Several objects can have the
 * same value.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public interface IObjectIndex {
	
	/**
	 * @param xo the given object by the fieldId configured for this index
	 */
	void deindex(XObject xo);
	
	/**
	 * @param xo the given object by the fieldId configured for this index
	 */
	void index(XObject xo);
	
	/**
	 * @param model used to load objects from internally stored XIDs
	 * @param value
	 * @return all previously indexed objects (in the given model) that have
	 *         'value' as the value of the fieldId configured for this index
	 */
	Set<XObject> lookup(XModel model, XValue value);
	
}

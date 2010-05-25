package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.value.XValue;



/**
 * An {@link XFieldState} models a field in an {@link XObjectState}.
 * 
 * 
 * @author voelkel
 * @author kaidel
 * 
 */
public interface XFieldState extends IHasXID, Serializable, IHasXAddress {
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @throws IllegalStateException if the fieldState has no parent
	 *             {@link XAddress}.
	 */
	void delete();
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @throws IllegalStateException if the fieldState has no parent
	 *             {@link XAddress}.
	 */
	void save();
	
	/**
	 * Gets the current revision number of this XField.
	 * 
	 * @return The current revision number of this XField.
	 */
	long getRevisionNumber();
	
	/**
	 * @return the current value
	 */
	XValue getValue();
	
	/**
	 * Set the current revision number.
	 * 
	 * @param revisionNumber
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * Set this field to the new value.
	 * 
	 * @param value
	 */
	void setValue(XValue value);
	
}

package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


/**
 * An {@link XFieldState} represents the inner state of an {@link XField}, for
 * example for persistence purposes.
 * 
 * An {@link XFieldState} stores the
 * <ul>
 * <li> {@link XID} of the {@link XField}
 * <li>the revision number of the {@link XField}
 * <li>the {@link XValue} of the {@link XField}
 * </ul>
 * 
 * An {@link XModelState} can be serialized, and therefore be used e.g. in GWT.
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
	 * Store the data of this XFieldState in the attached persistence layer,
	 * i.e. the one determined by calling {@link XStateFactory}.create...().
	 */
	void save();
	
	/**
	 * Gets the current revision number of the {@link XField} which state is
	 * being represented by this XFieldState.
	 * 
	 * @return The current revision number of the {@link XField} which state is
	 *         being represented by this XFieldState.
	 */
	long getRevisionNumber();
	
	/**
	 * @return the current {@link XValue} stored by this XFieldState
	 */
	XValue getValue();
	
	/**
	 * Set the current revision number.
	 * 
	 * @param revisionNumber the new revision number
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * Set the stored {@link XValue} to the given {@link XValue}.
	 * 
	 * @param value The new {@link XValue} (passing 'null' implies a remove of
	 *            the currently stored {@link XValue})
	 */
	void setValue(XValue value);
	
}

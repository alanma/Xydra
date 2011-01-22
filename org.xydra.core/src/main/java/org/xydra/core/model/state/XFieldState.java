package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXID;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;


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
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object (if not null) must have been created by the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}.
	 */
	void delete(XStateTransaction transaction);
	
	/**
	 * Gets the current revision number of the {@link XField} which state is
	 * being represented by this XFieldState.
	 * 
	 * @return The current revision number of the {@link XField} which state is
	 *         being represented by this XFieldState.
	 */
	long getRevisionNumber();
	
	/**
	 * Gets the currently stored {@link XValue} of the {@link XField} which is
	 * represented by this {@link XFieldState}.
	 * 
	 * @return the currently stored {@link XValue} of the {@link XField} which
	 *         is represented by this {@link XFieldState}.
	 */
	XValue getValue();
	
	/**
	 * Store this state information in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object (if not null) must have been created by the containing
	 *            {@link XObjectState}, {@link XModelState} or
	 *            {@link XRepositoryState}.
	 */
	void save(XStateTransaction transaction);
	
	/**
	 * Set the current revision number.
	 * 
	 * Implementations should not persist this change until the corresponding
	 * save unless they can guarantee that no other state calls will fail.
	 * 
	 * @param revisionNumber the new revision number
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * Sets the stored {@link XValue} to the given {@link XValue}.
	 * 
	 * Implementations should not persist this change until the corresponding
	 * save unless they can guarantee that no other state calls will fail.
	 * 
	 * @param value The new {@link XValue} (passing 'null' implies a remove of
	 *            the currently stored {@link XValue})
	 */
	void setValue(XValue value);
	
}

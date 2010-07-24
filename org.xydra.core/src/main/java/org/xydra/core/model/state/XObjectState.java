package org.xydra.core.model.state;

import java.util.Iterator;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;


/**
 * An {@link XObjectState} represents the inner state of an {@link XObject}, for
 * example for persistence purposes.
 * 
 * An {@link XObjectState} stores the
 * <ul>
 * <li> {@link XID} of the {@link XObject}
 * <li>the revision number of the {@link XObject}
 * <li>the child-{@link XField XField} of the {@link XModel} in form of their
 * {@link XFieldState XFieldStates}
 * </ul>
 * 
 * An {@link XFieldState} can be serialized, and therefore be used e.g. in GWT.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XObjectState extends IHasXID, Iterable<XID>, IHasXAddress {
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
	 * @returns an {@link Iterator} over the {@link XID}s of all children, which
	 *          are {@link XFieldState} objects.
	 */
	Iterator<XID> iterator();
	
	/**
	 * Take the {@link XFieldState} and links it as a child of this objectState.
	 * Also sets this {@link XObjectState} as the parent. Neither this fact nor
	 * the {@link XFieldState} itself is persisted by this operation.
	 * 
	 * 
	 * @param fieldState
	 */
	void addFieldState(XFieldState fieldState);
	
	/**
	 * Returns the current revision number of this XObject.
	 * 
	 * @return The current revision number of this XObject.
	 */
	long getRevisionNumber();
	
	/**
	 * Checks whether this XObject contains an XField with the given XID
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this XObject contains an XField with the given XID,
	 *         false otherwise
	 */
	boolean hasFieldState(XID id);
	
	/**
	 * @return true if there are no child {@link XFieldState}.
	 */
	boolean isEmpty();
	
	/**
	 * Get a {@link XFieldState} contained in this repository from the
	 * appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the field has not been deleted AND
	 * not been removed from this object. It is however not guaranteed to fail
	 * if the field has been removed.
	 */
	XFieldState getFieldState(XID id);
	
	/**
	 * Create a new {@link XFieldState} in the same persistence layer as this
	 * repository state.
	 */
	XFieldState createFieldState(XID id);
	
	/**
	 * Removes the {@link XFieldState} from this {@link XObjectState}
	 * 
	 * @param fieldState The {@link XFieldState} to be removed
	 */
	void removeFieldState(XID fieldStateId);
	
	/**
	 * Set the stored revisonNumber to the given value.
	 * 
	 * @param revisionNumber
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * @return the state of the change log which is logging this object or null
	 *         if this object is contained within a model
	 */
	XChangeLogState getChangeLogState();
	
}

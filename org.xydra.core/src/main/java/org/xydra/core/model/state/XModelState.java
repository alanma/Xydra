package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;



/**
 * {@link XModelState}s can be stored in a {@link XRepositoryState}, but they
 * can equally live independently.
 * 
 * An {@link XModelState} is a set of {@link XObjectState}s.
 * 
 * An {@link XModelState} can be serialised, and hence can be used e.g. in GWT.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XModelState extends IHasXID, Serializable, Iterable<XID>, IHasXAddress {
	/**
	 * Take the {@link XObjectState} and links it as a child of this
	 * {@link XModelState}. Also sets this {@link XModelState} as the parent.
	 * Neither this fact nor the {@link XObjectState} itself is persisted by
	 * this operation.
	 * 
	 * @param objectState
	 */
	void addObjectState(XObjectState objectState);
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @throws IllegalStateException if the fieldState has no parent
	 *             {@link XAddress}.
	 */
	void delete();
	
	/**
	 * Returns the current revision number of this XModel.
	 * 
	 * @return The current revision number of this XModel.
	 */
	long getRevisionNumber();
	
	/**
	 * Checks whether this XModel already contains an XObject with the given
	 * XID.
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this XModels already contains an XObject with the given
	 *         XID, false otherwise
	 */
	boolean hasObjectState(XID id);
	
	boolean isEmpty();
	
	/**
	 * Get a {@link XObjectState} contained in this repository from the
	 * appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the object has not been deleted AND
	 * not been removed from this model. It is however not guaranteed to fail if
	 * the object has been removed.
	 */
	XObjectState getObjectState(XID id);
	
	/**
	 * Create a new {@link XObjectState} in the same persistence layer as this
	 * repository state.
	 */
	XObjectState createObjectState(XID id);
	
	/**
	 * Removes the given {@link XObjectState} from this {@link XModelState}.
	 * 
	 * @param objectState The {@link XObjectState} to be removed
	 */
	void removeObjectState(XID objectStateId);
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @throws IllegalStateException if the fieldState has no parent
	 *             {@link XAddress}.
	 */
	void save();
	
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * get the state of the change log which is logging this model.
	 * 
	 * @return the state of the change log which is logging this model
	 */
	XChangeLogState getChangeLogState();
	
}

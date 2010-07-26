package org.xydra.core.model.state;

import java.io.Serializable;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;


/**
 * An {@link XModelState} represents the inner state of an {@link XModel}, for
 * example for persistence purposes.
 * 
 * An {@link XModelState} stores the
 * <ul>
 * <li> {@link XID} of the {@link XModel}
 * <li>the revision number of the {@link XModel}
 * <li>the child-{@link XObject XObjects} of the {@link XModel} in form of their
 * {@link XObjectState XObjectStates}
 * </ul>
 * 
 * An {@link XModelState} can be serialized, and therefore be used e.g. in GWT.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XModelState extends IHasXID, Serializable, Iterable<XID>, IHasXAddress {
	/**
	 * Links the given {@link XObjectState} as a child of this XModelState. This
	 * means that the {@link XObject} represented by the given
	 * {@link XObjectState} is a child-{@link XObject} of the {@link XModel}
	 * represented by this XModelState. Also sets this XModelState as the
	 * parent. Neither this fact nor the {@link XObjectState} itself is
	 * persisted by this operation.
	 * 
	 * @param objectState The {@link XObjectState} which is to be added as a
	 *            child
	 */
	void addObjectState(XObjectState objectState);
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 */
	void delete();
	
	/**
	 * Gets the current revision number of the {@link XModel} which state is
	 * being represented by this XModelState.
	 * 
	 * @return The current revision number of the {@link XModel} which state is
	 *         being represented by this XModelState.
	 */
	long getRevisionNumber();
	
	/**
	 * Checks whether the {@link XModel} represented by this XModelState already
	 * contains an {@link XObject} with the given {@link XID} by checking
	 * whether this XModelState is linked with its {@link XObjectState}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if the {@link XModel} represented by this XModelState
	 *         already contains an {@link XObject} with the given {@link XID},
	 *         false otherwise
	 */
	boolean hasObjectState(XID id);
	
	/**
	 * Returns true, if this XModelState has no child-{@link XObjectState
	 * XObjectStates}
	 * 
	 * @return true, if this XModelState has no child-{@link XObjectState
	 *         XObjectStates}
	 */
	boolean isEmpty();
	
	/**
	 * Get the specified {@link XObjectState} contained in this XModelState from
	 * the appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the {@link XObject} represented by
	 * the requested {@link XObjectState} is not already deleted AND and was not
	 * removed from the {@link XModel} represented by this XModelState. It is
	 * however not guaranteed to fail if only the {@link XObject} was removed.
	 * 
	 * @param id The {@link XID} of the {@link XObject} which
	 *            {@link XObjectState} is to be returned
	 * @return The {@link XObjectState} corresponding to the given {@link XID}
	 *         or null if no such {@link XObjectState} exists
	 */
	XObjectState getObjectState(XID id);
	
	/**
	 * Creates a new {@link XObjectState} in the same persistence layer as this
	 * XModelState and adds it as a child of this state.
	 * 
	 * @param id The {@link XID} for the new {@link XObjectState}
	 * @return The newly created {@link XObjectState}
	 */
	XObjectState createObjectState(XID id);
	
	/**
	 * Removes the specified {@link XObjectState} from this XModelState.
	 * 
	 * @param objectStateID The {@link XID} of the {@link XObjectState} which is
	 *            to be removed
	 */
	void removeObjectState(XID objectStateId);
	
	/**
	 * Store this state information in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 */
	void save();
	
	/**
	 * Sets the stored revision number
	 * 
	 * @param revisionNumber the revision number
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * Gets the {@link XChangeLogState} of the {@link XChangeLog} which is
	 * logging the {@link XObject} represented by this XObjectState.
	 * 
	 * @return the {@link XChangeLogState} of the {@link XChangeLog} which is
	 *         logging the {@link XObject} represented by this XObjectState.
	 */
	XChangeLogState getChangeLogState();
	
}

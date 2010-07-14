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
	 * Take the {@link XObjectState} and links it as a child of this
	 * {@link XModelState}, which means that the {@link XObject} represented by
	 * the given {@link XObjectState} is a child-{@link XObject} of the
	 * {@link XModel} which is represented by this XModelState. Also sets this
	 * {@link XModelState} as the parent. Neither this fact nor the
	 * {@link XObjectState} itself is persisted by this operation.
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
	 *         being represented by this XFieldState.
	 */
	long getRevisionNumber();
	
	/**
	 * Checks whether the XModel represented by this {@link XModelState} already
	 * contains an {@link XObject} with the given {@link XID} by checking
	 * whether this XModelState is linked with its {@link XObjectState}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if the {@link XModel} already contains an {@link XObject}
	 *         with the given {@link XID}, false otherwise
	 */
	boolean hasObjectState(XID id);
	
	/**
	 * @return true, if this XModelState has no child-{@link XObjectState
	 *         XObjectStates}
	 */
	boolean isEmpty();
	
	/**
	 * Get a {@link XObjectState} contained in this repository from the
	 * appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the {@link XObject} represented by
	 * the requested {@link XObjectState} is not already deleted AND and was not
	 * removed from the {@link XModel} represented by this XModelState. It is
	 * however not guaranteed to fail if only the {@link XObject} was removed.
	 */
	XObjectState getObjectState(XID id);
	
	/**
	 * Create a new {@link XObjectState} in the same persistence layer as this
	 * XModelState and add it as a child of this state.
	 */
	XObjectState createObjectState(XID id);
	
	/**
	 * Removes the given {@link XObjectState} from this {@link XModelState}.
	 * 
	 * @param objectState The {@link XObjectState} which is to be removed
	 */
	void removeObjectState(XID objectStateId);
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 */
	void save();
	
	/**
	 * Sets the revision number
	 * 
	 * @param revisionNumber the revision number
	 */
	void setRevisionNumber(long revisionNumber);
	
	/**
	 * Gets the {@link XChangeLogState} of the {@link XChangeLog} which is
	 * logging the {@link XModel} represented by this XModelState.
	 * 
	 * @return the {@link XChangeLogState} of the {@link XChangeLog} which is
	 *         logging the {@link XModel} represented by this XModelState.
	 */
	XChangeLogState getChangeLogState();
	
}

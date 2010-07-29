package org.xydra.core.model.state;

import java.util.Iterator;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XChangeLog;
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
 * An {@link XObjectState} can be serialized, and therefore be used e.g. in GWT.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XObjectState extends IHasXID, Iterable<XID>, IHasXAddress {
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 */
	void delete();
	
	/**
	 * Store this state information in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 */
	void save();
	
	/**
	 * Returns an {@link Iterator} over the {@link XID}s of all children-
	 * {@link XFieldState XFieldStates} of this XObjectState
	 * 
	 * @returns an {@link Iterator} over the {@link XID}s of all children-
	 *          {@link XFieldState XFieldStates} of this XObjectState
	 */
	Iterator<XID> iterator();
	
	/**
	 * Take the {@link XFieldState} and links it as a child of this
	 * {@link XObjectState}. This means that the {@link XField} represented by
	 * the given {@link XFieldState} is a child-{@link XField} of the
	 * {@link XObject} represented by this XObjectState. Also sets this
	 * {@link XFieldState} as the parent. Neither this fact nor the
	 * {@link XFieldState} itself is persisted by this operation.
	 * 
	 * @param fieldState The {@link XFieldState} which is to be added as a child
	 */
	void addFieldState(XFieldState fieldState);
	
	/**
	 * Gets the current revision number of the {@link XObject} which state is
	 * being represented by this XObjectState.
	 * 
	 * @return The current revision number of the {@link XObject} which state is
	 *         being represented by this XObjectState.
	 */
	long getRevisionNumber();
	
	/**
	 * Checks whether the {@link XObject} represented by this XObjectState
	 * already contains an {@link XField} with the given {@link XID} by checking
	 * whether this XObjectState is linked with its {@link XFieldState}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if the {@link XObject} represented by this XObjectState
	 *         already contains an {@link XField} with the given {@link XID},
	 *         false otherwise
	 */
	boolean hasFieldState(XID id);
	
	/**
	 * Returns true, if this XObjectState has no child-{@link XFieldState
	 * XFieldStates}
	 * 
	 * @return true, if this XObjectState has no child-{@link XFieldState
	 *         XFieldStates}
	 */
	boolean isEmpty();
	
	/**
	 * Gets the specified {@link XFieldState} contained in this XObjectState
	 * from the appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the {@link XField} represented by
	 * the requested {@link XFieldState} is not already deleted AND and was not
	 * removed from the {@link XObject} represented by this XObjectState. It is
	 * however not guaranteed to fail if only the {@link XField} was removed.
	 * 
	 * @param id The {@link XID} of the {@link XField} which {@link XFieldState}
	 *            is to be returned
	 * @return The {@link XFieldState} corresponding to the given {@link XID} or
	 *         null if no such {@link XFieldState} exists
	 */
	XFieldState getFieldState(XID id);
	
	/**
	 * Creates a new {@link XFieldState} in the same persistence layer as this
	 * XObjectState and adds it as a child of this XObjectState.
	 * 
	 * @param id The {@link XID} for the new {@link XFieldState}
	 * @return The newly created {@link XFieldState}
	 */
	XFieldState createFieldState(XID id);
	
	/**
	 * Removes the specified {@link XFieldState} from this XObjectState.
	 * 
	 * @param objectStateID The {@link XID} of the {@link XFieldState} which is
	 *            to be removed
	 */
	void removeFieldState(XID fieldStateId);
	
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

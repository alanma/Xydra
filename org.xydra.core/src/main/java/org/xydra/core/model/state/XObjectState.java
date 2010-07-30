package org.xydra.core.model.state;

import java.util.Iterator;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


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
	 * Begin a simple transaction to prevent inconsistent states from being
	 * persisted.
	 * 
	 * Only the state backend itself can abort state transactions. null may be
	 * returned if the backend can guarantee that saving and deleting states is
	 * always possible. Otherwise, changes performed with this transaction
	 * object should not be persisted until the transaction object is passed to
	 * {@link #endTransaction()}.
	 * 
	 * Each state may only be part of at most one transaction at any given time.
	 * 
	 * The returned transaction object may only be used for this
	 * {@link XObjectState} as well as {@link XFieldState}s contained with this
	 * object.
	 */
	Object beginTransaction();
	
	/**
	 * Persist changes associated with the given transaction.
	 * 
	 * @param transaction must have been returned by {@link #beginTransaction()}
	 *            from this {@link XObjectState}
	 */
	void endTransaction(Object transaction);
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XObjectState} or
	 *            the containing {@link XModelState} or {@link XRepositoryState}
	 *            .
	 */
	void delete(Object transaction);
	
	/**
	 * Store this state information in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateFactory}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XObjectState} or
	 *            the containing {@link XModelState} or {@link XRepositoryState}
	 *            .
	 */
	void save(Object transaction);
	
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
	 * XObjectState and with an address contained within this object. This does
	 * not check if there is already such a state and does not add the created
	 * state as a child. The {@link XObject} implementation is responsible for
	 * doing so.
	 * 
	 * @param id The {@link XID} for the new {@link XFieldState}
	 * @return The newly created {@link XFieldState}
	 */
	XFieldState createFieldState(XID id);
	
	/**
	 * Removes the specified {@link XFieldState} from this XObjectState. This
	 * does not remove the actual {@link XFieldState} from the state backend,
	 * only the reference from this state. To cleanup the state use
	 * {@link XFieldState#delete(Object)}
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

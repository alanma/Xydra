package org.xydra.core.model.state;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXID;
import org.xydra.base.XID;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


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
	 * Implementations should not persist this change until the corresponding
	 * save unless they can guarantee that no other state calls will fail.
	 * 
	 * @param objectState The {@link XObjectState} which is to be added as a
	 *            child
	 */
	void addObjectState(XObjectState objectState);
	
	/**
	 * Begin a simple transaction to prevent inconsistent states from being
	 * persisted.
	 * 
	 * Only the state backend itself can abort state transactions. null may be
	 * returned if the backend can guarantee that saving and deleting states is
	 * always possible. Otherwise, changes performed with this transaction
	 * object should not be persisted until the transaction object is passed to
	 * {@link #endTransaction(XStateTransaction)}.
	 * 
	 * Each state may only be part of at most one transaction at any given time.
	 * 
	 * The returned transaction object may only be used for this
	 * {@link XModelState} as well as {@link XObjectState}s and
	 * {@link XFieldState}s contained with this model.
	 */
	XStateTransaction beginTransaction();
	
	/**
	 * Creates a new {@link XObjectState} in the same persistence layer as this
	 * XModelState and with an address contained within this model. This does
	 * not check if there is already such a state and does not add the created
	 * state as a child. The {@link XModel} implementation is responsible for
	 * doing so.
	 * 
	 * @param id The {@link XID} for the new {@link XObjectState}
	 * @return The newly created {@link XObjectState}
	 */
	XObjectState createObjectState(XID id);
	
	/**
	 * Delete this state information from the attached persistence layer that
	 * was used to create it.
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XModelState} or
	 *            the containing {@link XRepositoryState}.
	 */
	void delete(XStateTransaction transaction);
	
	/**
	 * Persist changes associated with the given transaction.
	 * 
	 * @param transaction must have been returned by {@link #beginTransaction()}
	 *            from this {@link XModelState}
	 */
	void endTransaction(XStateTransaction transaction);
	
	/**
	 * Gets the {@link XChangeLogState} of the {@link XChangeLog} which is
	 * logging the {@link XModel} represented by this XModelState.
	 * 
	 * @return the {@link XChangeLogState} of the {@link XChangeLog} which is
	 *         logging the {@link XModel} represented by this XModelState.
	 */
	XChangeLogState getChangeLogState();
	
	/**
	 * Gets the specified {@link XObjectState} contained in this XModelState
	 * from the appropriate persistence layer.
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
	 * Returns an {@link Iterator} over the {@link XID XIDs} of all children-
	 * {@link XObjectState XObjectStates} of this XModelState
	 * 
	 * @returns an {@link Iterator} over the {@link XID XIDs} of all children-
	 *          {@link XObjectState XObjectStates} of this XModelState
	 */
	Iterator<XID> iterator();
	
	/**
	 * Removes the specified {@link XObjectState} from this XModelState. This
	 * does not remove the actual {@link XObjectState} from the state backend,
	 * only the reference from this state. To cleanup the state use
	 * {@link XObjectState#delete(XStateTransaction)}
	 * 
	 * Implementations should not persist this change until the corresponding
	 * save unless they can guarantee that no other state calls will fail.
	 * 
	 * @param objectStateID The {@link XID} of the {@link XObjectState} which is
	 *            to be removed
	 */
	void removeObjectState(XID objectStateId);
	
	/**
	 * Store this state information in the attached persistence layer that was
	 * used to create it.
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XModelState} or
	 *            the containing {@link XRepositoryState}.
	 */
	void save(XStateTransaction transaction);
	
	/**
	 * Sets the stored revision number
	 * 
	 * Implementations should not persist this change until the corresponding
	 * save unless they can guarantee that no other state calls will fail.
	 * 
	 * @param revisionNumber the revision number
	 */
	void setRevisionNumber(long revisionNumber);
	
}

package org.xydra.core.model.state;

import java.util.Iterator;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XRepositoryState} represents the inner state of an
 * {@link XRepository}, for example for persistence purposes.
 * 
 * An {@link XRepositoryState} stores the
 * <ul>
 * <li> {@link XID} of the {@link XRepository}
 * <li>the revision number of the {@link XRepository}
 * <li>the child-{@link XModel XModels} of the {@link XRepository} in form of
 * their {@link XModelState XModelStates}
 * </ul>
 * 
 * An {@link XRepositoryState} can be serialized, and therefore be used e.g. in
 * GWT.
 * 
 * @author voelkel
 * 
 */
public interface XRepositoryState extends IHasXID, Iterable<XID>, IHasXAddress {
	
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
	 * {@link XRepositoryState} as well as {@link XModelState}s,
	 * {@link XObjectState}s and {@link XFieldState}s contained with this
	 * repository.
	 */
	Object beginTransaction();
	
	/**
	 * Persist changes associated with the given transaction.
	 * 
	 * @param transaction must have been returned by {@link #beginTransaction()}
	 *            from this {@link XRepositoryState}
	 */
	void endTransaction(Object transaction);
	
	/**
	 * Store this state information in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateStore#c}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XRepositoryState}
	 *            .
	 */
	void save(Object transaction);
	
	/**
	 * Delete this state information from the attached persistence layer, i.e.
	 * the one determined by calling {@link XStateStore}.create...().
	 * 
	 * @param transaction If not null, persist the change at the end of the
	 *            given transaction, otherwise persist it now. The transaction
	 *            object must have been created by this {@link XRepositoryState}
	 *            .
	 */
	void delete(Object transaction);
	
	/**
	 * Links the given {@link XModelState} as a child of this XRepositoryState.
	 * This means that the {@link XModel} represented by the given
	 * {@link XModelState} is a child-{@link XModel} of the {@link XRepository}
	 * represented by this XRepositoryState. Also sets this XRepositoryState as
	 * the parent. Neither this fact nor the {@link XModelState} itself is
	 * persisted by this operation.
	 * 
	 * @param modelState The {@link XModelState} which is to be added as a child
	 */
	void addModelState(XModelState modelState);
	
	/**
	 * Get the specified {@link XModelState} contained in this XRepositoryState
	 * from the appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the {@link XModel} represented by
	 * the requested {@link XModelState} is not already deleted AND and was not
	 * removed from the {@link XRepository} represented by this
	 * XRepositoryState. It is however not guaranteed to fail if only the
	 * {@link XModel} was removed.
	 * 
	 * @param id The {@link XID} of the {@link XModel} which {@link XModelState}
	 *            is to be returned
	 * @return The {@link XModelState} corresponding to the given {@link XID} or
	 *         null if no such {@link XModelState} exists
	 */
	XModelState getModelState(XID id);
	
	/**
	 * Create a new {@link XModelState} in the same persistence layer as this
	 * XRepositoryState and with an address contained within this repository.
	 * This does not check if there is already such a state and does not add the
	 * created state as a child. The {@link XRepository} implementation is
	 * responsible for doing so.
	 * 
	 * @param id The {@link XID} for the new {@link XModelState}
	 * @return The newly created {@link XModelState}
	 */
	XModelState createModelState(XID id);
	
	/**
	 * Checks whether the {@link XRepository} represented by this XModelState
	 * already contains an {@link XModel} with the given {@link XID} by checking
	 * whether this XRepositoryState is linked with its {@link XModelState}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if the {@link XRepository} represented by this
	 *         XRepositoryState already contains an {@link XModel} with the
	 *         given {@link XID}, false otherwise
	 */
	boolean hasModelState(XID id);
	
	/**
	 * Returns true, if this XRepositoryState has no child-{@link XModelState
	 * XModelStates}
	 * 
	 * @return true, if this XRepositoryState has no child-{@link XModelState
	 *         XModelStates}
	 */
	boolean isEmpty();
	
	/**
	 * Removes the specified {@link XModelState} from this XRepositoryState.
	 * This does not remove the actual {@link XModelState} from the state
	 * backend, only the reference from this state. To cleanup the state use
	 * {@link XModelState#delete(Object)}
	 * 
	 * @param modelStateID The {@link XID} of the {@link XModelState} which is
	 *            to be removed
	 */
	void removeModelState(XID modelStateId);
	
	/**
	 * Returns an {@link Iterator} over the {@link XID XIDs} of all children-
	 * {@link XModelState XModelStates} of this XRepositoryState
	 * 
	 * @returns an {@link Iterator} over the {@link XID XIDs} of all children-
	 *          {@link XModelState XModelStates} of this XRepositoryState
	 */
	Iterator<XID> iterator();
	
}

package org.xydra.core.model.state;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XID;


/**
 * Manages a set of {@link XModelState}s.
 * 
 * It has no revisionNumber and no parent. The repository state's {@link XID} is
 * only for data management purposes, not for referencing.
 * 
 * @author voelkel
 * 
 */
public interface XRepositoryState extends IHasXID, Iterable<XID>, IHasXAddress {
	
	/**
	 * Store the data of this object in the attached persistence layer, i.e. the
	 * one determined by calling {@link XStateStore#c}.create...().
	 */
	void save();
	
	/**
	 * Delete the data of this repository from the attached persistence layer,
	 * i.e. the one determined by calling {@link XStateStore}.create...(). Does
	 * not delete children (modelStates).
	 */
	void delete();
	
	/**
	 * Take the {@link XModelState} and links it as a child of this repository.
	 * Also sets this repository as the parent. Neither this fact nor the
	 * {@link XModelState} itself is persisted by this operation.
	 * 
	 * @param modelState
	 */
	void addModelState(XModelState modelState);
	
	/**
	 * Get a {@link XModelState} contained in this repository from the
	 * appropriate persistence layer.
	 * 
	 * This is only guaranteed to succeed if the model has not been deleted AND
	 * not been removed from this repository. It is however not guaranteed to
	 * fail if the model has been removed.
	 * 
	 */
	XModelState getModelState(XID id);
	
	/**
	 * Create a new {@link XModelState} in the same persistence layer as this
	 * repository state.
	 * 
	 * @param id
	 * @return
	 */
	XModelState createModelState(XID id);
	
	/**
	 * Checks whether this XRepository contains an {@link XModelState} with the
	 * given XID.
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this XRepository contains an {@link XModelState} with
	 *         the given XID, false otherwise
	 */
	boolean hasModelState(XID id);
	
	/**
	 * Returns true, if this repository has no child-modelStates
	 * 
	 * @return true, if this repository has no child-modelStates
	 */
	boolean isEmpty();
	
	/**
	 * Removes the given {@link XModelState} from this {@link XRepositoryState}.
	 * 
	 * @param model The {@link XModelState} to b removed
	 */
	void removeModelState(XID modelStateId);
	
}

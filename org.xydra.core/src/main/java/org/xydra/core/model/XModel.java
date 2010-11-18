package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.model.state.XModelState;


/**
 * An XModel is the core concept of Xydra. An XModel may be stored in an
 * {@link XRepository} or it may live independently. It may hold as many
 * {@link XObject XObjects} as you like. An XModel is typically used to model a
 * bigger structure, for example if we'd write a phone book application we might
 * use an XModel to represent everyone whose name starts with 'A' and use its
 * {@link XObject XObjects} to model the persons or we might even model the
 * whole phone book with one simple XModel.
 * 
 * An {@link XModel} can be serialized, and hence can be used e.g. in GWT.
 * 
 * Implementations of XModel should use an {@link XModelState} for storing and
 * representing the inner state of the XModel to allow maximum persistence
 * management flexibility.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XModel extends XLoggedModel, Serializable, XSynchronizesChanges {
	
	/**
	 * Returns the {@link XObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XObject} which is to be returned
	 * @return The {@link XObject} with the given {@link XID} or null, if no
	 *         corresponding {@link XObject} exists
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ReadOperation
	XObject getObject(XID objectId);
	
	/**
	 * Creates a new {@link XObject} with the given {@link XID} and adds it to
	 * this XModel or returns the already existing {@link XObject} if the given
	 * {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XObject} which is to be created
	 * 
	 * @return the newly created {@link XObject} or the already existing
	 *         {@link XObject} if the given {@link XID} was already taken
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	XObject createObject(XID id);
	
	/**
	 * Removes the {@link XObject} with the given {@link XID} from this XModel.
	 * 
	 * @param object The {@link XID} of the {@link XObject} which is to be
	 *            removed
	 * 
	 * @return true, if an {@link XObject} with the given {@link XID} did exist
	 *         in this XModel and could be removed
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	boolean removeObject(XID objectID);
	
	/**
	 * Executes the given {@link XModelCommand} if possible.
	 * 
	 * This method will fail if, the given {@link XModelCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XModelCommand}: the specified {@link XObject} does
	 * not exist and therefore cannot be removed
	 * <li>Add-type {@link XModelCommand}: the given {@link XID} is already
	 * taken and therefore a new {@link XObject} with this {@link XID} cannot be
	 * created
	 * <li>the model-{@link XID} in the {@link XModelCommand} does not concur
	 * with the {@link XID} of this XModel
	 * </ul>
	 * 
	 * @param command The {@link XModelCommand} which is to be executed
	 * 
	 * @return {@link XCommand#FAILED} if executing the {@link XModelCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XModelCommand} didn't change anything or if executing the
	 *         {@link XModelCommand} succeeded the revision number of the
	 *         {@link XModelEvent} caused by the {@link XModelCommand}.
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ModificationOperation
	long executeModelCommand(XModelCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getActor();
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * model.
	 * 
	 * @param actor for this model and its children, if any.
	 */
	void setActor(XID actor);
	
}

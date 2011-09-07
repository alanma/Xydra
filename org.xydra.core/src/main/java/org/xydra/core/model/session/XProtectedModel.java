package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.store.AccessException;


/**
 * An XProtectedModel is a wrapper (Decorator) for an {@link XModel} which links
 * the {@link XModel} with a specific actor (represented by its {@link XID}) and
 * automatically checks the access rights for this actor on the {@link XModel},
 * if a method is called and only executes the method, if the actor is allowed
 * to execute it (otherwise {@link AccessException XAccessExceptions} will be
 * thrown).
 * 
 * All change operations like adding new {@link XField XFields} executed on an
 * XProtectedModel will directly affect the wrapped {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedModel extends XLoggedModel, XExecutesCommands {
	
	/**
	 * Creates a new {@link XObject} with the given {@link XID} and adds it to
	 * this XProtecedModel or returns the already existing {@link XObject} if
	 * the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XObject} which is to be created
	 * @return the newly created {@link XObject} or the already existing
	 *         {@link XObject} if the given {@link XID} was already taken as an
	 *         {@link XProtectedObject} linked with the actor of this
	 *         XProtectedModel
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@Override
    @ModificationOperation
	XProtectedObject createObject(XID id);
	
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
	 * @return {@link XCommand#FAILED} if executing the {@link XModelCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XModelCommand} didn't change anything or if executing the
	 *         {@link XModelCommand} succeeded the revision number of the
	 *         {@link XModelEvent} caused by the {@link XModelCommand}.
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
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
	 * Returns the {@link XObject} contained in this model with the given
	 * {@link XID} wrapped as an {@link XProtectedObject} linked with the actor
	 * of this XProtectedModel.
	 * 
	 * @param id The {@link XID} of the {@link XObject} which is to be returned
	 * @return The {@link XObject} with the given {@link XID} or null, if no
	 *         corresponding {@link XObject} exists
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	@Override
    @ReadOperation
	XProtectedObject getObject(XID objectId);
	
}

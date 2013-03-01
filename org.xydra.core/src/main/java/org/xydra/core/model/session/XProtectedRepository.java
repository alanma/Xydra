package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.store.AccessException;


/**
 * An XProtectedRepository is a wrapper (Decorator) for an {@link XRepository}
 * which links the {@link XRepository} with a specific actor (represented by its
 * {@link XId}) and automatically checks the access rights for this actor on the
 * {@link XRepository}, if a method is called and only executes the method, if
 * the actor is allowed to execute it (otherwise {@link AccessException
 * XAccessExceptions} will be thrown).
 * 
 * All change operations like adding new {@link XModel XModels} executed on an
 * XProtectedRepository will directly affect the wrapped {@link XRepository}.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedRepository extends XReadableRepository, XSendsRepositoryEvents,
        XSendsModelEvents, XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,
        XExecutesCommands, XWritableRepository {
	
	/**
	 * Creates a new {@link XModel} with the given {@link XId} and adds it to
	 * this XProtectedRepository or returns the already existing {@link XModel}
	 * if the given {@link XId} was already taken.
	 * 
	 * @param id The {@link XId} for the {@link XModel} which is to be created
	 * @return the newly created {@link XModel} or the already existing
	 *         {@link XModel} if the given {@link XId} was already taken (both
	 *         as an XProtected... linked with the actor of this
	 *         XProtectedObject)
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@Override
    @ModificationOperation
	XProtectedModel createModel(XId id);
	
	/**
	 * Executes the given {@link XRepositoryCommand} if possible.
	 * 
	 * This method will fail if, the given {@link XRepositoryCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XRepositoryCommand}: the specified {@link XModel}
	 * does not exist and therefore cannot be removed
	 * <li>Add-type {@link XRepositoryCommand}: the given {@link XId} is already
	 * taken and therefore a new {@link XModel} with this {@link XId} cannot be
	 * created
	 * <li>the repository-{@link XId} in the {@link XRepositoryCommand} does not
	 * concur with the {@link XId} of this XProtectedRepository
	 * </ul>
	 * 
	 * @param command The {@link XRepositoryCommand} which is to be executed
	 * @return {@link XCommand#FAILED} if executing the
	 *         {@link XRepositoryCommand} failed, {@link XCommand#NOCHANGE} if
	 *         executing the {@link XRepositoryCommand} didn't change anything
	 *         or if executing the {@link XRepositoryCommand} succeeded the
	 *         revision number of the {@link XRepositoryEvent} caused by the
	 *         {@link XRepositoryCommand}.
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@ModificationOperation
	long executeRepositoryCommand(XRepositoryCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XId getActor();
	
	/**
	 * Returns the {@link XModel} contained in this repository with the given
	 * {@link XId} as an {@link XProtectedModel} linked with the actor of this
	 * XProtectedRepository.
	 * 
	 * @param id The {@link XId} of the {@link XModel} which is to be returned
	 * @return the {@link XModel} with the given {@link XId} as an
	 *         {@link XProtectedModel} linked with the actor of this
	 *         XProtectedRepository or null if no such {@link XReadableModel}
	 *         exists in this repository
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	@Override
    @ReadOperation
	XProtectedModel getModel(XId id);
	
}

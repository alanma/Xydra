package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A repository manages a set of {@link XModel XModels}.
 * 
 * Implementations of XRepository should use an {@link XRevWritableRepository}
 * for storing and representing the inner state of the XRepository to allow
 * maximum persistence management flexibility.
 * 
 * @author voelkel
 * 
 */
public interface XRepository extends XWritableRepository, XSendsRepositoryEvents,
        XSendsModelEvents, XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,
        XExecutesCommands {
	
	/**
	 * Creates a new {@link XModel} with the given {@link XId} and adds it to
	 * this XRepository or returns the already existing {@link XModel} if the
	 * given {@link XId} was already taken.
	 * 
	 * @param id The {@link XId} for the {@link XModel} which is to be created
	 * 
	 * @return the newly created {@link XModel} or the already existing
	 *         {@link XModel} if the given {@link XId} was already taken
	 */
	@Override
	@ModificationOperation
	XModel createModel(XId id);
	
	/**
	 * Execute the given {@link XCommand} if possible.
	 * 
	 * Not all implementations will be able to execute all commands.
	 * 
	 * @param command The {@link XCommand} which is to be executed
	 * @param callback
	 * 
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the {@link XEvent} caused by the
	 *         command.
	 */
	@ModificationOperation
	long executeCommand(XCommand command, XLocalChangeCallback callback);
	
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
	 * concur with the {@link XId} of this XRepository
	 * </ul>
	 * 
	 * @param command The {@link XRepositoryCommand} which is to be executed
	 * 
	 * @return {@link XCommand#FAILED} if executing the
	 *         {@link XRepositoryCommand} failed, {@link XCommand#NOCHANGE} if
	 *         executing the {@link XRepositoryCommand} didn't change anything
	 *         or if executing the {@link XRepositoryCommand} succeeded the
	 *         revision number of the {@link XRepositoryEvent} caused by the
	 *         {@link XRepositoryCommand}.
	 * 
	 *         TODO what is returned if a model has been successfully removed?
	 */
	@ModificationOperation
	long executeRepositoryCommand(XRepositoryCommand command);
	
	/**
	 * Returns the {@link XModel} contained in this repository with the given
	 * {@link XId}
	 * 
	 * @param id The {@link XId} of the {@link XModel} which is to be returned
	 * @return the {@link XModel} with the given {@link XId} or null if no such
	 *         {@link XModel} exists in this repository.
	 */
	@Override
	@ReadOperation
	XModel getModel(XId id);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 * 
	 *         TODO can this be null?
	 */
	XId getSessionActor();
	
	/**
	 * Removes the specified {@link XModel} from this XRepository.
	 * 
	 * @param repository The {@link XId} of the {@link XModel} which is to be
	 *            removed
	 * 
	 * @return true, if the specified {@link XModel} could be removed, false
	 *         otherwise
	 */
	@Override
	@ModificationOperation
	boolean removeModel(XId modelId);
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * repository. Recursively sets the session actor for all child
	 * {@link XModel}, {@link XObject}, and {@link XField}.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * 
	 * @param actor for this repository and its children, if any.
	 */
	void setSessionActor(XId actorId, String passwordHash);
	
}

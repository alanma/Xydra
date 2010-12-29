package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.state.XRepositoryState;


/**
 * A repository manages a set of {@link XModel XModels}.
 * 
 * Implementations of XRepository should use an {@link XRepositoryState} for
 * storing and representing the inner state of the XRepository to allow maximum
 * persistence management flexibility.
 * 
 * @author voelkel
 * 
 */
public interface XRepository extends XWritableRepository, XSendsRepositoryEvents, XSendsModelEvent,
        XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents, XExecutesCommands {
	
	/**
	 * Returns the {@link XModel} contained in this repository with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XModel} which is to be returned
	 * @return the {@link XModel} with the given {@link XID} or null if no such
	 *         {@link XModel} exists in this repository.
	 */
	@ReadOperation
	XModel getModel(XID id);
	
	/**
	 * Creates a new {@link XModel} with the given {@link XID} and adds it to
	 * this XRepository or returns the already existing {@link XModel} if the
	 * given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XModel} which is to be created
	 * 
	 * @return the newly created {@link XModel} or the already existing
	 *         {@link XModel} if the given {@link XID} was already taken
	 */
	@ModificationOperation
	XModel createModel(XID id);
	
	/**
	 * Removes the specified {@link XModel} from this XRepository.
	 * 
	 * @param repository The {@link XID} of the {@link XModel} which is to be
	 *            removed
	 * 
	 * @return true, if the specified {@link XModel} could be removed, false
	 *         otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID modelID);
	
	/**
	 * Executes the given {@link XRepositoryCommand} if possible.
	 * 
	 * This method will fail if, the given {@link XRepositoryCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XRepositoryCommand}: the specified {@link XModel}
	 * does not exist and therefore cannot be removed
	 * <li>Add-type {@link XRepositoryCommand}: the given {@link XID} is already
	 * taken and therefore a new {@link XModel} with this {@link XID} cannot be
	 * created
	 * <li>the repository-{@link XID} in the {@link XRepositoryCommand} does not
	 * concur with the {@link XID} of this XRepository
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
	 */
	@ModificationOperation
	long executeRepositoryCommand(XRepositoryCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getSessionActor();
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * repository. Recursively sets the session actor for all child
	 * {@link XModel}, {@link XObject}, and {@link XField}.
	 * 
	 * @param actor for this repository and its children, if any.
	 */
	void setSessionActor(XID actorId, String passwordHash);
	
	/**
	 * Execute the given {@link XCommand} if possible.
	 * 
	 * Not all implementations will be able to execute all commands.
	 * 
	 * @param command The {@link XCommand} which is to be executed
	 * 
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the {@link XEvent} caused by the
	 *         command.
	 */
	@ModificationOperation
	long executeCommand(XCommand command, XLocalChangeCallback callback);
	
}

package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A repository manages a set of {@link XModel XModels}.
 * 
 * @author voelkel
 * 
 */
public interface XRepository extends XBaseRepository, XSendsRepositoryEvents, XSendsModelEvent,
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
	 * @param actor The {@link XID} of the actor
	 * @param id The {@link XID} for the {@link XModel} which is to be created
	 * @return the newly created {@link XModel} or the already existing
	 *         {@link XModel} if the given {@link XID} was already taken
	 */
	@ModificationOperation
	XModel createModel(XID actor, XID id);
	
	/**
	 * Removes the specified {@link XModel} from this XRepository.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param model The {@link XID} of the {@link XModel} which is to be removed
	 * @return true, if the specified {@link XModel} could be removed, false
	 *         otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID actor, XID modelID);
	
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
	 * @param command The {@link XRepositoryCommand} which to be executed
	 * @return {@link XCommand#FAILED} if executing the
	 *         {@link XRepositoryCommand} failed, {@link XCommand#NOCHANGE} if
	 *         executing the {@link XRepositoryCommand} didn't change anything
	 *         or if executing the {@link XRepositoryCommand} succeeded the
	 *         revision number of the {@link XRepositoryEvent} caused by the
	 *         {@link XRepositoryCommand}.
	 */
	@ModificationOperation
	long executeRepositoryCommand(XID actor, XRepositoryCommand command);
	
}

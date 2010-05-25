package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;



/**
 * A repository manages a set of {@link XModel}s.
 * 
 * @author voelkel
 * 
 */
public interface XRepository extends XBaseRepository, XSendsRepositoryEvents, XSendsModelEvent,
        XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents, XExecutesCommands {
	
	/**
	 * @param id
	 * @return the XModel with the given id or null if no such model exists in
	 *         this repository.
	 */
	@ReadOperation
	XModel getModel(XID id);
	
	/**
	 * @param actor
	 * @param id
	 * @return an {@link XModel} created in the repository. If the {@link XID}
	 *         was already in use for another model, that {@link XModel} is
	 *         returned instead.
	 */
	@ModificationOperation
	XModel createModel(XID actor, XID id);
	
	/**
	 * Removes the given XModel from this XRepository.
	 * 
	 * @param actor The XID of the actor
	 * @param model The XID of the XModel which is to be removed
	 * @return true, if the specified model could be removed, false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID actor, XID modelID);
	
	/**
	 * Executes the given command if possible.
	 * 
	 * This method will fail if, - the given command cannot be executed (i.e.
	 * the specified model does not exist, and therefore cannot be removed
	 * [delete], or the given XID is already taken and therefore a new model
	 * with this XID cannot be created [add]) - the repository-XID in the
	 * command does not concur with the XID of this repository
	 * 
	 * @param command The command to be executed
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or {@link XCommand#CHANGED}.
	 */
	@ModificationOperation
	long executeRepositoryCommand(XID actor, XRepositoryCommand command);
	
}

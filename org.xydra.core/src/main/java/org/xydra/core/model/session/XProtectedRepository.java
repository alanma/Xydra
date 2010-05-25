package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsRepositoryEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;



/**
 * A wrapper for an {@link XRepository} for a specific actor.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedRepository extends XBaseRepository, XSendsRepositoryEvents,
        XSendsModelEvent, XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,
        XProtectedExecutesCommands {
	
	/**
	 * @param id
	 * @return the model with the given id or null if no such model exists in
	 *         this repository.
	 */
	@ReadOperation
	XProtectedModel getModel(XID id);
	
	/**
	 * @param id
	 * @return an model created in the repository. If the {@link XID} was
	 *         already in use for another model, that model is returned instead.
	 */
	@ModificationOperation
	XProtectedModel createModel(XID id);
	
	/**
	 * Removes the given model from this repository.
	 * 
	 * @param model The XID of the model which is to be removed
	 * @return true, if the specified model could be removed, false otherwise
	 */
	@ModificationOperation
	boolean removeModel(XID modelID);
	
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
	long executeRepositoryCommand(XRepositoryCommand command);
	
}

package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * A wrapper for an {@link XModel} for a specific actor.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedModel extends XLoggedModel, XProtectedExecutesTransactions,
        XProtectedExecutesCommands {
	
	/**
	 * @param id The XID of the wanted object
	 * @return The object with the given XID or null, if no corresponding
	 *         {@link XObject} exists
	 */
	@ReadOperation
	XProtectedObject getObject(XID objectId);
	
	/**
	 * Creates a new object and adds it to this model.
	 * 
	 * @param id The XID of the object to be created
	 * @return the created object or the already existing object with the given
	 *         XID
	 */
	@ModificationOperation
	XProtectedObject createObject(XID id);
	
	/**
	 * Removes the given object from this model.
	 * 
	 * @param object The XID of the object which is to be removed
	 * @return true, if the given object did exist in the model and could be
	 *         removed
	 */
	@ModificationOperation
	boolean removeObject(XID objectID);
	
	/**
	 * Executes the given command if possible.
	 * 
	 * This method will fail if,
	 * <ul>
	 * <li>the given command cannot be executed (i.e. the specified field does
	 * not exist, and therefore cannot be removed [delete], or the given XID is
	 * already taken and therefore a new field with this XID cannot be created
	 * [add])
	 * <li>the model-XID in the command does not concur with the XID of this
	 * model
	 * </ul>
	 * 
	 * @param command The command to be executed
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeModelCommand(XModelCommand command);
	
}

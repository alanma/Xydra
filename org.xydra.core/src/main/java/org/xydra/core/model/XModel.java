package org.xydra.core.model;

import java.io.Serializable;
import java.util.List;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelCommand;


/**
 * {@link XModel}s can be stored in a {@link XRepository}, but they can equally
 * live independently.
 * 
 * An {@link XModel} is a set of {@link XObject}s.
 * 
 * An {@link XModel} can be serialised, and hence can be used e.g. in GWT.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XModel extends XLoggedModel, Serializable, XExecutesTransactions,
        XExecutesCommands {
	
	/**
	 * @param id The XID of the wanted {@link XObject}
	 * @return The {@link XObject} with the given XID or null, if no
	 *         corresponding {@link XObject} exists
	 */
	@ReadOperation
	XObject getObject(XID objectId);
	
	/**
	 * Creates a new XObject and adds it to this XModel.
	 * 
	 * @param actor The XID of the actor
	 * @param id The XID of the XObject to be created
	 * @return the created object or the already existing object with the given
	 *         XID
	 */
	@ModificationOperation
	XObject createObject(XID actor, XID id);
	
	/**
	 * Removes the given XObject from this XModel.
	 * 
	 * @param actor The XID of the actor
	 * @param object The XID of the XObject which is to be removed
	 * @return true, if the given object did exist in the model and could be
	 *         removed
	 */
	@ModificationOperation
	boolean removeObject(XID actor, XID objectID);
	
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
	long executeModelCommand(XID actor, XModelCommand command);
	
	/**
	 * Roll back the model state (including revisions) to a specific revision.
	 * This will erase all events following this revision from the change log.
	 * Listeners are not automatically restored, but events are sent out for all
	 * changes made.
	 */
	void rollback(long revision);
	
	/**
	 * Roll back to the given lastRevision, apply the remoteChanges and (re)
	 * apply the localChanges. Only sends out as few events as possible and
	 * preserve listeners on entities that are temporarily removed but adjusts
	 * the change log to look as it will on the server.
	 * 
	 * @param remoteChanges The remote changes that since the last sync,
	 *            including local changes that have been saved remotely.
	 * @param lastRevision The revision to insert the remoteChanges at.
	 * @param localChanges Local changes that haven't been saved remotely yet.
	 *            This list will be modified with updated commands.
	 * @return the results for the localChanges
	 */
	long[] syncChanges(List<XEvent> remoteChanges, long lastRevision, XID actor,
	        List<XCommand> localChanges);
	
}

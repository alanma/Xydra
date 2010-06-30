package org.xydra.core.model;

import java.io.Serializable;
import java.util.List;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;


/**
 * An XModel is the core concept of Xydra. An XModel may be stored in an
 * {@link XRepository} or it may live independently. It may hold as many
 * {@link XObject XObjects} as you like. An XModel is typically used to model a
 * bigger structure, for example if we'd write a phone book application we might
 * use an XModel to represent everyone whose name starts with 'A' and use its
 * {@link XObject XObjects} to model the persons or we might even model the
 * whole phone book with one simple XModel.
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
	 * Returns the {@link XObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XObject} which is to be returned
	 * @return The {@link XObject} with the given {@link XID} or null, if no
	 *         corresponding {@link XObject} exists
	 */
	@ReadOperation
	XObject getObject(XID objectId);
	
	/**
	 * Creates a new {@link XObject} with the given {@link XID} and adds it to
	 * this XModel or returns the already existing {@link XObject} if the given
	 * {@link XID} was already taken.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param id The {@link XID} for the {@link XObject} which is to be created
	 * @return the newly created {@link XObject} or the already existing
	 *         {@link XObject} if the given {@link XID} was already taken
	 */
	@ModificationOperation
	XObject createObject(XID actor, XID id);
	
	/**
	 * Removes the {@link XObject} with the given {@link XID} from this XModel.
	 * 
	 * @param actor The {@link XID} of the actor
	 * @param object The {@link XID} of the {@link XObject} which is to be
	 *            removed
	 * @return true, if an {@link XObject} with the given {@link XID} did exist
	 *         in this XModel and could be removed
	 */
	@ModificationOperation
	boolean removeObject(XID actor, XID objectID);
	
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
	 * @param command The {@link XModelCommand} which to be executed
	 * @return {@link XCommand#FAILED} if executing the {@link XModelCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XModelCommand} didn't change anything or if executing the
	 *         {@link XModelCommand} succeeded the revision number of the
	 *         {@link XModelEvent} caused by the {@link XModelCommand}.
	 */
	@ModificationOperation
	long executeModelCommand(XID actor, XModelCommand command);
	
	/**
	 * Roll back the model state (including revisions) to a specific revision.
	 * This will erase all {@link XEvent XEvents} following this revision from
	 * the {@link XChangeLog} of this XModel. Listeners that were/are registered
	 * to the entities that are manipulated by this rollback are not
	 * automatically restored or removed, but {@link XEvent XEvents} are sent
	 * out for all changes made.
	 */
	void rollback(long revision);
	
	/**
	 * Roll back to the given lastRevision, apply the remoteChanges and (re)
	 * apply the localChanges. Only sends out as few {@link XEvent XEvents} as
	 * possible and preserve listeners on entities that are temporarily removed
	 * but adjusts the {@link XChangeLog} to look as it will on the server.
	 * 
	 * This method will not check that the localChanges have already been
	 * applied previously. It will just throw away any changes after
	 * lastRevision, apply the remoteChanges and then apply the localChanges. No
	 * redundant {@link XEvent XEvents} are changed and {@link XObject} and
	 * {@link XField} objects that are temporarily removed are preserved,
	 * including any registered listeners.
	 * 
	 * @param remoteChanges The remote changes that happended since the last
	 *            sync, including local changes that have been saved remotely.
	 * @param lastRevision The revision to insert the remoteChanges at.
	 * @param localChanges Local changes that haven't been saved remotely yet.
	 *            This list will be modified with updated commands.
	 * @return the results for the localChanges
	 */
	long[] synchronize(List<XEvent> remoteChanges, long lastRevision, XID actor,
	        List<XCommand> localChanges);
	
}

package org.xydra.core.model;

import java.util.List;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions} and synchronize remote changes.
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public interface XSynchronizesChanges extends XExecutesCommands, XExecutesTransactions,
        IHasXAddress {
	
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
	        List<XCommand> localChanges, List<? extends XSynchronizationCallback> callbacks);
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this model or object.
	 */
	XChangeLog getChangeLog();
	
}

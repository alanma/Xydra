package org.xydra.store.impl.gae.changes;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;


public interface IGaeChangesService {
	
	/**
	 * Execute the given {@link XCommand} as a transaction.
	 * 
	 * // IMPROVE maybe let the caller provide an XID that can be used to check
	 * // the status in case there is a GAE timeout?
	 * 
	 * @param command The command to execute. (can be a {@link XTransaction})
	 * @param actorId The actor to log in the resulting event.
	 * @return If the command executed successfully, the revision of the
	 *         resulting {@link XEvent} or {@link XCommand#NOCHANGE} if the
	 *         command din't change anything; {@link XCommand#FAILED} otherwise.
	 * 
	 * @throws VoluntaryTimeoutException if we came too close to the timeout
	 *             while executing the command. A caller may catch this
	 *             exception and try again, but doing so may just result in a
	 *             timeout from GAE if TIME_CRITICAL is set to more than half
	 *             the GAE timeout.
	 * 
	 * @see XydraStore#executeCommands(XID, String, XCommand[], Callback)
	 */
	long executeCommand(XCommand command, XID actorId);
	
	/**
	 * @return the {@link XAddress} of the model managed by this
	 *         {@link IGaeChangesService} instance.
	 */
	XAddress getModelAddress();
	
	/**
	 * @return the model's current revision number. Non-existing models are
	 *         signalled as {@link XCommand#FAILED}, i.e. those that have just
	 *         been removed from the repository.
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[], Callback)
	 */
	long getCurrentRevisionNumber();
	
	/**
	 * Fetch a range of events from the datastore.
	 * 
	 * See also {@link GetEventsRequest}.
	 * 
	 * @see XydraStore#getEvents(XID, String, GetEventsRequest[],
	 *      org.xydra.store.Callback)
	 * 
	 *      TODO localVm Implementation should cache retrieved events in a
	 *      localVmCache and never ask for them again.
	 * 
	 * @param beginRevision inclusive
	 * @param endRevision inclusive
	 * @return a list of events or null if this model was never created. The
	 *         list might contain fewer elements than the range implies.
	 */
	List<XEvent> getEventsBetween(long beginRevision, long endRevision);
	
	AsyncValue getValue(long fieldRev, int transindex);
	
	/**
	 * @return true if model exists
	 */
	boolean exists();
	
}

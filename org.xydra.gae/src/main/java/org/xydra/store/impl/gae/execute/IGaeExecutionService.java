package org.xydra.store.impl.gae.execute;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.changes.VoluntaryTimeoutException;


public interface IGaeExecutionService {
	
	/**
	 * Execute the given {@link XCommand} as a transaction.
	 * 
	 * // IMPROVE maybe let the caller provide an XId that can be used to check
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
	 * @see XydraStore#executeCommands(XId, String, XCommand[], Callback)
	 */
	long executeCommand(XCommand command, XId actorId);
	
}

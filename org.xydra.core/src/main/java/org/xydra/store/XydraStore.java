package org.xydra.store;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;


/**
 * To be used over HTTPS or in VM.
 * 
 * @author voelkel
 * 
 */
public interface XydraStore {
	
	/* security */

	/**
	 * TODO avoid brute-force attacks
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @param callback
	 */
	void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback);
	
	/* snapshots */

	/**
	 * @return a read-only interface to the snapshot for the model this time.
	 *         Individual parts of the model are loaded into memory as-needed.
	 *         Returns null if no such model exists.
	 * 
	 *         TODO Models may not actually be loaded lazyly, better to leave
	 *         this up to the implementation. ~Daniel
	 */
	
	/**
	 * throws {@link AccessException}
	 * 
	 * throws {@link TimeoutException}, {@link ConnectionException}
	 * 
	 * throws {@link RequestException}
	 * 
	 * throws {@link InternalStoreException}
	 * 
	 * @param modelAddress
	 * @param callback
	 * @throws IllegalArgumentException if the arguments are invalid
	 */
	void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddress,
	        Callback<XBaseModel[]> callback) throws IllegalArgumentException;
	
	/**
	 * TODO negative revIds for non-existing models
	 * 
	 * @param actorId
	 * @param modelAddress
	 * @param callback
	 * @throws IllegalArgumentException
	 */
	void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddress,
	        Callback<long[]> callback) throws IllegalArgumentException;
	
	void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddress,
	        Callback<XBaseObject[]> callback);
	
	void getModelIds(XID actorId, String passwordHash, XID repositoryId, Callback<Set<XID>> callback);
	
	/* commands */

	/**
	 * Execute the given command and log the actorId. No
	 * authentication/authorization checks are done.
	 * 
	 * TODO replace actorId with a more general context to allow logging time,
	 * IP Address, etc.
	 * 
	 * @return TODO document die long return value verhält sich wie bei allen
	 *         anderen #executeCommand methoden in core (z.B. in XModel) ->
	 *         {@link XCommand#FAILED} für fehler, {@link XCommand#NOCHANGE}
	 *         wenn sich nichts geändert hat, revision number sonst
	 */
	void executeCommand(XID actorId, String passwordHash, XCommand[] command,
	        Callback<long[]> callback);
	
	/* events */

	/**
	 * Returns an iterator over all {@link XEvent XEvents} that occurred after
	 * (and including) beginRevision and before (but not including) endRevision.
	 * 
	 * @param beginRevision the beginning revision number of the interval from
	 *            which all {@link XEvent XEvents} are to be returned - can be
	 *            less than {@link #getFirstRevisionNumber()} to get all
	 *            {@link XEvent XEvents} up to endRevision
	 * @param endRevision the end revision number of the interval from which all
	 *            {@link XEvent XEvents} are to be returned - can be greater
	 *            than {@link #getCurrentRevisionNumber()} to get all
	 *            {@link XEvent XEvents} since beginRevision
	 * @return an iterator over all {@link XEvent XEvents} that occurred during
	 *         the specified interval of revision numbers
	 * @throws IndexOutOfBoundsException if beginRevision or endRevision are
	 *             negative
	 * @throws IllegalArgumentException if beginRevision is greater than
	 *             endRevision
	 */
	/**
	 * @param actorId
	 * @param address model, object or field
	 * @param beginRevision
	 * @param endRevision
	 * @param callback for each model, a list of changes
	 */
	void getEvents(XID actorId, String passwordHash, XAddress[] address, long beginRevision,
	        long endRevision, Callback<XEvent[][]> callback);
	
	/**
	 * Redundant, but network-optimised method.
	 * 
	 * @param actorId
	 * @param command
	 * @param addressToGetEventsFor
	 * @param beginRevision
	 * @param endRevision
	 * @param callback
	 */
	void executeCommandAndGetEvents(XID actorId, String passwordHash, XCommand[] command,
	        XAddress[] addressToGetEventsFor, long beginRevision, long endRevision,
	        Callback<Pair<long[],XEvent[][]>> callback);
	
	/* rights by convention __ */

}

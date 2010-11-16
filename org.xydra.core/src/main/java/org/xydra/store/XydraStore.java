package org.xydra.store;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.index.query.Pair;

import com.sun.org.apache.xpath.internal.objects.XObject;


/**
 * A small yet complete API for a persistence layer that offers the follwing
 * features:
 * <ol>
 * <li>Persistence</li>
 * <li>Asynchronous design to be used over a network</li>
 * <li>Access Rights</li>
 * <li>Versioning</li>
 * <li>Transactions and Command-pattern</li>
 * <li>Retrieval of events</li>
 * <li>Partial loading (models or objects)</li>
 * </ol>
 * 
 * <h3>Usage guidelines</h3> For secure usage this API should be used over HTTPS
 * or within the same VM.
 * 
 * Exceptions: Each method may throw in the callback one of the following
 * exceptions:
 * <ul>
 * <li>{@link AutorisationException} if actorId and passwordHash don't match</li>
 * <li>{@link AccessException} if the actorId has not the rights to do the
 * operation</li>
 * <li>{@link TimeoutException} if the implementation did not respond during a
 * given time</li>
 * <li>{@link ConnectionException} if there is a problem to connect to the
 * implementation at all. Note that a {@link TimeoutException} might also be
 * thrown if there is in fact a {@link ConnectionException}. They are not easy
 * to distinguish.</li>
 * <li>{@link RequestException} if the supplied arguments are considered
 * syntactically or semantically invalid</li>
 * <li>{@link InternalStoreException} if the implementation encounters another
 * problem, typically caused by the hosting platform, i.e. an I/O error.</li>
 * <li></li>
 * </ul>
 * 
 * <h3>Implementation guidelines</h3> TODO Prevent brute-force attacks by
 * throwing an exception when too many operations per time use the wrong
 * actorId/passwordHash combination.
 * 
 * @author voelkel
 * @author dscharrer
 */
public interface XydraStore {
	
	static final long MODEL_DOES_NOT_EXIST = -1;
	
	/* security */

	/**
	 * Redundant method to allow a quick (network-efficient) check if an actorId
	 * and passwordHash are valid for authentication.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param callback Asynchronous callback to signal success or failure.
	 *            <code>true</code> is returned if the actorId and the supplied
	 *            passwordHash match.
	 */
	void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback);
	
	/* snapshots */

	/**
	 * Returns a read-only snapshots of {@link XModel} state at the point in
	 * time when this request was processed.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param modelAddresses an array of {@link XAddress} for which models to
	 *            get snapshots. Each {@link XAddress} must address an
	 *            {@link XModel} (repositoryId/modelId/-/-).
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, an array of {@link XBaseModel} is returned, in the
	 *            same order of the modelAddresses given in the request. A null
	 *            value in the array signals that the requested model does not
	 *            exist in the store - or that the actorId has no read-access on
	 *            it.
	 * @throws IllegalArgumentException if the arguments are invalid
	 * 
	 *             Implementation note: Implementation may chose to supply a
	 *             lazy-loading stub only.
	 */
	void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<XBaseModel[]> callback) throws IllegalArgumentException;
	
	/**
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param modelAddresses an array of {@link XAddress} for which the latest
	 *            model revision should be retrieved. Each {@link XAddress} must
	 *            address an {@link XModel} (repositoryId/modelId/-/-).
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, the returned array contains in the same order as in
	 *            the request array (modelAddresses) the revision number of the
	 *            addressed model as a long. Non-existing models (and those for
	 *            which the actorId has no read-access) are signalled as
	 *            {@link #MODEL_DOES_NOT_EXIST}.
	 * @throws IllegalArgumentException
	 */
	void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<long[]> callback) throws IllegalArgumentException;
	
	/**
	 * Returns read-only snapshots of {@link XObject} state at the point in time
	 * when this request was processed.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param objectAddresses an array of {@link XAddress} for which objects to
	 *            get snapshots. Each {@link XAddress} must address an
	 *            {@link XObject} (repositoryId/modelId/objectId/-).
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, an array of {@link XBaseObject} is returned, in the
	 *            same order of the objectAddresses given in the request. A null
	 *            value in the array signals that the requested object does not
	 *            exist in the store - or that the actorId has no read-access on
	 *            it.
	 * @throws IllegalArgumentException if the arguments are invalid
	 * 
	 *             Implementation note: Implementation may chose to supply a
	 *             lazy-loading stub only.
	 */
	void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<XBaseObject[]> callback);
	
	/**
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param repositoryId The id of the repository for which to return the list
	 *            of all modelIds stored in it.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success a Set of all {@link XID} of all {@link XModel} for
	 *            which the given actorId has read-access in the repository is
	 *            returned.
	 */
	void getModelIds(XID actorId, String passwordHash, XID repositoryId, Callback<Set<XID>> callback);
	
	// ------------------ documented until here
	
	/* commands */

	/**
	 * Execute the given command and log the actorId. No
	 * authentication/authorization checks are done.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. TODO replace actorId with a more general context to
	 *            allow logging time, IP Address, etc.
	 * 
	 * @return TODO document die long return value verhält sich wie bei allen
	 *         anderen #executeCommand methoden in core (z.B. in XModel) ->
	 *         {@link XCommand#FAILED} für fehler, {@link XCommand#NOCHANGE}
	 *         wenn sich nichts geändert hat, revision number sonst
	 * @param callback Asynchronous callback to signal success or failure.
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
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param address model, object or field
	 * @param beginRevision
	 * @param endRevision
	 * @param callback Asynchronous callback to signal success or failure.
	 * @param callback for each model, a list of changes
	 */
	void getEvents(XID actorId, String passwordHash, XAddress[] address, long beginRevision,
	        long endRevision, Callback<XEvent[][]> callback);
	
	/**
	 * Redundant, but network-optimised method.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param command
	 * @param addressToGetEventsFor
	 * @param beginRevision
	 * @param endRevision
	 * @param callback Asynchronous callback to signal success or failure.
	 */
	void executeCommandAndGetEvents(XID actorId, String passwordHash, XCommand[] command,
	        XAddress[] addressToGetEventsFor, long beginRevision, long endRevision,
	        Callback<Pair<long[],XEvent[][]>> callback);
	
	/* rights by convention __ */

}

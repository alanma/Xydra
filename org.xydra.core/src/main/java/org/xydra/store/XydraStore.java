package org.xydra.store;

import java.util.Set;

import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.index.query.Pair;


/**
 * A small yet complete API for a persistence layer that offers the following
 * features:
 * <ol>
 * <li>Persistence</li>
 * <li>Asynchronous design to be used over a network</li>
 * <li>Access Rights</li>
 * <li>Versioning</li>
 * <li>Transactions and Command-pattern</li>
 * <li>Retrieval of events</li>
 * <li>Partial loading (supports loading single models or objects)</li>
 * </ol>
 * 
 * <h3>Usage guidelines</h3> For secure usage this API should be used over HTTPS
 * or within the same VM.
 * 
 * <h3>Confidentiality</h3> In general, a given actorId is first authenticated,
 * which is throttled to a certain number of login attempts per time interval.
 * If that threshold is exceeded, a {@link QuotaException} is thrown.
 * 
 * Once the actorId is authenticated, Xydra checks if it has the necessary
 * rights to perform an operation (read or write). If not, an
 * {@link AccessException} is returned, in most methods within a
 * {@link BatchedResult}. This implies an actorId that is known in the Xydra
 * store implementation can find out what models and objects a repository
 * contains, even if it has no read access. It can do this by issuing some
 * read-requests and check if the result is null (entity does not exist) or
 * whether there is a {@link BatchedResult} with {@link AccessException}. This
 * design will make implementing clients using the API easier to debug.
 * 
 * <h3>Exceptions</h3> Each method may throw in the callback one of the
 * following exceptions:
 * <ul>
 * <li>{@link QuotaException} to prevent brute-force attacks when too many
 * operations per time use the wrong actorId/passwordHash combination.</li>
 * <li>{@link AuthorisationException} if actorId and passwordHash don't match</li>
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
 * <h3>Implementation guidelines</h3> For anonymous users over HTTP, the
 * IP-Address could be used as an actorId.
 * 
 * @author voelkel
 * @author dscharrer
 */
public interface XydraStore {
	
	static final long MODEL_DOES_NOT_EXIST = -1;
	
	/**
	 * SECURITY.
	 * 
	 * Redundant method to allow a quick (network-efficient) check if an actorId
	 * and passwordHash are valid for authentication.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param callback Asynchronous callback to signal success or failure.
	 *            <code>true</code> is returned if the actorId and the supplied
	 *            passwordHash match.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 *             Only the callback may be null.
	 */
	void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Retrieve read-only snapshots of {@link XModel} states at the point in
	 * time when this request is processed.
	 * 
	 * Possible exceptions to received via callback.onError (see class comment
	 * in {@link XydraStore} and comments in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * 
	 * Possible exceptions for single entries in the returned
	 * {@link BatchedResult}:
	 * <ul>
	 * <li>{@link RequestException} for addresses that do not address an
	 * {@link XModel}.</li>
	 * <li>{@link AccessException} for a modelAddress the given actorId may not
	 * read</li>
	 * </ul>
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
	 *            success, an array of {@link BatchedResult} is returned, in the
	 *            same order of the modelAddresses given in the request. A null
	 *            value in the array signals that the requested model does not
	 *            exist in the store. May not be null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 * 
	 *             Implementation note: Implementation may choose to supply a
	 *             lazy-loading stub only.
	 */
	void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * Possible exceptions in the {@link BatchedResult}:
	 * <ul>
	 * <li>{@link RequestException} for addresses that do not address an
	 * {@link XModel}.</li>
	 * <li>{@link AccessException} for a modelAddress the given actorId may not
	 * read</li>
	 * </ul>
	 * 
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
	 *            {@link #MODEL_DOES_NOT_EXIST}. May not be null.
	 * 
	 *            TODO what is returned here if the passed address does not
	 *            refer to an XModel ({@link #MODEL_DOES_NOT_EXIST} doesn't
	 *            really fit here, I think) ~Bjoern
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 */
	void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Returns read-only snapshots of {@link XObject} state at the point in time
	 * when this request was processed.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * Possible exceptions in the {@link BatchedResult}:
	 * <ul>
	 * <li>{@link RequestException} for addresses that do not address an
	 * {@link XObject}.</li>
	 * <li>{@link AccessException} for a objectAddress the given actorId may not
	 * read</li>
	 * </ul>
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
	 *            it. May not be null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 * 
	 *             Implementation note: Implementation may chose to supply a
	 *             lazy-loading stub only.
	 */
	void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success a Set of all {@link XID} of all {@link XModel XModels}
	 *            for which the given actorId has read-access in the repository
	 *            is returned.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 *             May not be null.
	 */
	void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * One {@link XydraStore} instance refers to exactly one Xydra
	 * {@link XRepository}.
	 * 
	 * Every authenticated actorId may read the repository ID.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, the repositoryId of this store is returned. May not
	 *            be null.
	 * @throws IllegalArgumentException if any of the given parameters is null.
	 */
	void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException;
	
	/**
	 * Change state.
	 * 
	 * Check permissions, command pre-conditions, execute the command and log
	 * the resulting events.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * Possible exceptions in the {@link BatchedResult}:
	 * <ul>
	 * <li>{@link AccessException} for a command that could not be executed
	 * because the given actorId has not the necessary rights to do this (read
	 * and write)</li>
	 * <li>{@link RequestException} for a command that is in itself
	 * inconsistent, i.e. a {@link XTransaction} that contains no commands.</li>
	 * </ul>
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param commands An array of commands that are executed in the given
	 *            order. Note that no transaction semantics are applied. Each
	 *            individual command might fail or succeed. If you want
	 *            transaction semantics, you need to wrap commands in a
	 *            {@link XTransaction}.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, the supplied array contains in the same order as the
	 *            supplied commands the result of executing the command. A
	 *            non-negative number indicates the resulting revision number of
	 *            the changed entity.
	 * 
	 *            For successful commands that changed something, the the return
	 *            value is always a revision number that can be used to retrieve
	 *            the corresponding event using {@link #getEvents()}
	 * 
	 *            Like any other {@link XCommand}, {@link XTransaction}s only
	 *            "take up" a single revision, which is the one passed to the
	 *            callback. For {@link XTransaction}s as well as
	 *            {@link XRepositoryCommand}s, {@link XModelCommand}s and
	 *            {@link XObjectCommand}s of type remove, the event saved in the
	 *            change log may be either a {@link XTransactionEvent} or an
	 *            {@link XAtomicEvent}, depending on whether there are actually
	 *            multiple changes.
	 * 
	 *            Negative numbers indicate a special result:
	 *            {@link XCommand#FAILED} signals a failure,
	 *            {@link XCommand#NOCHANGE} signals that the command did not
	 *            change anything.
	 * 
	 *            Commands may still "take up" a revision number, even if they
	 *            failed or didn't change anything, causing the next command to
	 *            skip a revision number. This means that there can be revision
	 *            numbers without any associated events. The revision of the
	 *            model however is only updated if anything actually changed.
	 * 
	 *            Even after a the callback's {@link Callback#onSuccess(Object)}
	 *            method has been called, the change may not actually be
	 *            returned yet by {@link #getModelSnapshots()},
	 *            {@link #getModelIds()}, {@link #getModelRevisions()} and
	 *            {@link #getObjectSnapshots()} yet. The change will however
	 *            eventually be returned by those methods, and will stay
	 *            persistent once it does. Also, no changes with greater
	 *            revision numbers will become visible before this one, but
	 *            their callbacks' {@link Callback#onSuccess(Object)} method
	 *            might be called before this one.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 *             Only the callback may be null.
	 */
	void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException;
	
	/**
	 * EVENTS.
	 * 
	 * Fetch all events that happened for a given address in a given range of
	 * revisions. Batch operation for multiple such requests.
	 * 
	 * Possible exceptions (see class comment in {@link XydraStore} and comments
	 * in each exception):
	 * <ul>
	 * <li>{@link QuotaException} to prevent brute-force attacks when too many
	 * operations per time use the wrong actorId/passwordHash combination.</li>
	 * <li>{@link AuthorisationException}</li>
	 * <li>{@link ConnectionException}</li>
	 * <li>{@link TimeoutException}</li>
	 * <li>{@link InternalStoreException}</li>
	 * </ul>
	 * Possible exceptions in each {@link BatchedResult}:
	 * <ul>
	 * <li>{@link AccessException} if the given actorId may not read the entity
	 * with the {@link XAddress} in the {@link GetEventsRequest}.</li>
	 * <li>{@link RequestException} (a) if beginRevision is greater than
	 * endRevision; (b) if beginRevision or endRevision are negative</li>
	 * </ul>
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param getEventsRequest an array of requests for events. See
	 *            {@link GetEventsRequest}.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, this callback returns an array which has one entry
	 *            for each requested XAddress. Each entry is itself an array of
	 *            XEvents, in the order in which they happened. May not be null.
	 * @throws IllegalArgumentException if any of the given parameters is null.
	 */
	void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequest,
	        Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException;
	
	// ------------------ documented until here
	
	/**
	 * COMMANDS + EVENTS.
	 * 
	 * Redundant, network-optimised method to combine in one method call the
	 * effects of {@link #executeCommands(XID, String, XCommand[], Callback)}
	 * and {@link #getEvents(XID, String, GetEventsRequest[], Callback)}.
	 * 
	 * The exceptions in this methods are simply the union of the exceptions in
	 * {@link #executeCommands(XID, String, XCommand[], Callback)} and
	 * {@link #getEvents(XID, String, GetEventsRequest[], Callback)}.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services.
	 * @param commands See
	 *            {@link #executeCommands(XID, String, XCommand[], Callback)}
	 * @param getEventRequests See
	 *            {@link #getEvents(XID, String, GetEventsRequest[], Callback)}
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, this method returns a {@link Pair} where the first
	 *            component is the the same (including the same kind of
	 *            exceptions) as the result of
	 *            {@link #executeCommands(XID, String, XCommand[], Callback)}
	 *            and the second is the same as the result of
	 *            {@link #getEvents(XID, String, GetEventsRequest[], Callback)}
	 *            (also including the same exceptions) .
	 */
	void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException;
	
	/* rights by convention, TODO document */

}

package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.store.AccessException;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.TimeoutException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


/**
 * This interface is the single-operation version of {@link XydraStore}. To be
 * more efficient, passwordHash may be set to null and no
 * authentication/authorisation checks are performed. Access right checks ARE
 * performed.
 * 
 * 
 * A small yet complete API for a persistence layer that offers the following
 * features:
 * <ol>
 * <li>Persistence</li>
 * <li>Asynchronous design to be used over a network</li>
 * <li>Access Rights (authentication and authorisation)</li>
 * <li>Versioning</li>
 * <li>Transactions and Command-pattern</li>
 * <li>Retrieval of all occurred events</li>
 * <li>Partial loading (supports loading single models or objects)</li>
 * </ol>
 * 
 * <h3>Usage guidelines</h3>
 * 
 * For secure usage this API should be used over HTTPS.
 * 
 * <h3>Confidentiality</h3>
 * 
 * In general, a given actorId is first authenticated, which is throttled to a
 * certain number of login attempts per time interval. If that threshold is
 * exceeded, a {@link QuotaException} is thrown.
 * 
 * TODO Clarify behaviour (when delayed how much, when blocked, how to unblock,
 * ...)
 * 
 * Once the actorId is authenticated, Xydra checks if the actor has the
 * necessary rights to perform an operation (read or write). If not, an
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

public interface XydraSingleOperationStore {
	
	/**
	 * SECURITY.
	 * 
	 * Redundant method to allow a quick (network-efficient) check if an actorId
	 * and passwordHash are valid for authentication.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param callback Asynchronous callback to signal success or failure.
	 *            <code>true</code> is returned if the actorId and the supplied
	 *            passwordHash match. Must not be null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 */
	void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException;
	
	/**
	 * Change state.
	 * 
	 * Check permissions, command pre-conditions, execute the command and log
	 * the resulting events.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param command A command to be executedIt might fail or succeed. If you
	 *            want transaction semantics, you need to wrap commands in a
	 *            {@link XTransaction}.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, return the result of executing the command. A
	 *            non-negative number indicates the resulting revision number of
	 *            the changed entity.
	 * 
	 *            For successful commands that changed something, the return
	 *            value is always a revision number that can be used to retrieve
	 *            the corresponding event using
	 *            {@link #getEvents(XID, String, GetEventsRequest, Callback)}
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
	 *            returned yet by
	 *            {@link #getModelSnapshot(XID, String, XAddress, Callback)},
	 *            {@link #getModelIds(XID, String, Callback)},
	 *            {@link #getModelRevision(XID, String, XAddress, Callback)} and
	 *            {@link #getObjectSnapshot(XID, String, XAddress, Callback)}
	 *            yet. The change will however eventually be returned by those
	 *            methods, and will stay persistent once it does. Also, no
	 *            changes with greater revision numbers will become visible
	 *            before this one, but their callbacks'
	 *            {@link Callback#onSuccess(Object)} method might be called
	 *            before this one.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 *             Only the callback may be null.
	 */
	void executeCommand(XID actorId, String passwordHash, XCommand command, Callback<Long> callback)
	        throws IllegalArgumentException;
	
	/**
	 * EVENTS.
	 * 
	 * Fetch all events that happened for a given address in a given range of
	 * revisions. Batch operation for multiple such requests.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param getEventsRequest a request for events. See
	 *            {@link GetEventsRequest}.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, this callback returns an array of XEvents, in the
	 *            order in which they happened.
	 * 
	 *            The array can be null to indicate that the requested entity
	 *            does not exist or the user is not allowed to read it.
	 * 
	 * @throws IllegalArgumentException if any of the given parameters is null.
	 */
	void getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest,
	        Callback<XEvent[]> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success a Set of all {@link XID} of all {@link XModel XModels}
	 *            for which the given actorId has read-access in the repository
	 *            is returned.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 *             Must not be null.
	 */
	void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param modelAddress an array of {@link XAddress} for which the latest
	 *            model revision should be retrieved. Each {@link XAddress} must
	 *            address an {@link XModel} (repositoryId/modelId/-/-).
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, the revision number of the addressed model as a long.
	 *            A non-existing model (and those for which the actorId has no
	 *            read-access) is signaled as {@link XCommand#FAILED}. Must not
	 *            be null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 */
	void getModelRevision(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<Long> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Retrieve read-only snapshots of {@link XModel} states at the point in
	 * time when this request is processed.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param modelAddress the {@link XAddress} for which model to get a
	 *            snapshot. The {@link XAddress} must address an {@link XModel}
	 *            (repositoryId/modelId/-/-).
	 * 
	 *            TODO How can a client request a specific version of a
	 *            snapshot?
	 * 
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, a null value signals that the requested model does
	 *            not exist in the store. Must not be null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 * 
	 *             Implementation note: Implementation may choose to supply a
	 *             lazy-loading stub only.
	 */
	void getModelSnapshot(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<XReadableModel> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * Returns read-only snapshots of {@link XObject} state at the point in time
	 * when this request was processed.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param objectAddress an array of {@link XAddress} for which objects to
	 *            get snapshots. Each {@link XAddress} must address an
	 *            {@link XObject} (repositoryId/modelId/objectId/-).
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, an {@link XReadableObject} is returned. A null value
	 *            signals that the requested object does not exist in the store
	 *            - or that the actorId has no read-access on it. Must not be
	 *            null.
	 * @throws IllegalArgumentException if one of the given parameters is null.
	 * 
	 *             Implementation note: Implementation may chose to supply a
	 *             lazy-loading stub only.
	 */
	void getObjectSnapshot(XID actorId, String passwordHash, XAddress objectAddress,
	        Callback<XReadableObject> callback) throws IllegalArgumentException;
	
	/**
	 * Read current state.
	 * 
	 * One {@link XydraSingleOperationStore} instance refers to exactly one
	 * Xydra {@link XRepository}.
	 * 
	 * Every authenticated actorId may read the repository ID.
	 * 
	 * Possible exceptions to be received via callback.onError (see class
	 * comment in {@link XydraSingleOperationStore} and comments in each
	 * exception):
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
	 *            services. If null, all access is granted.
	 * @param callback Asynchronous callback to signal success or failure. On
	 *            success, the repositoryId of this store is returned. Must not
	 *            be null.
	 * @throws IllegalArgumentException if any of the given parameters is null.
	 */
	void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException;
	
	/**
	 * @return a {@link XydraStoreAdmin} interface that contains local
	 *         administration functions that are not exposed via REST.
	 *         Client-side implementations that do not allow any of these
	 *         administrative methods may return null.
	 */
	XydraStoreAdmin getXydraStoreAdmin();
	
}

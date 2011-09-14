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
import org.xydra.store.RevisionState;
import org.xydra.store.TimeoutException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


/**
 * This interface is the blocking (synchronous), single-operation version of
 * {@link XydraStore}.
 * 
 * To be more efficient, passwordHash may be set to null and no
 * authentication/authorisation checks are performed. Access right checks ARE
 * performed.
 * 
 * 
 * 
 * 
 * <h2>Adapted from {@link XydraStore}</h2>
 * 
 * A small yet complete API for a persistence layer that offers the following
 * features:
 * <ol>
 * <li>Persistence</li>
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
 * <h3>Implementation guidelines</h3> For anonymous users over HTTP, the
 * IP-Address could be used as an actorId.
 * 
 * @author voelkel
 * @author dscharrer
 */

public interface XydraBlockingStore {
	
	/**
	 * SECURITY.
	 * 
	 * Redundant method to allow a quick (network-efficient) check if an actorId
	 * and passwordHash are valid for authentication.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @return <code>true</code> if the actorId and the supplied passwordHash
	 *         match.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 */
	boolean checkLogin(XID actorId, String passwordHash) throws IllegalArgumentException,
	        QuotaException, TimeoutException, ConnectionException, RequestException,
	        InternalStoreException;
	
	/**
	 * Change state.
	 * 
	 * Check permissions, command pre-conditions, execute the command and log
	 * the resulting events.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @param command A command to be executedIt might fail or succeed. If you
	 *            want transaction semantics, you need to wrap commands in a
	 *            {@link XTransaction}.
	 * @return the result of executing the command. A non-negative number
	 *         indicates the resulting revision number of the changed entity.
	 * 
	 *         For successful commands that changed something, the return value
	 *         is always a revision number that can be used to retrieve the
	 *         corresponding event using
	 *         {@link #getEvents(XID, String, GetEventsRequest)}
	 * 
	 *         Like any other {@link XCommand}, {@link XTransaction}s only
	 *         "take up" a single revision, which is the one passed to the
	 *         callback. For {@link XTransaction}s as well as
	 *         {@link XRepositoryCommand}s, {@link XModelCommand}s and
	 *         {@link XObjectCommand}s of type remove, the event saved in the
	 *         change log may be either a {@link XTransactionEvent} or an
	 *         {@link XAtomicEvent}, depending on whether there are actually
	 *         multiple changes.
	 * 
	 *         Negative numbers indicate a special result:
	 *         {@link XCommand#FAILED} signals a failure,
	 *         {@link XCommand#NOCHANGE} signals that the command did not change
	 *         anything.
	 * 
	 *         Commands may still "take up" a revision number, even if they
	 *         failed or didn't change anything, causing the next command to
	 *         skip a revision number. This means that there can be revision
	 *         numbers without any associated events. The revision of the model
	 *         however is only updated if anything actually changed.
	 * 
	 *         Even after a the callback's {@link Callback#onSuccess(Object)}
	 *         method has been called, the change may not actually be returned
	 *         yet by {@link #getModelSnapshot(XID, String, XAddress)},
	 *         {@link #getModelIds(XID, String)},
	 *         {@link #getModelRevision(XID, String, XAddress)} and
	 *         {@link #getObjectSnapshot(XID, String, XAddress)} yet. The change
	 *         will however eventually be returned by those methods, and will
	 *         stay persistent once it does. Also, no changes with greater
	 *         revision numbers will become visible before this one, but their
	 *         callbacks' {@link Callback#onSuccess(Object)} method might be
	 *         called before this one.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws AccessException if the actorId has not the rights to do the
	 *             operation
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 */
	long executeCommand(XID actorId, String passwordHash, XCommand command)
	        throws IllegalArgumentException, QuotaException, AuthorisationException,
	        AccessException, TimeoutException, ConnectionException, RequestException,
	        InternalStoreException;
	
	/**
	 * EVENTS.
	 * 
	 * Fetch all events that happened for a given address in a given range of
	 * revisions.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @param getEventsRequest a request for events. See
	 *            {@link GetEventsRequest}.
	 * @return an array of XEvents, in the order in which they happened.
	 * 
	 *         The array can be null to indicate that the requested entity does
	 *         not exist or the user is not allowed to read it.
	 * 
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws AccessException if the actorId has not the rights to do the
	 *             operation
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid. (a) if beginRevision
	 *             is greater than endRevision; (b) if beginRevision or
	 *             endRevision are negative
	 */
	XEvent[] getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest)
	        throws IllegalArgumentException, QuotaException, AuthorisationException,
	        AccessException, TimeoutException, ConnectionException, RequestException,
	        InternalStoreException;
	
	/**
	 * Read current state.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @return a Set of all {@link XID} of all {@link XModel XModels} for which
	 *         the given actorId has read-access in the repository.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 */
	Set<XID> getModelIds(XID actorId, String passwordHash) throws IllegalArgumentException,
	        QuotaException, AuthorisationException, TimeoutException, ConnectionException,
	        RequestException, InternalStoreException;
	
	/**
	 * Read current state.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @param modelAddress an array of {@link XAddress} for which the latest
	 *            model revision should be retrieved. Each {@link XAddress} must
	 *            address an {@link XModel} (repositoryId/modelId/-/-).
	 * @return the revision number of the addressed model as a long. A
	 *         non-existing model (and those for which the actorId has no
	 *         read-access) is signalled as {@link XCommand#FAILED}.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 */
	RevisionState getModelRevision(XID actorId, String passwordHash, XAddress modelAddress)
	        throws IllegalArgumentException, QuotaException, AuthorisationException,
	        TimeoutException, ConnectionException, RequestException, InternalStoreException;
	
	/**
	 * Read current state.
	 * 
	 * Retrieve read-only snapshots of {@link XModel} states at the point in
	 * time when this request is processed.
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
	 *            TODO @Daniel: How can a client request a specific version of a
	 *            snapshot?
	 * 
	 * @return a null value signals that the requested model does not exist in
	 *         the store or that the requesting actor has not the necessary
	 *         rights to see it.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 * 
	 *             Implementation note: Implementation may choose to supply a
	 *             lazy-loading stub only.
	 */
	XReadableModel getModelSnapshot(XID actorId, String passwordHash, XAddress modelAddress)
	        throws IllegalArgumentException, QuotaException, AuthorisationException,
	        TimeoutException, ConnectionException, RequestException, InternalStoreException;
	
	/**
	 * Read current state.
	 * 
	 * Returns read-only snapshots of {@link XObject} state at the point in time
	 * when this request was processed.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @param objectAddress an array of {@link XAddress} for which objects to
	 *            get snapshots. Each {@link XAddress} must address an
	 *            {@link XObject} (repositoryId/modelId/objectId/-).
	 * @return a {@link XReadableObject}. A null value signals that the
	 *         requested object does not exist in the store - or that the
	 *         actorId has no read-access on it.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 * 
	 *             Implementation note: Implementation may chose to supply a
	 *             lazy-loading stub only.
	 */
	XReadableObject getObjectSnapshot(XID actorId, String passwordHash, XAddress objectAddress)
	        throws IllegalArgumentException, QuotaException, AuthorisationException,
	        TimeoutException, ConnectionException, RequestException, InternalStoreException;
	
	/**
	 * Read current state.
	 * 
	 * One {@link XydraBlockingStore} instance refers to exactly one Xydra
	 * {@link XRepository}.
	 * 
	 * Every authenticated actorId may read the repository ID.
	 * 
	 * @param actorId The actor who is performing this operation.
	 * @param passwordHash The MD5 hash of the secret actor password prefixed
	 *            with "Xydra" to avoid transmitting the same string over the
	 *            network if the user uses the same password for multiple
	 *            services. If null, all access is granted.
	 * @return the repositoryId of this store.
	 * @throws IllegalArgumentException if one of the given parameters is null
	 *             (except passwordHash, which may be null).
	 * @throws QuotaException to prevent brute-force attacks when too many
	 *             operations per time use the wrong actorId/passwordHash
	 *             combination.
	 * @throws TimeoutException if the implementation did not respond during a
	 *             given time
	 * @throws ConnectionException if there is a problem to connect to the
	 *             implementation at all. Note that a {@link TimeoutException}
	 *             might also be thrown if there is in fact a
	 *             {@link ConnectionException}. They are not easy to
	 *             distinguish.
	 * @throws InternalStoreException if the implementation encounters another
	 *             problem, typically caused by the hosting platform, i.e. an
	 *             I/O error.
	 * @throws AuthorisationException if actorId and passwordHash don't match
	 * @throws RequestException if the supplied arguments are considered
	 *             syntactically or semantically invalid
	 */
	XID getRepositoryId(XID actorId, String passwordHash) throws IllegalArgumentException,
	        QuotaException, AuthorisationException, TimeoutException, ConnectionException,
	        RequestException, InternalStoreException;
	
	/**
	 * @return a {@link XydraStoreAdmin} interface that contains local
	 *         administration functions that are not exposed via REST.
	 *         Client-side implementations that do not allow any of these
	 *         administrative methods may return null.
	 */
	XydraStoreAdmin getXydraStoreAdmin();
	
}

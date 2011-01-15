package org.xydra.store.impl.delegating;

import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.QuotaException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.XPasswordDatabase;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.memory.MemoryBlockingPersistence;


/**
 * An in-memory implementation of {@link XydraStore} that uses internally two
 * {@link DelegatingAllowAllStore} instances. One for storing the actual
 * repository data and one for all data related to access rights.
 * 
 * A {@link GroupModelWrapper} is used to use repository as a
 * {@link XGroupDatabase}.
 * 
 * Every component that has a reference to this {@link DelegatingSecureStore}
 * instance has complete access to change everything (via
 * {@link #getGroupDatabase()} which can be used to create accounts with all
 * rights).
 * 
 * @author xamde
 */
public class DelegatingSecureStore implements XydraStore, XydraStoreAdmin {
	
	private static final Logger log = LoggerFactory.getLogger(DelegatingSecureStore.class);
	
	/**
	 * Maximal number of failed login attempts for this store.
	 */
	public static final int MAX_FAILED_LOGIN_ATTEMPTS = 10;
	
	private final DelegatingAllowAllStore allowAllStore;
	
	private final GroupModelWrapper groupModelWrapper;
	
	/**
	 * Default constructor uses an in-memory implementation
	 */
	public DelegatingSecureStore() {
		this(new DelegatingAllowAllStore(new MemoryBlockingPersistence(XX.toId("data"))));
	}
	
	/**
	 * @param allowAllStore is used internally to store user data and access
	 *            rights and accounts.
	 */
	public DelegatingSecureStore(DelegatingAllowAllStore allowAllStore) {
		this.allowAllStore = allowAllStore;
		Credentials credentials = new Credentials(DelegatingAllowAllStore.INTERNAL_XYDRA_ADMIN_ID,
		        "ignored");
		this.groupModelWrapper = new GroupModelWrapper(credentials, allowAllStore);
		initialiseAccessRights();
	}
	
	private void assertNonNullCallback(Callback<?> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
	}
	
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		
		/* always deny the INTERNAL_XYDRA_ADMIN_ID all access from outside */
		if(actorId.equals(DelegatingAllowAllStore.INTERNAL_XYDRA_ADMIN_ID)) {
			callback.onSuccess(false);
			return;
		}
		try {
			if(this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
				this.groupModelWrapper.resetFailedLoginAttempts(actorId);
				callback.onSuccess(true);
			} else {
				int failedLoginAttempts = this.groupModelWrapper
				        .incrementFailedLoginAttempts(actorId);
				if(failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
					/* let user wait 10 seconds and inform administrator */
					try {
						Thread.sleep(10 * 1000);
					} catch(InterruptedException e) {
						log.warn("could not sleep while throttling potential hacker", e);
					}
					// TODO inform admin better
					log.warn("SECURITY: Potential hacking attempt on account '" + actorId + "'");
					callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
					        + " failed login attempts."));
				} else {
					callback.onSuccess(false);
				}
			}
		} catch(Exception e) {
			callback.onFailure(e);
		}
	}
	
	@Override
	public void clear() {
		this.allowAllStore.clear();
		// re-do this
		initialiseAccessRights();
	}
	
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.executeCommands(actorId, passwordHash, commands, callback);
		}
	}
	
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.executeCommandsAndGetEvents(actorId, passwordHash, commands,
			        getEventRequests, callback);
		}
	}
	
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequest,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.getEvents(actorId, passwordHash, getEventsRequest, callback);
		}
	}
	
	/**
	 * @return the {@link XGroupDatabase} used by this {@link XydraStore}.
	 * 
	 *         Note: A reference to a {@link XydraStore} gives a developer full
	 *         access to all data, including users, groups, and passwords. Real
	 *         security is only effective by using Xydra over REST.
	 * 
	 *         This method should not be exposed over REST.
	 */
	public XGroupDatabase getGroupDatabase() {
		return this.groupModelWrapper;
	}
	
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.getModelIds(actorId, passwordHash, callback);
		}
	}
	
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			this.allowAllStore.getModelRevisions(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) throws IllegalArgumentException {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.getModelSnapshots(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	private void assertNonNullActorAndPassword(XID actorId, String passwordHash) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId may not be null");
		}
		if(passwordHash == null) {
			throw new IllegalArgumentException("passwordHash may not be null");
		}
	}
	
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.getObjectSnapshots(actorId, passwordHash, objectAddresses, callback);
		}
	}
	
	/**
	 * @return the {@link XPasswordDatabase} used by this {@link XydraStore}.
	 *         Note: A reference to a {@link XydraStore} gives a developer full
	 *         access to all data, including users, groups, and passwords. Real
	 *         security is only effective by using Xydra over REST.
	 * 
	 *         This method should not be exposed over REST.
	 */
	public XPasswordDatabase getPasswordDatabase() {
		return this.groupModelWrapper;
	}
	
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback) {
		assertNonNullActorAndPassword(actorId, passwordHash);
		assertNonNullCallback(callback);
		if(validLogin(actorId, passwordHash, callback)) {
			// TODO check access rights, not only login
			this.allowAllStore.getRepositoryId(actorId, passwordHash, callback);
		}
	}
	
	@Override
	public String getXydraAdminPasswordHash() {
		return this.groupModelWrapper.getPasswordHash(XYDRA_ADMIN_ID);
	}
	
	@Override
	public XydraStore getXydraStore() {
		return this;
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this;
	}
	
	/**
	 * Set up a XydraAdmin account, to ensure this account is always defined.
	 */
	private void initialiseAccessRights() {
		// initialise XydraAdmin pass with a long, random string
		String password = UUID.uuid(100);
		String passwordHash = HashUtils.getXydraPasswordHash(password);
		setXydraAdminPasswordHash(passwordHash);
	}
	
	@Override
	public void setXydraAdminPasswordHash(String passwordHash) {
		this.groupModelWrapper.setPasswordHash(XYDRA_ADMIN_ID, passwordHash);
	}
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @param callback may be null
	 * @return true if acotrId and passwordHash are a valid login combination
	 */
	private boolean validLogin(XID actorId, String passwordHash, Callback<?> callback) {
		/* always deny the INTERNAL_XYDRA_ADMIN_ID all access from outside */
		if(actorId.equals(DelegatingAllowAllStore.INTERNAL_XYDRA_ADMIN_ID)) {
			if(callback != null) {
				callback.onFailure(new AuthorisationException("Account '"
				        + DelegatingAllowAllStore.INTERNAL_XYDRA_ADMIN_ID + "' is always denied."));
			}
			return false;
		}
		
		/* enforce maximal number of wrong attempts */
		if(this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			return true;
		} else {
			int failedLoginAttempts = this.groupModelWrapper.incrementFailedLoginAttempts(actorId);
			if(failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
				/* let user wait 10 seconds and inform administrator */
				try {
					Thread.sleep(10 * 1000);
				} catch(InterruptedException e) {
					log.warn("could not sleep while throttling potential hacker", e);
				}
				// TODO inform admin better
				log.warn("SECURITY: Potential hacking attempt on account '" + actorId + "'");
				if(callback != null) {
					callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
					        + " failed login attempts."));
				}
			} else {
				if(callback != null) {
					callback.onFailure(new AuthorisationException(
					        "Unauthorised login/passwordHash " + actorId + "/" + passwordHash));
				}
			}
			return false;
		}
	}
	
}

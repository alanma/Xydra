package org.xydra.store.impl.memory;

import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.QuotaException;
import org.xydra.store.XydraStore;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.XPasswordDatabase;


/**
 * An in-memory implementation of {@link XydraStore} that uses internally two
 * {@link AllowAllStore} instances. One for storing the actual repository data
 * and one for all data related to access rights.
 * 
 * A {@link GroupModelWrapper} is used to use repository as a
 * {@link XGroupDatabase}.
 * 
 * @author voelkel
 */
/**
 * @author xamde
 * 
 */
public class MemoryStore implements XydraStore {
	
	/**
	 * Maximal number of failed login attempts for this store.
	 */
	public static final int MAX_FAILED_LOGIN_ATTEMPTS = 10;
	
	private final AllowAllStore data;
	private final AllowAllStore rights;
	private final GroupModelWrapper groupModelWrapper;
	private int failedLoginAttempts;
	
	/*
	 * TODO Implementation of "failed-login-attack"-prevention is a bit strange
	 * in my opinion. Exceeding MAX_FAILED_LOGIN_ATTEMPTS results in a complete
	 * shut-down of this store, and does not only hinder one specific account
	 * from trying to log-in as I would expect something like this to work.
	 * Maybe we should discuss & improve this? ~Bjoern
	 */

	/**
	 * @param data
	 * @param rights
	 */
	public MemoryStore() {
		this(new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("data"))),
		        new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("rights"))));
	}
	
	/**
	 * @param data
	 * @param rights
	 */
	public MemoryStore(AllowAllStore data, AllowAllStore rights) {
		/*
		 * TODO Daniel: why not store both data and rights in the same store
		 * instance, but with different repository IDs? max: That would make the
		 * concept of a repository as the largest possible unit of data
		 * obsolete. "One repo = all data" makes maintenance (backups) easier.
		 */
		this.data = new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("data")));
		this.rights = new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(
		        XX.toId("rights")));
		this.groupModelWrapper = new GroupModelWrapper(this.rights, XX.toId("actors"));
		this.failedLoginAttempts = 0;
	}
	
	/**
	 * This is just a temporary method to somehow make it possible to test
	 * MemoryStore until I know how I'm supposed to work with the access rights
	 * here. Do NOT use this anywhere else!
	 * 
	 * ~Bjoern
	 */
	public GroupModelWrapper getGroupModelWrapper() {
		return this.groupModelWrapper;
	}
	
	/**
	 * @return the {@link XGroupDatabase} used by this {@link XydraStore}.
	 * 
	 *         Note: A reference to a {@link XydraStore} gives a developer full
	 *         access to all data, including users, groups, and passwords. Real
	 *         security is only effective by using Xydra over REST.
	 */
	public XGroupDatabase getGroupDatabase() {
		return this.groupModelWrapper;
	}
	
	/**
	 * @return the {@link XPasswordDatabase} used by this {@link XydraStore}.
	 *         Note: A reference to a {@link XydraStore} gives a developer full
	 *         access to all data, including users, groups, and passwords. Real
	 *         security is only effective by using Xydra over REST.
	 */
	public XPasswordDatabase getPasswordDatabase() {
		return this.groupModelWrapper;
	}
	
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		checkCallback(callback);
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		try {
			boolean authenticated = this.groupModelWrapper.isValidLogin(actorId, passwordHash);
			
			if(!authenticated) {
				this.failedLoginAttempts++;
				if(this.failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
					callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
					        + " failed login attempts."));
					
					// do not call onSuccess after this occurred
					return;
				}
				
			}
			callback.onSuccess(authenticated);
		} catch(Exception e) {
			callback.onFailure(e);
		}
	}
	
	private void checkCallback(Callback<Boolean> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
	}
	
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			if(callback != null) {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.executeCommands(actorId, passwordHash, commands, callback);
		}
	}
	
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			if(callback != null) {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.executeCommandsAndGetEvents(actorId, passwordHash, commands,
			        getEventRequests, callback);
		}
	}
	
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequest,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		this.data.getEvents(actorId, passwordHash, getEventsRequest, callback);
	}
	
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			this.failedLoginAttempts++;
			
			if(this.failedLoginAttempts > MAX_FAILED_LOGIN_ATTEMPTS) {
				callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts."));
			} else {
				
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.getModelIds(actorId, passwordHash, callback);
		}
	}
	
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			this.failedLoginAttempts++;
			
			if(this.failedLoginAttempts > MAX_FAILED_LOGIN_ATTEMPTS) {
				callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts."));
			} else {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.getModelRevisions(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) throws IllegalArgumentException {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			this.failedLoginAttempts++;
			
			if(this.failedLoginAttempts > MAX_FAILED_LOGIN_ATTEMPTS) {
				callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts."));
			} else {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			// FIXME check access rights
			// FIXME need to check the repoId on modelAddresses and load from
			// this.rights instead if requested ARM data
			// (both also apply to other methods)
			this.data.getModelSnapshots(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			this.failedLoginAttempts++;
			
			if(this.failedLoginAttempts > MAX_FAILED_LOGIN_ATTEMPTS) {
				callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts."));
			} else {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.getObjectSnapshots(actorId, passwordHash, objectAddresses, callback);
		}
	}
	
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			this.failedLoginAttempts++;
			
			if(this.failedLoginAttempts > MAX_FAILED_LOGIN_ATTEMPTS) {
				callback.onFailure(new QuotaException(MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts."));
			} else {
				callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
				        + actorId + "/" + passwordHash));
			}
		} else {
			this.data.getRepositoryId(actorId, passwordHash, callback);
		}
	}
	
}

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
import org.xydra.store.XydraStore;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.access.XGroupDatabase;


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
public class MemoryStore implements XydraStore {
	
	private final AllowAllStore data;
	private final AllowAllStore rights;
	private final GroupModelWrapper groupModelWrapper;
	
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
		// TODO why not store both data and rights in the same store instance,
		// but with different repository IDs?
		this.data = new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("data")));
		this.rights = new AllowAllStore(new MemoryNoAccessRightsNoBatchNoAsyncStore(XX
		        .toId("rights")));
		this.groupModelWrapper = new GroupModelWrapper(this.rights, XX.toId("actors"));
		
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
	
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		checkCallback(callback);
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		callback.onSuccess(this.groupModelWrapper.isValidLogin(actorId, passwordHash));
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
			callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
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
			callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
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
			callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
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
			callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
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
			callback.onFailure(new AuthorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getRepositoryId(actorId, passwordHash, callback);
		}
	}
	
}

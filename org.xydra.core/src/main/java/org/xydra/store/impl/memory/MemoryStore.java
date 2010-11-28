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
import org.xydra.store.AutorisationException;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.access.XGroupDatabase;


/**
 * An in-memory implementation of {@link XydraStore} that uses internally two
 * {@link AllowAllMemoryStore} instances. One for storing the actual repository
 * data and one for all data related to access rights.
 * 
 * A {@link GroupModelWrapper} is used to use repository as a
 * {@link XGroupDatabase}.
 * 
 * @author voelkel
 */
public class MemoryStore implements XydraStore {
	
	private AllowAllMemoryStore data;
	private AllowAllMemoryStore rights;
	private GroupModelWrapper groupModelWrapper;
	
	/**
	 * @param data
	 * @param rights
	 */
	public MemoryStore() {
		this.data = new AllowAllMemoryStore(XX.toId("data"));
		this.rights = new AllowAllMemoryStore(XX.toId("rights"));
		this.groupModelWrapper = new GroupModelWrapper(this.rights, XX.toId("rights"), XX
		        .toId("actors"));
		
	}
	
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		callback.onSuccess(this.groupModelWrapper.isValidLogin(actorId, passwordHash));
	}
	
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<long[]> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.executeCommands(actorId, passwordHash, commands, callback);
		}
	}
	
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        XAddress[] addressesToGetEventsFor, long beginRevision, long endRevision,
	        Callback<Pair<long[],XEvent[][]>> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.executeCommandsAndGetEvents(actorId, passwordHash, commands,
			        addressesToGetEventsFor, beginRevision, endRevision, callback);
		}
	}
	
	public void getEvents(XID actorId, String passwordHash, XAddress[] addresses,
	        long beginRevision, long endRevision, Callback<XEvent[][]> callback) {
		this.data.getEvents(actorId, passwordHash, addresses, beginRevision, endRevision, callback);
	}
	
	public void getModelIds(XID actorId, String passwordHash, XID repositoryId,
	        Callback<Set<XID>> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getModelIds(actorId, passwordHash, repositoryId, callback);
		}
	}
	
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<long[]> callback) throws IllegalArgumentException {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getModelRevisions(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<XBaseModel[]> callback) throws IllegalArgumentException {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getModelSnapshots(actorId, passwordHash, modelAddresses, callback);
		}
	}
	
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<XBaseObject[]> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getObjectSnapshots(actorId, passwordHash, objectAddresses, callback);
		}
	}
	
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback) {
		if(!this.groupModelWrapper.isValidLogin(actorId, passwordHash)) {
			callback.onFailure(new AutorisationException("Unauthorised login/passwordHash "
			        + actorId + "/" + passwordHash));
		} else {
			this.data.getRepositoryId(actorId, passwordHash, callback);
		}
	}
	
}

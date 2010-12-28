package org.xydra.store.impl.memory;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;


/**
 * A simple in-memory implementation of {@link XydraStore} that allows
 * everything and simply ignores all actorId and passwordHash parameters.
 * 
 * Delegates all calls to a simpler {@link XydraNoAccessRightsStore}.
 * 
 * TODO should share code with gae impl
 * 
 * @author voelkel
 * 
 */
public class AllowAllStore implements XydraStore {
	
	protected XydraNoAccessRightsStore storeWithoutAccessRights;
	
	public AllowAllStore(XydraNoAccessRightsStore base) {
		this.storeWithoutAccessRights = base;
	}
	
	public AllowAllStore(XydraNoAccessRightsNoBatchNoAsyncStore base) {
		this.storeWithoutAccessRights = new SynchronousNoAccessRightsStore(base);
	}
	
	@Override
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		callback.onSuccess(true);
	}
	
	@Override
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		if(actorId == null || passwordHash == null || commands == null) {
			throw new IllegalArgumentException("actorID, commands and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.executeCommands(actorId, commands, callback);
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		if(actorId == null || passwordHash == null || commands == null || getEventRequests == null) {
			throw new IllegalArgumentException(
			        "actorID, commands, getEventRequests and/or passwordHash were null");
		}
		
		this.storeWithoutAccessRights.executeCommandsAndGetEvents(actorId, commands,
		        getEventRequests, callback);
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequest,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		if(actorId == null || passwordHash == null || getEventsRequest == null) {
			throw new IllegalArgumentException(
			        "actorID, getEventsRequests and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getEvents(getEventsRequest, callback);
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getModelIds(callback);
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		if(actorId == null || passwordHash == null || modelAddresses == null) {
			throw new IllegalArgumentException(
			        "actorID, modelAddresses and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getModelRevisions(modelAddresses, callback);
	}
	
	@Override
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) throws IllegalArgumentException {
		if(actorId == null || passwordHash == null || modelAddresses == null) {
			throw new IllegalArgumentException(
			        "actorID, modelAddresses and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getModelSnapshots(modelAddresses, callback);
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		if(actorId == null || passwordHash == null || objectAddresses == null) {
			throw new IllegalArgumentException(
			        "actorID, objectAddresses and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getObjectSnapshots(objectAddresses, callback);
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getRepositoryId(callback);
	}
}

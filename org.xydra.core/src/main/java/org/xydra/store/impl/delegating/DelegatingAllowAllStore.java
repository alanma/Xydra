package org.xydra.store.impl.delegating;

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
import org.xydra.store.XydraStoreAdmin;


/**
 * A simple in-memory implementation of {@link XydraStore} that allows
 * everything and simply ignores all actorId and passwordHash parameters.
 * 
 * Delegates all calls to a simpler {@link XydraAsyncBatchPersistence}.
 * 
 * @author voelkel
 */
public class DelegatingAllowAllStore implements XydraStore, XydraStoreAdmin {
	
	protected XydraAsyncBatchPersistence storeWithoutAccessRights;
	private String xydraAdminPasswordHash;
	
	public DelegatingAllowAllStore(XydraAsyncBatchPersistence base) {
		this.storeWithoutAccessRights = base;
	}
	
	public DelegatingAllowAllStore(XydraBlockingPersistence base) {
		this.storeWithoutAccessRights = new DelegatingAsyncBatchPersistence(base);
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
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.executeCommands(actorId, commands, callback);
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		
		this.storeWithoutAccessRights.executeCommandsAndGetEvents(actorId, commands,
		        getEventRequests, callback);
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getEvents(getEventsRequests, callback);
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
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getModelRevisions(modelAddresses, callback);
	}
	
	@Override
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) throws IllegalArgumentException {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
		}
		this.storeWithoutAccessRights.getModelSnapshots(modelAddresses, callback);
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorID and/or passwordHash were null");
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
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this;
	}
	
	@Override
	public XydraStore getXydraStore() {
		return this;
	}
	
	@Override
	public void clear() {
		this.storeWithoutAccessRights.clear();
	}
	
	@Override
	public void setXydraAdminPasswordHash(String xydraAdminPasswordHash) {
		/**
		 * this store ignores the XydraAdmin as ANY user may do EVERYTHING
		 * already. We store it only to fulfill the XydraStoreAdmin interface
		 */
		this.xydraAdminPasswordHash = xydraAdminPasswordHash;
	}
	
	@Override
	public String getXydraAdminPasswordHash() {
		return this.xydraAdminPasswordHash;
	}
}

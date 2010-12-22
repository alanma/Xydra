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


/**
 * An implementation of {@link XydraNoAccessRightsStore} that maps all async
 * batch calls to an instance of {@link XydraNoAccessRightsNoBatchNoAsyncStore}
 * which treats them as single-operation blocking call.
 * 
 * @author voelkel
 */
public class SynchronousNoAccessRightsStore implements XydraNoAccessRightsStore {
	
	protected XydraNoAccessRightsNoBatchNoAsyncStore noBatchStore;
	
	public SynchronousNoAccessRightsStore(XydraNoAccessRightsNoBatchNoAsyncStore base) {
		this.noBatchStore = base;
	}
	
	private BatchedResult<Long>[] executeCommands(XID actorId, XCommand[] commands) {
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] results = new BatchedResult[commands.length];
		for(int i = 0; i < commands.length; i++) {
			try {
				results[i] = new BatchedResult<Long>(this.noBatchStore.executeCommand(actorId,
				        commands[i]));
			} catch(Exception e) {
				results[i] = new BatchedResult<Long>(e);
			}
		}
		return results;
	}
	
	@Override
	public void executeCommands(XID actorId, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		BatchedResult<Long>[] result = executeCommands(actorId, commands);
		if(callback != null) {
			callback.onSuccess(result);
		}
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		BatchedResult<Long>[] commandResults = executeCommands(actorId, commands);
		BatchedResult<XEvent[]>[] eventResults = getEvents(getEventRequests);
		if(callback != null) {
			callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(
			        commandResults, eventResults));
		}
	}
	
	private BatchedResult<XEvent[]>[] getEvents(GetEventsRequest[] getEventsRequests) {
		@SuppressWarnings("unchecked")
		BatchedResult<XEvent[]>[] results = new BatchedResult[getEventsRequests.length];
		for(int i = 0; i < getEventsRequests.length; i++) {
			try {
				XEvent[] result = this.noBatchStore.getEvents(getEventsRequests[i].address,
				        getEventsRequests[i].beginRevision, getEventsRequests[i].endRevision);
				results[i] = new BatchedResult<XEvent[]>(result);
			} catch(Exception e) {
				results[i] = new BatchedResult<XEvent[]>(e);
			}
		}
		return results;
	}
	
	@Override
	public void getEvents(GetEventsRequest[] getEventRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		if(callback != null) {
			callback.onSuccess(getEvents(getEventRequests));
		}
	}
	
	@Override
	public void getModelIds(Callback<Set<XID>> callback) {
		Set<XID> ids;
		try {
			ids = this.noBatchStore.getModelIds();
		} catch(Exception e) {
			if(callback != null) {
				callback.onFailure(e);
			}
			return;
		}
		if(callback != null) {
			callback.onSuccess(ids);
		}
	}
	
	@Override
	public void getModelRevisions(XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) {
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] results = new BatchedResult[modelAddresses.length];
		for(int i = 0; i < modelAddresses.length; i++) {
			try {
				long result = this.noBatchStore.getModelRevision(modelAddresses[i]);
				results[i] = new BatchedResult<Long>(result);
			} catch(Exception e) {
				results[i] = new BatchedResult<Long>(e);
			}
		}
		if(callback != null) {
			callback.onSuccess(results);
		}
	}
	
	@Override
	public void getModelSnapshots(XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) {
		@SuppressWarnings("unchecked")
		BatchedResult<XBaseModel>[] results = new BatchedResult[modelAddresses.length];
		
		for(int i = 0; i < modelAddresses.length; i++) {
			try {
				XBaseModel result = this.noBatchStore.getModelSnapshot(modelAddresses[i]);
				results[i] = new BatchedResult<XBaseModel>(result);
			} catch(Exception e) {
				results[i] = new BatchedResult<XBaseModel>(e);
			}
		}
		if(callback != null) {
			callback.onSuccess(results);
		}
	}
	
	@Override
	public void getObjectSnapshots(XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		@SuppressWarnings("unchecked")
		BatchedResult<XBaseObject>[] results = new BatchedResult[objectAddresses.length];
		for(int i = 0; i < objectAddresses.length; i++) {
			try {
				XBaseObject result = this.noBatchStore.getObjectSnapshot(objectAddresses[i]);
				results[i] = new BatchedResult<XBaseObject>(result);
			} catch(Exception e) {
				results[i] = new BatchedResult<XBaseObject>(e);
			}
		}
		if(callback != null) {
			callback.onSuccess(results);
		}
	}
	
	@Override
	public void getRepositoryId(Callback<XID> callback) {
		XID repoId = this.noBatchStore.getRepositoryId();
		if(callback != null) {
			callback.onSuccess(repoId);
		}
	}
	
}

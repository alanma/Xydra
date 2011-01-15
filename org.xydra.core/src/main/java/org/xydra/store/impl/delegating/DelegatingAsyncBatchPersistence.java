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


/**
 * An implementation of {@link XydraAsyncBatchPersistence} that maps all
 * asynchronous batch calls to an instance of {@link XydraBlockingPersistence} which
 * treats them as single-operation blocking call.
 * 
 * @author voelkel
 */
public class DelegatingAsyncBatchPersistence implements XydraAsyncBatchPersistence {
	
	protected XydraBlockingPersistence noBatchStore;
	
	public DelegatingAsyncBatchPersistence(XydraBlockingPersistence base) {
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
		if(commands == null) {
			throw new IllegalArgumentException("commands must not be null");
		}
		BatchedResult<Long>[] result = executeCommands(actorId, commands);
		if(callback != null) {
			callback.onSuccess(result);
		}
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        GetEventsRequest[] getEventsRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		if(commands == null) {
			throw new IllegalArgumentException("commands must not be null");
		}
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests must not be null");
		}
		BatchedResult<Long>[] commandResults = executeCommands(actorId, commands);
		BatchedResult<XEvent[]>[] eventResults = getEvents(getEventsRequests);
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
	public void getEvents(GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests must not be null");
		}
		callback.onSuccess(getEvents(getEventsRequests));
	}
	
	@Override
	public void getModelIds(Callback<Set<XID>> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		Set<XID> ids;
		try {
			ids = this.noBatchStore.getModelIds();
		} catch(Exception e) {
			callback.onFailure(e);
			return;
		}
		callback.onSuccess(ids);
	}
	
	@Override
	public void getModelRevisions(XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(modelAddresses == null) {
			throw new IllegalArgumentException("modelAddresses must not be null");
		}
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
		callback.onSuccess(results);
	}
	
	@Override
	public void getModelSnapshots(XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(modelAddresses == null) {
			throw new IllegalArgumentException("modelAddresses must not be null");
		}
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
		callback.onSuccess(results);
	}
	
	@Override
	public void getObjectSnapshots(XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		if(objectAddresses == null) {
			throw new IllegalArgumentException("objectAddresses must not be null");
		}
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
		callback.onSuccess(results);
	}
	
	@Override
	public void getRepositoryId(Callback<XID> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
		XID repoId = this.noBatchStore.getRepositoryId();
		callback.onSuccess(repoId);
	}
	
	@Override
	public void clear() {
		this.noBatchStore.clear();
	}
	
}

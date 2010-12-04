package org.xydra.store.impl.memory;

import java.util.ArrayList;
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
 * TODO catch exceptions and pass them to the callback
 * 
 * @author voelkel
 */
public class SynchronousNoAccessRightsStore implements XydraNoAccessRightsStore {
	
	protected XydraNoAccessRightsNoBatchNoAsyncStore noBatchStore;
	
	public SynchronousNoAccessRightsStore(XydraNoAccessRightsNoBatchNoAsyncStore base) {
		this.noBatchStore = base;
	}
	
	@SuppressWarnings("unchecked")
	private BatchedResult<Long>[] executeCommands(XID actorId, XCommand[] commands) {
		ArrayList<Long[]> list = new ArrayList<Long[]>(commands.length);
		BatchedResult<Long>[] results = (BatchedResult<Long>[])list.toArray();
		for(int i = 0; i < commands.length; i++) {
			results[i] = new BatchedResult<Long>(this.noBatchStore.executeCommand(actorId,
			        commands[i]));
		}
		return results;
	}
	
	@Override
	public void executeCommands(XID actorId, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		callback.onSuccess(executeCommands(actorId, commands));
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		BatchedResult<Long>[] commandResults = executeCommands(actorId, commands);
		BatchedResult<XEvent[]>[] eventResults = getEvents(getEventRequests);
		callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(
		        commandResults, eventResults));
	}
	
	@SuppressWarnings("unchecked")
	private BatchedResult<XEvent[]>[] getEvents(GetEventsRequest[] getEventsRequests) {
		ArrayList<XEvent[]> list = new ArrayList<XEvent[]>();
		BatchedResult<XEvent[]>[] results = (BatchedResult<XEvent[]>[])list.toArray();
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
		callback.onSuccess(getEvents(getEventRequests));
	}
	
	@Override
	public void getModelIds(Callback<Set<XID>> callback) {
		callback.onSuccess(this.noBatchStore.getModelIds());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void getModelRevisions(XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) {
		ArrayList<Long[]> list = new ArrayList<Long[]>(modelAddresses.length);
		BatchedResult<Long>[] results = (BatchedResult<Long>[])list.toArray();
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void getModelSnapshots(XAddress[] modelAddresses,
	        Callback<BatchedResult<XBaseModel>[]> callback) {
		ArrayList<XBaseModel[]> list = new ArrayList<XBaseModel[]>(modelAddresses.length);
		BatchedResult<XBaseModel>[] results = (BatchedResult<XBaseModel>[])list.toArray();
		
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void getObjectSnapshots(XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) {
		ArrayList<XBaseObject[]> list = new ArrayList<XBaseObject[]>(objectAddresses.length);
		BatchedResult<XBaseObject>[] results = (BatchedResult<XBaseObject>[])list.toArray();
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
		callback.onSuccess(this.noBatchStore.getRepositoryId());
	}
	
}

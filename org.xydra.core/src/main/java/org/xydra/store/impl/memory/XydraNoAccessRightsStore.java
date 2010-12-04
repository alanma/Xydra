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
 * A variant of the {@link XydraStore} without access rights parameters.
 * 
 * @author voelkel
 */
public interface XydraNoAccessRightsStore {
	
	void executeCommands(XID actorId, XCommand[] commands, Callback<BatchedResult<Long>[]> callback);
	
	void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        GetEventsRequest[] getEventRequests, Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback);
	
	void getEvents(GetEventsRequest[] getEventRequests, Callback<BatchedResult<XEvent[]>[]> callback);
	
	void getModelIds(Callback<Set<XID>> callback);
	
	void getModelRevisions(XAddress[] modelAddresses, Callback<BatchedResult<Long>[]> callback);
	
	void getModelSnapshots(XAddress[] modelAddresses, Callback<BatchedResult<XBaseModel>[]> callback);
	
	void getObjectSnapshots(XAddress[] objectAddresses, Callback<BatchedResult<XBaseObject>[]> callback);
	
	void getRepositoryId(Callback<XID> callback);
	
}

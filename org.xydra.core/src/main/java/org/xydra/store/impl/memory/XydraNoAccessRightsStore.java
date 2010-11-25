package org.xydra.store.impl.memory;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.Callback;


public interface XydraNoAccessRightsStore {
	
	void executeCommands(XID actorId, XCommand[] commands, Callback<long[]> callback);
	
	void executeCommandsAndGetEvents(XCommand[] commands, XAddress[] addressesToGetEventsFor,
	        long beginRevision, long endRevision, Callback<Pair<long[],XEvent[][]>> callback);
	
	void getEvents(XAddress[] addresses, long beginRevision, long endRevision,
	        Callback<XEvent[][]> callback);
	
	void getModelIds(XID repositoryId, Callback<Set<XID>> callback);
	
	void getModelRevisions(XAddress[] modelAddresses, Callback<long[]> callback);
	
	void getModelSnapshots(XAddress[] modelAddresses, Callback<XBaseModel[]> callback);
	
	void getObjectSnapshots(XAddress[] objectAddresses, Callback<XBaseObject[]> callback);
	
	void getRepositoryId(Callback<XID> callback);
	
}

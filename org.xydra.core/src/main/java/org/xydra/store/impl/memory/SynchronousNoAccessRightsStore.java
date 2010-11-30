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


/**
 * An implementation of {@link XydraNoAccessRightsStore} that maps all async
 * batch calls to an instance of {@link XydraNoAccessRightsNoBatchNoAsyncStore}
 * which treats them as single-op blocking call.
 * 
 * TODO catch excpetions and pass them to the callback
 * 
 * @author voelkel
 */
public class SynchronousNoAccessRightsStore implements XydraNoAccessRightsStore {
	
	protected XydraNoAccessRightsNoBatchNoAsyncStore noBatchStore;
	
	public SynchronousNoAccessRightsStore(XydraNoAccessRightsNoBatchNoAsyncStore base) {
		this.noBatchStore = base;
	}
	
	private long[] executeCommands(XID actorId, XCommand[] commands) {
		long[] results = new long[commands.length];
		for(int i = 0; i < commands.length; i++) {
			results[i] = this.noBatchStore.executeCommand(actorId, commands[i]);
		}
		return results;
	}
	
	@Override
	public void executeCommands(XID actorId, XCommand[] commands, Callback<long[]> callback) {
		callback.onSuccess(executeCommands(actorId, commands));
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, XCommand[] commands,
	        XAddress[] addressesToGetEventsFor, long beginRevision, long endRevision,
	        Callback<Pair<long[],XEvent[][]>> callback) {
		long[] commandResults = executeCommands(actorId, commands);
		XEvent[][] eventResults = getEvents(addressesToGetEventsFor, beginRevision, endRevision);
		callback.onSuccess(new Pair<long[],XEvent[][]>(commandResults, eventResults));
	}
	
	private XEvent[][] getEvents(XAddress[] addresses, long beginRevision, long endRevision) {
		XEvent[][] results = new XEvent[addresses.length][];
		for(int i = 0; i < addresses.length; i++) {
			results[i] = this.noBatchStore.getEvents(addresses[i], beginRevision, endRevision);
		}
		return results;
	}
	
	@Override
	public void getEvents(XAddress[] addresses, long beginRevision, long endRevision,
	        Callback<XEvent[][]> callback) {
		callback.onSuccess(getEvents(addresses, beginRevision, endRevision));
	}
	
	@Override
	public void getModelIds(XID repositoryId, Callback<Set<XID>> callback) {
		callback.onSuccess(this.noBatchStore.getModelIds());
	}
	
	@Override
	public void getModelRevisions(XAddress[] modelAddresses, Callback<long[]> callback) {
		long[] results = new long[modelAddresses.length];
		for(int i = 0; i < modelAddresses.length; i++) {
			results[i] = this.noBatchStore.getModelRevision(modelAddresses[i]);
		}
		callback.onSuccess(results);
	}
	
	@Override
	public void getModelSnapshots(XAddress[] modelAddresses, Callback<XBaseModel[]> callback) {
		XBaseModel[] results = new XBaseModel[modelAddresses.length];
		for(int i = 0; i < modelAddresses.length; i++) {
			results[i] = this.noBatchStore.getModelSnapshot(modelAddresses[i]);
		}
		callback.onSuccess(results);
	}
	
	@Override
	public void getObjectSnapshots(XAddress[] objectAddresses, Callback<XBaseObject[]> callback) {
		XBaseObject[] results = new XBaseObject[objectAddresses.length];
		for(int i = 0; i < objectAddresses.length; i++) {
			results[i] = this.noBatchStore.getObjectSnapshot(objectAddresses[i]);
		}
		callback.onSuccess(results);
	}
	
	@Override
	public void getRepositoryId(Callback<XID> callback) {
		callback.onSuccess(this.noBatchStore.getRepositoryId());
	}
	
}

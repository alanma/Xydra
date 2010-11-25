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
import org.xydra.store.XydraStore;


/**
 * A simple in-memory implementation of {@link XydraStore} that allows
 * everything.
 * 
 * TODO should share code with gae impl
 * 
 * @author voelkel
 * 
 */
public class AllowAllMemoryStore implements XydraStore {
	
	private XydraNoAccessRightsStore storeWithoutAccessRights;
	
	public AllowAllMemoryStore(XID repositoryId) {
		this.storeWithoutAccessRights = new MemoryNoAccessRightsStore(repositoryId);
	}
	
	@Override
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback) {
		callback.onSuccess(true);
	}
	
	@Override
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<long[]> callback) {
		this.storeWithoutAccessRights.executeCommands(actorId, commands, callback);
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        XAddress[] addressesToGetEventsFor, long beginRevision, long endRevision,
	        Callback<Pair<long[],XEvent[][]>> callback) {
		this.storeWithoutAccessRights.executeCommandsAndGetEvents(commands,
		        addressesToGetEventsFor, beginRevision, endRevision, callback);
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, XAddress[] addresses,
	        long beginRevision, long endRevision, Callback<XEvent[][]> callback) {
		this.storeWithoutAccessRights.getEvents(addresses, beginRevision, endRevision, callback);
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, XID repositoryId,
	        Callback<Set<XID>> callback) {
		this.storeWithoutAccessRights.getModelIds(repositoryId, callback);
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<long[]> callback) throws IllegalArgumentException {
		this.storeWithoutAccessRights.getModelRevisions(modelAddresses, callback);
	}
	
	@Override
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<XBaseModel[]> callback) throws IllegalArgumentException {
		this.storeWithoutAccessRights.getModelSnapshots(modelAddresses, callback);
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<XBaseObject[]> callback) {
		this.storeWithoutAccessRights.getObjectSnapshots(objectAddresses, callback);
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback) {
		this.storeWithoutAccessRights.getRepositoryId(callback);
	}
	
}

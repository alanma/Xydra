package org.xydra.store.impl.gae;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.server.impl.InfrastructureServiceFactory;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.changes.GaeChangesService;
import org.xydra.store.impl.gae.changes.InternalGaeXEntity;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotService;
import org.xydra.store.impl.memory.AllowAllStore;
import org.xydra.store.impl.memory.MemoryStore;
import org.xydra.store.impl.memory.XydraNoAccessRightsNoBatchNoAsyncStore;


/**
 * An {@link XydraStore} implementation that persists changes in the Google
 * Appengine datastore.
 * 
 * @author dscharrer
 */
public class GaeXydraStore implements XydraNoAccessRightsNoBatchNoAsyncStore {
	
	private final XAddress repoAddr;
	
	public GaeXydraStore(XID repoId) {
		
		// To enable local JUnit testing with multiple threads
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Register AppEngine infrastructure services
		InfrastructureServiceFactory.setProvider(new GaeInfrastructureProvider());
		
		this.repoAddr = XX.toAddress(repoId, null, null, null);
	}
	
	private XAddress getModelAddress(XID modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}
	
	private GaeChangesService getChangesService(XID modelId) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// IMPROVE cache GaeChangesService instances?
		return new GaeChangesService(getModelAddress(modelId));
	}
	
	private GaeSnapshotService getSnapshotService(XID modelId) {
		// IMPROVE cache GaeSnapshotService instances?
		return new GaeSnapshotService(getChangesService(modelId));
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		
		GaeSnapshotService s = getSnapshotService(modelId);
		
		if(s == null) {
			return null;
		}
		
		return s.getSnapshot();
	}
	
	@Override
	public long executeCommand(XID actorId, XCommand command) {
		checkAddres(command.getTarget());
		
		XID modelId = command.getChangedEntity().getModel();
		
		// TODO wrap GAE exceptions in InternalStoreExceptions
		
		return getChangesService(modelId).executeCommand(command, actorId);
	}
	
	@Override
	public XEvent[] getEvents(XAddress address, long beginRevision, long endRevision) {
		checkAddres(address);
		if(address.getModel() == null) {
			throw new RequestException("address must specify a model, was " + address);
		}
		Iterator<XEvent> it = getChangesService(address.getModel()).getEventsBetween(beginRevision,
		        endRevision);
		ArrayList<XEvent> events = new ArrayList<XEvent>();
		while(it.hasNext()) {
			events.add(it.next());
		}
		return events.toArray(new XEvent[events.size()]);
	}
	
	@Override
	public Set<XID> getModelIds() {
		return InternalGaeXEntity.findChildren(this.repoAddr);
	}
	
	@Override
	public long getModelRevision(XAddress address) {
		checkAddres(address);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + address);
		}
		return getChangesService(address.getModel()).getCurrentRevisionNumber();
	}
	
	@Override
	public XBaseModel getModelSnapshot(XAddress address) {
		checkAddres(address);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + address);
		}
		return getSnapshotService(address.getModel()).getSnapshot();
	}
	
	@Override
	public XBaseObject getObjectSnapshot(XAddress address) {
		checkAddres(address);
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("address must refer to an object, was " + address);
		}
		// IMPROVE generate the object snapshot directly
		return getSnapshotService(address.getModel()).getSnapshot().getObject(address.getObject());
	}
	
	private void checkAddres(XAddress address) {
		if(!this.repoAddr.equalsOrContains(address)) {
			throw new RequestException("address " + address + " is not contained in repository "
			        + this.repoAddr);
		}
	}
	
	@Override
	public XID getRepositoryId() {
		return this.repoAddr.getRepository();
	}
	
	static public XydraStore get() {
		return new MemoryStore(new AllowAllStore(new GaeXydraStore(XX.toId("data"))),
		        new AllowAllStore(new GaeXydraStore(XX.toId("rights"))));
	}
	
}

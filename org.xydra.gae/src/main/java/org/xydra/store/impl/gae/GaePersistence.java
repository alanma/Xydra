package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.server.impl.InfrastructureServiceFactory;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.changes.GaeChangesService;
import org.xydra.store.impl.gae.changes.InternalGaeXEntity;
import org.xydra.store.impl.gae.changes.XIDLengthException;
import org.xydra.store.impl.gae.snapshot.GaeSnapshotService;


/**
 * An {@link XydraPersistence} implementation that persists changes in the
 * Google AppEngine datastore.
 * 
 * @author dscharrer
 */
@RunsInAppEngine(true)
@RunsInGWT(false)
public class GaePersistence implements XydraPersistence {
	
	private final XAddress repoAddr;
	
	/**
	 * This method is used to instantiate the persistence via reflection in
	 * SharedXydraPersistence.
	 * 
	 * @param repoId repository ID
	 */
	public GaePersistence(XID repoId) {
		if(repoId == null) {
			throw new IllegalArgumentException("repoId was null");
		}
		checkIdLength(repoId);
		
		// To enable local JUnit testing with multiple threads
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Register AppEngine infrastructure services
		InfrastructureServiceFactory.setProvider(new GaeInfrastructureProvider());
		
		this.repoAddr = XX.toAddress(repoId, null, null, null);
	}
	
	private static final int MAX_ID_LENGTH = 100;
	
	private static void checkIdLength(XID id) {
		if(id != null && id.toString().length() > MAX_ID_LENGTH) {
			throw new XIDLengthException(id);
		}
	}
	
	private static void checkIdLengths(XAtomicCommand command) {
		XAddress addr = command.getChangedEntity();
		// repo and model IDs already checked
		checkIdLength(addr.getObject());
		checkIdLength(addr.getField());
	}
	
	private static void checkIdLengths(XCommand command) {
		if(command instanceof XTransaction) {
			for(XAtomicCommand ac : (XTransaction)command) {
				checkIdLengths(ac);
			}
		} else {
			assert command instanceof XAtomicCommand;
			checkIdLengths((XAtomicCommand)command);
		}
	}
	
	private XAddress getModelAddress(XID modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}
	
	private Map<XID,GaeChangesService> gcsCache = new HashMap<XID,GaeChangesService>();
	
	private GaeChangesService getChangesService(XID modelId) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		synchronized(this.gcsCache) {
			GaeChangesService gcs = this.gcsCache.get(modelId);
			if(gcs == null) {
				gcs = new GaeChangesService(getModelAddress(modelId));
				this.gcsCache.put(modelId, gcs);
			}
			return gcs;
		}
	}
	
	private GaeSnapshotService getSnapshotService(XID modelId) {
		// IMPROVE cache GaeSnapshotService instances?
		return new GaeSnapshotService(getChangesService(modelId));
	}
	
	public XReadableModel getModelSnapshot(XID modelId) {
		if(modelId == null) {
			throw new IllegalArgumentException("modelId was null");
		}
		
		GaeSnapshotService s = getSnapshotService(modelId);
		
		if(s == null) {
			return null;
		}
		
		return s.getSnapshot();
	}
	
	@Override
	public long executeCommand(XID actorId, XCommand command) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId was null");
		}
		if(command == null) {
			throw new IllegalArgumentException("command was null");
		}
		checkAddres(command.getTarget());
		
		checkIdLengths(command);
		
		XID modelId = command.getChangedEntity().getModel();
		checkIdLength(modelId);
		
		// TODO wrap GAE exceptions in InternalStoreExceptions
		
		return getChangesService(modelId).executeCommand(command, actorId);
	}
	
	@Override
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		checkAddres(address);
		if(address.getModel() == null) {
			throw new RequestException("address must specify a model, was " + address);
		}
		return getChangesService(address.getModel()).getEventsBetween(beginRevision, endRevision);
	}
	
	@Override
	public Set<XID> getModelIds() {
		return InternalGaeXEntity.findChildren(this.repoAddr);
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		if(modelId == null) {
			throw new IllegalArgumentException("modelId was null");
		}
		return InternalGaeXEntity.exists(getModelAddress(modelId));
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
	public XWritableModel getModelSnapshot(XAddress address) {
		checkAddres(address);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + address);
		}
		return getSnapshotService(address.getModel()).getSnapshot();
	}
	
	@Override
	public XWritableObject getObjectSnapshot(XAddress address) {
		checkAddres(address);
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("address must refer to an object, was " + address);
		}
		// IMPROVE generate the object snapshot directly
		XWritableModel modelSnapshot = getSnapshotService(address.getModel()).getSnapshot();
		if(modelSnapshot == null) {
			return null;
		}
		return modelSnapshot.getObject(address.getObject());
	}
	
	/**
	 * Check for null and if the address is within this repo
	 * 
	 * @param address
	 */
	private void checkAddres(XAddress address) {
		if(address == null) {
			throw new IllegalArgumentException("address was null");
		}
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
		return new DelegatingSecureStore(new GaePersistence(getDefaultRepositoryId()),
		        XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
	
	/**
	 * This method is used in tests.
	 * 
	 * @return the default repository id used by {@link #get()}
	 */
	static public XID getDefaultRepositoryId() {
		return XX.toId("data");
	}
	
	@Override
	public void clear() {
		// FIXME implement: delete ALL local data
	}
	
}

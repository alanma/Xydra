package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
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
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.InternalStoreException;
import org.xydra.store.RequestException;
import org.xydra.store.RevisionState;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.changes.Utils;
import org.xydra.store.impl.gae.changes.XIDLengthException;
import org.xydra.store.impl.gae.execute.InternalGaeXEntity;

import com.google.appengine.api.datastore.CommittedButStillApplyingException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;


/**
 * An {@link XydraPersistence} implementation that persists changes in the
 * Google AppEngine datastore.
 * 
 * @author dscharrer
 */
@RunsInAppEngine(true)
@RunsInGWT(false)
@RequiresAppEngine(true)
public class GaePersistence implements XydraPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(GaePersistence.class);
	
	public static final boolean CACHE_MODEL_PERSISTENCES = true;
	
	private static final int MAX_ID_LENGTH = 100;
	
	@GaeOperation()
	private static void checkIdLength(XID id) {
		if(id != null && id.toString().length() > MAX_ID_LENGTH) {
			throw new XIDLengthException(id);
		}
	}
	
	@GaeOperation()
	private static void checkIdLengths(XAtomicCommand command) {
		XAddress addr = command.getChangedEntity();
		// repo and model IDs already checked
		checkIdLength(addr.getObject());
		checkIdLength(addr.getField());
	}
	
	@GaeOperation()
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
	
	/**
	 * @return a secure {@link XydraStore} instance based on a
	 *         {@link GaePersistence} with the default repository id
	 */
	static synchronized public XydraStore get() {
		return new DelegatingSecureStore(new GaePersistence(getDefaultRepositoryId()),
		        XydraStoreAdmin.XYDRA_ADMIN_ID);
	}
	
	/**
	 * @return the default repository id, as used e.g. by {@link #get()}
	 */
	@GaeOperation()
	static public XID getDefaultRepositoryId() {
		return XX.toId("data");
	}
	
	private Map<XID,GaeModelPersistence> modelPersistenceMap = new HashMap<XID,GaeModelPersistence>();
	
	private final XAddress repoAddr;
	
	/**
	 * This method is used to instantiate the persistence
	 * 
	 * @param repoId repository ID
	 */
	@GaeOperation()
	public GaePersistence(XID repoId) {
		if(repoId == null) {
			throw new IllegalArgumentException("repoId was null");
		}
		checkIdLength(repoId);
		this.repoAddr = XX.toAddress(repoId, null, null, null);
	}
	
	/**
	 * Check for null and if the address is within this repo
	 * 
	 * @param address
	 */
	@GaeOperation()
	private void checkAddress(XAddress address) {
		if(address == null) {
			throw new IllegalArgumentException("address was null");
		}
		if(!this.repoAddr.equalsOrContains(address)) {
			throw new RequestException("address " + address + " is not contained in repository "
			        + this.repoAddr);
		}
	}
	
	@Override
	public synchronized void clear() {
		log.info("Clear");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		SyncDatastore.clear();
		Memcache.clear();
	}
	
	@Override
	public synchronized long executeCommand(XID actorId, XCommand command) {
		log.debug(actorId + " executes command: " + DebugFormatter.format(command));
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(actorId == null) {
			throw new IllegalArgumentException("actorId was null");
		}
		if(command == null) {
			throw new IllegalArgumentException("command was null");
		}
		checkAddress(command.getTarget());
		checkIdLengths(command);
		
		XID modelId = command.getChangedEntity().getModel();
		checkIdLength(modelId);
		
		try {
			return getModelPersistence(modelId).executeCommand(command, actorId);
		} catch(DatastoreTimeoutException e) {
			throw new InternalStoreException("Storage did not work - please retry", e, 503);
		} catch(DatastoreFailureException e) {
			throw new InternalStoreException("Storage failed. Don't retry.", e, 500);
		} catch(CommittedButStillApplyingException e) {
			throw new InternalStoreException(
			        "Storage waiting for some work to complete - please retry", e, 503);
		}
	}
	
	/**
	 * @param modelId ..
	 * @return the {@link GaeModelPersistence} responsible for managing the
	 *         given modelId
	 */
	@GaeOperation()
	private GaeModelPersistence getModelPersistence(XID modelId) {
		synchronized(this.modelPersistenceMap) {
			GaeModelPersistence modelPersistence = null;
			if(CACHE_MODEL_PERSISTENCES) {
				modelPersistence = this.modelPersistenceMap.get(modelId);
			}
			if(modelPersistence == null) {
				modelPersistence = new GaeModelPersistence(getModelAddress(modelId));
				this.modelPersistenceMap.put(modelId, modelPersistence);
			}
			return modelPersistence;
		}
	}
	
	@Override
	public synchronized List<XEvent> getEvents(XAddress address, long beginRevision,
	        long endRevision) {
		checkAddress(address);
		if(address.getModel() == null) {
			throw new RequestException("address must specify a model, was " + address);
		}
		log.debug("getEvents for " + address + " [" + beginRevision + "," + endRevision + "]");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(address.getModel()).getEventsBetween(address, beginRevision,
		        endRevision);
	}
	
	@GaeOperation()
	private XAddress getModelAddress(XID modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}
	
	@Override
	@GaeOperation(datastoreRead = true)
	public synchronized Set<XID> getModelIds() {
		log.debug("getModelIds");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return Utils.findChildren(this.repoAddr);
	}
	
	@Override
	public synchronized RevisionState getModelRevision(XAddress address) {
		checkAddress(address);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + address);
		}
		log.debug("getModelRevision of " + address);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(address.getModel()).getModelRevision();
	}
	
	@Override
	public synchronized XWritableModel getModelSnapshot(XAddress address) {
		checkAddress(address);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + address);
		}
		log.debug("get model snapshot of " + address);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(address.getModel()).getSnapshot();
	}
	
	@Override
	public synchronized XWritableObject getObjectSnapshot(XAddress address) {
		checkAddress(address);
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("address must refer to an object, was " + address);
		}
		log.debug("get object snapshot of " + address);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// TODO getting object snapshots must be more performant
		return getModelPersistence(address.getModel()).getObjectSnapshot(address.getObject());
	}
	
	@Override
	@GaeOperation()
	public XID getRepositoryId() {
		return this.repoAddr.getRepository();
	}
	
	@Override
	public synchronized boolean hasModel(XID modelId) {
		if(modelId == null) {
			throw new IllegalArgumentException("modelId was null");
		}
		log.debug("model '" + modelId + "' exists?");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return InternalGaeXEntity.exists(getModelAddress(modelId));
	}
	
}

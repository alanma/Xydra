package org.xydra.store.impl.gae;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.ModelRevision;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.gae.changes.Utils;
import org.xydra.store.impl.gae.changes.XIdLengthException;
import org.xydra.store.impl.gae.ng.GaeModelPersistenceNG;

import com.google.appengine.api.datastore.CommittedButStillApplyingException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


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
	
	public static final String LAST_UNICODE_CHAR = "\uFFFF";
	
	@GaeOperation()
	private static void checkIdLength(XId id) {
		if(id != null && id.toString().length() > MAX_ID_LENGTH) {
			throw new XIdLengthException(id);
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
	static public XId getDefaultRepositoryId() {
		return XX.toId("data");
	}
	
	// private Map<XId,IGaeModelPersistence> modelPersistenceMapXXXXOLD = new
	// HashMap<XId,IGaeModelPersistence>();
	
	private Cache<XId,IGaeModelPersistence> modelPersistenceMap = CacheBuilder.newBuilder()
	        .maximumSize(10).expireAfterAccess(5, TimeUnit.MINUTES).build();
	
	private final XAddress repoAddr;
	
	/**
	 * This method is used to instantiate the persistence
	 * 
	 * @param repoId repository ID
	 */
	@GaeOperation()
	public GaePersistence(XId repoId) {
		log.debug("static stuff done");
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
	public synchronized long executeCommand(XId actorId, XCommand command) {
		// FIXME log less
		log.info(actorId + " executes command: " + DebugFormatter.format(command));
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if(actorId == null) {
			throw new IllegalArgumentException("actorId was null");
		}
		if(command == null) {
			throw new IllegalArgumentException("command was null");
		}
		checkAddress(command.getTarget());
		checkIdLengths(command);
		
		XId modelId = command.getChangedEntity().getModel();
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
	 * @return the {@link IGaeModelPersistence} responsible for managing the
	 *         given modelId. Never null.
	 */
	@GaeOperation()
	@Setting("decides which GAE impl is used")
	private IGaeModelPersistence getModelPersistence(XId modelId) {
		synchronized(this.modelPersistenceMap) {
			IGaeModelPersistence modelPersistence = null;
			if(CACHE_MODEL_PERSISTENCES) {
				modelPersistence = this.modelPersistenceMap.getIfPresent(modelId);
			}
			if(modelPersistence == null) {
				XAddress modelAddress = getModelAddress(modelId);
				XyAssert.xyAssert(modelAddress != null);
				modelPersistence = new GaeModelPersistenceNG(modelAddress);
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
	private XAddress getModelAddress(XId modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}
	
	@Override
	@GaeOperation(datastoreRead = true)
	public synchronized Set<XId> getManagedModelIds() {
		log.debug("getModelIds");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// find models ids by looking for all change entities with rev=0
		
		Key first = KeyFactory.createKey(KeyStructure.KIND_XCHANGE,
		        "0/" + this.repoAddr.getRepository());
		Key last = KeyFactory.createKey(KeyStructure.KIND_XCHANGE, first.getName()
		        + LAST_UNICODE_CHAR);
		
		Query q = new Query(KeyStructure.KIND_XCHANGE);
		
		q.setFilter(
		
		CompositeFilterOperator.and(
		
		new Query.FilterPredicate(Utils.PROP_KEY, FilterOperator.GREATER_THAN, first),
		
		new Query.FilterPredicate(Utils.PROP_KEY, FilterOperator.LESS_THAN, last)
		
		));
		
		q.setKeysOnly();
		
		Set<XId> managedModelIds = new HashSet<XId>();
		for(Entity e : SyncDatastore.prepareQuery(q).asIterable()) {
			Key key = e.getKey();
			XAddress xa = KeyStructure.getAddressFromChangeKey(key);
			assert xa.getRepository().equals(this.repoAddr.getRepository());
			managedModelIds.add(xa.getModel());
		}
		log.debug("This repo " + this.repoAddr + " manages " + managedModelIds.size() + " models");
		return managedModelIds;
	}
	
	@Override
	public synchronized ModelRevision getModelRevision(GetWithAddressRequest addressRequest) {
		checkAddress(addressRequest.address);
		if(addressRequest.address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + addressRequest);
		}
		log.debug("getModelRevision of " + addressRequest);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(addressRequest.address.getModel()).getModelRevision(
		        addressRequest.includeTentative);
	}
	
	@Override
	public synchronized XWritableModel getModelSnapshot(GetWithAddressRequest addressRequest) {
		checkAddress(addressRequest.address);
		if(addressRequest.address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + addressRequest);
		}
		log.debug("get model snapshot of " + addressRequest);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(addressRequest.address.getModel()).getSnapshot(
		        addressRequest.includeTentative);
	}
	
	@Override
	public synchronized XWritableObject getObjectSnapshot(GetWithAddressRequest addressRequest) {
		checkAddress(addressRequest.address);
		if(addressRequest.address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("address must refer to an object, was " + addressRequest);
		}
		log.debug("get object snapshot of " + addressRequest);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// TODO getting object snapshots must be more performant
		return getModelPersistence(addressRequest.address.getModel()).getObjectSnapshot(
		        addressRequest.address.getObject(), addressRequest.includeTentative);
	}
	
	@Override
	@GaeOperation()
	public XId getRepositoryId() {
		return this.repoAddr.getRepository();
	}
	
	@Override
	public synchronized boolean hasManagedModel(XId modelId) {
		if(modelId == null) {
			throw new IllegalArgumentException("modelId was null");
		}
		log.debug("model '" + modelId + "' exists?");
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		return getModelPersistence(modelId).modelHasBeenManaged();
	}
	
}

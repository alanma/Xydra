package org.xydra.store.impl.gae;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.InternalStoreException;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.impl.delegate.DelegatingSecureStore;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.gae.changes.XIdLengthException;
import org.xydra.store.impl.gae.ng.GaeModelPersistenceNG;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.XGae;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.CommittedButStillApplyingException;
import org.xydra.xgae.datastore.api.DatastoreFailureException;
import org.xydra.xgae.datastore.api.DatastoreTimeoutException;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SPreparedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

	@XGaeOperation()
	private static void checkIdLength(XId id) {
		if (id != null && id.toString().length() > MAX_ID_LENGTH) {
			throw new XIdLengthException(id);
		}
	}

	@XGaeOperation()
	private static void checkIdLengths(XAtomicCommand command) {
		XAddress addr = command.getChangedEntity();
		// repo and model IDs already checked
		checkIdLength(addr.getObject());
		checkIdLength(addr.getField());
	}

	@XGaeOperation()
	private static void checkIdLengths(XCommand command) {
		if (command instanceof XTransaction) {
			for (XAtomicCommand ac : (XTransaction) command) {
				checkIdLengths(ac);
			}
		} else {
			assert command instanceof XAtomicCommand;
			checkIdLengths((XAtomicCommand) command);
		}
	}

	/**
	 * @return a secure {@link XydraStore} instance based on a
	 *         {@link GaePersistence} with the default repository id
	 */
	static synchronized public XydraStore create() {
		return new DelegatingSecureStore(new GaePersistence(getDefaultRepositoryId()),
				XydraStoreAdmin.XYDRA_ADMIN_ID);
	}

	/**
	 * @return the default repository id, as used e.g. by {@link #create()}
	 */
	@XGaeOperation()
	static public XId getDefaultRepositoryId() {
		return XX.toId("data");
	}

	// private Map<XId,IGaeModelPersistence> modelPersistenceMapXXXXOLD = new
	// HashMap<XId,IGaeModelPersistence>();

	private Cache<XId, IGaeModelPersistence> modelPersistenceMap;

	private final XAddress repoAddr;

	/**
	 * This method is used to instantiate the persistence
	 * 
	 * @param repoId
	 *            repository ID
	 */
	@XGaeOperation()
	public GaePersistence(XId repoId) {
		this.modelPersistenceMap = CacheBuilder.newBuilder().maximumSize(10)
				.expireAfterAccess(5, TimeUnit.MINUTES).build();
		log.debug("static stuff done");
		if (repoId == null) {
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
	@XGaeOperation()
	private void checkAddress(XAddress address) {
		if (address == null) {
			throw new IllegalArgumentException("address was null");
		}
		if (!this.repoAddr.equalsOrContains(address)) {
			throw new RequestException("address " + address + " is not contained in repository "
					+ this.repoAddr);
		}
	}

	@Override
	public synchronized void clear() {
		log.info("Clear");
		XGae.get().datastore().sync().clear();
		XGae.get().memcache().clear();
		this.modelPersistenceMap.invalidateAll();
		assert this.modelPersistenceMap.asMap().isEmpty();
		this.modelPersistenceMap.cleanUp();
		assert this.modelPersistenceMap.size() == 0;
		InstanceContext.clear();
		Memcache.clear();
	}

	@Override
	public synchronized long executeCommand(XId actorId, XCommand command) {
		// FIXME log less
		log.info(actorId + " executes command: " + DebugFormatter.format(command));
		if (actorId == null) {
			throw new IllegalArgumentException("actorId was null");
		}
		if (command == null) {
			throw new IllegalArgumentException("command was null");
		}
		checkAddress(command.getTarget());
		checkIdLengths(command);

		XId modelId = command.getChangedEntity().getModel();
		checkIdLength(modelId);

		try {
			IGaeModelPersistence mp = getModelPersistence(modelId);
			return mp.executeCommand(command, actorId);
		} catch (DatastoreTimeoutException e) {
			throw new InternalStoreException("Storage did not work - please retry", e, 503);
		} catch (DatastoreFailureException e) {
			throw new InternalStoreException("Storage failed. Don't retry.", e, 500);
		} catch (CommittedButStillApplyingException e) {
			throw new InternalStoreException(
					"Storage waiting for some work to complete - please retry", e, 503);
		}
	}

	/**
	 * @param modelId
	 *            ..
	 * @return the {@link IGaeModelPersistence} responsible for managing the
	 *         given modelId. Never null.
	 */
	@XGaeOperation()
	@Setting("decides which GAE impl is used")
	private IGaeModelPersistence getModelPersistence(XId modelId) {
		synchronized (this.modelPersistenceMap) {
			IGaeModelPersistence modelPersistence = null;
			if (CACHE_MODEL_PERSISTENCES) {
				modelPersistence = this.modelPersistenceMap.getIfPresent(modelId);
			}
			if (modelPersistence == null) {
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
		if (address.getModel() == null) {
			throw new RequestException("address must specify a model, was " + address);
		}
		log.debug("getEvents for " + address + " [" + beginRevision + "," + endRevision + "]");
		return getModelPersistence(address.getModel()).getEventsBetween(address, beginRevision,
				endRevision);
	}

	@XGaeOperation()
	private XAddress getModelAddress(XId modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}

	@Override
	@XGaeOperation(datastoreRead = true)
	public synchronized Set<XId> getManagedModelIds() {
		log.debug("getModelIds");

		// find models ids by looking for all change entities with rev=0
		String low = "0/" + this.repoAddr.getRepository();
		String high = low + LAST_UNICODE_CHAR;
		SPreparedQuery preparedQuery = XGae.get().datastore().sync()
				.prepareRangeQuery(KeyStructure.KIND_XCHANGE, true, low, high);

		Set<XId> managedModelIds = new HashSet<XId>();
		for (SEntity e : preparedQuery.asIterable()) {
			SKey key = e.getKey();
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
		if (addressRequest.address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + addressRequest);
		}
		log.debug("getModelRevision of " + addressRequest);
		IGaeModelPersistence mp = getModelPersistence(addressRequest.address.getModel());
		return mp.getModelRevision(addressRequest.includeTentative);
	}

	@Override
	public synchronized XWritableModel getModelSnapshot(GetWithAddressRequest addressRequest) {
		checkAddress(addressRequest.address);
		if (addressRequest.address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("address must refer to a model, was " + addressRequest);
		}
		log.debug("get model snapshot of " + addressRequest);
		return getModelPersistence(addressRequest.address.getModel()).getSnapshot(
				addressRequest.includeTentative);
	}

	@Override
	public synchronized XWritableObject getObjectSnapshot(GetWithAddressRequest addressRequest) {
		checkAddress(addressRequest.address);
		if (addressRequest.address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("address must refer to an object, was " + addressRequest);
		}
		log.debug("get object snapshot of " + addressRequest);
		// TODO getting object snapshots must be more performant
		return getModelPersistence(addressRequest.address.getModel()).getObjectSnapshot(
				addressRequest.address.getObject(), addressRequest.includeTentative);
	}

	@Override
	@XGaeOperation()
	public XId getRepositoryId() {
		return this.repoAddr.getRepository();
	}

	@Override
	public synchronized boolean hasManagedModel(XId modelId) {
		if (modelId == null) {
			throw new IllegalArgumentException("modelId was null");
		}
		log.debug("model '" + modelId + "' exists?");
		return getModelPersistence(modelId).modelHasBeenManaged();
	}

}

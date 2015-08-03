package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.StoreException;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.Callback;
import org.xydra.store.XydraStoreAdmin;


/**
 * Delegate to a blocking store.
 *
 * For better performance the null-checks for callback (in cases where it may be
 * null), and actorId are not performed (here modelled as assertions only).
 *
 * If password is null, no authorisation checks are performed - access rights
 * are checked.
 *
 * @author xamde
 *
 */
public class DelegateToBlockingStore implements XydraSingleOperationStore {

	private static final Logger log = LoggerFactory.getLogger(DelegateToBlockingStore.class);

	private final XydraBlockingStore blockingStore;

	public DelegateToBlockingStore(final XydraBlockingStore blockingStore) {
		this.blockingStore = blockingStore;
	}

	@Override
	public void checkLogin(final XId actorId, final String passwordHash, final Callback<Boolean> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final boolean result = this.blockingStore.checkLogin(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void executeCommand(final XId actorId, final String passwordHash, final XCommand command,
	        final Callback<Long> callbackOrNull) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		try {
			final long result = this.blockingStore.executeCommand(actorId, passwordHash, command);
			if(callbackOrNull != null) {
				callbackOrNull.onSuccess(result);
			}
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			if(callbackOrNull != null) {
				callbackOrNull.onFailure(e);
			}
		}
	}

	@Override
	public void getEvents(final XId actorId, final String passwordHash, final GetEventsRequest getEventsRequest,
	        final Callback<XEvent[]> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final XEvent[] result = this.blockingStore.getEvents(actorId, passwordHash, getEventsRequest);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getModelIds(final XId actorId, final String passwordHash, final Callback<Set<XId>> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final Set<XId> result = this.blockingStore.getModelIds(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getModelRevision(final XId actorId, final String passwordHash,
	        final GetWithAddressRequest modelAddress, final Callback<ModelRevision> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final ModelRevision result = this.blockingStore.getModelRevision(actorId, passwordHash,
			        modelAddress);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getModelSnapshot(final XId actorId, final String passwordHash, final GetWithAddressRequest modelAddress,
	        final Callback<XReadableModel> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final XReadableModel result = this.blockingStore.getModelSnapshot(actorId, passwordHash,
			        modelAddress);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getObjectSnapshot(final XId actorId, final String passwordHash, final GetWithAddressRequest objectAddressRequest,
	        final Callback<XReadableObject> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final XReadableObject result = this.blockingStore.getObjectSnapshot(actorId, passwordHash,
			        objectAddressRequest);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getRepositoryId(final XId actorId, final String passwordHash, final Callback<XId> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			final XId result = this.blockingStore.getRepositoryId(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this.blockingStore.getXydraStoreAdmin();
	}

}

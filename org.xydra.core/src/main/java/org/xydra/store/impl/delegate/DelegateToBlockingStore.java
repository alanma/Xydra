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
	
	private XydraBlockingStore blockingStore;
	
	public DelegateToBlockingStore(XydraBlockingStore blockingStore) {
		this.blockingStore = blockingStore;
	}
	
	@Override
	public void checkLogin(XId actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			boolean result = this.blockingStore.checkLogin(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void executeCommand(XId actorId, String passwordHash, XCommand command,
	        Callback<Long> callbackOrNull) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		try {
			long result = this.blockingStore.executeCommand(actorId, passwordHash, command);
			if(callbackOrNull != null) {
				callbackOrNull.onSuccess(result);
			}
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			if(callbackOrNull != null) {
				callbackOrNull.onFailure(e);
			}
		}
	}
	
	@Override
	public void getEvents(XId actorId, String passwordHash, GetEventsRequest getEventsRequest,
	        Callback<XEvent[]> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			XEvent[] result = this.blockingStore.getEvents(actorId, passwordHash, getEventsRequest);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getModelIds(XId actorId, String passwordHash, Callback<Set<XId>> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			Set<XId> result = this.blockingStore.getModelIds(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getModelRevision(XId actorId, String passwordHash,
	        GetWithAddressRequest modelAddress, Callback<ModelRevision> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			ModelRevision result = this.blockingStore.getModelRevision(actorId, passwordHash,
			        modelAddress);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getModelSnapshot(XId actorId, String passwordHash, GetWithAddressRequest modelAddress,
	        Callback<XReadableModel> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			XReadableModel result = this.blockingStore.getModelSnapshot(actorId, passwordHash,
			        modelAddress);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getObjectSnapshot(XId actorId, String passwordHash, GetWithAddressRequest objectAddressRequest,
	        Callback<XReadableObject> callback) throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			XReadableObject result = this.blockingStore.getObjectSnapshot(actorId, passwordHash,
			        objectAddressRequest);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getRepositoryId(XId actorId, String passwordHash, Callback<XId> callback)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		XyAssert.xyAssert(callback != null); assert callback != null;
		try {
			XId result = this.blockingStore.getRepositoryId(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this.blockingStore.getXydraStoreAdmin();
	}
	
}

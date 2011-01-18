package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.MAXDone;
import org.xydra.store.StoreException;
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
@MAXDone
public class DelegateToBlockingStore implements XydraSingleOperationStore {
	
	private static final Logger log = LoggerFactory.getLogger(DelegateToBlockingStore.class);
	
	private XydraBlockingStore blockingStore;
	
	public DelegateToBlockingStore(XydraBlockingStore blockingStore) {
		this.blockingStore = blockingStore;
	}
	
	@Override
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			boolean result = this.blockingStore.checkLogin(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getModelSnapshot(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<XReadableModel> callback) throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
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
	public void getModelRevision(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<Long> callback) throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			long result = this.blockingStore.getModelRevision(actorId, passwordHash, modelAddress);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getObjectSnapshot(XID actorId, String passwordHash, XAddress objectAddress,
	        Callback<XReadableObject> callback) throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			XReadableObject result = this.blockingStore.getObjectSnapshot(actorId, passwordHash,
			        objectAddress);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			Set<XID> result = this.blockingStore.getModelIds(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			XID result = this.blockingStore.getRepositoryId(actorId, passwordHash);
			callback.onSuccess(result);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void executeCommand(XID actorId, String passwordHash, XCommand command,
	        Callback<Long> callbackOrNull) throws IllegalArgumentException {
		assert actorId != null;
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
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest,
	        Callback<XEvent[]> callback) throws IllegalArgumentException {
		assert actorId != null;
		assert callback != null;
		try {
			XEvent[] result = this.blockingStore.getEvents(actorId, passwordHash, getEventsRequest);
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

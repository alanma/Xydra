package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.MAXDone;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


/**
 * Delegate asynchronous batch methods to asynchronous single-operation methods.
 * 
 * This expects that wrapped {@link XydraSingleOperationStore} to be able to
 * complete the requests and call any callbacks before we return, which might
 * not be the case for event-loop dependent implementations.
 * 
 * TODO wouldn't it be better to either delegate to a {@link XydraBlockingStore}
 * (a {@link XydraBlockingStore}->{@link XydraSingleOperationStore} wrapper can
 * then be used) -OR- to not wait for callbacks before returning and handle the
 * batched callback in the last called {@link SingleOpCallback}?
 * 
 * @author xamde
 */
@MAXDone
public class DelegateToSingleOperationStore implements XydraStore {
	
	private static class SingleOpCallback<T> implements Callback<T> {
		
		public boolean done = false;
		private Throwable exception;
		private T result;
		
		@Override
		public synchronized void onFailure(Throwable exception) {
			this.exception = exception;
			this.done = true;
			this.notify();
		}
		
		@Override
		public synchronized void onSuccess(T result) {
			this.result = result;
			this.done = true;
			this.notify();
		}
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(DelegateToSingleOperationStore.class);
	
	private XydraSingleOperationStore singleOpStore;
	
	public DelegateToSingleOperationStore(XydraSingleOperationStore singleOpStore) {
		this.singleOpStore = singleOpStore;
	}
	
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.checkLogin(actorId, passwordHash, callback);
	}
	
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callbackOrNull) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		@SuppressWarnings("unchecked")
		SingleOpCallback<Long>[] singleOpCallback = new SingleOpCallback[commands.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callbackOrNull)) {
				return;
			}
			// call n individual asynchronous single operations
			for(int i = 0; i < commands.length; i++) {
				singleOpCallback[i] = new SingleOpCallback<Long>();
				this.singleOpStore.executeCommand(actorId, null, commands[i], singleOpCallback[i]);
			}
			// wait at least once for all calls to complete
			boolean waiting = true;
			while(waiting) {
				// hopefully remains false
				waiting = false;
				int i = 0;
				while(i < singleOpCallback.length && !waiting) {
					if(!singleOpCallback[i].done) {
						waiting = true;
						try {
							singleOpCallback[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == singleOpCallback.length || waiting == true;
			}
			assert waiting == false;
			if(callbackOrNull != null) {
				// compose individual results
				@SuppressWarnings("unchecked")
				BatchedResult<Long>[] batchedResult = new BatchedResult[commands.length];
				for(int i = 0; i < commands.length; i++) {
					if(singleOpCallback[i].exception == null) {
						// success
						batchedResult[i] = new BatchedResult<Long>(singleOpCallback[i].result);
					} else {
						// failure
						batchedResult[i] = new BatchedResult<Long>(singleOpCallback[i].exception);
					}
				}
				callbackOrNull.onSuccess(batchedResult);
			}
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			if(callbackOrNull != null) {
				callbackOrNull.onFailure(e);
			} else {
				log.warn("Nobody cares for the exception", e);
			}
		}
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		@SuppressWarnings("unchecked")
		SingleOpCallback<Long>[] executeCommandCallbacks = new SingleOpCallback[commands.length];
		@SuppressWarnings("unchecked")
		SingleOpCallback<XEvent[]>[] getEventCallbacks = new SingleOpCallback[getEventRequests.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			// call n individual asynchronous single operations
			for(int i = 0; i < commands.length; i++) {
				executeCommandCallbacks[i] = new SingleOpCallback<Long>();
				this.singleOpStore.executeCommand(actorId, null, commands[i],
				        executeCommandCallbacks[i]);
			}
			for(int j = 0; j < getEventRequests.length; j++) {
				getEventCallbacks[j] = new SingleOpCallback<XEvent[]>();
				this.singleOpStore.getEvents(actorId, null, getEventRequests[j],
				        getEventCallbacks[j]);
			}
			// wait at least once for all calls to complete
			boolean waitingForCommands = true;
			while(waitingForCommands) {
				// hopefully remains false
				waitingForCommands = false;
				int i = 0;
				while(i < executeCommandCallbacks.length && !waitingForCommands) {
					if(!executeCommandCallbacks[i].done) {
						waitingForCommands = true;
						try {
							executeCommandCallbacks[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == executeCommandCallbacks.length || waitingForCommands == true;
			}
			assert waitingForCommands == false;
			boolean waitingForEvents = true;
			while(waitingForEvents) {
				// hopefully remains false
				waitingForEvents = false;
				int j = 0;
				while(j < getEventCallbacks.length && !waitingForEvents) {
					if(!getEventCallbacks[j].done) {
						waitingForEvents = true;
						try {
							getEventCallbacks[j].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					j++;
				}
				assert j == getEventCallbacks.length || waitingForEvents == true;
			}
			assert waitingForEvents == false;
			// compose individual results
			@SuppressWarnings("unchecked")
			BatchedResult<Long>[] commandResult = new BatchedResult[commands.length];
			for(int i = 0; i < commands.length; i++) {
				if(executeCommandCallbacks[i].exception == null) {
					// success
					commandResult[i] = new BatchedResult<Long>(executeCommandCallbacks[i].result);
				} else {
					// failure
					commandResult[i] = new BatchedResult<Long>(executeCommandCallbacks[i].exception);
				}
			}
			@SuppressWarnings("unchecked")
			BatchedResult<XEvent[]>[] eventResult = new BatchedResult[getEventRequests.length];
			for(int i = 0; i < getEventRequests.length; i++) {
				if(getEventCallbacks[i].exception == null) {
					// success
					eventResult[i] = new BatchedResult<XEvent[]>(getEventCallbacks[i].result);
				} else {
					// failure
					eventResult[i] = new BatchedResult<XEvent[]>(getEventCallbacks[i].exception);
				}
			}
			Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> pair = new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(
			        commandResult, eventResult);
			callback.onSuccess(pair);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest,
	        Callback<XEvent[]> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getEvents(actorId, passwordHash, getEventsRequest, callback);
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		@SuppressWarnings("unchecked")
		SingleOpCallback<XEvent[]>[] singleOpCallback = new SingleOpCallback[getEventsRequests.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			// call n individual asynchronous single operations
			for(int i = 0; i < getEventsRequests.length; i++) {
				singleOpCallback[i] = new SingleOpCallback<XEvent[]>();
				this.singleOpStore.getEvents(actorId, null, getEventsRequests[i],
				        singleOpCallback[i]);
			}
			// wait at least once for all calls to complete
			boolean waiting = true;
			while(waiting) {
				// hopefully remains false
				waiting = false;
				int i = 0;
				while(i < singleOpCallback.length && !waiting) {
					if(!singleOpCallback[i].done) {
						waiting = true;
						try {
							singleOpCallback[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == singleOpCallback.length || waiting == true;
			}
			assert waiting == false;
			// compose individual results
			@SuppressWarnings("unchecked")
			BatchedResult<XEvent[]>[] batchedResult = new BatchedResult[getEventsRequests.length];
			for(int i = 0; i < getEventsRequests.length; i++) {
				if(singleOpCallback[i].exception == null) {
					// success
					batchedResult[i] = new BatchedResult<XEvent[]>(singleOpCallback[i].result);
				} else {
					// failure
					batchedResult[i] = new BatchedResult<XEvent[]>(singleOpCallback[i].exception);
				}
			}
			callback.onSuccess(batchedResult);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getModelIds(actorId, passwordHash, callback);
	}
	
	public void getModelRevision(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<Long> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getModelRevision(actorId, passwordHash, modelAddress, callback);
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddresses);
		@SuppressWarnings("unchecked")
		SingleOpCallback<Long>[] singleOpCallback = new SingleOpCallback[modelAddresses.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddresses.length; i++) {
				singleOpCallback[i] = new SingleOpCallback<Long>();
				this.singleOpStore.getModelRevision(actorId, null, modelAddresses[i],
				        singleOpCallback[i]);
			}
			// wait at least once for all calls to complete
			boolean waiting = true;
			while(waiting) {
				// hopefully remains false
				waiting = false;
				int i = 0;
				while(i < singleOpCallback.length && !waiting) {
					if(!singleOpCallback[i].done) {
						waiting = true;
						try {
							singleOpCallback[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == singleOpCallback.length || waiting == true;
			}
			assert waiting == false;
			// compose individual results
			@SuppressWarnings("unchecked")
			BatchedResult<Long>[] batchedResult = new BatchedResult[modelAddresses.length];
			for(int i = 0; i < modelAddresses.length; i++) {
				if(singleOpCallback[i].exception == null) {
					// success
					batchedResult[i] = new BatchedResult<Long>(singleOpCallback[i].result);
				} else {
					// failure
					batchedResult[i] = new BatchedResult<Long>(singleOpCallback[i].exception);
				}
			}
			callback.onSuccess(batchedResult);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	public void getModelSnapshot(XID actorId, String passwordHash, XAddress modelAddress,
	        Callback<XBaseModel> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getModelSnapshot(actorId, passwordHash, modelAddress, callback);
	}
	
	@Override
	public synchronized void getModelSnapshots(XID actorId, String passwordHash,
	        XAddress[] modelAddresses, Callback<BatchedResult<XBaseModel>[]> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddresses);
		@SuppressWarnings("unchecked")
		SingleOpCallback<XBaseModel>[] singleOpCallback = new SingleOpCallback[modelAddresses.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddresses.length; i++) {
				singleOpCallback[i] = new SingleOpCallback<XBaseModel>();
				this.singleOpStore.getModelSnapshot(actorId, null, modelAddresses[i],
				        singleOpCallback[i]);
			}
			// wait at least once for all calls to complete
			boolean waiting = true;
			while(waiting) {
				// hopefully remains false
				waiting = false;
				int i = 0;
				while(i < singleOpCallback.length && !waiting) {
					if(!singleOpCallback[i].done) {
						waiting = true;
						try {
							singleOpCallback[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == singleOpCallback.length || waiting == true;
			}
			assert waiting == false;
			// compose individual results
			@SuppressWarnings("unchecked")
			BatchedResult<XBaseModel>[] batchedResult = new BatchedResult[modelAddresses.length];
			for(int i = 0; i < modelAddresses.length; i++) {
				if(singleOpCallback[i].exception == null) {
					// success
					batchedResult[i] = new BatchedResult<XBaseModel>(singleOpCallback[i].result);
				} else {
					// failure
					batchedResult[i] = new BatchedResult<XBaseModel>(singleOpCallback[i].exception);
				}
			}
			callback.onSuccess(batchedResult);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	public void getObjectSnapshot(XID actorId, String passwordHash, XAddress objectAddress,
	        Callback<XBaseObject> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getObjectSnapshot(actorId, passwordHash, objectAddress, callback);
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XBaseObject>[]> callback) throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(objectAddresses);
		
		@SuppressWarnings("unchecked")
		SingleOpCallback<XBaseObject>[] singleOpCallback = new SingleOpCallback[objectAddresses.length];
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			// call n individual asynchronous single operations
			for(int i = 0; i < objectAddresses.length; i++) {
				singleOpCallback[i] = new SingleOpCallback<XBaseObject>();
				this.singleOpStore.getObjectSnapshot(actorId, null, objectAddresses[i],
				        singleOpCallback[i]);
			}
			// wait at least once for all calls to complete
			boolean waiting = true;
			while(waiting) {
				// hopefully remains false
				waiting = false;
				int i = 0;
				while(i < singleOpCallback.length && !waiting) {
					if(!singleOpCallback[i].done) {
						waiting = true;
						try {
							singleOpCallback[i].wait();
						} catch(InterruptedException e) {
							log.debug("While waiting for single-op calls to complete", e);
						}
					}
					i++;
				}
				assert i == singleOpCallback.length || waiting == true;
			}
			assert waiting == false;
			// compose individual results
			@SuppressWarnings("unchecked")
			BatchedResult<XBaseObject>[] batchedResult = new BatchedResult[objectAddresses.length];
			for(int i = 0; i < objectAddresses.length; i++) {
				if(singleOpCallback[i].exception == null) {
					// success
					batchedResult[i] = new BatchedResult<XBaseObject>(singleOpCallback[i].result);
				} else {
					// failure
					batchedResult[i] = new BatchedResult<XBaseObject>(singleOpCallback[i].exception);
				}
			}
			callback.onSuccess(batchedResult);
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getRepositoryId(actorId, passwordHash, callback);
	}
	
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this.singleOpStore.getXydraStoreAdmin();
	}
	
	/**
	 * @param actorId
	 * @param passwordHash
	 * @param callback may be null if used in
	 *            {@link #executeCommands(XID, String, XCommand[], Callback)}
	 * @return
	 */
	private synchronized boolean validLogin(XID actorId, String passwordHash, Callback<?> callback) {
		SingleOpCallback<Boolean> loginCallback = new SingleOpCallback<Boolean>();
		this.singleOpStore.checkLogin(actorId, passwordHash, loginCallback);
		while(!loginCallback.done) {
			try {
				loginCallback.wait();
			} catch(InterruptedException e) {
				log.debug("Could not wait for checkLogin", e);
			}
		}
		if(loginCallback.exception != null) {
			if(callback != null) {
				callback.onFailure(loginCallback.exception);
			} else {
				log.warn("Nobody cares for the exception", loginCallback.exception);
			}
			return false;
		} else if(loginCallback.result == false) {
			if(callback != null) {
				callback.onFailure(new AuthorisationException("Could not authorise '" + actorId
				        + "'"));
			}
			return false;
		}
		assert loginCallback.done && loginCallback.result != null && loginCallback.result == true;
		return true;
	}
	
}

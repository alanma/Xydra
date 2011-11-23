package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.RequestException;
import org.xydra.store.ModelRevision;
import org.xydra.store.StoreException;
import org.xydra.store.WaitingCallback;
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
 * batched callback in the last called SingleOpCallback?
 * 
 * @author xamde
 */

public class DelegateToSingleOperationStore implements XydraStore {
	
	private static class SingleOpCallback<T> extends WaitingCallback<T> {
		
		MultiOpCallback<T> multi;
		int index;
		
		public SingleOpCallback(MultiOpCallback<T> multi, int index) {
			this.multi = multi;
			this.index = index;
		}
		
		@Override
		public synchronized void onFailure(Throwable exception) {
			super.onFailure(exception);
			this.multi.addResult(this.index, new BatchedResult<T>(exception));
		}
		
		@Override
		public synchronized void onSuccess(T result) {
			super.onSuccess(result);
			this.multi.addResult(this.index, new BatchedResult<T>(result));
		}
		
	}
	
	private static class MultiOpCallback<T> {
		
		private final BatchedResult<T>[] batchedResult;
		private int resultsRemaining;
		private final Callback<BatchedResult<T>[]> cb;
		
		@SuppressWarnings("unchecked")
		public MultiOpCallback(int nCommands, Callback<BatchedResult<T>[]> cb) {
			this.batchedResult = new BatchedResult[nCommands];
			this.resultsRemaining = nCommands;
			this.cb = cb;
			if(this.resultsRemaining == 0) {
				if(this.cb != null) {
					this.cb.onSuccess(this.batchedResult);
				}
			}
		}
		
		synchronized protected void addResult(int index, BatchedResult<T> result) {
			assert this.batchedResult[index] == null;
			this.batchedResult[index] = result;
			this.resultsRemaining--;
			if(this.resultsRemaining == 0) {
				if(this.cb != null) {
					this.cb.onSuccess(this.batchedResult);
				}
				notifyAll();
			}
		}
		
		synchronized public BatchedResult<T>[] getResult() {
			while(this.resultsRemaining > 0) {
				try {
					wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			return this.batchedResult;
		}
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(DelegateToSingleOperationStore.class);
	
	private XydraSingleOperationStore singleOpStore;
	
	public DelegateToSingleOperationStore(XydraSingleOperationStore singleOpStore) {
		this.singleOpStore = singleOpStore;
	}
	
	@Override
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.checkLogin(actorId, passwordHash, callback);
	}
	
	private static class DefaultCallback<T> implements Callback<BatchedResult<T>[]> {
		
		@Override
		public void onFailure(Throwable exception) {
			log.warn("Nobody cares for the exception", exception);
		}
		
		@Override
		public void onSuccess(BatchedResult<T>[] object) {
			for(BatchedResult<?> res : object) {
				if(res.getException() != null) {
					log.warn("Nobody cares for the exception", res.getException());
				}
			}
		}
		
	}
	
	private static final Callback<BatchedResult<Long>[]> defaultCommandsCallback = new DefaultCallback<Long>();
	
	@Override
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callbackOrNull) throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		
		Callback<BatchedResult<Long>[]> callback = callbackOrNull;
		if(callback == null) {
			callback = defaultCommandsCallback;
		}
		
		try {
			
			// authorise only once
			if(!validLogin(actorId, passwordHash, callbackOrNull)) {
				return;
			}
			
			executeCommands(actorId, commands, callback);
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
		}
	}
	
	private MultiOpCallback<Long> executeCommands(XID actorId, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		
		MultiOpCallback<Long> multi = new MultiOpCallback<Long>(commands.length, callback);
		
		@SuppressWarnings("unchecked")
		SingleOpCallback<Long>[] soc = new SingleOpCallback[commands.length];
		
		// call n individual asynchronous single operations
		for(int i = 0; i < commands.length; i++) {
			soc[i] = new SingleOpCallback<Long>(multi, i);
			try {
				XCommand command = fixCommand(soc, commands[i], i);
				if(command == null) {
					soc[i].onSuccess(XCommand.FAILED);
				} else {
					this.singleOpStore.executeCommand(actorId, null, command, soc[i]);
				}
			} catch(StoreException e) {
				log.warn("Telling callback: ", e);
				soc[i].onFailure(e);
			}
		}
		
		return multi;
	}
	
	private XCommand fixCommand(SingleOpCallback<Long>[] soc, XCommand command, int idx) {
		
		if(command == null) {
			throw new RequestException("command was null");
		}
		
		if(command instanceof XAtomicCommand) {
			return fixAtomicCommand(soc, idx, (XAtomicCommand)command);
		}
		
		assert command instanceof XTransaction;
		XTransaction trans = (XTransaction)command;
		
		boolean isRelative = false;
		for(XAtomicCommand ac : trans) {
			if(!ac.isForced() && ac.getRevisionNumber() >= XCommand.RELATIVE_REV) {
				isRelative = true;
				break;
			}
		}
		
		if(!isRelative) {
			return trans;
		}
		
		XAtomicCommand[] fixedCommands = new XAtomicCommand[trans.size()];
		for(int i = 0; i < trans.size(); i++) {
			fixedCommands[i] = fixAtomicCommand(soc, idx, trans.getCommand(i));
			if(fixedCommands[i] == null) {
				return null;
			}
		}
		
		return MemoryTransaction.createTransaction(trans.getTarget(), fixedCommands);
	}
	
	private XAtomicCommand fixAtomicCommand(SingleOpCallback<Long>[] soc, int i, XAtomicCommand ac) {
		
		if(ac.isForced() || ac.getRevisionNumber() < XCommand.RELATIVE_REV) {
			// not relative
			return ac;
		}
		
		assert ac instanceof XFieldCommand || ac.getChangeType() != ChangeType.ADD : " add entity commands don't have real / relative revisions";
		
		int index = (int)(ac.getRevisionNumber() - XCommand.RELATIVE_REV);
		
		if(index >= i) {
			throw new RequestException("invalid relative revision in command vith index " + i
			        + ": " + index + ", command was " + ac);
		}
		
		// wait for the result of the command we depend on
		assert soc[index] != null;
		long rev = soc[index].getResult();
		
		if(ac instanceof XRepositoryCommand) {
			assert ac.getChangeType() == ChangeType.REMOVE;
			return MemoryRepositoryCommand.createRemoveCommand(ac.getTarget(), rev,
			        ((XRepositoryCommand)ac).getModelId());
		} else if(ac instanceof XModelCommand) {
			assert ac.getChangeType() == ChangeType.REMOVE;
			return MemoryModelCommand.createRemoveCommand(ac.getTarget(), rev,
			        ((XModelCommand)ac).getObjectId());
		} else if(ac instanceof XObjectCommand) {
			assert ac.getChangeType() == ChangeType.REMOVE;
			return MemoryObjectCommand.createRemoveCommand(ac.getTarget(), rev,
			        ((XObjectCommand)ac).getFieldId());
		} else if(ac instanceof XFieldCommand) {
			switch(ac.getChangeType()) {
			case ADD:
				return MemoryFieldCommand.createAddCommand(ac.getTarget(), rev,
				        ((XFieldCommand)ac).getValue());
			case CHANGE:
				return MemoryFieldCommand.createChangeCommand(ac.getTarget(), rev,
				        ((XFieldCommand)ac).getValue());
			case REMOVE:
				return MemoryFieldCommand.createRemoveCommand(ac.getTarget(), rev);
			default:
				throw new AssertionError("unexpected command: " + ac);
			}
		} else {
			throw new AssertionError("unexpected command: " + ac);
		}
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			MultiOpCallback<Long> res = executeCommands(actorId, commands, null);
			BatchedResult<Long>[] revs = res.getResult();
			
			MultiOpCallback<XEvent[]> events = getEvents(actorId, getEventRequests, null);
			
			callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(revs,
			        events.getResult()));
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			getEvents(actorId, getEventsRequests, callback);
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	private MultiOpCallback<XEvent[]> getEvents(XID actorId, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		
		MultiOpCallback<XEvent[]> multi = new MultiOpCallback<XEvent[]>(getEventsRequests.length,
		        callback);
		
		// call n individual asynchronous single operations
		for(int i = 0; i < getEventsRequests.length; i++) {
			SingleOpCallback<XEvent[]> soc = new SingleOpCallback<XEvent[]>(multi, i);
			try {
				this.singleOpStore.getEvents(actorId, null, getEventsRequests[i], soc);
			} catch(StoreException e) {
				log.warn("Telling callback: ", e);
				soc.onFailure(e);
			}
		}
		
		// original callback is called automatically once all individual
		// callbacks have been called
		
		return multi;
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getModelIds(actorId, passwordHash, callback);
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<ModelRevision>[]> callback) throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddresses);
		
		try {
			
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			MultiOpCallback<ModelRevision> multi = new MultiOpCallback<ModelRevision>(
			        modelAddresses.length, callback);
			
			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddresses.length; i++) {
				SingleOpCallback<ModelRevision> soc = new SingleOpCallback<ModelRevision>(multi, i);
				try {
					this.singleOpStore.getModelRevision(actorId, null, modelAddresses[i], soc);
				} catch(StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}
			
			// original callback is called automatically once all individual
			// callbacks have been called
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public synchronized void getModelSnapshots(XID actorId, String passwordHash,
	        XAddress[] modelAddresses, Callback<BatchedResult<XReadableModel>[]> callback)
	        throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddresses);
		
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			MultiOpCallback<XReadableModel> multi = new MultiOpCallback<XReadableModel>(
			        modelAddresses.length, callback);
			
			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddresses.length; i++) {
				SingleOpCallback<XReadableModel> soc = new SingleOpCallback<XReadableModel>(multi,
				        i);
				try {
					this.singleOpStore.getModelSnapshot(actorId, null, modelAddresses[i], soc);
				} catch(StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}
			
			// original callback is called automatically once all individual
			// callbacks have been called
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {
		
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(objectAddresses);
		
		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}
			
			MultiOpCallback<XReadableObject> multi = new MultiOpCallback<XReadableObject>(
			        objectAddresses.length, callback);
			
			// call n individual asynchronous single operations
			for(int i = 0; i < objectAddresses.length; i++) {
				SingleOpCallback<XReadableObject> soc = new SingleOpCallback<XReadableObject>(
				        multi, i);
				try {
					this.singleOpStore.getObjectSnapshot(actorId, null, objectAddresses[i], soc);
				} catch(StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}
			
			// original callback is called automatically once all individual
			// callbacks have been called
			
		} catch(StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getRepositoryId(actorId, passwordHash, callback);
	}
	
	@Override
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
		
		WaitingCallback<Boolean> loginCallback = new WaitingCallback<Boolean>();
		this.singleOpStore.checkLogin(actorId, passwordHash, loginCallback);
		
		Throwable exception = loginCallback.getException();
		
		if(exception != null) {
			if(callback != null) {
				callback.onFailure(exception);
			} else {
				log.warn("Nobody cares for the exception", exception);
			}
			return false;
		} else if(loginCallback.getResult() == false) {
			if(callback != null) {
				callback.onFailure(new AuthorisationException("Could not authorise '" + actorId
				        + "'"));
			}
			return false;
		}
		assert loginCallback.getResult() != null && loginCallback.getResult() == Boolean.TRUE;
		return true;
	}
	
}

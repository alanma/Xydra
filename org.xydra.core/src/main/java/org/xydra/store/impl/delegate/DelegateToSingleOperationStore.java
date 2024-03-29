package org.xydra.store.impl.delegate;

import java.util.Set;

import org.xydra.base.XId;
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
import org.xydra.core.StoreException;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.RequestException;
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

		public SingleOpCallback(final MultiOpCallback<T> multi, final int index) {
			this.multi = multi;
			this.index = index;
		}

		@Override
		public synchronized void onFailure(final Throwable exception) {
			super.onFailure(exception);
			this.multi.addResult(this.index, new BatchedResult<T>(exception));
		}

		@Override
		public synchronized void onSuccess(final T result) {
			super.onSuccess(result);
			this.multi.addResult(this.index, new BatchedResult<T>(result));
		}

	}

	private static class MultiOpCallback<T> {

		private final BatchedResult<T>[] batchedResult;
		private int resultsRemaining;
		private final Callback<BatchedResult<T>[]> cb;

		@SuppressWarnings("unchecked")
		public MultiOpCallback(final int nCommands, final Callback<BatchedResult<T>[]> cb) {
			this.batchedResult = new BatchedResult[nCommands];
			this.resultsRemaining = nCommands;
			this.cb = cb;
			if(this.resultsRemaining == 0) {
				if(this.cb != null) {
					this.cb.onSuccess(this.batchedResult);
				}
			}
		}

		synchronized protected void addResult(final int index, final BatchedResult<T> result) {
			XyAssert.xyAssert(this.batchedResult[index] == null);
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
				} catch(final InterruptedException e) {
					e.printStackTrace();
				}
			}
			return this.batchedResult;
		}

	}

	private static final Logger log = LoggerFactory.getLogger(DelegateToSingleOperationStore.class);

	private final XydraSingleOperationStore singleOpStore;

	public DelegateToSingleOperationStore(final XydraSingleOperationStore singleOpStore) {
		this.singleOpStore = singleOpStore;
	}

	@Override
	public void checkLogin(final XId actorId, final String passwordHash, final Callback<Boolean> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.checkLogin(actorId, passwordHash, callback);
	}

	private static class DefaultCallback<T> implements Callback<BatchedResult<T>[]> {

		@Override
		public void onFailure(final Throwable exception) {
			log.warn("Nobody cares for the exception", exception);
		}

		@Override
		public void onSuccess(final BatchedResult<T>[] object) {
			for(final BatchedResult<?> res : object) {
				if(res.getException() != null) {
					log.warn("Nobody cares for the exception", res.getException());
				}
			}
		}

	}

	private static final Callback<BatchedResult<Long>[]> defaultCommandsCallback = new DefaultCallback<Long>();

	@Override
	public void executeCommands(final XId actorId, final String passwordHash, final XCommand[] commands,
	        final Callback<BatchedResult<Long>[]> callbackOrNull) throws IllegalArgumentException {

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

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
		}
	}

	private MultiOpCallback<Long> executeCommands(final XId actorId, final XCommand[] commands,
	        final Callback<BatchedResult<Long>[]> callback) {

		final MultiOpCallback<Long> multi = new MultiOpCallback<Long>(commands.length, callback);

		@SuppressWarnings("unchecked")
		final
		SingleOpCallback<Long>[] soc = new SingleOpCallback[commands.length];

		// call n individual asynchronous single operations
		for(int i = 0; i < commands.length; i++) {
			soc[i] = new SingleOpCallback<Long>(multi, i);
			try {
				final XCommand command = fixCommand(soc, commands[i], i);
				if(command == null) {
					soc[i].onSuccess(XCommand.FAILED);
				} else {
					this.singleOpStore.executeCommand(actorId, null, command, soc[i]);
				}
			} catch(final StoreException e) {
				log.warn("Telling callback: ", e);
				soc[i].onFailure(e);
			}
		}

		return multi;
	}

	private static XCommand fixCommand(final SingleOpCallback<Long>[] soc, final XCommand command, final int idx) {

		if(command == null) {
			throw new RequestException("command was null");
		}

		if(command instanceof XAtomicCommand) {
			return fixAtomicCommand(soc, idx, (XAtomicCommand)command);
		}

		XyAssert.xyAssert(command instanceof XTransaction);
		final XTransaction trans = (XTransaction)command;

		boolean isRelative = false;
		for(final XAtomicCommand ac : trans) {
			if(!ac.isForced() && ac.getRevisionNumber() >= XCommand.RELATIVE_REV) {
				isRelative = true;
				break;
			}
		}

		if(!isRelative) {
			return trans;
		}

		final XAtomicCommand[] fixedCommands = new XAtomicCommand[trans.size()];
		for(int i = 0; i < trans.size(); i++) {
			fixedCommands[i] = fixAtomicCommand(soc, idx, trans.getCommand(i));
			if(fixedCommands[i] == null) {
				return null;
			}
		}

		return MemoryTransaction.createTransaction(trans.getTarget(), fixedCommands);
	}

	private static XAtomicCommand fixAtomicCommand(final SingleOpCallback<Long>[] soc, final int i,
	        final XAtomicCommand ac) {

		if(ac.isForced() || ac.getRevisionNumber() < XCommand.RELATIVE_REV) {
			// not relative
			return ac;
		}

		assert ac instanceof XFieldCommand || ac.getChangeType() != ChangeType.ADD : " add entity commands don't have real / relative revisions";

		final int index = (int)(ac.getRevisionNumber() - XCommand.RELATIVE_REV);

		if(index >= i) {
			throw new RequestException("invalid relative revision in command vith index " + i
			        + ": " + index + ", command was " + ac);
		}

		// wait for the result of the command we depend on
		XyAssert.xyAssert(soc[index] != null); assert soc[index] != null;
		final long rev = soc[index].getResult();

		if(ac instanceof XRepositoryCommand) {
			XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
			return MemoryRepositoryCommand.createRemoveCommand(ac.getTarget(), rev,
			        ((XRepositoryCommand)ac).getModelId());
		} else if(ac instanceof XModelCommand) {
			XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
			return MemoryModelCommand.createRemoveCommand(ac.getTarget(), rev,
			        ((XModelCommand)ac).getObjectId());
		} else if(ac instanceof XObjectCommand) {
			XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
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
	public void executeCommandsAndGetEvents(final XId actorId, final String passwordHash, final XCommand[] commands,
	        final GetEventsRequest[] getEventRequests,
	        final Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException {

		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);

		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}

			final MultiOpCallback<Long> res = executeCommands(actorId, commands, null);
			final BatchedResult<Long>[] revs = res.getResult();

			final MultiOpCallback<XEvent[]> events = getEvents(actorId, getEventRequests, null);

			callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(revs,
			        events.getResult()));

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getEvents(final XId actorId, final String passwordHash, final GetEventsRequest[] getEventsRequests,
	        final Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException {

		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);

		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}

			getEvents(actorId, getEventsRequests, callback);

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	private MultiOpCallback<XEvent[]> getEvents(final XId actorId, final GetEventsRequest[] getEventsRequests,
	        final Callback<BatchedResult<XEvent[]>[]> callback) {

		final MultiOpCallback<XEvent[]> multi = new MultiOpCallback<XEvent[]>(getEventsRequests.length,
		        callback);

		// call n individual asynchronous single operations
		for(int i = 0; i < getEventsRequests.length; i++) {
			final SingleOpCallback<XEvent[]> soc = new SingleOpCallback<XEvent[]>(multi, i);
			try {
				this.singleOpStore.getEvents(actorId, null, getEventsRequests[i], soc);
			} catch(final StoreException e) {
				log.warn("Telling callback: ", e);
				soc.onFailure(e);
			}
		}

		// original callback is called automatically once all individual
		// callbacks have been called

		return multi;
	}

	@Override
	public void getModelIds(final XId actorId, final String passwordHash, final Callback<Set<XId>> callback)
	        throws IllegalArgumentException {
		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		this.singleOpStore.getModelIds(actorId, passwordHash, callback);
	}

	@Override
	public void getModelRevisions(final XId actorId, final String passwordHash,
	        final GetWithAddressRequest[] modelAddresses,
	        final Callback<BatchedResult<ModelRevision>[]> callback) throws IllegalArgumentException {

		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddresses);

		try {

			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}

			final MultiOpCallback<ModelRevision> multi = new MultiOpCallback<ModelRevision>(
			        modelAddresses.length, callback);

			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddresses.length; i++) {
				final SingleOpCallback<ModelRevision> soc = new SingleOpCallback<ModelRevision>(multi, i);
				try {
					this.singleOpStore.getModelRevision(actorId, null, modelAddresses[i], soc);
				} catch(final StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}

			// original callback is called automatically once all individual
			// callbacks have been called

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public synchronized void getModelSnapshots(final XId actorId, final String passwordHash,
	        final GetWithAddressRequest[] modelAddressRequests,
	        final Callback<BatchedResult<XReadableModel>[]> callback) throws IllegalArgumentException {

		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(modelAddressRequests);

		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}

			final MultiOpCallback<XReadableModel> multi = new MultiOpCallback<XReadableModel>(
			        modelAddressRequests.length, callback);

			// call n individual asynchronous single operations
			for(int i = 0; i < modelAddressRequests.length; i++) {
				final SingleOpCallback<XReadableModel> soc = new SingleOpCallback<XReadableModel>(multi,
				        i);
				try {
					this.singleOpStore
					        .getModelSnapshot(actorId, null, modelAddressRequests[i], soc);
				} catch(final StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}

			// original callback is called automatically once all individual
			// callbacks have been called

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getObjectSnapshots(final XId actorId, final String passwordHash, final GetWithAddressRequest[] objectAddressRequests,
	        final Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {

		DelegationUtils.assertNonNullActorAndPassword(actorId, passwordHash);
		DelegationUtils.assertNonNullCallback(callback);
		DelegationUtils.assertNonNull(objectAddressRequests);

		try {
			// authorise only once
			if(!validLogin(actorId, passwordHash, callback)) {
				return;
			}

			final MultiOpCallback<XReadableObject> multi = new MultiOpCallback<XReadableObject>(
			        objectAddressRequests.length, callback);

			// call n individual asynchronous single operations
			for(int i = 0; i < objectAddressRequests.length; i++) {
				final SingleOpCallback<XReadableObject> soc = new SingleOpCallback<XReadableObject>(
				        multi, i);
				try {
					this.singleOpStore.getObjectSnapshot(actorId, null, objectAddressRequests[i], soc);
				} catch(final StoreException e) {
					log.warn("Telling callback: ", e);
					soc.onFailure(e);
				}
			}

			// original callback is called automatically once all individual
			// callbacks have been called

		} catch(final StoreException e) {
			log.warn("Telling callback: ", e);
			callback.onFailure(e);
		}
	}

	@Override
	public void getRepositoryId(final XId actorId, final String passwordHash, final Callback<XId> callback)
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
	 *            {@link #executeCommands(XId, String, XCommand[], Callback)}
	 * @return
	 */
	private synchronized boolean validLogin(final XId actorId, final String passwordHash, final Callback<?> callback) {

		final WaitingCallback<Boolean> loginCallback = new WaitingCallback<Boolean>();
		this.singleOpStore.checkLogin(actorId, passwordHash, loginCallback);

		final Throwable exception = loginCallback.getException();

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
		XyAssert.xyAssert(loginCallback.getResult() != null && loginCallback.getResult() == Boolean.TRUE);
		return true;
	}

}

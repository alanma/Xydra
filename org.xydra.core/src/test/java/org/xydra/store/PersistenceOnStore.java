package org.xydra.store;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.XCopyUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Makes it easier to use a remote {@link XydraStore} by using it like a
 * {@link XydraPersistence}. Makes only sense for tests.
 * 
 * @author xamde
 */
public class PersistenceOnStore implements XydraPersistence {
	
	/** Default wait time for callback */
	private static final int WAIT_TIMEOUT = 2000;
	
	public PersistenceOnStore(XId actorId, String passwordHash, XydraStore store) {
		super();
		this.actorId = actorId;
		this.passwordHash = passwordHash;
		this.store = store;
	}
	
	private XydraStore store;
	
	private XId actorId;
	
	private String passwordHash;
	
	private XId repositoryId;
	
	@Override
	public void clear() {
		for(XId modelId : getManagedModelIds()) {
			deleteModel(modelId);
		}
	}
	
	private void deleteModel(XId modelId) {
		executeCommand(this.actorId,
		        X.getCommandFactory()
		                .createRemoveModelCommand(getRepositoryId(), modelId, -1, true));
	}
	
	@Override
	public long executeCommand(XId actorId, XCommand command) {
		SynchronousCallbackWithOneResult<BatchedResult<Long>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<Long>[]>();
		this.store
		        .executeCommands(actorId, this.passwordHash, new XCommand[] { command }, callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
			XyAssert.xyAssert(callback.getEffect().length == 1);
			e = callback.getEffect()[0].getException();
			if(e != null) {
				throw new StoreException("Callback effect[0] returned an error", e);
			}
		}
		return callback.getEffect()[0].getResult();
	}
	
	@Override
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XEvent[]>[]>();
		GetEventsRequest ger = new GetEventsRequest(address, beginRevision, endRevision);
		this.store.getEvents(this.actorId, this.passwordHash, new GetEventsRequest[] { ger },
		        callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
			XyAssert.xyAssert(callback.getEffect().length == 1);
			e = callback.getEffect()[0].getException();
			if(e != null) {
				throw new StoreException("Callback effect[0] returned an error", e);
			}
		}
		
		return Arrays.asList(callback.getEffect()[0].getResult());
	}
	
	@Override
	public Set<XId> getManagedModelIds() {
		SynchronousCallbackWithOneResult<Set<XId>> callback = new SynchronousCallbackWithOneResult<Set<XId>>();
		this.store.getModelIds(this.actorId, this.passwordHash, callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
		}
		return callback.getEffect();
	}
	
	@Override
	public ModelRevision getModelRevision(GetWithAddressRequest addressRequest) {
		SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<ModelRevision>[]>();
		this.store.getModelRevisions(this.actorId, this.passwordHash,
		        new GetWithAddressRequest[] { addressRequest }, callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
			XyAssert.xyAssert(callback.getEffect().length == 1);
			e = callback.getEffect()[0].getException();
			if(e != null) {
				throw new StoreException("Callback effect[0] returned an error", e);
			}
		}
		return callback.getEffect()[0].getResult();
	}
	
	@Override
	public XWritableModel getModelSnapshot(GetWithAddressRequest addressRequest) {
		SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableModel>[]>();
		this.store.getModelSnapshots(this.actorId, this.passwordHash,
		        new GetWithAddressRequest[] { addressRequest }, callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
			XyAssert.xyAssert(callback.getEffect().length == 1);
			e = callback.getEffect()[0].getException();
			if(e != null) {
				throw new StoreException("Callback effect[0] returned an error", e);
			}
		}
		XReadableModel readableModel = callback.getEffect()[0].getResult();
		return XCopyUtils.createSnapshot(readableModel);
	}
	
	@Override
	public XWritableObject getObjectSnapshot(GetWithAddressRequest addressRequest) {
		SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]> callback = new SynchronousCallbackWithOneResult<BatchedResult<XReadableObject>[]>();
		this.store.getObjectSnapshots(this.actorId, this.passwordHash,
		        new GetWithAddressRequest[] { addressRequest }, callback);
		callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
		{
			Throwable e = callback.getException();
			if(e != null) {
				throw new StoreException("Callback returned an error", e);
			}
			XyAssert.xyAssert(callback.getEffect().length == 1);
			e = callback.getEffect()[0].getException();
			if(e != null) {
				throw new StoreException("Callback effect[0] returned an error", e);
			}
		}
		XReadableObject readableObject = callback.getEffect()[0].getResult();
		return XCopyUtils.createSnapshot(readableObject);
	}
	
	@Override
	public XId getRepositoryId() {
		if(this.repositoryId == null) {
			SynchronousCallbackWithOneResult<XId> callback = new SynchronousCallbackWithOneResult<XId>();
			this.store.getRepositoryId(this.actorId, this.passwordHash, callback);
			callback.waitOnCallbackAndThrowExceptionForProblems(WAIT_TIMEOUT);
			{
				Throwable e = callback.getException();
				if(e != null) {
					throw new StoreException("Callback returned an error", e);
				}
			}
			this.repositoryId = callback.getEffect();
		}
		return this.repositoryId;
	}
	
	@Override
	public boolean hasManagedModel(XId modelId) {
		return getModelRevision(new GetWithAddressRequest(modeladdress(modelId), false)) != null;
	}
	
	private XAddress modeladdress(XId modelId) {
		return XX.toAddress(getRepositoryId(), modelId, null, null);
	}
	
}

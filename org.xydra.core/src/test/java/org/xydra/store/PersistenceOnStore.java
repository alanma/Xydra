package org.xydra.store;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.XCopyUtils;
import org.xydra.store.impl.delegate.XydraPersistence;


public class PersistenceOnStore implements XydraPersistence {
	
	public PersistenceOnStore(XID actorId, String passwordHash, XydraStore store) {
		super();
		this.actorId = actorId;
		this.passwordHash = passwordHash;
		this.store = store;
	}
	
	private XydraStore store;
	
	private XID actorId;
	
	private String passwordHash;
	
	private XID repositoryId;
	
	@Override
	public void clear() {
		for(XID modelId : getModelIds()) {
			deleteModel(modelId);
		}
	}
	
	private void deleteModel(XID modelId) {
		executeCommand(this.actorId,
		        X.getCommandFactory()
		                .createRemoveModelCommand(getRepositoryId(), modelId, -1, true));
	}
	
	@Override
	public long executeCommand(XID actorId, XCommand command) {
		SynchronousTestCallback<BatchedResult<Long>[]> callback = new SynchronousTestCallback<BatchedResult<Long>[]>();
		this.store
		        .executeCommands(actorId, this.passwordHash, new XCommand[] { command }, callback);
		Throwable e = callback.getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		assert callback.getEffect().length == 1;
		e = callback.getEffect()[0].getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		return callback.getEffect()[0].getResult();
	}
	
	@Override
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		SynchronousTestCallback<BatchedResult<XEvent[]>[]> callback = new SynchronousTestCallback<BatchedResult<XEvent[]>[]>();
		GetEventsRequest ger = new GetEventsRequest(address, beginRevision, endRevision);
		this.store.getEvents(this.actorId, this.passwordHash, new GetEventsRequest[] { ger },
		        callback);
		Throwable e = callback.getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		assert callback.getEffect().length == 1;
		e = callback.getEffect()[0].getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		return Arrays.asList(callback.getEffect()[0].getResult());
	}
	
	@Override
	public Set<XID> getModelIds() {
		SynchronousTestCallback<Set<XID>> callback = new SynchronousTestCallback<Set<XID>>();
		this.store.getModelIds(this.actorId, this.passwordHash, callback);
		if(callback.getException() != null) {
			throw new RuntimeException(callback.getException());
		}
		return callback.getEffect();
	}
	
	@Override
	public RevisionState getModelRevision(XAddress address) {
		SynchronousTestCallback<BatchedResult<RevisionState>[]> callback = new SynchronousTestCallback<BatchedResult<RevisionState>[]>();
		this.store.getModelRevisions(this.actorId, this.passwordHash, new XAddress[] { address },
		        callback);
		Throwable e = callback.getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		assert callback.getEffect().length == 1;
		e = callback.getEffect()[0].getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		return callback.getEffect()[0].getResult();
	}
	
	@Override
	public XWritableModel getModelSnapshot(XAddress address) {
		SynchronousTestCallback<BatchedResult<XReadableModel>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableModel>[]>();
		this.store.getModelSnapshots(this.actorId, this.passwordHash, new XAddress[] { address },
		        callback);
		Throwable e = callback.getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		assert callback.getEffect().length == 1;
		e = callback.getEffect()[0].getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		XReadableModel readableModel = callback.getEffect()[0].getResult();
		return XCopyUtils.createSnapshot(readableModel);
	}
	
	@Override
	public XWritableObject getObjectSnapshot(XAddress address) {
		SynchronousTestCallback<BatchedResult<XReadableObject>[]> callback = new SynchronousTestCallback<BatchedResult<XReadableObject>[]>();
		this.store.getObjectSnapshots(this.actorId, this.passwordHash, new XAddress[] { address },
		        callback);
		Throwable e = callback.getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		assert callback.getEffect().length == 1;
		e = callback.getEffect()[0].getException();
		if(e != null) {
			throw new RuntimeException(e);
		}
		XReadableObject readableObject = callback.getEffect()[0].getResult();
		return XCopyUtils.createSnapshot(readableObject);
	}
	
	@Override
	public XID getRepositoryId() {
		if(this.repositoryId == null) {
			SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
			this.store.getRepositoryId(this.actorId, this.passwordHash, callback);
			if(callback.getException() != null) {
				throw new RuntimeException(callback.getException());
			}
			this.repositoryId = callback.getEffect();
			
		}
		return this.repositoryId;
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		return getModelRevision(modeladdress(modelId)).modelExists();
	}
	
	private XAddress modeladdress(XID modelId) {
		return XX.toAddress(getRepositoryId(), modelId, null, null);
	}
	
}

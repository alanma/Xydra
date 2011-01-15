package org.xydra.store.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.store.RequestException;
import org.xydra.store.impl.delegating.XydraBlockingPersistence;


/**
 * Stores state in memory. Each model is managed as a
 * {@link MemoryModelPersistence}.
 * 
 * @author voelkel
 * @author dscharrer
 */
public class MemoryBlockingPersistence implements XydraBlockingPersistence {
	
	private XID repoId;
	
	private Map<XID,MemoryModelPersistence> models = new HashMap<XID,MemoryModelPersistence>();
	
	public MemoryBlockingPersistence(XID repositoryId) {
		this.repoId = repositoryId;
	}
	
	private MemoryModelPersistence getModelService(XID modelId) {
		if(modelId == null) {
			throw new IllegalArgumentException("modelId must not be null");
		}
		synchronized(this.models) {
			MemoryModelPersistence ms = this.models.get(modelId);
			if(ms == null) {
				XAddress modelAddr = XX.toAddress(this.repoId, modelId, null, null);
				ms = new MemoryModelPersistence(modelAddr);
				this.models.put(modelId, ms);
			}
			return ms;
		}
	}
	
	private void checkRepoId(XAddress address) {
		if(!this.repoId.equals(address.getRepository())) {
			throw new IllegalArgumentException("wrong repository ID: was " + address
			        + " but expected " + this.repoId);
		}
	}
	
	@Override
	public long executeCommand(XID actorId, XCommand command) {
		XAddress address = command.getChangedEntity();
		checkRepoId(address);
		return getModelService(address.getModel()).executeCommand(actorId, command);
	}
	
	@Override
	public XEvent[] getEvents(XAddress address, long beginRevision, long endRevision) {
		checkRepoId(address);
		return getModelService(address.getModel()).getEvents(address, beginRevision, endRevision);
	}
	
	@Override
	public Set<XID> getModelIds() {
		Set<XID> modelIds = new HashSet<XID>();
		synchronized(this.models) {
			modelIds.addAll(this.models.keySet());
		}
		// TODO filter to exclude models that don't actually exist right now?
		return modelIds;
	}
	
	@Override
	public long getModelRevision(XAddress address) {
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model revison, was "
			        + address);
		}
		checkRepoId(address);
		return getModelService(address.getModel()).getRevisionNumber();
	}
	
	@Override
	public XBaseModel getModelSnapshot(XAddress address) {
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model snapshot, was "
			        + address);
		}
		checkRepoId(address);
		return getModelService(address.getModel()).getModelSnapshot();
	}
	
	@Override
	public XBaseObject getObjectSnapshot(XAddress address) {
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("must use an object address to get an object snapshot, was "
			        + address);
		}
		checkRepoId(address);
		return getModelService(address.getModel()).getObjectSnapshot(address.getObject());
	}
	
	@Override
	public XID getRepositoryId() {
		return this.repoId;
	}
	
	@Override
	public void clear() {
		this.models.clear();
	}
	
}

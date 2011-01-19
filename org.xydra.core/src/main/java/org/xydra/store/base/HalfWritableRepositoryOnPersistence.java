package org.xydra.store.base;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.base.XID;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.impl.delegate.XydraPersistence;


public class HalfWritableRepositoryOnPersistence extends AbstractHalfWritableOnPersistence
        implements XHalfWritableRepository {
	
	public HalfWritableRepositoryOnPersistence(XydraPersistence persistence, XID executingActorId) {
		super(persistence, executingActorId);
	}
	
	public XHalfWritableModel createModel(XID modelId) {
		XCommand command = X.getCommandFactory().createAddModelCommand(
		        this.persistence.getRepositoryId(), modelId, false);
		this.persistence.executeCommand(this.executingActorId, command);
		return getModel(modelId);
	}
	
	@Override
	public XAddress getAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        null, null, null);
		}
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.persistence.getRepositoryId();
	}
	
	public XHalfWritableModel getModel(XID modelId) {
		if(hasModel(modelId)) {
			// make sure changes to model are reflected in persistence
			return new HalfWritableModelOnPersistence(this.persistence, this.executingActorId,
			        modelId);
		} else {
			return null;
		}
	}
	
	public boolean hasModel(XID modelId) {
		return this.persistence.getModelIds().contains(modelId);
	}
	
	public boolean isEmpty() {
		return this.persistence.getModelIds().isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.persistence.getModelIds().iterator();
	}
	
	public boolean removeModel(XID modelId) {
		boolean result = hasModel(modelId);
		XCommand command = X.getCommandFactory().createRemoveModelCommand(
		        this.persistence.getRepositoryId(), modelId, XCommand.FORCED, false);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result;
	}
	
}

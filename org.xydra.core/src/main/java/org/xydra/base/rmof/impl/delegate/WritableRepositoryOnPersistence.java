package org.xydra.base.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.store.impl.delegate.XydraPersistence;


public class WritableRepositoryOnPersistence extends AbstractWritableOnPersistence implements
        XWritableRepository {
	
	public WritableRepositoryOnPersistence(XydraPersistence persistence, XID executingActorId) {
		super(persistence, executingActorId);
	}
	
	public XWritableModel createModel(XID modelId) {
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
	
	public XWritableModel getModel(XID modelId) {
		if(hasModel(modelId)) {
			// make sure changes to model are reflected in persistence
			return new WritableModelOnPersistence(this.persistence, this.executingActorId, modelId);
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

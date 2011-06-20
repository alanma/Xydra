package org.xydra.store.rmof.impl.delegate;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


public class WritableRepositoryOnPersistence extends AbstractWritableOnPersistence implements
        XWritableRepository {
	
	private static final Logger log = LoggerFactory
	        .getLogger(WritableRepositoryOnPersistence.class);
	
	public WritableRepositoryOnPersistence(XydraPersistence persistence, XID executingActorId) {
		super(persistence, executingActorId);
	}
	
	public XWritableModel createModel(XID modelId) {
		XWritableModel model = getModel(modelId);
		if(model == null) {
			XCommand command = X.getCommandFactory().createAddModelCommand(
			        this.persistence.getRepositoryId(), modelId, true);
			long l = this.persistence.executeCommand(this.executingActorId, command);
			if(l < 0) {
				log.warn("creating model '" + modelId + "' failed with " + l);
			}
			model = getModel(modelId);
		}
		return model;
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
		return this.persistence.hasModel(modelId);
	}
	
	public boolean isEmpty() {
		return this.persistence.getModelIds().isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.persistence.getModelIds().iterator();
	}
	
	public boolean removeModel(XID modelId) {
		boolean result = hasModel(modelId);
		// long modelRevision =
		// this.persistence.getModelRevision(XX.resolveModel(getAddress(),
		// modelId));
		XCommand command = X.getCommandFactory().createRemoveModelCommand(
		        this.persistence.getRepositoryId(), modelId, XCommand.FORCED, true);
		long commandResult = this.persistence.executeCommand(this.executingActorId, command);
		assert commandResult >= 0;
		return result;
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
}

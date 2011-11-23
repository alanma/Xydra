package org.xydra.store.rmof.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * An implementation of {@link XWritableRepository} that uses no internal
 * caching.
 * 
 * @author xamde
 */
public class WritableRepositoryOnPersistence extends AbstractWritableOnPersistence implements
        XWritableRepository {
	
	private static final Logger log = LoggerFactory
	        .getLogger(WritableRepositoryOnPersistence.class);
	
	public WritableRepositoryOnPersistence(XydraPersistence persistence, XID executingActorId) {
		super(persistence, executingActorId);
	}
	
	/** First try to get model, if not found: create it. */
	@Override
	public XWritableModel createModel(XID modelId) {
		XWritableModel model = getModel(modelId);
		if(model == null) {
			assert !this.persistence.getModelRevision(getModelAddress(modelId)).modelExists();
			
			XCommand command = X.getCommandFactory().createAddModelCommand(
			        this.persistence.getRepositoryId(), modelId, true);
			long l = this.persistence.executeCommand(this.executingActorId, command);
			if(l < 0) {
				log.warn("creating model '" + modelId + "' failed with " + l);
			}
			assert this.persistence.getModelRevision(getModelAddress(modelId)).modelExists() : "model should exist "
			        + this.persistence.getModelRevision(getModelAddress(modelId));
			
			model = getModel(modelId);
			assert model != null;
		}
		return model;
	}
	
	private XAddress getModelAddress(XID modelId) {
		return X.getIDProvider().fromComponents(this.persistence.getRepositoryId(), modelId, null,
		        null);
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
	
	@Override
	public XWritableModel getModel(XID modelId) {
		if(hasModel(modelId)) {
			// make sure changes to model are reflected in persistence
			return new WritableModelOnPersistence(this.persistence, this.executingActorId, modelId);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		assert this.persistence != null;
		return this.persistence.hasManagedModel(modelId)
		        && this.persistence.getModelRevision(
		                XX.resolveModel(this.persistence.getRepositoryId(), modelId)).modelExists();
	}
	
	@Override
	public boolean isEmpty() {
		return this.persistence.getManagedModelIds().isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		Set<XID> existingModelIds = new HashSet<XID>();
		for(XID modelId : this.persistence.getManagedModelIds()) {
			if(this.persistence.getModelRevision(
			        XX.resolveModel(this.persistence.getRepositoryId(), modelId)).modelExists()) {
				existingModelIds.add(modelId);
			}
		}
		return existingModelIds.iterator();
	}
	
	@Override
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

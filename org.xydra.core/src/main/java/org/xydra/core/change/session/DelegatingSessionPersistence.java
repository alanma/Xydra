package org.xydra.core.change.session;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * State-less
 * 
 * @author xamde
 * 
 */
public class DelegatingSessionPersistence implements ISessionPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(DelegatingSessionPersistence.class);
	
	private XydraPersistence persistence;
	private XID actorId;
	
	public DelegatingSessionPersistence(XydraPersistence persistence, XID actorId) {
		this.persistence = persistence;
		this.actorId = actorId;
	}
	
	// all commands are forced
	@Override
	public long applyChangesAsTxn(SessionCachedModel sessionCacheModel, XID actorId)
	        throws SessionException {
		assert actorId != null;
		log.debug("applyChangesAsTxn");
		
		if(!sessionCacheModel.hasChanges()) {
			log.debug("Model has no changes.");
			return XCommand.NOCHANGE;
		}
		
		// TODO move model creation into txn
		ModelRevision modelRev = this.persistence.getModelRevision(sessionCacheModel.getAddress());
		if(!modelRev.modelExists()) {
			long l = this.persistence.executeCommand(this.actorId, X.getCommandFactory()
			        .createForcedAddModelCommand(getRepositoryId(), sessionCacheModel.getID()));
			if(XCommandUtils.failed(l)) {
				throw new SessionException("Could not create model '"
				        + sessionCacheModel.getAddress() + "'. Got: " + l);
			}
		}
		
		// TODO 2012-01 Sync: add "safe" vs. "forced" param to underlying txns
		XTransactionBuilder builder = new XTransactionBuilder(sessionCacheModel.getAddress());
		sessionCacheModel.commitTo(builder);
		XTransaction txn = builder.build();
		long result = this.persistence.executeCommand(actorId, txn);
		// TODO if success, make sure to reflect new revNrs
		return result;
	}
	
	@Override
	public long createModel(XID modelId) {
		XRepositoryCommand createModelCommand = X.getCommandFactory().createForcedAddModelCommand(
		        getRepositoryId(), modelId);
		return this.persistence.executeCommand(this.actorId, createModelCommand);
	}
	
	@Override
	public long removeModel(XID modelId) {
		XRepositoryCommand createModelCommand = X.getCommandFactory()
		        .createForcedRemoveModelCommand(getRepositoryId(), modelId);
		return this.persistence.executeCommand(this.actorId, createModelCommand);
	}
	
	@Override
	public XID getRepositoryId() {
		return this.persistence.getRepositoryId();
	}
	
	@Override
	public XReadableModel getModelSnapshot(XID modelId) {
		assert modelId != null;
		XAddress modelAddress = XX.resolveModel(getRepositoryId(), modelId);
		XWritableModel baseModel = this.persistence.getModelSnapshot(modelAddress);
		if(baseModel == null) {
			return null;
		}
		return baseModel;
	}
	
	@Override
	public XReadableObject getObjectSnapshot(XAddress objectAddress) {
		assert objectAddress != null;
		XWritableObject baseObject = this.persistence.getObjectSnapshot(objectAddress);
		if(baseObject == null) {
			return null;
		}
		return baseObject;
	}
	
	@Override
	public void deleteAllData() {
		this.persistence.clear();
	}
	
}

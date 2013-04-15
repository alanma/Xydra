package org.xydra.store.session;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.X;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;


/**
 * State-less
 * 
 * @author xamde
 * 
 */
public class DelegatingSessionPersistence implements ISessionPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(DelegatingSessionPersistence.class);
	
	private static final boolean INCLUDE_TENTATIVE = true;
	
	private XydraPersistence persistence;
	private XId actorId;
	
	public DelegatingSessionPersistence(XydraPersistence persistence, XId actorId) {
		this.persistence = persistence;
		this.actorId = actorId;
	}
	
	// all commands are forced
	@Override
	public long applyChangesAsTxn(SessionCachedModel sessionCacheModel, XId actorId)
	        throws SessionException {
		log.debug("applyChangesAsTxn");
		XyAssert.xyAssert(actorId != null);
		assert actorId != null;
		
		ModelRevision modelRev = this.persistence.getModelRevision(new GetWithAddressRequest(
		        sessionCacheModel.getAddress(), INCLUDE_TENTATIVE));
		boolean modelExists = modelRev.modelExists();
		
		if(!sessionCacheModel.hasChanges() && modelExists) {
			log.debug("Model has no changes.");
			return XCommand.NOCHANGE;
		}
		

		long result = XCommand.FAILED;
		
		// TODO move model creation into txn
		if(!modelExists) {
			result = this.persistence.executeCommand(this.actorId, X.getCommandFactory()
			        .createForcedAddModelCommand(getRepositoryId(), sessionCacheModel.getId()));
			if(XCommandUtils.failed(result)) {
				throw new SessionException("Could not create model '"
				        + sessionCacheModel.getAddress() + "'. Got: " + result);
			}
		}
		
		if(sessionCacheModel.hasChanges()) {
			// all commands are FORCED
			XTransactionBuilder builder = new XTransactionBuilder(sessionCacheModel.getAddress(),
		// TODO 2012-01 Sync: add "safe" vs. "forced" param to underlying txns
			        // FIXME Why always forced?
			        true);
			sessionCacheModel.commitTo(builder);
			XTransaction txn = builder.build();
			result = this.persistence.executeCommand(actorId, txn);
			// TODO if success, make sure to reflect new revNrs
		}
		return result;
	}
	
	@Override
	public long createModel(XId modelId) {
		XRepositoryCommand createModelCommand = X.getCommandFactory().createForcedAddModelCommand(
		        getRepositoryId(), modelId);
		return this.persistence.executeCommand(this.actorId, createModelCommand);
	}
	
	@Override
	public long removeModel(XId modelId) {
		XRepositoryCommand createModelCommand = X.getCommandFactory()
		        .createForcedRemoveModelCommand(getRepositoryId(), modelId);
		return this.persistence.executeCommand(this.actorId, createModelCommand);
	}
	
	@Override
	public XId getRepositoryId() {
		return this.persistence.getRepositoryId();
	}
	
	@Override
	public XReadableModel getModelSnapshot(GetWithAddressRequest modelRequest) {
		XyAssert.xyAssert(modelRequest != null);
		assert modelRequest != null;
		XWritableModel baseModel = this.persistence.getModelSnapshot(modelRequest);
		if(baseModel == null) {
			return null;
		}
		return baseModel;
	}
	
	@Override
	public XReadableObject getObjectSnapshot(GetWithAddressRequest objectAddressRequest) {
		XyAssert.xyAssert(objectAddressRequest != null);
		assert objectAddressRequest != null;
		XWritableObject baseObject = this.persistence.getObjectSnapshot(objectAddressRequest);
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

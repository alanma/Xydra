package org.xydra.server.backend;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


public class GaeRepositoryService {
	
	private final XID repoId;
	
	public GaeRepositoryService(XID repoId) {
		if(repoId == null) {
			throw new NullPointerException("repoId may not be null");
		}
		this.repoId = repoId;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		
		if(!this.repoId.equals(command.getTarget().getRepository())) {
			return XCommand.FAILED;
		}
		
		XID modelId = command.getChangedEntity().getModel();
		
		return getModelService(modelId).executeCommand(command, actorId);
	}
	
	private XAddress getModelAddress(XID modelId) {
		return XX.toAddress(this.repoId, modelId, null, null);
	}
	
	private GaeModelService getModelService(XID modelId) {
		// IMPROVE cache GaeModelService instances
		return new GaeModelService(getModelAddress(modelId));
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		return getModelService(modelId);
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		return getModelService(modelId).getSnapshot();
	}
	
}

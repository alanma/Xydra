package org.xydra.server.backend;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


public class GaeRepositoryService implements XRepositoryService {
	
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
		
		XAddress modelAddr = getModelAddress(command.getChangedEntity().getModel());
		
		// IMPROVE cache GaeModelService instances
		return new GaeModelService(modelAddr).executeCommand(command, actorId);
	}
	
	private XAddress getModelAddress(XID modelId) {
		return XX.toAddress(this.repoId, modelId, null, null);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		// IMPROVE cache GaeModelService instances
		return new GaeModelService(getModelAddress(modelId));
	}
	
	public XBaseModel getModelSnapshot(XID modelId, long revision) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

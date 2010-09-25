package org.xydra.server.backend;

import org.xydra.core.change.XCommand;
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
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public XBaseModel getModelSnapshot(XID modelId, long revision) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

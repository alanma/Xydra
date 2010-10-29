package org.xydra.server.impl.memory;

import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.session.impl.arm.ArmProtectedBaseModel;
import org.xydra.core.model.session.impl.arm.ArmProtectedChangeLog;
import org.xydra.server.IXydraServer;
import org.xydra.server.IXydraSession;


/**
 * {@link IXydraSession} implementation that checks all access against an
 * {@link XAccessManager} and forwards allowed operations to an
 * {@link IXydraServer}.
 * 
 * @author dscharrer
 * 
 */
public class ArmXydraSession implements IXydraSession {
	
	private final IXydraServer server;
	private final XID actor;
	
	public ArmXydraSession(IXydraServer server, XID actorId) {
		this.server = server;
		this.actor = actorId;
	}
	
	public long executeCommand(XCommand command) {
		
		if(!this.server.getAccessManager().canExecute(this.actor, command)) {
			throw new XAccessException("cannot execute command: " + command);
		}
		
		return this.server.executeCommand(command, this.actor);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		XAccessManager arm = this.server.getAccessManager();
		
		if(!arm.canKnowAboutModel(this.actor, getRepositoryAddress(), modelId)) {
			throw new XAccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getRepositoryAddress());
		}
		
		XChangeLog log = this.server.getChangeLog(modelId);
		if(log == null) {
			return null;
		}
		
		return new ArmProtectedChangeLog(log, arm, this.actor);
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		
		XAccessManager arm = this.server.getAccessManager();
		
		if(!arm.canKnowAboutModel(this.actor, getRepositoryAddress(), modelId)) {
			throw new XAccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getRepositoryAddress());
		}
		
		XBaseModel model = this.server.getModelSnapshot(modelId);
		if(model == null) {
			return null;
		}
		
		return new ArmProtectedBaseModel(model, arm, this.actor);
	}
	
	public XAddress getRepositoryAddress() {
		return this.server.getRepositoryAddress();
	}
	
}

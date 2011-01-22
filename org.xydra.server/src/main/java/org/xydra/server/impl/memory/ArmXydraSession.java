package org.xydra.server.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.session.impl.arm.ArmProtectedBaseModel;
import org.xydra.core.model.session.impl.arm.ArmProtectedChangeLog;
import org.xydra.server.IXydraServer;
import org.xydra.server.IXydraSession;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * {@link IXydraSession} implementation that checks all access against an
 * {@link XAuthorisationManager} and forwards allowed operations to an
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
			throw new AccessException("cannot execute command: " + command);
		}
		
		return this.server.executeCommand(command, this.actor);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		XAuthorisationManager arm = this.server.getAccessManager();
		
		if(!arm.canKnowAboutModel(this.actor, getRepositoryAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getRepositoryAddress());
		}
		
		XChangeLog log = this.server.getChangeLog(modelId);
		if(log == null) {
			return null;
		}
		
		return new ArmProtectedChangeLog(log, arm, this.actor);
	}
	
	public XReadableModel getModelSnapshot(XID modelId) {
		
		XAuthorisationManager arm = this.server.getAccessManager();
		
		if(!arm.canKnowAboutModel(this.actor, getRepositoryAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getRepositoryAddress());
		}
		
		XReadableModel model = this.server.getModelSnapshot(modelId);
		if(model == null) {
			return null;
		}
		
		return new ArmProtectedBaseModel(model, arm, this.actor);
	}
	
	public XAddress getRepositoryAddress() {
		return this.server.getRepositoryAddress();
	}
	
}

package org.xydra.server.impl.memory;

import java.util.Iterator;

import org.xydra.core.X;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.MemoryStateStore;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


public class MemoryXydraServer implements IXydraServer {
	
	private XRepository repo;
	
	private XGroupDatabase groups;
	
	private XAccessManager accessManager;
	
	/**
	 * Parameter-less constructor. Called form {@link XydraRestServer}.
	 */
	public MemoryXydraServer() {
		// Set the repository, group DB and access manager
		XSPI.setStateStore(new MemoryStateStore());
		this.repo = X.createMemoryRepository();
		this.groups = new MemoryGroupDatabase();
		this.accessManager = new MemoryAccessManager(this.groups);
	}
	
	public XAccessManager getAccessManager() {
		return this.accessManager;
	}
	
	public XGroupDatabase getGroups() {
		return this.groups;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		return this.repo.executeCommand(actorId, command);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			return null;
		}
		
		return model.getChangeLog();
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		return this.repo.getModel(modelId);
	}
	
	public XAddress getRepositoryAddress() {
		return this.repo.getAddress();
	}
	
	public Iterator<XID> iterator() {
		return this.repo.iterator();
	}
	
}

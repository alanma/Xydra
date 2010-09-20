package org.xydra.server.impl.gae;

import org.xydra.core.X;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.server.IXydraServer;


/**
 * Setup the Xydra server to use an in-memory repository.
 * 
 * @author dscharrer
 * 
 */
public class GaeXydraServer implements IXydraServer {
	
	private XGroupDatabase groups;
	private XRepository repo;
	private XAccessManager accessManager;
	
	public GaeXydraServer() {
		
		// Set the repository, group DB and access manager
		XSPI.setStateStore(new GaeStateStore());
		this.repo = X.createMemoryRepository();
		this.groups = GaeGroups.loadGroups();
		this.accessManager = GaeAccess.loadAccessManager(this.repo.getAddress(), this.groups);
		
	}
	
	public XAccessManager getAccessManagerForModel(XAddress modelAddr, XGroupDatabase groups) {
		return GaeAccess.loadAccessManager(modelAddr, groups);
	}
	
	public XAccessManager getAccessManager() {
		return this.accessManager;
	}
	
	public XGroupDatabase getGroups() {
		return this.groups;
	}
	
	public XRepository getRepository() {
		return this.repo;
	}
	
}

package org.xydra.server.impl.memory;

import org.xydra.core.X;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.model.XRepository;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


public class MemoryXydraServer implements IXydraServer {
	
	private XRepository repository;
	
	private XGroupDatabase groups;
	
	private XAccessManager accessManager;
	
	/**
	 * Parameter-less constructor. Called form {@link XydraRestServer}.
	 */
	public MemoryXydraServer() {
		// Set the repository, group DB and access manager
		this.repository = X.createMemoryRepository();
		this.groups = new MemoryGroupDatabase();
		this.accessManager = new MemoryAccessManager(this.groups);
	}
	
	public XAccessManager getAccessManager() {
		return this.accessManager;
	}
	
	public XRepository getRepository() {
		return this.repository;
	}
	
	public XGroupDatabase getGroups() {
		return this.groups;
	}
	
}

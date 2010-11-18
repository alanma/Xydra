package org.xydra.server.impl.memory;

import java.util.Iterator;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabaseWithListeners;
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
import org.xydra.log.gae.GaeLoggerFactorySPI;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


/**
 * An {@link IXydraServer} backend that uses an in-memory {@link XRepository}
 * and does not persist changes.
 * 
 * @author dscharrer
 * 
 */
public class MemoryXydraServer implements IXydraServer {
	
	private XRepository repo;
	
	private XGroupDatabaseWithListeners groups;
	
	private XAccessManager accessManager;
	
	/**
	 * Parameter-less constructor. Called form {@link XydraRestServer}.
	 * 
	 * @param actorId TODO
	 */
	public MemoryXydraServer() {
		// setup logging
		// FIXME why does the in-memory server need GAE logging? ~Daniel
		GaeLoggerFactorySPI.init();
		// Set the repository, group DB and access manager
		XSPI.setStateStore(new MemoryStateStore());
		// TODO use an id from config
		XID actorId = XX.toId("MemoryXydraServer");
		this.repo = X.createMemoryRepository(actorId);
		this.groups = new MemoryGroupDatabase();
		this.accessManager = new MemoryAccessManager(this.groups);
	}
	
	public XAccessManager getAccessManager() {
		return this.accessManager;
	}
	
	public XGroupDatabaseWithListeners getGroups() {
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
		// FIXME this is not a real snapshot
		return this.repo.getModel(modelId);
	}
	
	public XAddress getRepositoryAddress() {
		return this.repo.getAddress();
	}
	
	public Iterator<XID> iterator() {
		return this.repo.iterator();
	}
	
}

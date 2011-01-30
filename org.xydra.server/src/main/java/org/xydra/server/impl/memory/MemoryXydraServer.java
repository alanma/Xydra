package org.xydra.server.impl.memory;

import java.util.Iterator;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.log.gae.GaeLoggerFactorySPI;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


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
	
	private XAuthorisationManager accessManager;
	
	/**
	 * Parameter-less constructor. Called form {@link XydraRestServer}.
	 */
	public MemoryXydraServer() {
		// setup logging
		// FIXME why does the in-memory server need GAE logging? ~Daniel
		GaeLoggerFactorySPI.init();
		// TODO use an id from config
		XID actorId = XX.toId("MemoryXydraServer");
		this.repo = X.createMemoryRepository(actorId);
		this.groups = new MemoryGroupDatabase();
		this.accessManager = new MemoryAuthorisationManager(this.groups);
	}
	
	public XAuthorisationManager getAccessManager() {
		return this.accessManager;
	}
	
	public XGroupDatabaseWithListeners getGroups() {
		return this.groups;
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		return this.repo.executeCommand(command);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		XModel model = this.repo.getModel(modelId);
		if(model == null) {
			return null;
		}
		
		return model.getChangeLog();
	}
	
	public XReadableModel getModelSnapshot(XID modelId) {
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

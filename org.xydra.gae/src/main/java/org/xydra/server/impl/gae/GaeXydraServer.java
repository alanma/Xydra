package org.xydra.server.impl.gae;

import java.util.Iterator;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.access.XGroupDatabaseWithListeners;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.server.IXydraServer;
import org.xydra.server.impl.InfrastructureServiceFactory;
import org.xydra.store.impl.gae.GaeInfrastructureProvider;
import org.xydra.store.impl.gae.GaeTestfixer;


/**
 * Setup the Xydra server to use an in-memory repository.
 * 
 * @author dscharrer
 * 
 */
public class GaeXydraServer implements IXydraServer {
	
	private XGroupDatabaseWithListeners groups;
	private XRepository repo;
	private XAccessManagerWithListeners accessManager;
	
	public GaeXydraServer() {
		/* switch on test fix mode if run from local POM */
		String gaetestfix = System.getProperty("gaetestfix");
		if(gaetestfix != null && gaetestfix.equalsIgnoreCase("true")) {
			GaeTestfixer.enable();
		}
		
		// To enable local JUnit testing with multiple threads
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Register AppEngine infrastructure services
		InfrastructureServiceFactory.setProvider(new GaeInfrastructureProvider());
		
		// Set the repository, group DB and access manager
		XSPI.setStateStore(new GaeStateStore());
		this.repo = X.createMemoryRepository(XX.toId(GaeXydraServer.class.getName()));
		this.groups = GaeGroups.loadGroups();
		this.accessManager = GaeAccess.loadAccessManager(this.repo.getAddress(), this.groups);
		
	}
	
	public XAccessManagerWithListeners getAccessManagerForModel(XAddress modelAddr,
	        XGroupDatabaseWithListeners groups) {
		return GaeAccess.loadAccessManager(modelAddr, groups);
	}
	
	public XAccessManagerWithListeners getAccessManager() {
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

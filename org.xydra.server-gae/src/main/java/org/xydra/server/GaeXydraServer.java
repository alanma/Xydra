package org.xydra.server;

import org.xydra.core.X;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.server.RepositoryManager.ArmLoader;


/**
 * Setup the Xydra server to use an in-memory repository.
 * 
 * @author dscharrer
 * 
 */
public class GaeXydraServer {
	
	static {
		
		// Initialize the repository manager.
		initializeRepositoryManager();
		
		// Initialize the REST API.
		XydraServer.restless("");
		
		// TODO are these needed?
		new GSetupResource().restless("");
		new LogTestResource().restless("");
		
	}
	
	public synchronized static void initializeRepositoryManager() {
		
		if(!RepositoryManager.isInitialized()) {
			
			// Set the repository, group DB and access manager
			XSPI.setStateStore(new GaeStateStore());
			XRepository repo = X.createMemoryRepository();
			RepositoryManager.setRepository(repo);
			XGroupDatabase groups = GaeGroups.loadGroups();
			RepositoryManager.setGroups(groups);
			XAccessManager arm = GaeAccess.loadAccessManager(repo.getAddress(), groups);
			RepositoryManager.setAccessManager(arm, new GaeArmLoader());
			
		}
	}
	
	private static class GaeArmLoader implements ArmLoader {
		
		public XAccessManager loadArmForModel(XAddress modelAddr, XGroupDatabase groups) {
			return GaeAccess.loadAccessManager(modelAddr, groups);
		}
		
	}
	
}

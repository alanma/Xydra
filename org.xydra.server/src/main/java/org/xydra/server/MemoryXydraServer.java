package org.xydra.server;

import org.xydra.core.X;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;


/**
 * Setup the Xydra server to use an in-memory repository.
 * 
 * @author dscharrer
 * 
 */
public class MemoryXydraServer {
	
	static {
		
		// Initialize the repository manager.
		initializeRepositoryManager();
		
		// Initialize the REST API.
		XydraServer.restless("");
		
	}
	
	public synchronized static void initializeRepositoryManager() {
		
		if(!RepositoryManager.isInitialized()) {
			
			// Set the repository, group DB and access manager
			RepositoryManager.setRepository(X.createMemoryRepository());
			XGroupDatabase groups = new MemoryGroupDatabase();
			RepositoryManager.setGroups(groups);
			XAccessManager arm = new MemoryAccessManager(groups);
			RepositoryManager.setAccessManager(arm, null);
			
		}
	}
	
}

package org.xydra.server;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


/**
 * A session interface to {@link IXydraServer}.
 * 
 * @author dscharrer
 * 
 */
public interface IXydraSession {
	
	XAddress getRepositoryAddress();
	
	/**
	 * Return a read-only interface to the snapshot for the model this time.
	 * Individual parts of the model are loaded into memory as-needed.
	 */
	XBaseModel getModelSnapshot(XID modelId);
	
	/**
	 * Return an interface to read the change log entries for the given model.
	 */
	XChangeLog getChangeLog(XID modelId);
	
	/**
	 * Execute the given command.
	 * 
	 * TODO accept a "context" to allow logging time, IP Address, etc.
	 */
	long executeCommand(XCommand command);
	
}

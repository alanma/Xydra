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
	 * @return a read-only interface to the snapshot for the model this time.
	 *         Individual parts of the model are loaded into memory as-needed.
	 *         Returns null if no such model exists.
	 */
	XBaseModel getModelSnapshot(XID modelId);
	
	/**
	 * @return an interface to read the change log entries for the given model.
	 *         This may be null if the model doesn't exist.
	 */
	XChangeLog getChangeLog(XID modelId);
	
	/**
	 * Execute the given command.
	 */
	long executeCommand(XCommand command);
	
}

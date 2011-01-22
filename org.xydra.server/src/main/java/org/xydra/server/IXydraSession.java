package org.xydra.server;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.model.XChangeLog;


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
	 *         Returns null if no such model exists.
	 */
	XReadableModel getModelSnapshot(XID modelId);
	
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

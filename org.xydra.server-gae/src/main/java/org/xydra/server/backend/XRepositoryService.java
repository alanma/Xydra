package org.xydra.server.backend;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


public interface XRepositoryService {
	
	/**
	 * Return a read-only interface to the snapshot for the model at the
	 * specified revision. Individual parts of the model are loaded into memory
	 * as-needed.
	 */
	XBaseModel getModelSnapshot(XID modelId, long revision);
	
	/**
	 * Return an interface to read the change log entries for the given model.
	 */
	XChangeLog getChangeLog(XID modelId);
	
	/**
	 * Execute the given command and log the actorId. No
	 * authentication/authorization checks are done. TODO replace actorId with a
	 * more general context to allow logging time, IP Address, etc.
	 */
	long executeCommand(XCommand command, XID actorId);
	
}

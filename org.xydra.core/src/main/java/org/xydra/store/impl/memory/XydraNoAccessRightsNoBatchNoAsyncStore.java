package org.xydra.store.impl.memory;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.XydraStore;


/**
 * A variant of {@link XydraStore} without access rights parameters, single
 * operations instead of batch operations, and blocking (synchronous) operations
 * instead of asnychronous one.
 * 
 * @author voelkel
 */
public interface XydraNoAccessRightsNoBatchNoAsyncStore {
	
	/**
	 * Execute a command.
	 * 
	 * @param actorId
	 * @param xCommand
	 * @return
	 */
	long executeCommand(XID actorId, XCommand command);
	
	XEvent[] getEvents(XAddress address, long beginRevision, long endRevision);
	
	Set<XID> getModelIds();
	
	long getModelRevision(XAddress address);
	
	XBaseModel getModelSnapshot(XAddress address);
	
	XBaseObject getObjectSnapshot(XAddress address);
	
	XID getRepositoryId();
	
}

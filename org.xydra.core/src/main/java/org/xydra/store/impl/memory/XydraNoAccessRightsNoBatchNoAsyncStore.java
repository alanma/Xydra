package org.xydra.store.impl.memory;

import java.util.Set;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;


public interface XydraNoAccessRightsNoBatchNoAsyncStore {
	
	long executeCommand(XID actorId, XCommand xCommand);
	
	XEvent[] getEvents(XAddress xAddress, long beginRevision, long endRevision);
	
	Set<XID> getModelIds();
	
	long getModelRevision(XAddress xAddress);
	
	XBaseModel getModelSnapshot(XAddress xAddress);
	
	XBaseObject getObjectSnapshot(XAddress xAddress);
	
	XID getRepositoryId();
	
}

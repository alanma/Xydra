package org.xydra.store.impl.gae.snapshot;

import org.xydra.base.XID;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;


public abstract class AbstractGaeSnapshotServiceImpl implements IGaeSnapshotService {
	
	@Override
	public XRevWritableField getFieldSnapshot(long modelRevisionNumber, boolean precise,
	        XID objectId, XID fieldId) {
		
		XRevWritableObject objectSnapshot = getObjectSnapshot(modelRevisionNumber, precise,
		        objectId);
		if(objectSnapshot == null) {
			return null;
		}
		
		return objectSnapshot.getField(fieldId);
	}
	
	@Override
	public XRevWritableObject getObjectSnapshot(long modelRevisionNumber, boolean precise,
	        XID objectId) {
		
		/*
		 * IMPROVE(performance, defer) generate the object snapshot directly.
		 * While reading the change events, one could skip reading large,
		 * unaffected XVALUes.
		 * 
		 * Idea 2: Put object snapshots individually in a cache -- maybe only
		 * large ones?
		 * 
		 * Ideas 3: Easy to implement: If localVMcache has it, copy directly
		 * just the object from it.
		 */
		XRevWritableModel modelSnapshot = getModelSnapshot(modelRevisionNumber, precise);
		if(modelSnapshot == null) {
			return null;
		}
		
		return modelSnapshot.getObject(objectId);
	}
	
}

package org.xydra.store.impl.gae.snapshot;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;


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
	
	public XRevWritableModel getPartialSnapshot(long snapshotRev, Iterable<XAddress> locks) {
		
		Iterator<XAddress> it = locks.iterator();
		if(!it.hasNext()) {
			return null;
		}
		if(it.next().equals(getModelAddress())) {
			assert !it.hasNext();
			return getModelSnapshot(snapshotRev, true);
		}
		
		SimpleModel model = new SimpleModel(getModelAddress());
		
		for(XAddress lock : locks) {
			
			switch(lock.getAddressedType()) {
			
			case XFIELD: {
				XRevWritableField field = getFieldSnapshot(snapshotRev, true, lock.getObject(),
				        lock.getField());
				XRevWritableObject object = model.getObject(lock.getObject());
				if(field != null) {
					if(object == null) {
						object = model.createObject(lock.getObject());
						object.setRevisionNumber(XEvent.RevisionNotAvailable);
					}
					if(object.hasField(lock.getField())) {
						continue;
					}
					object.addField(field);
					break;
				} else if(object != null) {
					break;
				} else {
					/*
					 * we don't know if the object exists but might need this
					 * information, so we need to get the object snapshot
					 * 
					 * IMPROVE check if we actually need this info
					 */
				}
			}
				
				//$FALL-THROUGH$
			case XOBJECT: {
				assert !model.hasObject(lock.getObject());
				XRevWritableObject object = getObjectSnapshot(snapshotRev, true, lock.getObject());
				if(object != null) {
					model.addObject(object);
				}
				break;
			}
				
			default:
				assert false : "invalid lock: " + locks;
				
			}
		}
		
		return model;
	}
	
}

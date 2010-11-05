/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.ConcurrentModificationException;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * object state.
 * 
 * @author dscharrer
 * 
 */
public class InternalGaeObject extends InternalGaeContainerXEntity<InternalGaeField> implements
        XBaseObject {
	
	long objectRev = XEvent.RevisionNotAvailable;
	
	private InternalGaeObject(GaeChangesService changesService, XAddress objectAddr,
	        long objectRev, Set<XAddress> locks) {
		super(changesService, objectAddr, objectRev, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
	}
	
	public XID getID() {
		return getAddress().getObject();
	}
	
	public InternalGaeField getField(XID fieldId) {
		return getChild(fieldId);
	}
	
	public boolean hasField(XID fieldId) {
		return hasChild(fieldId);
	}
	
	@Override
	protected InternalGaeField loadChild(XAddress childAddr, Entity childEntity) {
		return InternalGaeField.get(getChangesService(), childAddr, childEntity);
	}
	
	@Override
	protected XAddress resolveChild(XAddress addr, XID childId) {
		return XX.resolveField(addr, childId);
	}
	
	@Override
	protected XID getChildId(XAddress childAddr) {
		assert childAddr.getAddressedType() == XType.XFIELD;
		return childAddr.getField();
	}
	
	public static void createObject(XAddress objectAddr, Set<XAddress> locks, long rev) {
		assert GaeChangesService.canWrite(objectAddr, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		Entity e = new Entity(KeyStructure.createCombinedKey(objectAddr));
		e.setProperty(PROP_PARENT, objectAddr.getParent().toURI());
		e.setUnindexedProperty(PROP_REVISION, rev);
		GaeUtils.putEntity(e);
	}
	
	public static void updateObjectRev(XAddress objectAddr, Set<XAddress> locks, long rev) {
		assert GaeChangesService.canRead(objectAddr, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		Key key = KeyStructure.createCombinedKey(objectAddr);
		
		while(true) {
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity e = GaeUtils.getEntity(key, trans);
			assert e != null : "should not be removed while we hold a lock to a contained field";
			long oldRev = (Long)e.getProperty(PROP_REVISION);
			
			assert oldRev != rev;
			if(oldRev >= rev) {
				// object revision is already up to date
				return;
			}
			
			e.setUnindexedProperty(PROP_REVISION, rev);
			GaeUtils.putEntity(e, trans);
			
			try {
				GaeUtils.endTransaction(trans);
				
				// Update successful.
				return;
				
			} catch(ConcurrentModificationException cme) {
				
				// Conflicting update => try again.
				try {
					// Sleep a minimal amount of time.
					// TODO sleep longer to prevent busy loop?
					Thread.sleep(0);
				} catch(InterruptedException e1) {
					// ignore
				}
				
			}
			
		}
		
	}
	
	public static InternalGaeObject get(GaeChangesService changesService, XAddress objectAddr,
	        Entity objectEntity, Set<XAddress> locks) {
		
		long objectRev = XEvent.RevisionNotAvailable;
		if(GaeChangesService.canWrite(objectAddr, locks)) {
			objectRev = (Long)objectEntity.getProperty(PROP_REVISION);
		} else {
			// objectRev may not be up to date / too far ahead
			// FIXME we don't always have the locks to get the objectRev
			// this way => some events may be missing the objectRev
		}
		return new InternalGaeObject(changesService, objectAddr, objectRev, locks);
	}
}

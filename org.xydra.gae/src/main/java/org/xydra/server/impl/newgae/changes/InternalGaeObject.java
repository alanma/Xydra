/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.ConcurrentModificationException;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.sun.org.apache.xpath.internal.objects.XObject;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * object state.
 * 
 * @author dscharrer
 * 
 */
public class InternalGaeObject extends InternalGaeContainerXEntity<InternalGaeField> implements
        XBaseObject {
	
	private static final Logger log = LoggerFactory.getLogger(InternalGaeObject.class);
	
	long objectRev = XEvent.RevisionNotAvailable;
	
	/**
	 * Construct a read-only interface to an {@link XObject} in the GAE
	 * datastore.
	 * 
	 * {@link InternalGaeObject}s are not constructed directly by
	 * {@link GaeChangesService} but through
	 * {@link InternalGaeModel#getObject(XID)}.
	 * 
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we have enough locks when reading fields as well
	 *            as to determine if we can calculate the object revision or if
	 *            we have to return {@link XEvent#RevisionNotAvailable} instead.
	 */
	protected InternalGaeObject(GaeChangesService changesService, XAddress objectAddr,
	        Entity objectEntity, Set<XAddress> locks) {
		super(changesService, objectAddr, getSavedRevison(objectAddr, objectEntity, locks), locks);
		assert KeyStructure.toAddress(objectEntity.getKey()).equals(objectAddr);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
	}
	
	private static long getSavedRevison(XAddress objectAddr, Entity objectEntity,
	        Set<XAddress> locks) {
		long objectRev;
		if(GaeChangesService.canWrite(objectAddr, locks)) {
			// We need the whole object, including all fields to be in a
			// consistent state in order to calculate
			objectRev = (Long)objectEntity.getProperty(PROP_REVISION);
		} else {
			// The saved objectRev may not be up to date / too far ahead, so it
			// can't be used here.
			objectRev = XEvent.RevisionNotAvailable;
			
			// FIXME we don't always have the locks to get the objectRev
			// this way => some events may be missing the objectRev
		}
		return objectRev;
	}
	
	@Override
	public long getRevisionNumber() {
		
		long rev = super.getRevisionNumber();
		
		if(rev == XEvent.RevisionNotAvailable) {
			// We don't have enough locks to get the objectRev.
			return XEvent.RevisionNotAvailable;
		}
		
		// There may be fields with a newer revision.
		if(this.objectRev == XEvent.RevisionNotAvailable) {
			
			for(XID fieldId : this) {
				XBaseField field = getField(fieldId);
				assert field != null;
				long fieldRev = field.getRevisionNumber();
				assert fieldRev >= 0;
				if(fieldRev > rev) {
					rev = fieldRev;
				}
			}
			
			this.objectRev = rev;
		}
		
		return this.objectRev;
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
		return new InternalGaeField(getChangesService(), childAddr, childEntity);
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
	
	/**
	 * Create an {@link XObject} in the GAE datastore.
	 * 
	 * It is up to the caller to acquire enough locks: The whole {@link XObject}
	 * needs to be locked while adding it.
	 * 
	 * @param objectAddr The address of the object to add.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the
	 *            {@link XObject}.
	 * @param rev The revision number of the current change. This will be saved
	 *            to the object entity.
	 */
	public static void createObject(XAddress objectAddr, Set<XAddress> locks, long rev) {
		assert GaeChangesService.canWrite(objectAddr, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		Entity e = new Entity(KeyStructure.createCombinedKey(objectAddr));
		e.setProperty(PROP_PARENT, objectAddr.getParent().toURI());
		e.setUnindexedProperty(PROP_REVISION, rev);
		GaeUtils.putEntity(e);
	}
	
	/**
	 * Update the saved revision of an internal object. This is only needed when
	 * removing a contained field (and not when we also created this object in
	 * the same transaction or changed another field). In all other cases, the
	 * actual object revision can be calculated from the revision numbers of all
	 * fields.
	 * 
	 * This function ensures that two processes trying to update the revision of
	 * the same object are properly synchronized. It is however the
	 * responsibility of the caller to hold sufficient locks so that the object
	 * is not removed for the duration of the call. A lock to any contained
	 * field will suffice.
	 * 
	 * @param objectAddr The object who'se revision needs to be updated.
	 * @param locks The locks held by the current process, to assert that there
	 *            are enough, so that the object won't be removed.
	 * @param rev The new revision number of the object. The objects revision
	 *            number will not be lowered if it is already higher than this.
	 */
	public static void updateObjectRev(XAddress objectAddr, Set<XAddress> locks, long rev) {
		
		// We only care that that object won't be removed, so a read lock
		// suffices.
		assert GaeChangesService.canRead(objectAddr, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		Key key = KeyStructure.createCombinedKey(objectAddr);
		
		while(true) {
			Transaction trans = GaeUtils.beginTransaction();
			
			Entity e = GaeUtils.getEntity(key, trans);
			assert e != null : "should not be removed while we hold a lock to a contained field";
			long oldRev = (Long)e.getProperty(PROP_REVISION);
			
			// Check that no other process has set a higher object revision.
			assert oldRev != rev;
			if(oldRev >= rev) {
				
				// Cleanup the transaction.
				GaeUtils.endTransaction(trans);
				
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
				/*
				 * Another process touched the object entity during our
				 * transaction. This is normal behavior and we should just try
				 * again.
				 */
				log.debug("Encountered concurrend modification while"
				        + " trying to update the revision of object " + objectAddr, cme);
				
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
	
}

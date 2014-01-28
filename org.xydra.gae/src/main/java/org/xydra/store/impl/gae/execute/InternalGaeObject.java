/**
 * 
 */
package org.xydra.store.impl.gae.execute;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.IDatastoreSync;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.STransaction;



/**
 * Internal helper class used by {@link IGaeChangesService} to access the
 * current object state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeObject extends InternalGaeContainerXEntity<InternalGaeField> implements
        XReadableObject {
    
    private static final Logger log = LoggerFactory.getLogger(InternalGaeObject.class);
    
    private long objectRev = XEvent.REVISION_NOT_AVAILABLE;
    
    /**
     * Construct a read-only interface to an {@link XObject} in the GAE
     * datastore.
     * 
     * {@link InternalGaeObject}s are not constructed directly by
     * {@link IGaeChangesService} but through
     * {@link InternalGaeModel#getObject(XId)}.
     * 
     * @param locks The locks held by the current process. These are used to
     *            assert that we have enough locks when reading fields as well
     *            as to determine if we can calculate the object revision or if
     *            we have to return {@link XEvent#REVISION_NOT_AVAILABLE}
     *            instead.
     */
    protected InternalGaeObject(IGaeChangesService changesService, XAddress objectAddr,
            SEntity objectEntity, GaeLocks locks) {
        super(changesService, objectAddr, getSavedRevison(objectAddr, objectEntity, locks), locks);
        assert KeyStructure.toAddress(objectEntity.getKey()).equals(objectAddr);
        assert objectAddr.getAddressedType() == XType.XOBJECT;
    }
    
    private static long getSavedRevison(XAddress objectAddr, SEntity objectEntity, GaeLocks locks) {
        long objectRev;
        if(locks.canWrite(objectAddr)) {
            // We need the whole object, including all fields to be in a
            // consistent state in order to calculate
            objectRev = (Long)objectEntity.getAttribute(PROP_REVISION);
        } else {
            // The saved objectRev may not be up to date / too far ahead, so it
            // can't be used here.
            objectRev = XEvent.REVISION_NOT_AVAILABLE;
            
            /*
             * Note: we don't always have the locks to get the objectRev this
             * way => some events may not have an objectRev. However needs the
             * objectRev has the locks, so that it can be computed then.
             */
        }
        return objectRev;
    }
    
    @Override
    public long getRevisionNumber() {
        
        long rev = super.getRevisionNumber();
        
        if(rev == XEvent.REVISION_NOT_AVAILABLE) {
            // We don't have enough locks to get the objectRev.
            return XEvent.REVISION_NOT_AVAILABLE;
        }
        
        // There may be fields with a newer revision.
        if(this.objectRev == XEvent.REVISION_NOT_AVAILABLE) {
            
            for(XId fieldId : this) {
                XReadableField field = getField(fieldId);
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
    
    @Override
    public XId getId() {
        return getAddress().getObject();
    }
    
    @Override
    public InternalGaeField getField(XId fieldId) {
        return getChild(fieldId);
    }
    
    @Override
    public boolean hasField(XId fieldId) {
        return hasChild(fieldId);
    }
    
    @Override
    protected InternalGaeField loadChild(XAddress childAddr, SEntity childEntity) {
        return new InternalGaeField(getChangesService(), childAddr, childEntity);
    }
    
    @Override
    protected XAddress resolveChild(XAddress addr, XId childId) {
        return XX.resolveField(addr, childId);
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
    static Future<SKey> createObject(XAddress objectAddr, GaeLocks locks, long rev) {
        assert locks.canWrite(objectAddr);
        assert objectAddr.getAddressedType() == XType.XOBJECT;
        
        SKey key = KeyStructure.createEntityKey(objectAddr);
        SEntity e = XGae.get().datastore().createEntity(key);
        e.setAttribute(PROP_REVISION, rev);
        return XGae.get().datastore().async().putEntity(e);
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
    static void updateObjectRev(XAddress objectAddr, GaeLocks locks, long rev) {
        
        // We only care that that object won't be removed, so a read lock
        // suffices.
        assert locks.canRead(objectAddr);
        assert objectAddr.getAddressedType() == XType.XOBJECT;
        IDatastoreSync syncDb = XGae.get().datastore().sync();
        
        SKey key = KeyStructure.createEntityKey(objectAddr);
        
        while(true) {
            STransaction txn = syncDb.beginTransaction();
            SEntity e = syncDb.getEntity(key, txn);
            assert e != null : "should not be removed while we hold a lock to a contained field";
            long oldRev = (Long)e.getAttribute(PROP_REVISION);
            
            // Check that no other process has set a higher object revision.
            assert oldRev != rev;
            if(oldRev >= rev) {
                
                // Cleanup the transaction.
                syncDb.endTransaction(txn);
                
                // object revision is already up to date
                return;
            }
            
            e.setAttribute(PROP_REVISION, rev);
            XGae.get().datastore().async().putEntity(e, txn);
            // Synchronized by endTransaction()
            
            try {
                syncDb.endTransaction(txn);
                
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
                    // TODO @Daniel sleep longer to prevent busy loop?
                    Thread.sleep(0);
                } catch(InterruptedException e1) {
                    // ignore
                }
                
            }
            
        }
        
    }
    
    @Override
    public XType getType() {
        return XType.XOBJECT;
    }
    
}

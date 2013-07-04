package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.rmof.impl.ISyncableState;
import org.xydra.base.rmof.impl.XExistsReadableModel;


/**
 * An {@link XWritableModel} whose revision can be set and to which existing
 * {@link XRevWritableObject XRevWritableObjects} can be added.
 */
public interface XRevWritableModel extends XWritableModel, XRevisionWritable, XExistsReadableModel,
        ISyncableState {
    
    /**
     * Add an existing object to this field. Objects created using
     * {@link #createObject(XId)} are automatically added.
     * 
     * This overwrites any existing object in this model with the same
     * {@link XId}.
     * 
     * @param object
     */
    @ModificationOperation
    void addObject(@NeverNull XRevWritableObject object);
    
    /* More specific return type */
    @Override
    @ModificationOperation
    XRevWritableObject createObject(@NeverNull XId id);
    
    /* More specific return type */
    @Override
    @ReadOperation
    XRevWritableObject getObject(@NeverNull XId objectId);
    
}

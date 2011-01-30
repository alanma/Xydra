package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * An {@link XWritableModel} whose revision can be set and to which existing
 * {@link XRevWritableObject XRevWritableObjects} can be added.
 */
public interface XRevWritableModel extends XWritableModel {
	
	/**
	 * Add an existing object to this field. Objects created using
	 * {@link #createObject(XID)} are automatically added.
	 * 
	 * This overwrites any existing object in this model with the same
	 * {@link XID}.
	 */
	@ModificationOperation
	void addObject(XRevWritableObject object);
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XRevWritableObject createObject(XID id);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XRevWritableObject getObject(XID objectId);
	
	/**
	 * Set the revision number of this model. Revision number of contained
	 * objects and fields are not changed.
	 * 
	 * @param rev the new revision number
	 */
	@ModificationOperation
	void setRevisionNumber(long rev);
	
}

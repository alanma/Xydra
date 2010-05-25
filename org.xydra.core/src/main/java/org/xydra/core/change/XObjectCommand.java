package org.xydra.core.change;

import org.xydra.core.model.XID;


public interface XObjectCommand extends XAtomicCommand {
	
	/**
	 * @return the XID of the repository this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return the XID of the model containing the object this command refers to
	 *         (may be null)
	 */
	XID getModelID();
	
	/**
	 * @return the XID of the object this command refers to
	 */
	XID getObjectID();
	
	/**
	 * @return the XID of the field this command will add/remove
	 */
	XID getFieldID();
	
	/**
	 * @return the current revision number of the field this XCommand refers to
	 */
	long getRevisionNumber();
	
}

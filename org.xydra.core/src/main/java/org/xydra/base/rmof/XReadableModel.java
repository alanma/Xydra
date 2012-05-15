package org.xydra.base.rmof;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * A basic model that at least supports read operations.
 * 
 * Some implementations are also {@link Serializable}.
 * 
 * @author dscharrer
 * 
 */
public interface XReadableModel extends XStateReadableModel {
	
	/* More specific return type */
	@ReadOperation
	XReadableObject getObject(@NeverNull XID objectId);
	
	/**
	 * Returns the current revision number of this {@link XReadableModel}.
	 * 
	 * @return The current revision number of this {@link XReadableModel}.
	 * @throws IllegalStateException if this model has already been removed
	 */
	@ReadOperation
	long getRevisionNumber();
	
}

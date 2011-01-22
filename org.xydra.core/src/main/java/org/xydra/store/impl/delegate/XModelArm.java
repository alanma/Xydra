package org.xydra.store.impl.delegate;

import org.xydra.base.XID;


/**
 * SPI that defines the required access control checks for simple read/write
 * access.
 * 
 * @author xamde
 */

public interface XModelArm {
	
	boolean hasFieldReadAccess(XID actorId, XID objectId, XID fieldId);
	
	boolean hasFieldWriteAccess(XID actorId, XID objectId, XID fieldId);
	
	/**
	 * @param actorId
	 * @return true if the actorId has READ-access on the corresponding model or
	 *         if the actorId is the <em>internal</em> admin account.
	 */
	boolean hasModelReadAccess(XID actorId);
	
	boolean hasModelWriteAccess(XID actorId);
	
	boolean hasObjectReadAccess(XID actorId, XID objectId);
	
	boolean hasObjectWriteAccess(XID actorId, XID objectId);
}

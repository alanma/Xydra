package org.xydra.store.impl.delegate;

import org.xydra.base.XId;


/**
 * SPI that defines the required access control checks for simple read/write
 * access.
 * 
 * @author xamde
 */

public interface XModelArm {
	
	boolean hasFieldReadAccess(XId actorId, XId objectId, XId fieldId);
	
	boolean hasFieldWriteAccess(XId actorId, XId objectId, XId fieldId);
	
	/**
	 * @param actorId
	 * @return true if the actorId has READ-access on the corresponding model or
	 *         if the actorId is the <em>internal</em> admin account.
	 */
	boolean hasModelReadAccess(XId actorId);
	
	boolean hasModelWriteAccess(XId actorId);
	
	boolean hasObjectReadAccess(XId actorId, XId objectId);
	
	boolean hasObjectWriteAccess(XId actorId, XId objectId);
}

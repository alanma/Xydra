package org.xydra.core.model.impl.memory;

import org.xydra.core.model.sync.XSynchronizer;


/**
 * Indicates that this entity is synchronizable, i.e. it can be synchronized and
 * persisted on a server using the {@link XSynchronizer}.
 * 
 * @author alpha
 * 
 */
public interface Synchronizable {
	
	/**
	 * 
	 * @return true if the entity has been synchronized AKA persisted, false if
	 *         it has pending local changes.
	 */
	boolean isSynchronized();
}

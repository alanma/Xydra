package org.xydra.base.rmof.impl;

import org.xydra.annotations.ReadOperation;


/**
 * Entities that maintain an internal 'exists' boolean flag. They make
 * implementing executing transactions much easier.
 *
 * @author xamde
 */
public interface XExistsReadable {

    /**
     * @return at runtime if the entity currently exists or not. This flag is
     *         not persisted.
     * @throws IllegalStateException if this object has already been removed
     */
    @ReadOperation
    boolean exists();

}

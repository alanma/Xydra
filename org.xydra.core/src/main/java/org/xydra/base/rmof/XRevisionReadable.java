package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;


public interface XRevisionReadable {

    /**
     * Gets the current revision number of this entity
     *
     * @return The current revision number of this entity
     * @throws IllegalStateException if this object has already been removed
     */
    @ReadOperation
    long getRevisionNumber();

}

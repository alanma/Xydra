package org.xydra.base.rmof;

import java.util.Iterator;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * A basic repository that at least supports read operations.
 * 
 * @author dscharrer
 */
public interface XStateReadableRepository extends XEntity, Iterable<XId> {
    
    /**
     * Returns the {@link XReadableModel} contained in this repository with the
     * given {@link XId}
     * 
     * @param id The {@link XId} of the {@link XReadableModel} which is to be
     *            returned
     * @return the {@link XReadableModel} with the given {@link XId} or null if
     *         no such {@link XReadableModel} exists in this repository.
     */
    @ReadOperation
    XStateReadableModel getModel(XId id);
    
    /**
     * Checks whether this repository contains an {@link XReadableModel} with
     * the given {@link XId}.
     * 
     * @param id The {@link XId} which is to be checked
     * @return true, if this repository contains an {@link XReadableModel} with
     *         the given {@link XId}, false otherwise
     */
    @ReadOperation
    boolean hasModel(XId id);
    
    /**
     * Returns true, if this repository has no child-models
     * 
     * @return true, if this repository has no child-models
     */
    @ReadOperation
    boolean isEmpty();
    
    /**
     * @return an iterator over the {@link XId XIds} of the child-models of this
     *         repository.
     */
    @Override
    @ReadOperation
    Iterator<XId> iterator();
    
}

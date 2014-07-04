package org.xydra.base.rmof;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * A basic model that at least supports read operations.
 * 
 * Some implementations are also {@link Serializable}.
 * 
 * @author dscharrer
 */
public interface XReadableModel extends XStateReadableModel, XRevisionReadable {
    
    /* More specific return type */
    @ReadOperation
    XReadableObject getObject(@NeverNull XId objectId);
    
}

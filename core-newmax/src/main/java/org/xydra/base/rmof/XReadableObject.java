package org.xydra.base.rmof;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


public interface XReadableObject extends XStateReadableObject, XRevisionReadable {
    
    /* More specific return type */
    @ReadOperation
    XReadableField getField(XId fieldId);
    
}

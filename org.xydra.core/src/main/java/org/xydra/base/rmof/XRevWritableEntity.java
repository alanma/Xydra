package org.xydra.base.rmof;

import org.xydra.base.rmof.impl.XExistsReadable;


/**
 * Marker interface for states that can be handled by EventDelta
 */
public interface XRevWritableEntity extends XRevisionWritable, XExistsReadable {
    
}

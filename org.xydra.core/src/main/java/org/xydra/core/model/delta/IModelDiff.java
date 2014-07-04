package org.xydra.core.model.delta;

import java.util.Collection;

import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableObject;

public interface IModelDiff {
    
    Collection<? extends XReadableObject> getAdded();
    
    Collection<? extends IObjectDiff> getPotentiallyChanged();
    
    Collection<XId> getRemoved();
    
}
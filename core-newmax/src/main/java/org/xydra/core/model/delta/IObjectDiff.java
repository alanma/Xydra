package org.xydra.core.model.delta;

import java.util.Collection;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;

public interface IObjectDiff extends IHasXId {
    Collection<? extends XReadableField> getAdded();
    
    Collection<? extends IFieldDiff> getPotentiallyChanged();
    
    Collection<XId> getRemoved();
    
    boolean hasChanges();
    
    @Override
    XId getId();
}
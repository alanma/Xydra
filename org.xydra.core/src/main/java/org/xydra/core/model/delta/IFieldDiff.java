package org.xydra.core.model.delta;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.value.XValue;

public interface IFieldDiff extends IHasXId {
    @Override
    XId getId();

    XValue getInitialValue();

    // same signature as XReadableField
    XValue getValue();

    boolean isChanged();
}
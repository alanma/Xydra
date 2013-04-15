package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XEvent;


public interface IGenericEventListener {
    
    void onEvent(XEvent event);
    
}

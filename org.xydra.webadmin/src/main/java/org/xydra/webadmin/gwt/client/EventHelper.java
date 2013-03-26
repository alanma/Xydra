package org.xydra.webadmin.gwt.client;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.event.shared.EventBus;


public class EventHelper {
    
    // TODO presenters/views? should listen
    public static void addModelChangeListener(XAddress modelAddres,
            IModelChangedEventHandler handler) {
        EventBus bus = XyAdmin.getInstance().getEventBus();
        bus.addHandlerToSource(ModelChangedEvent.TYPE, modelAddres, handler);
    }
    
    // TODO fire whenever the model has changed/appeared/removed
    public static void fireModelChangeEvent(XAddress modelAddress) {
        EventBus bus = XyAdmin.getInstance().getEventBus();
        ModelChangedEvent event = new ModelChangedEvent(modelAddress);
        bus.fireEventFromSource(event, modelAddress);
    }
    
}

package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent.IObjectChangedEventHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;


/**
 * Object has been added or removed or just changed
 * 
 * @author xamde
 */
public class ObjectChangedEvent extends GwtEvent<IObjectChangedEventHandler> {
    
    private XAddress objectAddress;
    
    public static final Type<IObjectChangedEventHandler> TYPE = new Type<IObjectChangedEventHandler>();
    
    public interface IObjectChangedEventHandler extends EventHandler {
        void onObjectChange(ObjectChangedEvent event);
    }
    
    public ObjectChangedEvent(XAddress objectAddress) {
        this.objectAddress = objectAddress;
    }
    
    public XAddress getObjectAddress() {
        return this.objectAddress;
    }
    
    @Override
    protected void dispatch(IObjectChangedEventHandler handler) {
        handler.onObjectChange(this);
    }
    
    @Override
    public GwtEvent.Type<IObjectChangedEventHandler> getAssociatedType() {
        return TYPE;
    }
}

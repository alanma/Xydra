package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;


/**
 * Model has been added or removed
 * 
 * @author xamde
 */
public class ModelChangedEvent extends GwtEvent<IModelChangedEventHandler> {
    
    private XAddress modelAddress;
    
    public static final Type<IModelChangedEventHandler> TYPE = new Type<IModelChangedEventHandler>();
    
    public interface IModelChangedEventHandler extends EventHandler {
        void onModelChange(ModelChangedEvent event);
    }
    
    public ModelChangedEvent(XAddress modelAddress) {
        this.modelAddress = modelAddress;
    }
    
    public XAddress getModelAddress() {
        return this.modelAddress;
    }
    
    @Override
    protected void dispatch(IModelChangedEventHandler handler) {
        handler.onModelChange(this);
    }
    
    @Override
    public GwtEvent.Type<IModelChangedEventHandler> getAssociatedType() {
        return TYPE;
    }
}

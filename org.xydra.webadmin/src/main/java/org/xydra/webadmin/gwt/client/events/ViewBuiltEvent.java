package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent.IViewBuiltHandler;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;


/**
 * Model has been committed
 * 
 * @author Andreas
 */
public class ViewBuiltEvent extends GwtEvent<IViewBuiltHandler> {
	
	private XAddress viewAddress;
	
	public static final Type<IViewBuiltHandler> TYPE = new Type<IViewBuiltHandler>();
	
	public interface IViewBuiltHandler extends EventHandler {
		void onViewBuilt(ViewBuiltEvent event);
	}
	
	public ViewBuiltEvent(XAddress modelAddress) {
		this.viewAddress = modelAddress;
	}
	
	public XAddress getModelAddress() {
		return this.viewAddress;
	}
	
	@Override
	protected void dispatch(IViewBuiltHandler handler) {
		handler.onViewBuilt(this);
	}
	
	@Override
	public GwtEvent.Type<IViewBuiltHandler> getAssociatedType() {
		return TYPE;
	}
	
	public String getErrorMessage() {
		return "error messages are not very smart yet...";
	}
	
}

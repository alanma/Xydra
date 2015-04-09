package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent.IViewBuiltHandler;

import com.google.web.bindery.event.shared.Event;

/**
 * Model has been committed
 * 
 * @author Andreas
 */
public class ViewBuiltEvent extends Event<IViewBuiltHandler> {

	private XAddress viewAddress;

	public static final Type<IViewBuiltHandler> TYPE = new Type<IViewBuiltHandler>();

	public interface IViewBuiltHandler {
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
	public Event.Type<IViewBuiltHandler> getAssociatedType() {
		return TYPE;
	}

	public String getErrorMessage() {
		return "error messages are not very smart yet...";
	}

}

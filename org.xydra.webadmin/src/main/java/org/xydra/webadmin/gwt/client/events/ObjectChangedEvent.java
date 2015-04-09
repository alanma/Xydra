package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent.IObjectChangedEventHandler;

import com.google.web.bindery.event.shared.Event;

/**
 * Object has been added or removed or just changed
 * 
 * @author xamde
 */
public class ObjectChangedEvent extends Event<IObjectChangedEventHandler> {

	private XAddress objectAddress;

	private EntityStatus status;

	private XId moreInfos;

	public static final Type<IObjectChangedEventHandler> TYPE = new Type<IObjectChangedEventHandler>();

	public interface IObjectChangedEventHandler {
		void onObjectChange(ObjectChangedEvent event);
	}

	public ObjectChangedEvent(XAddress objectAddress, EntityStatus status, XId moreInfos) {
		this.setStatus(status);
		this.setMoreInfos(moreInfos);
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
	public Event.Type<IObjectChangedEventHandler> getAssociatedType() {
		return TYPE;
	}

	public XId getMoreInfos() {
		return this.moreInfos;
	}

	public void setMoreInfos(XId moreInfos) {
		this.moreInfos = moreInfos;
	}

	public EntityStatus getStatus() {
		return this.status;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}
}

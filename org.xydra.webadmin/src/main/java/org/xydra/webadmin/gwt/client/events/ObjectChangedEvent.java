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

	private final XAddress objectAddress;

	private EntityStatus status;

	private XId moreInfos;

	public static final Type<IObjectChangedEventHandler> TYPE = new Type<IObjectChangedEventHandler>();

	public interface IObjectChangedEventHandler {
		void onObjectChange(ObjectChangedEvent event);
	}

	public ObjectChangedEvent(final XAddress objectAddress, final EntityStatus status, final XId moreInfos) {
		setStatus(status);
		setMoreInfos(moreInfos);
		this.objectAddress = objectAddress;
	}

	public XAddress getObjectAddress() {
		return this.objectAddress;
	}

	@Override
	protected void dispatch(final IObjectChangedEventHandler handler) {
		handler.onObjectChange(this);
	}

	@Override
	public Event.Type<IObjectChangedEventHandler> getAssociatedType() {
		return TYPE;
	}

	public XId getMoreInfos() {
		return this.moreInfos;
	}

	public void setMoreInfos(final XId moreInfos) {
		this.moreInfos = moreInfos;
	}

	public EntityStatus getStatus() {
		return this.status;
	}

	public void setStatus(final EntityStatus status) {
		this.status = status;
	}
}

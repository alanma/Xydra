package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;

import com.google.web.bindery.event.shared.Event;

/**
 * Model has been added or removed
 * 
 * @author xamde
 */
public class ModelChangedEvent extends Event<IModelChangedEventHandler> {

	private XAddress modelAddress;

	private EntityStatus status;

	private XId moreInfos;

	public static final Type<IModelChangedEventHandler> TYPE = new Type<IModelChangedEventHandler>();

	public interface IModelChangedEventHandler {
		void onModelChange(ModelChangedEvent event);
	}

	public ModelChangedEvent(XAddress modelAddress, EntityStatus status, XId moreInfos) {
		this.modelAddress = modelAddress;
		this.status = status;
		this.moreInfos = moreInfos;
	}

	public XAddress getModelAddress() {
		return this.modelAddress;
	}

	@Override
	protected void dispatch(IModelChangedEventHandler handler) {
		handler.onModelChange(this);
	}

	@Override
	public Event.Type<IModelChangedEventHandler> getAssociatedType() {
		return TYPE;
	}

	public EntityStatus getStatus() {
		return this.status;
	}

	public void setMoreInfos(XId moreInfos) {
		this.moreInfos = moreInfos;
	}

	public XId getMoreInfos() {
		return this.moreInfos;
	}

}

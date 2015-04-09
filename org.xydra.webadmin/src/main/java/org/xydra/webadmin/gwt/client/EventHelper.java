package org.xydra.webadmin.gwt.client;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.events.CommittingEvent;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.CommitStatus;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.ICommitEventHandler;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent.IObjectChangedEventHandler;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent.IViewBuiltHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class EventHelper {

	public static HandlerRegistration addRepoChangeListener(XAddress repoAddress,
			IRepoChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(RepoChangedEvent.TYPE, repoAddress, handler);
	}

	public static HandlerRegistration addModelChangedListener(XAddress modelAddress,
			IModelChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ModelChangedEvent.TYPE, modelAddress, handler);
	}

	public static HandlerRegistration addCommittingListener(XAddress modelAddress,
			ICommitEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(CommittingEvent.TYPE, modelAddress, handler);
	}

	public static HandlerRegistration addObjectChangedListener(XAddress objectAddress,
			IObjectChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ObjectChangedEvent.TYPE, objectAddress, handler);
	}

	public static HandlerRegistration addViewBuildListener(XAddress address,
			IViewBuiltHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ViewBuiltEvent.TYPE, address, handler);
	}

	public static void fireModelChangedEvent(XAddress modelAddress, EntityStatus status,
			XId moreInfos) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		ModelChangedEvent event = new ModelChangedEvent(modelAddress, status, moreInfos);
		bus.fireEventFromSource(event, modelAddress);
	}

	public static void fireRepoChangeEvent(XAddress repoAddress, EntityStatus status,
			RepoDataModel repoDataModel) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		RepoChangedEvent event = new RepoChangedEvent(repoAddress, status, repoDataModel);
		bus.fireEventFromSource(event, repoAddress);
	}

	public static void fireObjectChangedEvent(XAddress objectAddress, EntityStatus status,
			XId moreInfos) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		ObjectChangedEvent event = new ObjectChangedEvent(objectAddress, status, moreInfos);
		bus.fireEventFromSource(event, objectAddress);
	}

	public static void fireCommitEvent(XAddress modelAddress, CommitStatus status,
			Long revisionNumber) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		CommittingEvent event = new CommittingEvent(modelAddress, status, revisionNumber);
		bus.fireEventFromSource(event, modelAddress);
	}

	public static void fireViewBuiltEvent(XAddress presenterAddress) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		ViewBuiltEvent event = new ViewBuiltEvent(presenterAddress);
		bus.fireEventFromSource(event, presenterAddress);

	}

}

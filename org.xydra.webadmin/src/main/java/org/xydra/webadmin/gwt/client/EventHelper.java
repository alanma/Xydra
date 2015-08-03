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

	public static HandlerRegistration addRepoChangeListener(final XAddress repoAddress,
			final IRepoChangedEventHandler handler) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(RepoChangedEvent.TYPE, repoAddress, handler);
	}

	public static HandlerRegistration addModelChangedListener(final XAddress modelAddress,
			final IModelChangedEventHandler handler) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ModelChangedEvent.TYPE, modelAddress, handler);
	}

	public static HandlerRegistration addCommittingListener(final XAddress modelAddress,
			final ICommitEventHandler handler) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(CommittingEvent.TYPE, modelAddress, handler);
	}

	public static HandlerRegistration addObjectChangedListener(final XAddress objectAddress,
			final IObjectChangedEventHandler handler) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ObjectChangedEvent.TYPE, objectAddress, handler);
	}

	public static HandlerRegistration addViewBuildListener(final XAddress address,
			final IViewBuiltHandler handler) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		return bus.addHandlerToSource(ViewBuiltEvent.TYPE, address, handler);
	}

	public static void fireModelChangedEvent(final XAddress modelAddress, final EntityStatus status,
			final XId moreInfos) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		final ModelChangedEvent event = new ModelChangedEvent(modelAddress, status, moreInfos);
		bus.fireEventFromSource(event, modelAddress);
	}

	public static void fireRepoChangeEvent(final XAddress repoAddress, final EntityStatus status,
			final RepoDataModel repoDataModel) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		final RepoChangedEvent event = new RepoChangedEvent(repoAddress, status, repoDataModel);
		bus.fireEventFromSource(event, repoAddress);
	}

	public static void fireObjectChangedEvent(final XAddress objectAddress, final EntityStatus status,
			final XId moreInfos) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		final ObjectChangedEvent event = new ObjectChangedEvent(objectAddress, status, moreInfos);
		bus.fireEventFromSource(event, objectAddress);
	}

	public static void fireCommitEvent(final XAddress modelAddress, final CommitStatus status,
			final Long revisionNumber) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		final CommittingEvent event = new CommittingEvent(modelAddress, status, revisionNumber);
		bus.fireEventFromSource(event, modelAddress);
	}

	public static void fireViewBuiltEvent(final XAddress presenterAddress) {
		final EventBus bus = XyAdmin.getInstance().getEventBus();
		final ViewBuiltEvent event = new ViewBuiltEvent(presenterAddress);
		bus.fireEventFromSource(event, presenterAddress);

	}

}

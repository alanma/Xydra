package org.xydra.webadmin.gwt.client;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.event.shared.EventBus;


public class EventHelper {
	
	public static void addRepoChangeListener(XAddress repoAddress, IRepoChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		bus.addHandlerToSource(RepoChangedEvent.TYPE, repoAddress, handler);
	}
	
	public static void addModelChangeListener(XAddress modelAddress,
	        IModelChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		bus.addHandlerToSource(ModelChangedEvent.TYPE, modelAddress, handler);
	}
	
	public static void addCommittingListener(XAddress modelAddress, ICommitEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		bus.addHandlerToSource(CommittingEvent.TYPE, modelAddress, handler);
	}
	
	public static void addObjectChangedListener(XAddress objectAddress,
	        IObjectChangedEventHandler handler) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		bus.addHandlerToSource(ObjectChangedEvent.TYPE, objectAddress, handler);
	}
	
	public static void fireModelChangedEvent(XAddress modelAddress, EntityStatus status,
	        XId moreInfos) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		ModelChangedEvent event = new ModelChangedEvent(modelAddress, status, moreInfos);
		bus.fireEventFromSource(event, modelAddress);
	}
	
	public static void fireRepoChangeEvent(XAddress repoAddress, EntityStatus status) {
		EventBus bus = XyAdmin.getInstance().getEventBus();
		RepoChangedEvent event = new RepoChangedEvent(repoAddress, status);
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
	
}

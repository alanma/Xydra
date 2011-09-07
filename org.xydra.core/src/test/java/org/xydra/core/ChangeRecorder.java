package org.xydra.core;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * A listener for events of type T that records the received events in a
 * transaction.
 * 
 * @author dscharrer
 */
public class ChangeRecorder implements XRepositoryEventListener, XModelEventListener,
        XObjectEventListener, XFieldEventListener, XTransactionEventListener {
	
	/**
	 * Record all non-transaction events to the given model.
	 */
	static public List<XEvent> record(XModel model) {
		List<XEvent> eventList = new ArrayList<XEvent>();
		ChangeRecorder cr = new ChangeRecorder(eventList);
		model.addListenerForModelEvents(cr);
		model.addListenerForObjectEvents(cr);
		model.addListenerForFieldEvents(cr);
		return eventList;
	}
	
	/**
	 * Record all non-transaction events to the given object.
	 */
	static public List<XEvent> record(XObject object) {
		List<XEvent> eventList = new ArrayList<XEvent>();
		ChangeRecorder cr = new ChangeRecorder(eventList);
		object.addListenerForObjectEvents(cr);
		object.addListenerForFieldEvents(cr);
		return eventList;
	}
	
	/**
	 * Record all events to the given model/object.
	 */
	static public List<XEvent> recordTransactions(XSendsTransactionEvents entity) {
		List<XEvent> eventList = new ArrayList<XEvent>();
		ChangeRecorder cr = new ChangeRecorder(eventList);
		entity.addListenerForTransactionEvents(cr);
		return eventList;
	}
	
	/**
	 * Record all events (including transactions) except those that also occur
	 * in a transaction event.
	 */
	static public List<XEvent> recordWhole(XModel model) {
		List<XEvent> eventList = new ArrayList<XEvent>();
		ChangeRecorder cr = new ChangeRecorder(eventList, true);
		model.addListenerForModelEvents(cr);
		model.addListenerForObjectEvents(cr);
		model.addListenerForFieldEvents(cr);
		model.addListenerForTransactionEvents(cr);
		return eventList;
	}
	
	List<XEvent> eventList;
	
	boolean filter;
	
	public ChangeRecorder(List<XEvent> eventList) {
		this(eventList, false);
	}
	
	public ChangeRecorder(List<XEvent> eventList, boolean filter) {
		this.eventList = eventList;
		this.filter = filter;
	}
	
	@Override
    public void onChangeEvent(XFieldEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	@Override
    public void onChangeEvent(XModelEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	@Override
    public void onChangeEvent(XObjectEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	@Override
    public void onChangeEvent(XRepositoryEvent event) {
		this.eventList.add(event);
	}
	
	@Override
    public void onChangeEvent(XTransactionEvent event) {
		this.eventList.add(event);
	}
}

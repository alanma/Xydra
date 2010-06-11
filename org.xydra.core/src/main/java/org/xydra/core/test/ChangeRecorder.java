package org.xydra.core.test;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * A listener for events of type T that records the received events in a
 * transaction.
 * 
 * @param <T>
 */
public class ChangeRecorder implements XRepositoryEventListener, XModelEventListener,
        XObjectEventListener, XFieldEventListener, XTransactionEventListener {
	
	boolean filter;
	List<XEvent> eventList;
	
	public ChangeRecorder(List<XEvent> eventList) {
		this(eventList, false);
	}
	
	public ChangeRecorder(List<XEvent> eventList, boolean filter) {
		this.eventList = eventList;
	}
	
	public void onChangeEvent(XRepositoryEvent event) {
		this.eventList.add(event);
	}
	
	public void onChangeEvent(XModelEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	public void onChangeEvent(XObjectEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	public void onChangeEvent(XFieldEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}
	
	public void onChangeEvent(XTransactionEvent event) {
		this.eventList.add(event);
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
}

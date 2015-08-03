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
	 *
	 * @param model
	 * @return ...
	 */
	static public List<XEvent> record(final XModel model) {
		final List<XEvent> eventList = new ArrayList<XEvent>();
		final ChangeRecorder cr = new ChangeRecorder(eventList);
		model.addListenerForModelEvents(cr);
		model.addListenerForObjectEvents(cr);
		model.addListenerForFieldEvents(cr);
		return eventList;
	}

	/**
	 * Record all non-transaction events to the given object.
	 *
	 * @param object
	 * @return ...
	 */
	static public List<XEvent> record(final XObject object) {
		final List<XEvent> eventList = new ArrayList<XEvent>();
		final ChangeRecorder cr = new ChangeRecorder(eventList);
		object.addListenerForObjectEvents(cr);
		object.addListenerForFieldEvents(cr);
		return eventList;
	}

	/**
	 * Record all events to the given model/object.
	 *
	 * @param entity
	 * @return ...
	 */
	static public List<XEvent> recordTransactions(final XSendsTransactionEvents entity) {
		final List<XEvent> eventList = new ArrayList<XEvent>();
		final ChangeRecorder cr = new ChangeRecorder(eventList);
		entity.addListenerForTransactionEvents(cr);
		return eventList;
	}

	/**
	 * Record all events (including transactions) except those that also occur
	 * in a transaction event.
	 *
	 * @param model
	 * @return ...
	 */
	static public List<XEvent> recordWhole(final XModel model) {
		final List<XEvent> eventList = new ArrayList<XEvent>();
		final ChangeRecorder cr = new ChangeRecorder(eventList, true);
		model.addListenerForModelEvents(cr);
		model.addListenerForObjectEvents(cr);
		model.addListenerForFieldEvents(cr);
		model.addListenerForTransactionEvents(cr);
		return eventList;
	}

	List<XEvent> eventList;

	boolean filter;

	public ChangeRecorder(final List<XEvent> eventList) {
		this(eventList, false);
	}

	public ChangeRecorder(final List<XEvent> eventList, final boolean filter) {
		this.eventList = eventList;
		this.filter = filter;
	}

	@Override
	public void onChangeEvent(final XFieldEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}

	@Override
	public void onChangeEvent(final XModelEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}

	@Override
	public void onChangeEvent(final XObjectEvent event) {
		if(this.filter && event.inTransaction()) {
			// ignore
			return;
		}
		this.eventList.add(event);
	}

	@Override
	public void onChangeEvent(final XRepositoryEvent event) {
		this.eventList.add(event);
	}

	@Override
	public void onChangeEvent(final XTransactionEvent event) {
		this.eventList.add(event);
	}
}
